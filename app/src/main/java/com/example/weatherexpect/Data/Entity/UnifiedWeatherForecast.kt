package com.example.weatherexpect.Data.Entity

import java.io.Serializable

data class UnifiedWeatherForecast(
    val location: String,
    val fxDate: String,     // 预报日期
    val updateTime: String,
    val tempMax: String,    // 最高温度（字符串类型，匹配 API）
    val tempMin: String,    // 最低温度（字符串类型，匹配 API）
    val iconDay: String,    // 白天天气图标
    val textDay: String,    // 白天天气描述
    val iconNight: String,  // 夜间天气图标
    val textNight: String,  // 夜间天气描述
    val windDirDay: String, // 白天风向
    val windScaleDay: String, // 白天风力
    val precip: String,     // 降水量（字符串类型，匹配 API）
    val uvIndex: String,    // 紫外线指数（字符串类型，匹配 API）
    val humidity: String,   // 湿度（字符串类型，匹配 API）
    val pressure: String    // 气压（字符串类型，匹配 API）
) : Serializable {
    // 转换为数据库实体
    fun toEntity(): WeatherForecast {
        return WeatherForecast(
            location = location,
            fxDate = fxDate,
            updateTime = updateTime,
            tempMax = tempMax.toIntOrNull() ?: 0,
            tempMin = tempMin.toIntOrNull() ?: 0,
            iconDay = iconDay,
            textDay = textDay,
            iconNight = iconNight,
            textNight = textNight,
            windDirDay = windDirDay,
            windScaleDay = windScaleDay,
            precip = precip.toDoubleOrNull() ?: 0.0,
            uvIndex = uvIndex.toIntOrNull() ?: 0,
            humidity = humidity.toIntOrNull() ?: 0,
            pressure = pressure.toIntOrNull() ?: 0
        )
    }

    // 从数据库实体转换
    companion object {
        fun fromEntity(entity: WeatherForecast): UnifiedWeatherForecast {
            return UnifiedWeatherForecast(
                location = entity.location,
                fxDate = entity.fxDate,
                updateTime = entity.updateTime,
                tempMax = entity.tempMax.toString(),
                tempMin = entity.tempMin.toString(),
                iconDay = entity.iconDay,
                textDay = entity.textDay,
                iconNight = entity.iconNight,
                textNight = entity.textNight,
                windDirDay = entity.windDirDay,
                windScaleDay = entity.windScaleDay,
                precip = entity.precip.toString(),
                uvIndex = entity.uvIndex.toString(),
                humidity = entity.humidity.toString(),
                pressure = entity.pressure.toString()
            )
        }
    }
}