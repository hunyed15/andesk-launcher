package com.andesk.launcher.data.repository

import android.util.Log
import com.andesk.launcher.data.local.PrefsManager
import com.andesk.launcher.data.model.WeatherInfo
import com.andesk.launcher.data.remote.AmapWeatherApi
import com.andesk.launcher.data.remote.AmapForecastCast
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class WeatherRepository(private val prefsManager: PrefsManager) {

    companion object {
        private const val TAG = "WeatherRepo"
        // QWeather图标CDN
        private fun iconUrl(code: String) = "https://a.hecdn.net/img/common/icon/202106d/$code.png"
    }

    private val gson = Gson()

    private val api: AmapWeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(AmapWeatherApi.BASE_URL)
            .client(OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AmapWeatherApi::class.java)
    }

    suspend fun getWeather(): WeatherInfo? {
        if (prefsManager.isWeatherCacheValid()) {
            prefsManager.getWeatherCache()?.let {
                return try { gson.fromJson(it, WeatherInfo::class.java) } catch (_: Exception) { null }
            }
        }
        return fetchFromApi()
    }

    private suspend fun fetchFromApi(): WeatherInfo? {
        val adcode = prefsManager.weatherCityId
        Log.d(TAG, "请求天气: ${prefsManager.weatherCity} ($adcode)")

        return try {
            val (liveResp, forecastResp) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val live = api.getWeather(adcode, ext = "base").execute()
                val forecast = api.getWeather(adcode, ext = "all").execute()
                live to forecast
            }
            if (!liveResp.isSuccessful || liveResp.body()?.status != "1") {
                Log.e(TAG, "实况天气API失败: ${liveResp.code()}")
                return getCached()
            }
            val live = liveResp.body()?.lives?.firstOrNull() ?: return getCached()
            val todayForecast = if (forecastResp.isSuccessful && forecastResp.body()?.status == "1") {
                forecastResp.body()?.forecasts?.firstOrNull()?.casts?.firstOrNull()
            } else {
                Log.w(TAG, "预报天气API失败: ${forecastResp.code()}")
                null
            }

            val currentTemp = live.temperature.toIntOrNull() ?: 0
            val tempRange = resolveTempRange(todayForecast, currentTemp)

            val info = WeatherInfo(
                city = live.city,
                temp = currentTemp,
                tempMax = tempRange.second,
                tempMin = tempRange.first,
                text = live.weather,
                icon = mapIcon(live.weather),
                humidity = live.humidity.toIntOrNull() ?: 0,
                windDir = live.winddirection,
                windScale = "${live.windpower}级"
            )

            prefsManager.saveWeatherCache(gson.toJson(info))
            Log.d(TAG, "天气: ${info.temp}° ${info.tempRangeDisplay} ${info.text}, 实况=${live.reporttime}, 预报=${todayForecast?.date ?: "无"}")
            info
        } catch (e: Exception) {
            Log.e(TAG, "天气请求失败", e)
            getCached()
        }
    }

    private fun resolveTempRange(cast: AmapForecastCast?, currentTemp: Int): Pair<Int, Int> {
        val dayTemp = cast?.daytemp?.toIntOrNull()
        val nightTemp = cast?.nighttemp?.toIntOrNull()
        if (dayTemp != null && nightTemp != null) {
            return minOf(nightTemp, dayTemp) to maxOf(nightTemp, dayTemp)
        }
        return (currentTemp - 3) to currentTemp
    }

    private fun mapIcon(text: String): String = when {
        text.contains("晴") -> "100"
        text.contains("云") -> "101"
        text.contains("阴") -> "104"
        text.contains("雨") -> "305"
        text.contains("雪") -> "400"
        text.contains("雾") || text.contains("霾") -> "501"
        text.contains("风") -> "200"
        else -> "100"
    }

    private fun getCached(): WeatherInfo? {
        return prefsManager.getWeatherCache()?.let {
            try { gson.fromJson(it, WeatherInfo::class.java) } catch (_: Exception) { null }
        }
    }
}
