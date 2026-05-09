package com.example.weatherexpect.Data.Response.Response

import com.example.weatherexpect.Data.Response.Data.Location
import com.example.weatherexpect.Data.Response.Data.Refer
import com.google.gson.annotations.SerializedName

data class LocationResponse(
    @SerializedName("code") val code: String,
    @SerializedName("location") val locations: List<Location>,
    @SerializedName("refer") val refer: Refer
)