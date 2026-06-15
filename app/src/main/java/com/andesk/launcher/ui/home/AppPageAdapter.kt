package com.andesk.launcher.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andesk.launcher.R
import com.andesk.launcher.data.model.AppInfo
import com.andesk.launcher.data.model.Folder

/**
 * 分页适配器 - ViewPager2使用
 * 支持应用和文件夹
 */
class AppPageAdapter(
    private val columnsPerRow: Int,
    private val firstPageRows: Int,
    private val otherPageRows: Int,
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: (AppInfo) -> Boolean,
    private val onFolderClick: (Folder) -> Unit,
    private val onFolderLongClick: (Folder) -> Boolean,
    private val onUninstallClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppPageAdapter.PageViewHolder>() {

    private var allApps: List<AppInfo> = emptyList()
    private var allFolders: List<Folder> = emptyList()
    private var pages: List<List<DesktopItem>> = emptyList()
    private var isEditMode = false

    fun setApps(apps: List<AppInfo>, folders: List<Folder>) {
        allApps = apps
        allFolders = folders
        pages = splitIntoPages(apps, folders)
        notifyDataSetChanged()
    }

    /**
     * 将应用和文件夹分页
     */
    private fun splitIntoPages(apps: List<AppInfo>, folders: List<Folder>): List<List<DesktopItem>> {
        val result = mutableListOf<List<DesktopItem>>()
        
        // 创建桌面项列表（文件夹在前，应用在后）
        val desktopItems = mutableListOf<DesktopItem>()
        folders.forEach { desktopItems.add(DesktopItem.FolderItem(it)) }
        apps.forEach { desktopItems.add(DesktopItem.App(it)) }
        
        var currentIndex = 0

        // 第一页：2行 = 10个位置
        val firstPageCount = columnsPerRow * firstPageRows
        if (desktopItems.isNotEmpty()) {
            val firstPage = desktopItems.subList(0, minOf(firstPageCount, desktopItems.size))
            result.add(firstPage.toList())
            currentIndex = firstPage.size
        }

        // 后续页面：5行 = 25个位置
        val otherPageCount = columnsPerRow * otherPageRows
        while (currentIndex < desktopItems.size) {
            val endIndex = minOf(currentIndex + otherPageCount, desktopItems.size)
            val page = desktopItems.subList(currentIndex, endIndex)
            result.add(page.toList())
            currentIndex = endIndex
        }

        return result
    }

    fun setEditMode(editMode: Boolean) {
        isEditMode = editMode
        notifyDataSetChanged()
    }

    fun getPageCount(): Int = pages.size

    fun updatePageItems(pageIndex: Int, items: List<DesktopItem>) {
        if (pageIndex !in pages.indices) return
        pages = pages.toMutableList().also { it[pageIndex] = items.toList() }
    }

    fun getAppOrder(): List<String> {
        return pages.flatten().mapNotNull { item ->
            when (item) {
                is DesktopItem.App -> item.appInfo.packageName
                is DesktopItem.FolderItem -> null
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_page, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val pageItems = pages[position]
        holder.bind(pageItems, isEditMode)
    }

    override fun getItemCount(): Int = pages.size

    inner class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.rvPageApps)
        var currentAdapter: DesktopGridAdapter? = null

        fun bind(items: List<DesktopItem>, editMode: Boolean) {
            val adapter = currentAdapter ?: DesktopGridAdapter(
                    onAppClick = onAppClick,
                    onAppLongClick = onAppLongClick,
                    onFolderClick = onFolderClick,
                    onFolderLongClick = onFolderLongClick,
                    onUninstallClick = onUninstallClick
                ).also {
                    currentAdapter = it
                    recyclerView.apply {
                        layoutManager = GridLayoutManager(context, columnsPerRow)
                        adapter = it
                        itemAnimator = null
                        setHasFixedSize(true)
                    }
                }
            adapter.isEditMode = editMode
            adapter.setItems(items, allApps)
        }

        fun getAdapter(): DesktopGridAdapter? = currentAdapter
    }
}
