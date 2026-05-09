package com.example.weatherexpect.Data.Entity

import androidx.room.Entity

@Entity(tableName = "current_weather", primaryKeys = ["location"])
data class CurrentWeather(
    val location: String,
    val obsTime: String,
    val updateTime: String,
    val temp: Int,
    val feelsLike: Int,
    val icon: String,
    val text: String
)

/*
*now.obsTime 数据观测时间
now.temp 温度，默认单位：摄氏度
now.feelsLike 体感温度，默认单位：摄氏度
now.icon 天气状况的图标代码，另请参考天气图标项目
now.text 天气状况的文字描述，包括阴晴雨雪等天气状态的描述
* */