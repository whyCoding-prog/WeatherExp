package com.example.weatherexpect.Data.Response.Data

import com.google.gson.annotations.SerializedName

data class Daily(
    @SerializedName("fxDate") val fxDate: String,
    @SerializedName("sunrise") val sunrise: String?,
    @SerializedName("sunset") val sunset: String?,
    @SerializedName("moonrise") val moonrise: String?,
    @SerializedName("moonset") val moonset: String?,
    @SerializedName("moonPhase") val moonPhase: String,
    @SerializedName("moonPhaseIcon") val moonPhaseIcon: String,
    @SerializedName("tempMax") val tempMax: String,
    @SerializedName("tempMin") val tempMin: String,
    @SerializedName("iconDay") val iconDay: String,
    @SerializedName("textDay") val textDay: String,
    @SerializedName("iconNight") val iconNight: String,
    @SerializedName("textNight") val textNight: String,
    @SerializedName("wind360Day") val wind360Day: String,
    @SerializedName("windDirDay") val windDirDay: String,
    @SerializedName("windScaleDay") val windScaleDay: String,
    @SerializedName("windSpeedDay") val windSpeedDay: String,
    @SerializedName("wind360Night") val wind360Night: String,
    @SerializedName("windDirNight") val windDirNight: String,
    @SerializedName("windScaleNight") val windScaleNight: String,
    @SerializedName("windSpeedNight") val windSpeedNight: String,
    @SerializedName("humidity") val humidity: String,
    @SerializedName("precip") val precip: String,
    @SerializedName("pressure") val pressure: String,
    @SerializedName("vis") val vis: String,
    @SerializedName("cloud") val cloud: String?,
    @SerializedName("uvIndex") val uvIndex: String
)

/*
daily.fxDate 预报日期
daily.sunrise 日出时间，在高纬度地区可能为空
daily.sunset 日落时间，在高纬度地区可能为空
daily.moonrise 当天月升时间，可能为空
daily.moonset 当天月落时间，可能为空
daily.moonPhase 月相名称
daily.moonPhaseIcon 月相图标代码，另请参考天气图标项目
daily.tempMax 预报当天最高温度
daily.tempMin 预报当天最低温度
daily.iconDay 预报白天天气状况的图标代码，另请参考天气图标项目
daily.textDay 预报白天天气状况文字描述，包括阴晴雨雪等天气状态的描述
daily.iconNight 预报夜间天气状况的图标代码，另请参考天气图标项目
daily.textNight 预报晚间天气状况文字描述，包括阴晴雨雪等天气状态的描述
daily.wind360Day 预报白天风向360角度
daily.windDirDay 预报白天风向
daily.windScaleDay 预报白天风力等级
daily.windSpeedDay 预报白天风速，公里/小时
daily.wind360Night 预报夜间风向360角度
daily.windDirNight 预报夜间当天风向
daily.windScaleNight 预报夜间风力等级
daily.windSpeedNight 预报夜间风速，公里/小时
daily.precip 预报当天总降水量，默认单位：毫米
daily.uvIndex 紫外线强度指数
daily.humidity 相对湿度，百分比数值
daily.pressure 大气压强，默认单位：百帕
daily.vis 能见度，默认单位：公里
daily.cloud 云量，百分比数值。可能为空
**/


