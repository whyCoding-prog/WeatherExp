package com.example.weatherexpect

import android.app.Application
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.baidu.location.LocationClient
import com.example.weatherexpect.Dao.WeatherDatabase
import com.example.weatherexpect.Service.Setting
import com.example.weatherexpect.Service.WeatherService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MyApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob())

    lateinit var database: WeatherDatabase
        private set

    override fun onCreate() {
        super.onCreate()

        // 百度地图初始化
        LocationClient.setAgreePrivacy(true)
        SDKInitializer.setAgreePrivacy(this, true)
        SDKInitializer.initialize(this)
        SDKInitializer.setCoordType(CoordType.BD09LL)

        // 数据库初始化
        database = WeatherDatabase.getDatabase(
            context = this,
            scope = applicationScope
        )

        // WeatherService 和 Setting 初始化
        val weatherDao = database.weatherDao()
        Setting.initialize(weatherDao)
        WeatherService.initialize(weatherDao)


    }
}
