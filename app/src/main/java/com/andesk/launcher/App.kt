package com.andesk.launcher

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.andesk.launcher.data.local.PrefsManager
import com.jinrishici.sdk.android.JinrishiciClient

class App : Application(), Configuration.Provider, ImageLoaderFactory {

    companion object {
        lateinit var instance: App
            private set
        
        const val CHANNEL_WEATHER = "weather_updates"
        const val CHANNEL_KEY_MAPPING = "key_mapping_service"
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化今日诗词 SDK
        JinrishiciClient.getInstance().init(this)

        createNotificationChannels()
        
        // 应用保存的主题设置
        val prefsManager = PrefsManager(this)
        when (prefsManager.themeMode) {
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    /**
     * 配置Coil图片加载器
     * 针对低端设备优化
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            // 内存缓存
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)  // 使用25%可用内存
                    .build()
            }
            // 磁盘缓存
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(25L * 1024 * 1024)  // 25MB磁盘缓存
                    .build()
            }
            // 全局默认配置
            .crossfade(true)  // 淡入动画
            .crossfade(300)
            // 低端设备优化
            .allowHardware(true)  // 启用硬件加速
            .build()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // 内存紧张时释放资源
        if (level >= TRIM_MEMORY_MODERATE) {
            System.gc()
        }
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
            
            
            manager.createNotificationChannel(weatherChannel)
            
            // 按键映射服务通知渠道
            val keyMappingChannel = NotificationChannel(
                CHANNEL_KEY_MAPPING,
                "按键映射服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "按键映射服务运行通知"
                setShowBadge(false)
            }
            manager.createNotificationChannel(keyMappingChannel)
        }
    }
}
