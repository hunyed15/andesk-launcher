package com.andesk.launcher.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.andesk.launcher.App
import com.andesk.launcher.R
import com.andesk.launcher.ui.home.HomeActivity

/**
 * 按键映射通知服务（仅显示通知，实际按键处理在 AccessibilityService）
 */
class KeyMappingService : Service() {

    companion object {
        var isRunning = false
            private set
        private const val NOTIFICATION_ID = 1002
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        showNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    private fun showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                App.CHANNEL_KEY_MAPPING, "按键映射服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply { setShowBadge(false) }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val pi = PendingIntent.getActivity(this, 0,
            Intent(this, HomeActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = Notification.Builder(this, App.CHANNEL_KEY_MAPPING)
            .setContentTitle("安云桌面")
            .setContentText("按键映射运行中")
            .setSmallIcon(R.drawable.ic_home)
            .setContentIntent(pi)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }
}
