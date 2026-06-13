package com.andesk.launcher.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 和风天气API接口
 * 文档: https://dev.qweather.com/docs/api/
 */
interface QWeatherApi {

    companion object {
        // TODO: 替换为你的和风天气API Key
        const val API_KEY = "YOUR_API_KEY_HERE"
        const val BASE_URL = "https://devapi.qweather.com"
    }

    /**
     * 获取实时天气
     * https://dev.qweather.com/docs/api/weather/weather-now/
     */
    @GET("/v7/weather/now")
    suspend fun getWeatherNow(
        @Query("location") location: String,
        @Query("key") key: String = API_KEY
    ): WeatherNowResponse

    /**
     * 获取3天预报
     * https://dev.qweather.com/docs/api/weather/weather-daily-forecast/
     */
    @GET("/v7/weather/3d")
    suspend fun getWeather3Day(
        @Query("location") location: String,
        @Query("key") key: String = API_KEY
    ): WeatherDailyResponse

    /**
     * 城市搜索
     * https://dev.qweather.com/docs/api/geoapi/city-lookup/
     */
    @GET("https://geoapi.qweather.com/v2/city/lookup")
    suspend fun searchCity(
        @Query("location") location: String,
        @Query("key") key: String = API_KEY
    ): CitySearchResponse
}

// ==================== 响应数据类 ====================

data class WeatherNowResponse(
    @SerializedName("code") val code: String,
    @SerializedName("now") val now: WeatherNow?
)

data class WeatherNow(
    @SerializedName("temp") val temp: String,
    @SerializedName("feelsLike") val feelsLike: String,
    @SerializedName("text") val text: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("humidity") val humidity: String,
    @SerializedName("windDir") val windDir: String,
    @SerializedName("windScale") val windScale: String
)

data class WeatherDailyResponse(
    @SerializedName("code") val code: String,
    @SerializedName("daily") val daily: List<WeatherDaily>?
)

data class WeatherDaily(
    @SerializedName("fxDate") val fxDate: String,
    @SerializedName("tempMax") val tempMax: String,
    @SerializedName("tempMin") val tempMin: String,
    @SerializedName("textDay") val textDay: String,
    @SerializedName("textNight") val textNight: String,
    @SerializedName("iconDay") val iconDay: String,
    @SerializedName("iconNight") val iconNight: String
)

data class CitySearchResponse(
    @SerializedName("code") val code: String,
    @SerializedName("location") val location: List<CityLocation>?
)

data class CityLocation(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("adm1") val adm1: String,  // 省份
    @SerializedName("adm2") val adm2: String,  // 城市
    @SerializedName("country") val country: String
) {
    val fullName: String
        get() = if (adm1 == adm2) "$adm1 $name" else "$adm1 $adm2 $name"
}
