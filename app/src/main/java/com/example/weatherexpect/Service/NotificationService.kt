package com.example.weatherexpect.Service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.weatherexpect.Data.Entity.UnifiedWeatherForecast
import com.example.weatherexpect.MainActivity
import com.example.weatherexpect.R
import com.example.weatherexpect.Tool.Result.DailyWeatherResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationService(
    private val context: Context,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val weatherViewModel: WeatherViewModel
) {
    // 通知渠道 ID
    private val CHANNEL_ID = "weather_notification_channel"

    // 初始化通知渠道
    fun initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "天气通知",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "天气变化和提醒通知"
                enableVibration(true)
                setShowBadge(true)
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    // 发送天气通知
    fun sendWeatherNotification(forecast: UnifiedWeatherForecast) {
        // 创建通知内容
        val title = "今日天气"
        val content = "白天: ${forecast.textDay} | 夜间: ${forecast.textNight}"

        // 创建通知构建器
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.rc)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)  // 点击后自动消失

        // 如果需要点击通知跳转到应用
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(pendingIntent)

        // 发送通知
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(1, builder.build())
    }

    // 取消所有通知
    fun cancelAllNotifications() {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.cancelAll()
    }

    // 判断通知是否开启
    fun areNotificationsEnabled(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val manager = context.getSystemService(NotificationManager::class.java)
            return manager.areNotificationsEnabled()
        }
        return true  // Android 7.0以下默认开启
    }
}

