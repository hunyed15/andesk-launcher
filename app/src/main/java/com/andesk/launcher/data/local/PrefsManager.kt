package com.andesk.launcher.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PrefsManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "andesk_prefs"
        
        // 桌面设置
        private const val KEY_DOCK_APPS = "dock_apps"
        private const val KEY_CLOCK_FORMAT_24H = "clock_format_24h"
        private const val KEY_SHOW_WEATHER = "show_weather"
        private const val KEY_WEATHER_CITY = "weather_city"
        private const val KEY_WEATHER_CITY_ID = "weather_city_id"
        private const val KEY_TEMP_UNIT = "temp_unit" // C or F
        
        // 小圆点设置
        private const val KEY_FLOATING_ENABLED = "floating_enabled"
        private const val KEY_FLOATING_X = "floating_x"
        private const val KEY_FLOATING_Y = "floating_y"
        private const val KEY_FLOATING_ALPHA = "floating_alpha"
        
        // 壁纸设置
        private const val KEY_WALLPAPER_URI = "wallpaper_uri"
        private const val KEY_WALLPAPER_BLUR = "wallpaper_blur"
        
        // 天气缓存
        private const val KEY_WEATHER_CACHE = "weather_cache"
        private const val KEY_WEATHER_CACHE_TIME = "weather_cache_time"
        
        // 其他
        private const val KEY_FIRST_LAUNCH = "first_launch"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // ==================== 桌面设置 ====================

    var is24HourFormat: Boolean
        get() = prefs.getBoolean(KEY_CLOCK_FORMAT_24H, true)
        set(value) = prefs.edit().putBoolean(KEY_CLOCK_FORMAT_24H, value).apply()

    var showWeather: Boolean
        get() = prefs.getBoolean(KEY_SHOW_WEATHER, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_WEATHER, value).apply()

    var weatherCity: String
        get() = prefs.getString(KEY_WEATHER_CITY, "上海") ?: "上海"
        set(value) = prefs.edit().putString(KEY_WEATHER_CITY, value).apply()

    var weatherCityId: String
        get() = prefs.getString(KEY_WEATHER_CITY_ID, "101020100") ?: "101020100"
        set(value) = prefs.edit().putString(KEY_WEATHER_CITY_ID, value).apply()

    var tempUnit: String
        get() = prefs.getString(KEY_TEMP_UNIT, "C") ?: "C"
        set(value) = prefs.edit().putString(KEY_TEMP_UNIT, value).apply()

    // ==================== 小圆点设置 ====================

    var floatingEnabled: Boolean
        get() = prefs.getBoolean(KEY_FLOATING_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_FLOATING_ENABLED, value).apply()

    var floatingX: Int
        get() = prefs.getInt(KEY_FLOATING_X, -1)
        set(value) = prefs.edit().putInt(KEY_FLOATING_X, value).apply()

    var floatingY: Int
        get() = prefs.getInt(KEY_FLOATING_Y, -1)
        set(value) = prefs.edit().putInt(KEY_FLOATING_Y, value).apply()

    var floatingAlpha: Float
        get() = prefs.getFloat(KEY_FLOATING_ALPHA, 0.7f)
        set(value) = prefs.edit().putFloat(KEY_FLOATING_ALPHA, value).apply()

    // ==================== 壁纸设置 ====================

    var wallpaperUri: String?
        get() = prefs.getString(KEY_WALLPAPER_URI, null)
        set(value) = prefs.edit().putString(KEY_WALLPAPER_URI, value).apply()

    var wallpaperBlur: Int
        get() = prefs.getInt(KEY_WALLPAPER_BLUR, 0)
        set(value) = prefs.edit().putInt(KEY_WALLPAPER_BLUR, value).apply()

    // ==================== Dock栏应用 ====================

    fun getDockApps(): List<String> {
        val json = prefs.getString(KEY_DOCK_APPS, null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            // 默认Dock应用
            listOf(
                "com.android.dialer",
                "com.android.mms",
                "com.android.chrome",
                "com.android.settings"
            )
        }
    }

    fun setDockApps(apps: List<String>) {
        val json = gson.toJson(apps)
        prefs.edit().putString(KEY_DOCK_APPS, json).apply()
    }

    // ==================== 天气缓存 ====================

    fun saveWeatherCache(weatherJson: String) {
        prefs.edit()
            .putString(KEY_WEATHER_CACHE, weatherJson)
            .putLong(KEY_WEATHER_CACHE_TIME, System.currentTimeMillis())
            .apply()
    }

    fun getWeatherCache(): String? {
        return prefs.getString(KEY_WEATHER_CACHE, null)
    }

    fun getWeatherCacheTime(): Long {
        return prefs.getLong(KEY_WEATHER_CACHE_TIME, 0)
    }

    fun isWeatherCacheValid(): Boolean {
        val cacheTime = getWeatherCacheTime()
        val thirtyMinutes = 30 * 60 * 1000
        return (System.currentTimeMillis() - cacheTime) < thirtyMinutes
    }

    // ==================== 其他 ====================

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()

    /**
     * 清除所有数据
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
