package com.example.weatherexpect

import android.app.Application
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.baidu.location.LocationClient

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LocationClient.setAgreePrivacy(true)
        SDKInitializer.setAgreePrivacy(this, true)
        SDKInitializer.initialize(this)
        SDKInitializer.setCoordType(CoordType.BD09LL)
    }
}