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
import com.andesk.launcher.data.model.Folder

/**
 * 文件夹适配器
 */
class FolderAdapter(
    private val onFolderClick: (Folder) -> Unit,
    private val onFolderLongClick: (Folder) -> Boolean
) : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    private var folders: List<Folder> = emptyList()
    private var allApps: List<AppInfo> = emptyList()

    fun setData(folders: List<Folder>, allApps: List<AppInfo>) {
        this.folders = folders
        this.allApps = allApps
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_folder, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(folders[position])
    }

    override fun getItemCount(): Int = folders.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val folderIcon1: ImageView = itemView.findViewById(R.id.folderIcon1)
        private val folderIcon2: ImageView = itemView.findViewById(R.id.folderIcon2)
        private val folderIcon3: ImageView = itemView.findViewById(R.id.folderIcon3)
        private val folderIcon4: ImageView = itemView.findViewById(R.id.folderIcon4)
        private val tvFolderName: TextView = itemView.findViewById(R.id.tvFolderName)

        fun bind(folder: Folder) {
            tvFolderName.text = folder.name

            // 显示文件夹中的前4个应用图标
            val icons = listOf(folderIcon1, folderIcon2, folderIcon3, folderIcon4)
            val appPackageNames = folder.apps.take(4)
            
            // 隐藏不需要的图标
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

            // 点击事件
            itemView.setOnClickListener {
                onFolderClick(folder)
            }

            // 长按事件
            itemView.setOnLongClickListener {
                onFolderLongClick(folder)
            }
        }
    }
}
