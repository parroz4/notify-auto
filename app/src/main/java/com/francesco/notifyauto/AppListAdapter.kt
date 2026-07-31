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
    val category: String,
)

sealed class Row {
    data class Header(val title: String) : Row()
    data class App(val entry: AppEntry) : Row()
}

class AppListAdapter(
    private val context: Context,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_APP = 1
    }

    private var rows: List<Row> = emptyList()

    fun submit(rows: List<Row>) {
        this.rows = rows
        @Suppress("NotifyDataSetChanged")
        notifyDataSetChanged()
    }

    class HeaderHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.header_title)
    }

    class AppHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val label: TextView = view.findViewById(R.id.app_label)
        val packageName: TextView = view.findViewById(R.id.app_package)
        val toggle: SwitchCompat = view.findViewById(R.id.app_toggle)
    }

    override fun getItemViewType(position: Int) = when (rows[position]) {
        is Row.Header -> TYPE_HEADER
        is Row.App -> TYPE_APP
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderHolder(inflater.inflate(R.layout.item_header, parent, false))
        } else {
            AppHolder(inflater.inflate(R.layout.item_app, parent, false))
        }
    }

    override fun getItemCount() = rows.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is Row.Header -> (holder as HeaderHolder).title.text = row.title
            is Row.App -> {
                val app = row.entry
                holder as AppHolder
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
    }
}
