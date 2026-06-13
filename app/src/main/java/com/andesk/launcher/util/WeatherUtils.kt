package com.andesk.launcher.util

object WeatherUtils {

    /**
     * 和风天气图标代码对应描述
     */
    fun getWeatherDescription(iconCode: String): String {
        return when (iconCode) {
            "100" -> "晴"
            "101" -> "多云"
            "102" -> "少云"
            "103" -> "晴间多云"
            "104" -> "阴"
            "150" -> "晴"
            "151" -> "多云"
            "153" -> "晴间多云"
            "300", "301" -> "阵雨"
            "302", "303" -> "雷阵雨"
            "304" -> "雷阵雨伴有冰雹"
            "305", "306", "307" -> "小雨"
            "308", "309", "310", "311", "312" -> "大雨"
            "313" -> "冻雨"
            "314", "315", "316" -> "暴雨"
            "317", "318" -> "大暴雨"
            "399" -> "雨"
            "400", "401", "402", "403", "404", "405", "406", "407", "408", "409", "410" -> "雪"
            "499" -> "雪"
            "500", "501", "502", "503", "504", "507", "508" -> "雾"
            "509", "510", "511", "512", "513" -> "霾"
            "514", "515" -> "沙尘暴"
            "900" -> "热"
            "901" -> "冷"
            "999" -> "未知"
            else -> "未知"
        }
    }

    /**
     * 判断是否为白天（根据图标代码）
     */
    fun isDaytime(iconCode: String): Boolean {
        // 150-199通常是夜间图标
        val code = iconCode.toIntOrNull() ?: return true
        return code !in 150..199
    }

    /**
     * 获取天气背景色资源
     */
    fun getWeatherBgColor(iconCode: String): Int {
        return when {
            iconCode.startsWith("1") -> 0xFF87CEEB.toInt() // 晴天 - 天蓝色
            iconCode.startsWith("3") -> 0xFF708090.toInt() // 雨天 - 灰蓝色
            iconCode.startsWith("4") -> 0xFFDCDCDC.toInt() // 雪天 - 浅灰色
            iconCode.startsWith("5") -> 0xFF696969.toInt() // 雾霾 - 暗灰色
            else -> 0xFF87CEEB.toInt()
        }
    }
}
