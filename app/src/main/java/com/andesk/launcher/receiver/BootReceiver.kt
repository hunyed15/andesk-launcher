package com.andesk.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.andesk.launcher.data.local.PrefsManager
import com.andesk.launcher.service.KeyMappingService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = PrefsManager(context)
            if (!prefs.bootStart) return
            // 开机后启动按键映射服务
            if (prefs.keyMappingEnabled && !KeyMappingService.isRunning) {
                val serviceIntent = Intent(context, KeyMappingService::class.java)
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
