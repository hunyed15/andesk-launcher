package com.andesk.launcher.ui.home

import com.andesk.launcher.data.model.AppInfo
import com.andesk.launcher.data.model.Folder

/**
 * 桌面项（可以是应用或文件夹）
 */
sealed class DesktopItem {
    data class App(val appInfo: AppInfo) : DesktopItem()
    data class FolderItem(val folder: Folder) : DesktopItem()
}
