package com.francesco.notifyauto

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

/**
 * Gestisce "segna come letto" e "rispondi" richiesti da Android Auto.
 * L'app è di sola lettura: entrambe le azioni si limitano a chiudere
 * la notifica inoltrata.
 */
class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(NotificationForwarderService.EXTRA_NOTIFICATION_ID, -1)
        if (id != -1) {
            NotificationManagerCompat.from(context).cancel(id)
        }
    }
}
