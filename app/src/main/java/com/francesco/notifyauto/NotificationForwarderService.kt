package com.francesco.notifyauto

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput

/**
 * Intercetta le notifiche del telefono e le ripubblica in formato
 * MessagingStyle, l'unico che Android Auto mostra sullo schermo dell'auto
 * (e che l'Assistente può leggere ad alta voce).
 */
class NotificationForwarderService : NotificationListenerService() {

    companion object {
        const val CHANNEL_ID = "aa_forward"
        const val EXTRA_NOTIFICATION_ID = "forwarded_id"
        const val ACTION_MARK_READ = "com.francesco.notifyauto.MARK_READ"
        const val ACTION_REPLY = "com.francesco.notifyauto.REPLY"
        const val REMOTE_INPUT_KEY = "reply_text"
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.channel_description)
            setSound(null, null)
            enableVibration(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // La notifica originale è stata letta o cancellata sul telefono:
        // togliamo anche la copia inoltrata, così l'auto resta pulita.
        if (sbn.packageName == packageName) return
        NotificationManagerCompat.from(this).cancel(sbn.key.hashCode())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Mai rielaborare le nostre stesse notifiche: eviterebbe un loop infinito
        if (sbn.packageName == packageName) return

        val notification = sbn.notification

        // Salta notifiche persistenti (player musicali, servizi in foreground...)
        if (notification.flags and Notification.FLAG_ONGOING_EVENT != 0) return
        // Salta i riepiloghi di gruppo: inoltriamo solo le notifiche vere
        if (notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return

        if (sbn.packageName !in Prefs.enabledPackages(this)) return
        if (Prefs.onlyWhenConnected(this) && !CarConnection.isConnected(this)) return

        val extras = notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString()
        if (title.isNullOrBlank() && text.isNullOrBlank()) return

        forward(sbn, title, text)
    }

    private fun forward(sbn: StatusBarNotification, title: String?, text: String?) {
        val appLabel = try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(sbn.packageName, 0)
            ).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            sbn.packageName
        }

        val notificationId = sbn.key.hashCode()
        val sender = Person.Builder()
            .setName(if (title.isNullOrBlank()) appLabel else title)
            .build()

        val style = NotificationCompat.MessagingStyle(sender)
            .setConversationTitle(appLabel)
            .addMessage(
                if (text.isNullOrBlank()) title else text,
                System.currentTimeMillis(),
                sender
            )

        val markReadAction = NotificationCompat.Action.Builder(
            R.drawable.ic_notification, getString(R.string.action_mark_read),
            actionPendingIntent(ACTION_MARK_READ, notificationId, mutable = false)
        )
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
            .setShowsUserInterface(false)
            .build()

        // Android Auto mostra la conversazione solo se esiste un'azione di
        // risposta: la aggiungiamo anche se la risposta viene solo scartata.
        val replyAction = NotificationCompat.Action.Builder(
            R.drawable.ic_notification, getString(R.string.action_reply),
            actionPendingIntent(ACTION_REPLY, notificationId, mutable = true)
        )
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
            .setShowsUserInterface(false)
            .addRemoteInput(RemoteInput.Builder(REMOTE_INPUT_KEY).build())
            .build()

        val forwarded = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(style)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .addAction(markReadAction)
            .addAction(replyAction)
            .extend(NotificationCompat.CarExtender())
            .build()

        try {
            NotificationManagerCompat.from(this).notify(notificationId, forwarded)
        } catch (_: SecurityException) {
            // Permesso POST_NOTIFICATIONS revocato: non possiamo inoltrare
        }
    }

    private fun actionPendingIntent(action: String, notificationId: Int, mutable: Boolean): PendingIntent {
        val intent = Intent(this, NotificationActionReceiver::class.java)
            .setAction(action)
            .putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            .setData(android.net.Uri.parse("notifyauto://$notificationId/$action"))
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (mutable) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(this, notificationId, intent, flags)
    }
}
