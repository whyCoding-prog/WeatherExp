package com.example.weatherexpect.Tool.Result

import com.example.weatherexpect.Data.Entity.UnifiedWeatherForecast

sealed class DailyWeatherResult {
    data class Success(val daily: List<UnifiedWeatherForecast>) : DailyWeatherResult()
    data class Cached(val daily: List<UnifiedWeatherForecast>) : DailyWeatherResult()
    object Error : DailyWeatherResult()
}