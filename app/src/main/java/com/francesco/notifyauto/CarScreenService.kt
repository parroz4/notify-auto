package com.francesco.notifyauto

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.model.Action
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import androidx.car.app.validation.HostValidator
import androidx.core.app.NotificationManagerCompat

/**
 * Espone NotifyAuto come icona nel launcher di Android Auto:
 * una schermata minimale con il pulsante per cancellare in blocco
 * le notifiche inoltrate, direttamente dallo schermo dell'auto.
 */
class CarScreenService : CarAppService() {

    // App distribuita fuori dal Play Store: accettiamo qualsiasi host
    // (richiede "sorgenti sconosciute" attivo in Android Auto).
    override fun createHostValidator(): HostValidator =
        HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession(): Session = object : Session() {
        override fun onCreateScreen(intent: Intent): Screen =
            ClearNotificationsScreen(carContext)
    }
}

class ClearNotificationsScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val clearAction = Action.Builder()
            .setTitle(carContext.getString(R.string.car_clear_button))
            .setOnClickListener {
                NotificationManagerCompat.from(carContext).cancelAll()
                CarToast.makeText(
                    carContext,
                    carContext.getString(R.string.car_cleared),
                    CarToast.LENGTH_SHORT
                ).show()
            }
            .build()

        return MessageTemplate.Builder(carContext.getString(R.string.car_screen_message))
            .setTitle(carContext.getString(R.string.app_name))
            .setHeaderAction(Action.APP_ICON)
            .addAction(clearAction)
            .build()
    }
}
