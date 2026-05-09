package com.example.weatherexpect.Dao

import com.example.weatherexpect.Data.Entity.CurrentWeather
import com.example.weatherexpect.Data.Entity.WeatherForecast
import com.example.weatherexpect.Data.Response.Response.DailyWeatherResponse
import com.example.weatherexpect.Data.Response.Response.NowWeatherResponse


/*7天天气预报更新策略*/
class WeatherRepository(private val dao: WeatherDao) {

    suspend fun process7DayForecast(location: String, response: DailyWeatherResponse) {
        val firstDate = response.daily.first().fxDate
        val dbUpdateTime = dao.getForecastUpdateTime(location, firstDate)

        // 检查是否需要更新
        if (dbUpdateTime != response.updateTime) {
            val forecasts = response.daily.map { daily ->
                WeatherForecast(
                    location = location,
                    fxDate = daily.fxDate,
                    updateTime = response.updateTime,
                    tempMax = daily.tempMax.toInt(),
                    tempMin = daily.tempMin.toInt(),
                    iconDay = daily.iconDay,
                    textDay = daily.textDay,
                    iconNight = daily.iconNight,
                    textNight = daily.textNight,
                    windDirDay = daily.windDirDay,
                    windScaleDay = daily.windScaleDay,
                    precip = daily.precip.toDouble(),
                    uvIndex = daily.uvIndex.toInt(),
                    humidity = daily.humidity.toInt(),
                    pressure = daily.pressure.toInt()
                )
            }
            dao.insertAllForecasts(forecasts)
        }
    }

    suspend fun saveCurrentWeather(location: String, response: NowWeatherResponse) {
        val currentWeather = CurrentWeather(
            location = location,
            obsTime = response.now.obsTime,
            updateTime = response.updateTime,
            temp = response.now.temp.toInt(),
            feelsLike = response.now.feelsLike.toInt(),
            icon = response.now.icon,
            text = response.now.text
        )
        dao.insertCurrentWeather(currentWeather)
    }
}