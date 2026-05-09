package com.example.weatherexpect.Data.Response.Response

import com.example.weatherexpect.Data.Response.Data.Daily
import com.example.weatherexpect.Data.Response.Data.Refer
import com.google.gson.annotations.SerializedName

class DailyWeatherResponse (
    @SerializedName("code") val code: String,
    @SerializedName("updateTime") val updateTime: String,
    @SerializedName("fxLink") val fxLink: String,
    @SerializedName("daily") val daily: List<Daily>,
    @SerializedName("refer") val refer: Refer
)