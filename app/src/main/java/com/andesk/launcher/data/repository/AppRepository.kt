package com.andesk.launcher.data.repository

import android.content.Context
import com.andesk.launcher.data.local.FolderManager
import com.andesk.launcher.data.local.PrefsManager
import com.andesk.launcher.data.model.AppInfo
import com.andesk.launcher.data.model.Folder
import com.andesk.launcher.util.AppUtils

class AppRepository(
    private val context: Context,
    private val prefsManager: PrefsManager
) {
    private var cachedApps: List<AppInfo>? = null
    private val folderManager = FolderManager(context)

    /**
     * 获取所有已安装应用
     */
    fun getInstalledApps(forceRefresh: Boolean = false): List<AppInfo> {
        if (cachedApps == null || forceRefresh) {
            cachedApps = AppUtils.getInstalledApps(context)
        }
        return cachedApps!!
    }

    /**
     * 获取排序后的应用列表（不含文件夹中的应用）
     */
    fun getSortedApps(): List<AppInfo> {
        val allApps = getInstalledApps()
        val appOrder = folderManager.getAppOrder()
        val appsInFolders = folderManager.getAllFolders().flatMap { it.apps }.toSet()
        
        // 过滤掉文件夹中的应用
        val standaloneApps = allApps.filter { !appsInFolders.contains(it.packageName) }
        
        // 按保存的排序
        return if (appOrder.isNotEmpty()) {
            val orderedApps = mutableListOf<AppInfo>()
            // 先添加有排序的应用
            appOrder.forEach { packageName ->
                standaloneApps.find { it.packageName == packageName }?.let {
                    orderedApps.add(it)
                }
            }
            // 再添加新安装的应用
            standaloneApps.filter { !appOrder.contains(it.packageName) }.forEach {
                orderedApps.add(it)
            }
            orderedApps
        } else {
            standaloneApps
        }
    }

    /**
     * 保存应用排序
     */
    fun saveAppOrder(appOrder: List<String>) {
        folderManager.saveAppOrder(appOrder)
    }

    /**
     * 获取所有文件夹
     */
    fun getAllFolders(): List<Folder> {
        return folderManager.getAllFolders()
    }

    /**
     * 创建文件夹
     */
    fun createFolder(name: String, apps: List<String> = emptyList()): Folder {
        return folderManager.createFolder(name, apps)
    }

    /**
     * 删除文件夹
     */
    fun deleteFolder(folderId: String) {
        folderManager.deleteFolder(folderId)
    }

    /**
     * 重命名文件夹
     */
    fun renameFolder(folderId: String, newName: String) {
        folderManager.renameFolder(folderId, newName)
    }

    /**
     * 添加应用到文件夹
     */
    fun addAppToFolder(folderId: String, packageName: String) {
        folderManager.addAppToFolder(folderId, packageName)
    }

    /**
     * 从文件夹移除应用
     */
    fun removeAppFromFolder(folderId: String, packageName: String) {
        folderManager.removeAppFromFolder(folderId, packageName)
    }

    /**
     * 获取应用所在的文件夹
     */
    fun getFolderForApp(packageName: String): Folder? {
        return folderManager.getFolderForApp(packageName)
    }

    /**
     * 合并两个应用到新文件夹
     */
    fun mergeToFolder(packageName1: String, packageName2: String): Folder {
        val app1 = getInstalledApps().find { it.packageName == packageName1 }
        val app2 = getInstalledApps().find { it.packageName == packageName2 }
        
        val folderName = if (app1 != null && app2 != null) {
            "${app1.name}、${app2.name}"
        } else {
            "新文件夹"
        }
        
        return createFolder(folderName, listOf(packageName1, packageName2))
    }

    /**
     * 获取Dock栏应用
     */
    fun getDockApps(): List<AppInfo> {
        val dockPackageNames = prefsManager.getDockApps()
        val allApps = getInstalledApps()
        
        return dockPackageNames.mapNotNull { packageName ->
            allApps.find { it.packageName == packageName }
        }
    }

    /**
     * 保存Dock栏应用
     */
    fun saveDockApps(apps: List<AppInfo>) {
        val packageNames = apps.map { it.packageName }
        prefsManager.setDockApps(packageNames)
    }

    /**
     * 启动应用
     */
    fun launchApp(packageName: String): Boolean {
        return AppUtils.launchApp(context, packageName)
    }

    /**
     * 卸载应用
     */
    fun uninstallApp(packageName: String) {
        AppUtils.uninstallApp(context, packageName)
    }

    /**
     * 清除缓存（应用安装/卸载时调用）
     */
    fun clearCache() {
        cachedApps = null
    }
}
