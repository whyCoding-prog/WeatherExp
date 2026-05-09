package com.example.weatherexpect.Data.Entity

// 统一当前天气数据模型
data class UnifiedCurrentWeather(
    val location: String,
    val obsTime: String,
    val updateTime: String,
    val temp: String,      // 保持字符串类型以匹配API
    val feelsLike: String, // 保持字符串类型以匹配API
    val icon: String,
    val text: String
) {
    // 转换为数据库实体
    fun toEntity(): CurrentWeather {
        return CurrentWeather(
            location = location,
            obsTime = obsTime,
            updateTime = updateTime,
            temp = temp.toIntOrNull() ?: 0,
            feelsLike = feelsLike.toIntOrNull() ?: 0,
            icon = icon,
            text = text
        )
    }

    // 从数据库实体转换
    companion object {
        fun fromEntity(entity: CurrentWeather): UnifiedCurrentWeather {
            return UnifiedCurrentWeather(
                location = entity.location,
                obsTime = entity.obsTime,
                updateTime = entity.updateTime,
                temp = entity.temp.toString(),
                feelsLike = entity.feelsLike.toString(),
                icon = entity.icon,
                text = entity.text
            )
        }
    }
}

/*  val location: String,
    val obsTime: String,
    val updateTime: String,
    val temp: Int,
    val feelsLike: Int,
    val icon: String,
    val text: String
    */