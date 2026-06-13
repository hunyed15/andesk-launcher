package com.andesk.launcher

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Configuration

class App : Application(), Configuration.Provider {

    companion object {
        lateinit var instance: App
            private set
        
        const val CHANNEL_WEATHER = "weather_updates"
        const val CHANNEL_FLOATING = "floating_service"
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            
            // 天气更新通知渠道
            val weatherChannel = NotificationChannel(
                CHANNEL_WEATHER,
                "天气更新",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "天气数据更新通知"
                setShowBadge(false)
            }
            
            // 悬浮窗服务通知渠道
            val floatingChannel = NotificationChannel(
                CHANNEL_FLOATING,
                "Home小圆点",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Home小圆点服务通知"
                setShowBadge(false)
            }
            
            manager.createNotificationChannel(weatherChannel)
            manager.createNotificationChannel(floatingChannel)
        }
    }
}
