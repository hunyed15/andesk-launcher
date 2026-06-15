package com.andesk.launcher.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.andesk.launcher.R
import com.andesk.launcher.data.model.AppInfo

/**
 * 应用网格适配器
 */
class AppGridAdapter(
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: (AppInfo) -> Boolean,
    private val onUninstallClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppGridAdapter.ViewHolder>() {

    private var apps: List<AppInfo> = emptyList()
    var isEditMode = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun submitList(newApps: List<AppInfo>) {
        apps = newApps
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_grid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(apps[position], isEditMode)
    }

    override fun getItemCount(): Int = apps.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardIcon: View = itemView.findViewById(R.id.cardIcon)
        private val ivAppIcon: ImageView = itemView.findViewById(R.id.ivAppIcon)
        private val tvAppName: TextView = itemView.findViewById(R.id.tvAppName)
        private val btnUninstall: TextView = itemView.findViewById(R.id.btnUninstall)

        fun bind(appInfo: AppInfo, editMode: Boolean) {
            tvAppName.text = appInfo.name
            
            // 加载图标
            ivAppIcon.load(appInfo.icon) {
                size(128)
                crossfade(true)
            }

            // 编辑模式
            if (editMode) {
                btnUninstall.visibility = View.VISIBLE
                startJiggleAnimation()
            } else {
                btnUninstall.visibility = View.GONE
                cardIcon.clearAnimation()
            }

            // 点击事件
            itemView.setOnClickListener {
                if (!editMode) {
                    onAppClick(appInfo)
                }
            }

            // 长按事件
            itemView.setOnLongClickListener {
                onAppLongClick(appInfo)
            }

            // 卸载按钮点击
            btnUninstall.setOnClickListener {
                onUninstallClick(appInfo)
            }
        }

        private fun startJiggleAnimation() {
            val rotate = RotateAnimation(
                -1.5f, 1.5f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 300
                repeatCount = Animation.INFINITE
                repeatMode = Animation.REVERSE
            }
            cardIcon.startAnimation(rotate)
        }
    }
}
