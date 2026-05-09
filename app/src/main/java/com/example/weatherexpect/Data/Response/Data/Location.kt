package com.example.weatherexpect.Data.Response.Data

import com.google.gson.annotations.SerializedName

data class Location(
    @SerializedName("name") val name: String,
    @SerializedName("id") val id: String,
    @SerializedName("lat") val latitude: String,  // 纬度
    @SerializedName("lon") val longitude: String, // 经度
    @SerializedName("adm2") val adm2: String,
    @SerializedName("adm1") val adm1: String,
    @SerializedName("country") val country: String,
    @SerializedName("tz") val timeZone: String,
    @SerializedName("utcOffset") val utcOffset: String,
    @SerializedName("isDst") val isDst: String,
    @SerializedName("type") val type: String,
    @SerializedName("rank") val rank: String,
    @SerializedName("fxLink") val fxLink: String
)

/*
code 请参考状态码
location.name 地区/城市名称
location.id 地区/城市ID
location.lat 地区/城市纬度
location.lon 地区/城市经度
location.adm2 地区/城市的上级行政区划名称
location.adm1 地区/城市所属一级行政区域
location.country 地区/城市所属国家名称
location.tz 地区/城市所在时区
location.utcOffset 地区/城市目前与UTC时间偏移的小时数，参考详细说明
location.isDst 地区/城市是否当前处于夏令时。1 表示当前处于夏令时，0 表示当前不是夏令时。
location.type 地区/城市的属性
location.rank 地区评分
location.fxLink 该地区的天气预报网页链接，便于嵌入你的网站或应用
*/