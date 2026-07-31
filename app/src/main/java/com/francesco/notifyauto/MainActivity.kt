package com.francesco.notifyauto

import android.Manifest
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var grantButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.status_text)
        grantButton = findViewById(R.id.grant_button)
        grantButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        findViewById<SwitchCompat>(R.id.only_car_switch).apply {
            isChecked = Prefs.onlyWhenConnected(this@MainActivity)
            setOnCheckedChangeListener { _, checked ->
                Prefs.setOnlyWhenConnected(this@MainActivity, checked)
            }
        }

        findViewById<RecyclerView>(R.id.app_list).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = AppListAdapter(this@MainActivity, loadApps())
        }

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1
            )
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val granted = NotificationManagerCompat
            .getEnabledListenerPackages(this)
            .contains(packageName)
        statusText.text = getString(
            if (granted) R.string.status_listener_ok else R.string.status_listener_missing
        )
        grantButton.visibility = if (granted) Button.GONE else Button.VISIBLE
    }

    private fun loadApps(): List<AppEntry> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val launchable = packageManager.queryIntentActivities(launcherIntent, 0)
            .map { it.activityInfo.packageName }
            .toSet()

        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .asSequence()
            .filter { it.packageName != packageName }
            .filter {
                it.packageName in launchable ||
                    (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0
            }
            .map {
                AppEntry(
                    packageName = it.packageName,
                    label = packageManager.getApplicationLabel(it).toString(),
                    icon = packageManager.getApplicationIcon(it)
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }
}
