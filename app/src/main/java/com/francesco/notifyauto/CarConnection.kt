package com.francesco.notifyauto

import android.content.Context
import android.net.Uri

/**
 * Interroga il content provider esposto dall'app Android Auto
 * (lo stesso usato da androidx.car.app.connection.CarConnection)
 * senza dipendere dalla libreria car-app.
 */
object CarConnection {
    private val URI = Uri.parse("content://androidx.car.app.connection")
    private const val COLUMN = "CarConnectionState"
    private const val NOT_CONNECTED = 0

    fun isConnected(context: Context): Boolean {
        return try {
            context.contentResolver.query(URI, arrayOf(COLUMN), null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(COLUMN)
                if (idx >= 0 && cursor.moveToFirst()) {
                    cursor.getInt(idx) != NOT_CONNECTED
                } else false
            } ?: false
        } catch (_: Exception) {
            false
        }
    }
}
