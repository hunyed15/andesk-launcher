package com.andesk.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PackageReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                // 应用安装/卸载时，发送广播通知桌面刷新
                val refreshIntent = Intent(ACTION_REFRESH_APPS)
                context.sendBroadcast(refreshIntent)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH_APPS = "com.andesk.launcher.REFRESH_APPS"
    }
}
