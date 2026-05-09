package com.example.weatherexpect.Dao

import android.util.Log
import com.example.weatherexpect.Data.Entity.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull

object DatabaseInitializer {
    fun initializeDatabase(database: WeatherDatabase) {
        val TAG="DatabaseService"

        Log.d(TAG,"init database...")
        val dao = database.weatherDao()

        /*
        CoroutineScope(Dispatchers.IO).launch {
            // 检查是否已有用户设置
            val settings = dao.getUserSettings().let { flow ->
                flow.firstOrNull()
            }

            if (settings == null) {
                // 插入默认用户设置
                val defaultSettings = UserSettings(
                    id = "101010100",
                    lastLocation = "北京"

                )
                dao.insertUserSettings(defaultSettings)
                Log.i(TAG,"insert default settings")
            }

            Log.d(TAG,"finish initialization")
        }
         */
    }
}