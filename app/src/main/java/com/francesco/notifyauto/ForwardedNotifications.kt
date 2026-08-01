package com.francesco.notifyauto

import android.os.Handler
import android.os.Looper

/**
 * Ponte minimale tra il servizio di inoltro e la schermata sull'auto:
 * quando l'insieme delle notifiche inoltrate cambia, la schermata
 * (se aperta) viene invalidata e si ridisegna.
 */
object ForwardedNotifications {
    @Volatile
    var listener: (() -> Unit)? = null

    private val mainHandler = Handler(Looper.getMainLooper())

    fun notifyChanged() {
        mainHandler.post { listener?.invoke() }
    }
}
