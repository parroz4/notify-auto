package com.francesco.notifyauto

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView

data class AppEntry(
    val packageName: String,
    val label: String,
    val icon: Drawable,
)

class AppListAdapter(
    private val context: Context,
    private val apps: List<AppEntry>,
) : RecyclerView.Adapter<AppListAdapter.Holder>() {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val label: TextView = view.findViewById(R.id.app_label)
        val packageName: TextView = view.findViewById(R.id.app_package)
        val toggle: SwitchCompat = view.findViewById(R.id.app_toggle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return Holder(view)
    }

    override fun getItemCount() = apps.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val app = apps[position]
        holder.icon.setImageDrawable(app.icon)
        holder.label.text = app.label
        holder.packageName.text = app.packageName
        holder.toggle.setOnCheckedChangeListener(null)
        holder.toggle.isChecked = app.packageName in Prefs.enabledPackages(context)
        holder.toggle.setOnCheckedChangeListener { _, checked ->
            Prefs.setPackageEnabled(context, app.packageName, checked)
        }
    }
}
