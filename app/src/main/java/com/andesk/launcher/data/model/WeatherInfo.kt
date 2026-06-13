package com.andesk.launcher.data.model

data class WeatherInfo(
    val city: String,
    val temp: Int,
    val text: String,
    val icon: String,
    val humidity: Int,
    val windDir: String,
    val windScale: String,
    val updateTime: Long = System.currentTimeMillis()
) {
    val tempDisplay: String
        get() = "${temp}°C"
    
    val iconResName: String
        get() = "weather_ic_${icon}"
}
