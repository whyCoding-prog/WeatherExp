package com.example.weatherexpect.Notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.weatherexpect.Data.Entity.UnifiedWeatherForecast
import com.example.weatherexpect.MainActivity
import com.example.weatherexpect.R
import com.example.weatherexpect.Service.Setting
import com.example.weatherexpect.Tool.ActivityRequest.CHANNEL_ID
import com.example.weatherexpect.Tool.Result.DailyWeatherResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PollService: Service()
{
    //private val context: Context by lazy { applicationContext }
    private val repository: WeatherRepository by lazy {
        WeatherRepository.getInstance()  // 通过单例获取
    }
    private var serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        NotificationUtils.createForegroundChannel(this)
    }

    companion object {
        private val TAG = "Notification"
        const val ACTION_START_FOREGROUND = "ACTION_START_FOREGROUND"
        const val ACTION_STOP_FOREGROUND = "ACTION_STOP_FOREGROUND"
        const val ACTION_WEATHER_ALARM = "com.example.weatherexpect.ACTION_WEATHER_ALARM"

        private const val POLL_INTERVAL_MS = 30 * 60 * 1000L

        fun startService(context: Context) {
            val intent = Intent(context, PollService::class.java).apply {action = ACTION_START_FOREGROUND }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        // 控制闹钟启停
        fun setServiceAlarm(context: Context, isOn: Boolean) {
            val alarmIntent = Intent(context, PollReceiver::class.java).apply {
                action = ACTION_WEATHER_ALARM
            }

            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pi = PendingIntent.getBroadcast(context, 0, alarmIntent, flags)
            val alarmManager = context.getSystemService(AlarmManager::class.java)

            if (isOn) {
                alarmManager.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,       //屏幕灭也触发
                    SystemClock.elapsedRealtime() + 60_000,     //首次触发时间
                    POLL_INTERVAL_MS,       //间隔时间
                    pi
                )
            } else {
                alarmManager.cancel(pi) // 取消闹钟
                pi.cancel() // 释放 PendingIntent 资源
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "重要天气通知",  // 更明显的名称
                NotificationManager.IMPORTANCE_HIGH  // 提高重要性
            ).apply {
                description = "显示每日天气更新"
                setShowBadge(true)  // 允许在应用图标上显示角标
                enableLights(true)  // 启用指示灯
                lightColor = Color.RED  // 指示灯颜色
                enableVibration(true)  // 启用震动
                vibrationPattern = longArrayOf(0, 500, 250, 500)  // 震动模式
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "Notification channel created: $CHANNEL_ID")
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            cm.activeNetworkInfo?.isConnected == true
        }
    }

    //启动服务
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_FOREGROUND -> {    //开启前台服务，拉数据，发消息
                serviceScope.cancel()
                serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

                // 启动前台服务
                val notification = NotificationUtils.createForegroundNotification(this)
                startForeground(NotificationUtils.NOTIFICATION_ID, notification)

                /*// 执行后台任务
                CoroutineScope(Dispatchers.IO).launch {
                    fetchWeatherData()
                }*/

                serviceScope.launch {
                    try {
                        fetchWeatherData()
                    } catch (e: Exception) {
                        Log.e(TAG, "获取天气数据异常", e)
                    }
                }
            }
            //每次 launch 前重建
            ACTION_STOP_FOREGROUND -> {
                serviceScope.cancel()
                serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())  // 重建
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

        }
        return START_NOT_STICKY
    }

    private suspend fun fetchWeatherData() {
        if (!isNetworkAvailable()) return

        val location=Setting.currentCityID
        val cityName=Setting.currentCityName


        val result = repository.fetchDailyWeather(location, cityName)

        val dailyList = when (result) {
            is DailyWeatherResult.Success -> result.daily
            is DailyWeatherResult.Cached  -> result.daily
            is DailyWeatherResult.Error   -> {
                Log.e(TAG, "Weather fetch error")
                return
            }
        }

        if (dailyList.isNotEmpty()) {
            sendWeatherNotification(dailyList[0])
        } else {
            Log.w(TAG, "No daily weather data available")
        }
    }

    private fun sendWeatherNotification(forecast: UnifiedWeatherForecast) {
        createNotificationChannel()
        //logChannelStatus()

        // 创建通知构建器
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.transparent_icon)
            .setContentTitle("今日天气 · ${Setting.currentCityName}")
            .setContentText("白天: ${forecast.textDay}, 夜间: ${forecast.textNight}")
            .setTicker("新的天气通知")
            .setPriority(NotificationCompat.PRIORITY_HIGH)  // 优先级
            .setCategory(NotificationCompat.CATEGORY_REMINDER)  // 明确类别
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)  // 锁屏可见
            .setAutoCancel(true)  // 点击后自动消失

        //点击通知跳转到应用
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(pendingIntent)

        // 发送通知
        Log.d(TAG,"sending message")
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, builder.build())
    }

    override fun onBind(intent: Intent?): IBinder? = null


    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "PollService destroyed")
    }

}



/*
fun logChannelStatus() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = manager.getNotificationChannel(CHANNEL_ID)

        channel?.let {
            Log.d(TAG,
                    "Channe ID: ${it.id}, " +
                    "importance: ${it.importance}, " +
                    "is uesd: ${it.importance != NotificationManager.IMPORTANCE_NONE}"
            )
        } ?: Log.e(TAG, "通知渠道未创建!")
    }
}*/