package com.andesk.launcher.data.repository

import android.util.Log
import com.andesk.launcher.data.local.PrefsManager
import com.andesk.launcher.data.model.WeatherInfo
import com.andesk.launcher.data.remote.QWeatherClient
import com.google.gson.Gson

class WeatherRepository(private val prefsManager: PrefsManager) {

    companion object {
        private const val TAG = "WeatherRepository"
    }

    private val gson = Gson()

    /**
     * 获取天气信息（优先缓存）
     */
    suspend fun getWeather(): WeatherInfo? {
        // 先检查缓存
        if (prefsManager.isWeatherCacheValid()) {
            val cached = prefsManager.getWeatherCache()
            if (cached != null) {
                return try {
                    gson.fromJson(cached, WeatherInfo::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        }

        // 缓存过期或不存在，请求API
        return fetchWeatherFromApi()
    }

    /**
     * 从API获取天气
     */
    private suspend fun fetchWeatherFromApi(): WeatherInfo? {
        return try {
            val cityId = prefsManager.weatherCityId
            val response = QWeatherClient.api.getWeatherNow(cityId)

            if (response.code == "200" && response.now != null) {
                val weatherInfo = WeatherInfo(
                    city = prefsManager.weatherCity,
                    temp = response.now.temp.toIntOrNull() ?: 0,
                    text = response.now.text,
                    icon = response.now.icon,
                    humidity = response.now.humidity.toIntOrNull() ?: 0,
                    windDir = response.now.windDir,
                    windScale = response.now.windScale
                )

                // 保存到缓存
                prefsManager.saveWeatherCache(gson.toJson(weatherInfo))

                weatherInfo
            } else {
                Log.e(TAG, "Weather API error: ${response.code}")
                getCachedWeather()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch weather", e)
            getCachedWeather()
        }
    }

    /**
     * 获取缓存的天气（即使过期）
     */
    private fun getCachedWeather(): WeatherInfo? {
        val cached = prefsManager.getWeatherCache()
        return if (cached != null) {
            try {
                gson.fromJson(cached, WeatherInfo::class.java)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    /**
     * 搜索城市
     */
    suspend fun searchCity(query: String): List<com.andesk.launcher.data.remote.CityLocation> {
        return try {
            val response = QWeatherClient.api.searchCity(query)
            if (response.code == "200") {
                response.location ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to search city", e)
            emptyList()
        }
    }

    /**
     * 更新城市设置
     */
    fun updateCity(cityName: String, cityId: String) {
        prefsManager.weatherCity = cityName
        prefsManager.weatherCityId = cityId
    }
}
