package com.example.weatherexpect.Notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PollReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("PollReceiver", "Received broadcast: ${intent.action}")

        // 检查是否为我们的自定义 Action
        if (intent.action == PollService.ACTION_WEATHER_ALARM) {
            Log.d("PollReceiver", "Alarm triggered. Starting foreground service...")
            PollService.startService(context)
        } else {
            Log.w("PollReceiver", "Received unexpected action: ${intent.action}")
        }
    }
}

