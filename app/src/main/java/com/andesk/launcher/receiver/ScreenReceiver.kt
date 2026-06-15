package com.andesk.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.andesk.launcher.data.local.PrefsManager
import com.andesk.launcher.service.KeyMappingService

/**
 * 屏幕状态广播接收器 - 监听解锁 + 保活按键映射
 */
class ScreenReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_SCREEN_ON = "com.andesk.launcher.SCREEN_ON"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_USER_PRESENT -> {
                // 用户解锁 → 刷新天气/一言
                val refreshIntent = Intent(ACTION_SCREEN_ON).setPackage(context.packageName)
                context.sendBroadcast(refreshIntent)
                // 保活按键映射服务
                ensureKeyMappingRunning(context)
            }
            Intent.ACTION_SCREEN_ON -> {
                // 屏幕亮起也保活
                ensureKeyMappingRunning(context)
            }
        }
    }

    private fun ensureKeyMappingRunning(context: Context) {
        try {
            val prefs = PrefsManager(context)
            if (prefs.keyMappingEnabled && !KeyMappingService.isRunning) {
                val intent = Intent(context, KeyMappingService::class.java)
                context.startForegroundService(intent)
            }
        } catch (e: Exception) {
            // 静默失败
        }
    }
}
