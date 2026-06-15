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
import com.andesk.launcher.data.model.Folder
import java.util.Collections

/**
 * 桌面网格适配器 - 支持应用和文件夹
 */
class DesktopGridAdapter(
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: (AppInfo) -> Boolean,
    private val onFolderClick: (Folder) -> Unit,
    private val onFolderLongClick: (Folder) -> Boolean,
    private val onUninstallClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_APP = 0
        private const val TYPE_FOLDER = 1
    }

    private var items: MutableList<DesktopItem> = mutableListOf()
    private var allApps: List<AppInfo> = emptyList()
    var isEditMode = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    // 拖拽监听器
    var onItemMoved: ((fromPos: Int, toPos: Int) -> Unit)? = null
    var onMergeToFolder: ((fromPos: Int, toPos: Int) -> Unit)? = null

    init {
        setHasStableIds(true)
    }

    fun setItems(newItems: List<DesktopItem>, allApps: List<AppInfo>) {
        items = newItems.toMutableList()
        this.allApps = allApps
        notifyDataSetChanged()
    }

    fun getItems(): List<DesktopItem> = items.toList()

    /**
     * 移动项
     */
    fun onItemMove(fromPos: Int, toPos: Int) {
        Collections.swap(items, fromPos, toPos)
        notifyItemMoved(fromPos, toPos)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DesktopItem.App -> TYPE_APP
            is DesktopItem.FolderItem -> TYPE_FOLDER
        }
    }

    override fun getItemId(position: Int): Long {
        return when (val item = items[position]) {
            is DesktopItem.App -> item.appInfo.packageName.hashCode().toLong()
            is DesktopItem.FolderItem -> item.folder.id.hashCode().toLong()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_APP -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_app_grid, parent, false)
                AppViewHolder(view)
            }
            TYPE_FOLDER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_folder, parent, false)
                FolderViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is DesktopItem.App -> (holder as AppViewHolder).bind(item.appInfo, isEditMode)
            is DesktopItem.FolderItem -> (holder as FolderViewHolder).bind(item.folder)
        }
    }

    override fun getItemCount(): Int = items.size

    /**
     * 应用ViewHolder
     */
    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardIcon: View = itemView.findViewById(R.id.cardIcon)
        private val ivAppIcon: ImageView = itemView.findViewById(R.id.ivAppIcon)
        private val tvAppName: TextView = itemView.findViewById(R.id.tvAppName)
        private val btnUninstall: TextView = itemView.findViewById(R.id.btnUninstall)

        fun bind(appInfo: AppInfo, editMode: Boolean) {
            tvAppName.text = appInfo.name
            
            ivAppIcon.load(appInfo.icon) {
                size(128)
                crossfade(true)
            }

            if (editMode) {
                btnUninstall.visibility = View.VISIBLE
                startJiggleAnimation()
            } else {
                btnUninstall.visibility = View.GONE
                cardIcon.clearAnimation()
            }

            itemView.setOnClickListener {
                if (!editMode) onAppClick(appInfo)
            }

            itemView.setOnLongClickListener {
                onAppLongClick(appInfo)
            }

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

    /**
     * 文件夹ViewHolder
     */
    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val folderIcon1: ImageView = itemView.findViewById(R.id.folderIcon1)
        private val folderIcon2: ImageView = itemView.findViewById(R.id.folderIcon2)
        private val folderIcon3: ImageView = itemView.findViewById(R.id.folderIcon3)
        private val folderIcon4: ImageView = itemView.findViewById(R.id.folderIcon4)
        private val tvFolderName: TextView = itemView.findViewById(R.id.tvFolderName)

        fun bind(folder: Folder) {
            tvFolderName.text = folder.name

            val icons = listOf(folderIcon1, folderIcon2, folderIcon3, folderIcon4)
            val appPackageNames = folder.apps.take(4)
            
            icons.forEachIndexed { index, imageView ->
                if (index < appPackageNames.size) {
                    imageView.visibility = View.VISIBLE
                    val app = allApps.find { it.packageName == appPackageNames[index] }
                    imageView.load(app?.icon) {
                        size(64)
                    }
                } else {
                    imageView.visibility = View.INVISIBLE
                }
            }

            itemView.setOnClickListener {
                onFolderClick(folder)
            }

            itemView.setOnLongClickListener {
                onFolderLongClick(folder)
            }
        }
    }
}
