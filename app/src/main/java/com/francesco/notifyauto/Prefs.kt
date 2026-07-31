package com.francesco.notifyauto

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val FILE = "notify_auto_prefs"
    private const val KEY_ENABLED_PACKAGES = "enabled_packages"
    private const val KEY_ONLY_WHEN_CONNECTED = "only_when_connected"

    private fun sp(context: Context): SharedPreferences =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun enabledPackages(context: Context): Set<String> =
        sp(context).getStringSet(KEY_ENABLED_PACKAGES, emptySet()) ?: emptySet()

    fun setPackageEnabled(context: Context, packageName: String, enabled: Boolean) {
        val current = enabledPackages(context).toMutableSet()
        if (enabled) current.add(packageName) else current.remove(packageName)
        sp(context).edit().putStringSet(KEY_ENABLED_PACKAGES, current).apply()
    }

    fun onlyWhenConnected(context: Context): Boolean =
        sp(context).getBoolean(KEY_ONLY_WHEN_CONNECTED, true)

    fun setOnlyWhenConnected(context: Context, value: Boolean) {
        sp(context).edit().putBoolean(KEY_ONLY_WHEN_CONNECTED, value).apply()
    }
}
