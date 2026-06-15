package com.andesk.launcher.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Settings
import com.andesk.launcher.data.model.AppInfo
import java.text.Collator
import java.util.Locale

object AppUtils {

    private val chineseCollator: Collator = Collator.getInstance(Locale.CHINA)

    /**
     * 获取所有可启动的应用
     */
    fun getInstalledApps(context: Context): List<AppInfo> {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        val resolveInfos: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)
        
        return resolveInfos
            .filter { it.activityInfo.packageName != context.packageName } // 排除自己
            .map { resolveInfo ->
                AppInfo(
                    name = resolveInfo.loadLabel(packageManager).toString(),
                    packageName = resolveInfo.activityInfo.packageName,
                    icon = resolveInfo.loadIcon(packageManager),
                    isSystemApp = (resolveInfo.activityInfo.applicationInfo.flags and 
                        android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .sortedWith(compareByDescending<AppInfo> { hasChineseName(it.name) }
                .thenComparator { left, right ->
                    chineseCollator.compare(left.name, right.name)
                }
                .thenBy { it.packageName })
    }

    private fun hasChineseName(name: String): Boolean {
        return name.any { char ->
            Character.UnicodeScript.of(char.code) == Character.UnicodeScript.HAN
        }
    }

    /**
     * 启动应用
     */
    fun launchApp(context: Context, packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 卸载应用
     */
    fun uninstallApp(context: Context, packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * 打开应用详情设置
     */
    fun openAppSettings(context: Context, packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * 检查应用是否已安装
     */
    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * 获取应用名称
     */
    fun getAppName(context: Context, packageName: String): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
}
