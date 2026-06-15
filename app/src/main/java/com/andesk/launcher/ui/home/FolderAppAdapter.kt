package com.andesk.launcher.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.andesk.launcher.R
import com.andesk.launcher.data.model.AppInfo

/**
 * 文件夹内应用适配器
 */
class FolderAppAdapter(
    private val apps: List<AppInfo>,
    private val onAppClick: (AppInfo) -> Unit,
    private val onRemoveClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<FolderAppAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_folder_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    override fun getItemCount(): Int = apps.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAppIcon: ImageView = itemView.findViewById(R.id.ivAppIcon)
        private val tvAppName: TextView = itemView.findViewById(R.id.tvAppName)
        private val btnRemove: ImageView = itemView.findViewById(R.id.btnRemove)

        fun bind(appInfo: AppInfo) {
            tvAppName.text = appInfo.name
            
            ivAppIcon.load(appInfo.icon) {
                size(128)
                crossfade(true)
            }

            // 点击启动应用
            itemView.setOnClickListener {
                onAppClick(appInfo)
            }

            // 长按从文件夹移除
            itemView.setOnLongClickListener {
                onRemoveClick(appInfo)
                true
            }

            // 移除按钮
            btnRemove.setOnClickListener {
                onRemoveClick(appInfo)
            }
        }
    }
}
