package com.andesk.launcher.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

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
        
        
        // 壁纸设置
        private const val KEY_WALLPAPER_URI = "wallpaper_uri"
        private const val KEY_WALLPAPER_BLUR = "wallpaper_blur"
        
        // 天气缓存
        private const val KEY_WEATHER_CACHE = "weather_cache"
        private const val KEY_WEATHER_CACHE_TIME = "weather_cache_time"
        
        // 主题设置
        private const val KEY_THEME_MODE = "theme_mode" // light, dark
        
        // 按键映射设置
        private const val KEY_MAPPING_ENABLED = "key_mapping_enabled"
        private const val KEY_MAPPING_SINGLE_CLICK = "key_mapping_single_click" // home, none
        private const val KEY_MAPPING_SHOW_TOAST = "key_mapping_show_toast"
        
        // 其他
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_BOOT_START = "boot_start"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // ==================== 桌面设置 ====================

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "light") ?: "light"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    // ==================== 按键映射设置 ====================

    var keyMappingEnabled: Boolean
        get() = prefs.getBoolean(KEY_MAPPING_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_MAPPING_ENABLED, value).apply()

    var keyMappingSingleClick: String
        get() = prefs.getString(KEY_MAPPING_SINGLE_CLICK, "home") ?: "home"
        set(value) = prefs.edit().putString(KEY_MAPPING_SINGLE_CLICK, value).apply()

    var keyMappingShowToast: Boolean
        get() = prefs.getBoolean(KEY_MAPPING_SHOW_TOAST, true)
        set(value) = prefs.edit().putBoolean(KEY_MAPPING_SHOW_TOAST, value).apply()

    var is24HourFormat: Boolean
        get() = prefs.getBoolean(KEY_CLOCK_FORMAT_24H, true)
        set(value) = prefs.edit().putBoolean(KEY_CLOCK_FORMAT_24H, value).apply()

    var showWeather: Boolean
        get() = prefs.getBoolean(KEY_SHOW_WEATHER, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_WEATHER, value).apply()

    var bootStart: Boolean
        get() = prefs.getBoolean(KEY_BOOT_START, true)
        set(value) = prefs.edit().putBoolean(KEY_BOOT_START, value).apply()

    var weatherCity: String
        get() = prefs.getString(KEY_WEATHER_CITY, "上海") ?: "上海"
        set(value) = prefs.edit().putString(KEY_WEATHER_CITY, value).apply()

    var weatherCityId: String
        get() = prefs.getString(KEY_WEATHER_CITY_ID, "310000") ?: "310000"
        set(value) = prefs.edit().putString(KEY_WEATHER_CITY_ID, value).apply()

    var tempUnit: String
        get() = prefs.getString(KEY_TEMP_UNIT, "C") ?: "C"
        set(value) = prefs.edit().putString(KEY_TEMP_UNIT, value).apply()

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
            try {
                // 使用JSONArray解析，避免TypeToken泛型擦除问题
                val jsonArray = org.json.JSONArray(json)
                val result = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    result.add(jsonArray.getString(i))
                }
                result
            } catch (e: Exception) {
                e.printStackTrace()
                getDefaultDockApps()
            }
        } else {
            getDefaultDockApps()
        }
    }

    private fun getDefaultDockApps(): List<String> {
        return listOf(
            "com.android.dialer",
            "com.android.mms",
            "com.android.chrome",
            "com.android.settings"
        )
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
        return (System.currentTimeMillis() - cacheTime) < 30 * 60 * 1000
    }

    fun clearWeatherCache() {
        prefs.edit().remove(KEY_WEATHER_CACHE).remove(KEY_WEATHER_CACHE_TIME).apply()
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
