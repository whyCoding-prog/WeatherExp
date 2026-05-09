package com.example.weatherexpect.Service

import com.example.weatherexpect.Data.Response.Response.DailyWeatherResponse
import com.example.weatherexpect.Data.Response.Response.LocationResponse
import com.example.weatherexpect.Data.Response.Response.NowWeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {
    @GET("/v7/weather/now")     //GET方式，请求路径
    suspend fun getNowWeather(
        @Query("location")location:String,   //请求参数
        @Query("lang") lang: String = "en"
    ): NowWeatherResponse    //响应参数

    @GET("/v7/weather/7d")
    suspend fun getDailyWeather(
        @Query("location")location: String,
        @Query("lang")lang: String="en"
    ):DailyWeatherResponse

    @GET("/geo/v2/city/lookup")
    suspend fun getLocation(
        @Query("location")location: String,
        @Query("number")number:String="10"  //返回10个结果
    ):LocationResponse
}