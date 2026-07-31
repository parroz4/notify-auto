package com.francesco.notifyauto

import android.Manifest
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
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
    private lateinit var adapter: AppListAdapter

    private var allApps: List<AppEntry> = emptyList()
    private var query: String = ""

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

        adapter = AppListAdapter(this)
        findViewById<RecyclerView>(R.id.app_list).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        findViewById<EditText>(R.id.search_input).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                query = s?.toString()?.trim().orEmpty()
                refreshList()
            }
        })

        findViewById<Button>(R.id.select_all_button).setOnClickListener {
            Prefs.setPackagesEnabled(this, filteredApps().map { it.packageName }, true)
            refreshList()
        }
        findViewById<Button>(R.id.deselect_all_button).setOnClickListener {
            Prefs.setPackagesEnabled(this, filteredApps().map { it.packageName }, false)
            refreshList()
        }

        allApps = loadApps()
        refreshList()

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
        // Riporta in cima le app attivate durante la sessione precedente
        if (allApps.isNotEmpty()) refreshList()
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

    private fun filteredApps(): List<AppEntry> {
        if (query.isBlank()) return allApps
        return allApps.filter {
            it.label.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
        }
    }

    /**
     * Costruisce le righe della lista: prima le app attive, poi le restanti
     * raggruppate per categoria. La lista non viene ricostruita a ogni tocco
     * dell'interruttore, altrimenti la riga scapperebbe da sotto il dito.
     */
    private fun refreshList() {
        val enabled = Prefs.enabledPackages(this)
        val filtered = filteredApps()
        val rows = mutableListOf<Row>()

        val (active, inactive) = filtered.partition { it.packageName in enabled }
        if (active.isNotEmpty()) {
            rows.add(Row.Header(getString(R.string.category_active, active.size)))
            active.forEach { rows.add(Row.App(it)) }
        }

        val other = getString(R.string.category_other)
        inactive
            .groupBy { it.category }
            .toSortedMap(compareBy<String> { it == other }.thenBy { it.lowercase() })
            .forEach { (category, apps) ->
                rows.add(Row.Header(category))
                apps.forEach { rows.add(Row.App(it)) }
            }
        adapter.submit(rows)
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
                    icon = packageManager.getApplicationIcon(it),
                    category = ApplicationInfo.getCategoryTitle(this, it.category)
                        ?.toString() ?: getString(R.string.category_other)
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }
}
