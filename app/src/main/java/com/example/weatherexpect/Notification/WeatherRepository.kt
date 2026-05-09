package com.example.weatherexpect.Notification

import android.util.Log
import com.example.weatherexpect.Service.WeatherService
import com.example.weatherexpect.Tool.Result.DailyWeatherResult
import com.example.weatherexpect.Tool.Result.NowWeatherResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class WeatherRepository {
    private val TAG = "WeatherRepository"
    private val weatherService = WeatherService.getInstance()

    // 单例模式实现
    companion object {
        @Volatile
        private var instance: WeatherRepository? = null

        fun getInstance(): WeatherRepository {
            return instance ?: synchronized(this) {
                instance ?: WeatherRepository().also { instance = it }
            }
        }
    }

    // 获取每日天气数据
    suspend fun fetchDailyWeather(location: String, cityName: String): DailyWeatherResult {
        Log.d(TAG, "Fetching daily weather for location: $location, city: $cityName")
        return try {
            weatherService.fetchDailyWeather(location, cityName)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching daily weather: ${e.message}", e)
            throw e
        }
    }
}