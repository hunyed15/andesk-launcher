package com.andesk.launcher.ui.recent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.andesk.launcher.R

class RecentAppsAdapter(
    private val onKillApp: (String) -> Unit
) : ListAdapter<RecentApp, RecentAppsAdapter.ViewHolder>(RecentAppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAppName: TextView = itemView.findViewById(R.id.tvAppName)
        private val tvProcessName: TextView = itemView.findViewById(R.id.tvProcessName)
        private val tvImportance: TextView = itemView.findViewById(R.id.tvImportance)
        private val btnKill: Button = itemView.findViewById(R.id.btnKill)

        fun bind(app: RecentApp) {
            tvAppName.text = app.processName.substringAfterLast('.')
            tvProcessName.text = app.processName
            tvImportance.text = getImportanceText(app.importance)

            btnKill.setOnClickListener {
                onKillApp(app.processName)
            }
        }

        private fun getImportanceText(importance: Int): String {
            return when {
                importance < 100 -> "前台"
                importance < 200 -> "可见"
                importance < 300 -> "服务"
                importance < 400 -> "后台"
                else -> "缓存"
            }
        }
    }

    class RecentAppDiffCallback : DiffUtil.ItemCallback<RecentApp>() {
        override fun areItemsTheSame(oldItem: RecentApp, newItem: RecentApp): Boolean {
            return oldItem.processName == newItem.processName
        }

        override fun areContentsTheSame(oldItem: RecentApp, newItem: RecentApp): Boolean {
            return oldItem == newItem
        }
    }
}
