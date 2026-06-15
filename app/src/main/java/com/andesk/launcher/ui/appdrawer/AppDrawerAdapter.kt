package com.andesk.launcher.ui.appdrawer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.andesk.launcher.R
import com.andesk.launcher.data.model.AppInfo

class AppDrawerAdapter(
    private val onAppClick: (AppInfo) -> Unit
) : ListAdapter<AppInfo, AppDrawerAdapter.ViewHolder>(AppDiffCallback()) {

    // 字母索引位置映射
    private val letterPositions = mutableMapOf<String, Int>()

    override fun submitList(list: List<AppInfo>?) {
        super.submitList(list)
        // 更新字母索引位置
        letterPositions.clear()
        list?.forEachIndexed { index, appInfo ->
            val firstLetter = appInfo.name.firstOrNull()?.uppercase() ?: "#"
            val letter = if (firstLetter[0] in 'A'..'Z') firstLetter else "#"
            if (!letterPositions.containsKey(letter)) {
                letterPositions[letter] = index
            }
        }
    }

    /**
     * 获取字母对应的列表位置
     */
    fun getPositionForLetter(letter: String): Int {
        return letterPositions[letter] ?: -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_drawer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAppIcon: ImageView = itemView.findViewById(R.id.ivAppIcon)
        private val tvAppName: TextView = itemView.findViewById(R.id.tvAppName)

        fun bind(appInfo: AppInfo) {
            tvAppName.text = appInfo.name
            
            // 使用Coil加载图标
            ivAppIcon.load(appInfo.icon) {
                size(128)
                crossfade(true)
            }

            itemView.setOnClickListener {
                onAppClick(appInfo)
            }
        }
    }

    class AppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem == newItem
        }
    }
}
