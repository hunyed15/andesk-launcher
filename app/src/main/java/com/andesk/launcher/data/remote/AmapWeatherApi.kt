package com.andesk.launcher.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 高德天气 REST API
 */
interface AmapWeatherApi {

    companion object {
        const val BASE_URL = "https://restapi.amap.com"
        const val KEY = "709099802f21e97b7361939c35ed4bf8"
    }

    @GET("/v3/weather/weatherInfo")
    fun getWeather(
        @Query("city") adcode: String,
        @Query("key") key: String = KEY,
        @Query("extensions") ext: String = "base"
    ): Call<AmapWeatherResponse>

    /** IP定位 */
    @GET("/v3/ip")
    fun ipLocation(
        @Query("key") key: String = KEY
    ): Call<AmapIpResponse>
}

data class AmapWeatherResponse(
    @SerializedName("status") val status: String,
    @SerializedName("lives") val lives: List<AmapLiveWeather>?,
    @SerializedName("forecasts") val forecasts: List<AmapForecast>?
)

data class AmapLiveWeather(
    @SerializedName("province") val province: String,
    @SerializedName("city") val city: String,
    @SerializedName("adcode") val adcode: String,
    @SerializedName("weather") val weather: String,
    @SerializedName("temperature") val temperature: String,
    @SerializedName("winddirection") val winddirection: String,
    @SerializedName("windpower") val windpower: String,
    @SerializedName("humidity") val humidity: String,
    @SerializedName("reporttime") val reporttime: String
)

data class AmapForecast(
    @SerializedName("province") val province: String,
    @SerializedName("city") val city: String,
    @SerializedName("adcode") val adcode: String,
    @SerializedName("reporttime") val reporttime: String,
    @SerializedName("casts") val casts: List<AmapForecastCast>?
)

data class AmapForecastCast(
    @SerializedName("date") val date: String,
    @SerializedName("week") val week: String,
    @SerializedName("dayweather") val dayweather: String,
    @SerializedName("nightweather") val nightweather: String,
    @SerializedName("daytemp") val daytemp: String,
    @SerializedName("nighttemp") val nighttemp: String,
    @SerializedName("daywind") val daywind: String,
    @SerializedName("nightwind") val nightwind: String,
    @SerializedName("daypower") val daypower: String,
    @SerializedName("nightpower") val nightpower: String
)

data class AmapIpResponse(
    @SerializedName("status") val status: String,
    @SerializedName("province") val province: String,
    @SerializedName("city") val city: String,
    @SerializedName("adcode") val adcode: String
)

object AmapApiHolder {
    val api: AmapWeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(AmapWeatherApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AmapWeatherApi::class.java)
    }
}
