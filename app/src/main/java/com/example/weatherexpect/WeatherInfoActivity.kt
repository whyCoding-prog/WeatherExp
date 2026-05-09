package com.example.weatherexpect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weatherexpect.Data.Entity.UnifiedWeatherForecast

class WeatherInfoActivity : AppCompatActivity() {

    val weekdayMap = mapOf(
        0 to "星期日",
        1 to "星期一",
        2 to "星期二",
        3 to "星期三",
        4 to "星期四",
        5 to "星期五",
        6 to "星期六"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_weather_info)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // 获取传递的天气数据
        val weather = intent.getSerializableExtra("weather") as? UnifiedWeatherForecast

        Log.d("MainActivity","success to WeatherInfoActivity")


        // 显示天气详情
        weather?.let {

            val weekDay=weekdayMap[CalculateWeekday(it.fxDate)%7]
            findViewById<TextView>(R.id.weekInfo).setText(weekDay)
            val date=ChangeDate(it.fxDate)
            findViewById<TextView>(R.id.dateInfo).setText(date)

            findViewById<TextView>(R.id.dateInfo).text = ChangeDate(it.fxDate)

            var str=it.tempMax+"℃"
            findViewById<TextView>(R.id.maxTempInfo).setText(str)

            str="${it.tempMin}℃"
            findViewById<TextView>(R.id.minTempInfo).setText(str)
            str="风向：${it.windDirDay}"
            findViewById<TextView>(R.id.windDirDay).setText(str)
            str="风力等级：${it.windScaleDay}"
            findViewById<TextView>(R.id.windScaleDay).setText(str)
            str="总降水量：${it.precip}"
            findViewById<TextView>(R.id.precip).setText(str)
            str="紫外线强度：${it.uvIndex}"
            findViewById<TextView>(R.id.uvIndex).setText(str)
            str="相对湿度：${it.humidity}"
            findViewById<TextView>(R.id.humidity).setText(str)
            str="大气压强：${it.pressure}"
            findViewById<TextView>(R.id.pressure).setText(str)

        }

        // 设置返回按钮点击事件
        findViewById<TextView>(R.id.backBtn_).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.shareBtn).setOnClickListener {
            shareWeatherInfo(weather!!)
        }



    }

    fun ChangeDate(inputDate:String):String{
        val parts = inputDate.split("-")
        val month = parts[1].toInt().toString()
        val day = parts[2].toInt().toString()
        val date = "$month/$day"
        return date
    }

    fun CalculateWeekday(dateString: String): Int {
        val parts = dateString.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()

        val m = if (month < 3) month + 12 else month
        val y = if (month < 3) year - 1 else year

        val h = (day + 13 * (m + 1) / 5 + y + y / 4 - y / 100 + y / 400) % 7
        return if (h == 0) 7 else h
    }
    // 分享天气信息的方法
    private fun shareWeatherInfo(weather:UnifiedWeatherForecast) {
        // 构建分享内容
        val shareContent = buildString {
            append("【${weather.location}天气预报】\n")
            append("日期：${weather.fxDate}\n")
            append("天气：${weather.textDay}\n")
            append("温度：${weather.tempMin}°C ~ ${weather.tempMax}°C\n")
            append("湿度：${weather.humidity}%\n")
            append("风力：${weather.windDirDay} ${weather.windScaleDay}级\n")
            append("更新时间：${weather.updateTime}")
        }

        // 创建分享意图
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent)

        // 启动分享选择器
        startActivity(Intent.createChooser(shareIntent, "分享天气信息"))
    }


}