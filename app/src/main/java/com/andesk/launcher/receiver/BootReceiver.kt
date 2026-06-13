package com.andesk.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.andesk.launcher.data.local.PrefsManager
import com.andesk.launcher.ui.floating.FloatingService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefsManager = PrefsManager(context)
            
            // 开机启动Home小圆点服务
            if (prefsManager.floatingEnabled) {
                val serviceIntent = Intent(context, FloatingService::class.java)
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
