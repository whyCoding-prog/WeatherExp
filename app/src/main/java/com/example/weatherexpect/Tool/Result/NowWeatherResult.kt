package com.example.weatherexpect.Tool.Result

import com.example.weatherexpect.Data.Entity.UnifiedCurrentWeather

sealed class NowWeatherResult {
    data class Success(val weather: UnifiedCurrentWeather) : NowWeatherResult()
    data class Cached(val weather: UnifiedCurrentWeather) : NowWeatherResult()
    object Error : NowWeatherResult()
}