package com.andesk.launcher.util

import android.app.ActivityManager
import android.content.Context
import android.os.Debug

object MemoryUtils {

    data class MemoryInfo(
        val totalMB: Long,
        val usedMB: Long,
        val availableMB: Long,
        val usagePercent: Int
    )

    /**
     * 获取内存信息字符串，如 "2.4G/4G"
     */
    fun getMemoryInfo(context: Context): MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val totalMB = memInfo.totalMem / (1024 * 1024)
        val availableMB = memInfo.availMem / (1024 * 1024)
        val usedMB = totalMB - availableMB
        val usagePercent = ((usedMB.toFloat() / totalMB) * 100).toInt()

        return MemoryInfo(
            totalMB = totalMB,
            usedMB = usedMB,
            availableMB = availableMB,
            usagePercent = usagePercent
        )
    }

    /**
     * 获取运行中的应用进程
     */
    fun getRunningProcesses(context: Context): List<ActivityManager.RunningAppProcessInfo> {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return activityManager.runningAppProcesses ?: emptyList()
    }

    /**
     * 清理后台进程
     * 返回被清理的进程数
     */
    fun killBackgroundProcesses(context: Context, excludePackages: List<String> = emptyList()): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processes = getRunningProcesses(context)
        var killedCount = 0

        for (process in processes) {
            // 跳过当前应用和排除列表
            if (process.processName == context.packageName) continue
            if (excludePackages.any { process.processName.contains(it) }) continue
            
            // 只清理后台进程（importance >= 400）
            if (process.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
                activityManager.killBackgroundProcesses(process.processName)
                killedCount++
            }
        }

        return killedCount
    }

    /**
     * 获取应用内存使用情况（MB）
     */
    fun getAppMemoryUsage(context: Context, pid: Int): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = activityManager.getProcessMemoryInfo(intArrayOf(pid))
        return if (memoryInfo.isNotEmpty()) {
            memoryInfo[0].totalPss.toLong() / 1024 // KB to MB
        } else {
            0
        }
    }

    /**
     * 获取当前应用内存使用（MB）
     */
    fun getCurrentAppMemoryMB(context: Context): Long {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        return usedMemory / (1024 * 1024)
    }

    /**
     * 系统是否内存不足
     */
    fun isLowMemory(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return memInfo.lowMemory
    }
}
