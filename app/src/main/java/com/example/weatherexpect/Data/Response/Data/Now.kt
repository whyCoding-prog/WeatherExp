package com.example.weatherexpect.Data.Response.Data

import com.google.gson.annotations.SerializedName

data class Now(
    @SerializedName("obsTime")val obsTime: String,
    @SerializedName("temp")val temp: String,
    @SerializedName("feelsLike")val feelsLike: String,
    @SerializedName("icon")val icon: String,
    @SerializedName("text")val text: String,
    @SerializedName("wind360")val wind360: String,
    @SerializedName("windDir")val windDir: String,
    @SerializedName("windScale")val windScale: String,
    @SerializedName("windSpeed")val windSpeed: String,
    @SerializedName("humidity")val humidity: String,
    @SerializedName("precip")val precip: String,
    @SerializedName("pressure")val pressure: String,
    @SerializedName("vis")val vis: String,
    @SerializedName("cloud")val cloud: String?,
    @SerializedName("dew")val dew: String?
)

/*
*now.obsTime 数据观测时间
now.temp 温度，默认单位：摄氏度
now.feelsLike 体感温度，默认单位：摄氏度
now.icon 天气状况的图标代码，另请参考天气图标项目
now.text 天气状况的文字描述，包括阴晴雨雪等天气状态的描述
now.wind360 风向360角度
now.windDir 风向
now.windScale 风力等级
now.windSpeed 风速，公里/小时
now.humidity 相对湿度，百分比数值
now.precip 过去1小时降水量，默认单位：毫米
now.pressure 大气压强，默认单位：百帕
now.vis 能见度，默认单位：公里
now.cloud 云量，百分比数值。可能为空
now.dew 露点温度。可能为空
* */

