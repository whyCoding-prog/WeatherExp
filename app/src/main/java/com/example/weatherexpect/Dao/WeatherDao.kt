package com.example.weatherexpect.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherexpect.Data.Entity.CurrentWeather
import com.example.weatherexpect.Data.Entity.UserSettings
import com.example.weatherexpect.Data.Entity.WeatherForecast
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    // 天气预报操作
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(forecast: WeatherForecast)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllForecasts(forecasts: List<WeatherForecast>)

    @Query("DELETE FROM weather_forecast WHERE location = :location") // 假设表名为 daily_weather
    suspend fun deleteAllForecastsByLocation(location: String)

    @Query("SELECT * FROM weather_forecast WHERE location = :location ORDER BY fxDate ASC")
    fun getForecasts(location: String): Flow<List<WeatherForecast>>

    @Query("SELECT updateTime FROM weather_forecast WHERE location = :location AND fxDate = :date LIMIT 1")
    suspend fun getForecastUpdateTime(location: String, date: String): String?

    // 当前天气操作
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentWeather(weather: CurrentWeather)

    @Query("SELECT * FROM current_weather WHERE location = :location")
    fun getCurrentWeather(location: String): Flow<CurrentWeather?>

    @Query("DELETE FROM current_weather WHERE location = :location")
    suspend fun deleteByLocation(location: String)

    // 用户设置操作-----------------------------------------------------------------------------------

    // 获取用户设置
    @Query("SELECT * FROM user_settings LIMIT 1")
    fun getUserSettings(): Flow<UserSettings>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUserSettings(settings: UserSettings)

    @Query("DELETE FROM user_settings")
    suspend fun deleteOldUserSettings()
}