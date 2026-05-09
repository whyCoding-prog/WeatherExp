package com.example.weatherexpect.Data.Response.Response

import com.example.weatherexpect.Data.Response.Data.Now
import com.example.weatherexpect.Data.Response.Data.Refer
import com.google.gson.annotations.SerializedName

data class NowWeatherResponse(
    @SerializedName("code")val code: String,
    @SerializedName("updateTime")val updateTime: String,
    @SerializedName("fxLink")val fxLink: String,
    @SerializedName("now")val now: Now,
    @SerializedName("refer")val refer: Refer
)
/*
code 请参考状态码
updateTime 当前API的最近更新时间
fxLink 当前数据的响应式页面，便于嵌入网站或应用
* */