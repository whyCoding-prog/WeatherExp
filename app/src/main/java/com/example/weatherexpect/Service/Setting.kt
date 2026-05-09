package com.example.weatherexpect.Service

import android.util.Log
import com.example.weatherexpect.Dao.WeatherDao
import com.example.weatherexpect.Data.Entity.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

object Setting {
    // 默认值
    var currentCityName = "北京"
    var currentCityID = "101010100"
    var notificationSetting = false

    var unitValue = 0
    var unitValue_=1

    private lateinit var weatherDao: WeatherDao

    // 初始化方法（在应用启动时调用）
    fun initialize(dao: WeatherDao) {
        weatherDao = dao
        loadSettings()
    }

    // 从数据库加载设置
    private fun loadSettings() {
        runBlocking {
            try {
                val settings = weatherDao.getUserSettings().first()
                currentCityName = settings.lastLocation
                currentCityID = settings.id
                notificationSetting = settings.notification
                Log.d("Setting", "Loaded settings: $currentCityName, $currentCityID, $notificationSetting")
            } catch (e: Exception) {
                Log.e("Setting", "Failed to load settings, using defaults", e)
            }
        }
    }

    // 更新设置到数据库（在应用退出时调用）
    fun saveSettings() {
        runBlocking {
            try {
                weatherDao.deleteOldUserSettings()
                val settings = UserSettings(
                    id = currentCityID,
                    lastLocation = currentCityName,
                    notification = notificationSetting
                )
                weatherDao.updateUserSettings(settings)
                Log.d("Setting", "Settings saved")
            } catch (e: Exception) {
                Log.e("Setting", "Failed to save settings", e)
            }
        }
    }

    // 更新城市信息
    fun updateCity(id: String, name: String) {
        currentCityID = id
        currentCityName = name
    }

    // 更新通知设置
    fun updateNotification(enabled: Boolean) {
        notificationSetting = enabled
    }
}