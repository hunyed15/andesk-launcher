package com.andesk.launcher.data.model

data class WeatherInfo(
    val city: String,
    val temp: Int,           // 当前温度
    val tempMax: Int,        // 最高温度
    val tempMin: Int,        // 最低温度
    val text: String,        // 天气描述
    val icon: String,        // 天气图标代码
    val humidity: Int,       // 湿度
    val windDir: String,     // 风向
    val windScale: String,   // 风速
    val updateTime: Long = System.currentTimeMillis()
) {
    val tempDisplay: String
        get() = "${temp}°C"
    
    val tempRangeDisplay: String
        get() = "${tempMin}°~${tempMax}°"
    
    val iconResName: String
        get() = "weather_ic_${icon}"
}
