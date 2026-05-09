package com.example.weatherexpect.Data.Entity

import androidx.room.Entity

@Entity(tableName = "weather_forecast", primaryKeys = ["location", "fxDate"])
data class WeatherForecast(
    val location: String,
    val fxDate: String,     //预报日期
    val updateTime: String,
    val tempMax: Int,
    val tempMin: Int,
    val iconDay: String,
    val textDay: String,
    val iconNight: String,
    val textNight: String,
    val windDirDay: String,
    val windScaleDay: String,
    val precip: Double,
    val uvIndex: Int,
    val humidity: Int,
    val pressure: Int
)

/*
fxDate 预报日期
tempMax 预报当天最高温度
tempMin 预报当天最低温度
iconDay 预报白天天气状况的图标代码，另请参考天气图标项目
textDay 预报白天天气状况文字描述，包括阴晴雨雪等天气状态的描述
iconNight 预报夜间天气状况的图标代码，另请参考天气图标项目
textNight 预报晚间天气状况文字描述，包括阴晴雨雪等天气状态的描述
windDirDay 预报白天风向
windScaleDay 预报白天风力等级
precip 预报当天总降水量，默认单位：毫米
uvIndex 紫外线强度指数
humidity 相对湿度，百分比数值
pressure 大气压强，默认单位：百帕
**/