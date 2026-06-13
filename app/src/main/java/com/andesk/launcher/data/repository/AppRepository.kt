package com.andesk.launcher.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import com.andesk.launcher.data.local.PrefsManager
import com.andesk.launcher.data.model.AppInfo
import com.andesk.launcher.util.AppUtils

class AppRepository(
    private val context: Context,
    private val prefsManager: PrefsManager
) {
    private var cachedApps: List<AppInfo>? = null

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
