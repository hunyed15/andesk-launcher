package com.andesk.launcher.util

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager

object DeviceUtils {

    /**
     * 获取设备总内存（MB）
     */
    fun getTotalMemoryMB(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.totalMem / (1024 * 1024)
    }

    /**
     * 获取可用内存（MB）
     */
    fun getAvailableMemoryMB(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem / (1024 * 1024)
    }

    /**
     * 是否为低内存设备（<=2GB）
     */
    fun isLowMemoryDevice(context: Context): Boolean {
        return getTotalMemoryMB(context) <= 2048
    }

    /**
     * 是否为Android Go设备
     */
    fun isAndroidGoDevice(): Boolean {
        return Build.DEVICE?.contains("go", ignoreCase = true) == true ||
               Build.MODEL?.contains("go", ignoreCase = true) == true
    }

    /**
     * 获取屏幕宽度（dp）
     */
    fun getScreenWidthDp(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)
        return (metrics.widthPixels / metrics.density).toInt()
    }

    /**
     * 获取屏幕高度（dp）
     */
    fun getScreenHeightDp(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)
        return (metrics.heightPixels / metrics.density).toInt()
    }

    /**
     * 获取设备型号
     */
    fun getDeviceModel(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    /**
     * 获取Android版本
     */
    fun getAndroidVersion(): String {
        return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }
}
