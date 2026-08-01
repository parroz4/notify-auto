package com.francesco.notifyauto

import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.constraints.ConstraintManager
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Row as CarRow
import androidx.car.app.model.Template
import androidx.car.app.validation.HostValidator
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Espone NotifyAuto come icona nel launcher di Android Auto:
 * l'elenco delle notifiche inoltrate, con cancellazione singola
 * (tocco sulla riga) o totale ("Cancella tutte").
 */
class CarScreenService : CarAppService() {

    // App distribuita fuori dal Play Store: accettiamo qualsiasi host
    // (richiede "sorgenti sconosciute" attivo in Android Auto).
    override fun createHostValidator(): HostValidator =
        HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession(): Session = object : Session() {
        override fun onCreateScreen(intent: Intent): Screen =
            ForwardedListScreen(carContext)
    }
}

class ForwardedListScreen(carContext: CarContext) : Screen(carContext) {

    init {
        ForwardedNotifications.listener = { invalidate() }
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                ForwardedNotifications.listener = null
            }
        })
    }

    override fun onGetTemplate(): Template {
        val active = carContext.getSystemService(NotificationManager::class.java)
            .activeNotifications
            .filter { it.notification.flags and Notification.FLAG_GROUP_SUMMARY == 0 }
            .sortedByDescending { it.postTime }

        if (active.isEmpty()) {
            return MessageTemplate.Builder(carContext.getString(R.string.car_empty))
                .setTitle(carContext.getString(R.string.app_name))
                .setHeaderAction(Action.APP_ICON)
                .build()
        }

        val maxRows = try {
            carContext.getCarService(ConstraintManager::class.java)
                .getContentLimit(ConstraintManager.CONTENT_LIMIT_TYPE_LIST)
        } catch (_: Exception) {
            6
        }

        val list = ItemList.Builder()
        active.take(maxRows).forEach { sbn ->
            val extras = sbn.notification.extras
            val app = extras.getString(NotificationForwarderService.EXTRA_SRC_APP) ?: ""
            val title = extras.getString(NotificationForwarderService.EXTRA_SRC_TITLE) ?: ""
            val text = extras.getString(NotificationForwarderService.EXTRA_SRC_TEXT) ?: ""

            val rowTitle = listOf(app, title).filter { it.isNotBlank() }
                .joinToString(" — ").ifBlank { carContext.getString(R.string.app_name) }

            list.addItem(
                CarRow.Builder()
                    .setTitle(rowTitle)
                    .addText(text.ifBlank { " " })
                    .setOnClickListener {
                        NotificationManagerCompat.from(carContext).cancel(sbn.id)
                        invalidate()
                    }
                    .build()
            )
        }

        val clearAll = Action.Builder()
            .setTitle(carContext.getString(R.string.car_clear_button))
            .setOnClickListener {
                NotificationManagerCompat.from(carContext).cancelAll()
                CarToast.makeText(
                    carContext,
                    carContext.getString(R.string.car_cleared),
                    CarToast.LENGTH_SHORT
                ).show()
                invalidate()
            }
            .build()

        return ListTemplate.Builder()
            .setSingleList(list.build())
            .setTitle(carContext.getString(R.string.car_list_title))
            .setHeaderAction(Action.APP_ICON)
            .setActionStrip(ActionStrip.Builder().addAction(clearAll).build())
            .build()
    }
}
