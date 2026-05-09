package com.example.weatherexpect

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore.Downloads
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.weatherexpect.Dao.WeatherDao
import com.example.weatherexpect.Dao.WeatherDatabase
import com.example.weatherexpect.Data.Entity.UnifiedCurrentWeather
import com.example.weatherexpect.Data.Entity.UnifiedWeatherForecast
import com.example.weatherexpect.Notification.PollService
import com.example.weatherexpect.Notification.PollService.Companion.setServiceAlarm
import com.example.weatherexpect.Service.Setting
import com.example.weatherexpect.Service.Setting.unitValue
import com.example.weatherexpect.Service.Setting.unitValue_
import com.example.weatherexpect.Service.WeatherService
import com.example.weatherexpect.Service.WeatherViewModel
import com.example.weatherexpect.Tool.ActivityRequest
import com.example.weatherexpect.Tool.ActivityRequest.REQUEST_NOTIFICATION_PERMISSION
import com.example.weatherexpect.Tool.Result.DailyWeatherResult
import com.example.weatherexpect.Tool.Result.NowWeatherResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.sqrt
import android.Manifest
import android.content.SharedPreferences


class MainActivity : AppCompatActivity() {

    lateinit var settingBtn:Button
    lateinit var changeCityBtn:Button
    lateinit var currentCityText:TextView

    lateinit var weatherViewModel:WeatherViewModel

    var content_:String="aa"

    // 延迟初始化数据库实例
    private val database: WeatherDatabase by lazy {
        // 使用应用上下文获取数据库实例
        WeatherDatabase.getDatabase(
            context = applicationContext,
            scope = lifecycleScope // 使用Activity的生命周期作用域
        )
    }

    // 获取DAO
    private val weatherDao: WeatherDao by lazy { database.weatherDao() }

    val weekdayMap = mapOf(
        0 to "星期日",
        1 to "星期一",
        2 to "星期二",
        3 to "星期三",
        4 to "星期四",
        5 to "星期五",
        6 to "星期六"
    )

    lateinit var day1:LinearLayout
    lateinit var day2:LinearLayout
    lateinit var day3:LinearLayout
    lateinit var day4:LinearLayout
    lateinit var day5:LinearLayout
    lateinit var day6:LinearLayout
    lateinit var day7:LinearLayout

    private var days: Array<UnifiedWeatherForecast?> = arrayOfNulls(7)

    private lateinit var pref: SharedPreferences

    val TAG="MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        initWidget()

        Log.d("WeatherService","Last id:"+Setting.currentCityID)
        Log.d("WeatherService","Last name:"+Setting.currentCityName)
        UpdataWeather(Setting.currentCityID,Setting.currentCityName)


        changeCityBtn.setOnClickListener {
            startActivityForResult(Intent(this,changeCityActivity::class.java), ActivityRequest.REQUEST_CHANGE_CITY)
        }

        if (Setting.notificationSetting) {
            PollService.setServiceAlarm(this, true)
        }

        pref = getSharedPreferences("settings", MODE_PRIVATE)

        findViewById<Button>(R.id.settingBtn).setOnClickListener{ view->
            // 创建 PopupMenu
            val popup = PopupMenu(this, view)
            val inflater = popup.menuInflater
            inflater.inflate(R.menu.menu_settings, popup.menu)

            // 恢复消息提示状态
            //val pref = getSharedPreferences("settings", Context.MODE_PRIVATE)
            popup.menu.findItem(R.id.menu_notification)?.isChecked =
                pref.getBoolean("notification_enabled", false)

            // 设置菜单项点击事件
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_share -> {
                        shareText()
                        true
                    }
                    R.id.menu_notification -> {
                        // 切换状态
                        item.isChecked = !item.isChecked
                        Setting.notificationSetting = item.isChecked
                        Setting.saveSettings()
                        pref.edit()
                            .putBoolean("notification_enabled", item.isChecked)
                            .apply()

                        //启动或关闭通知
                        if (item.isChecked) {
                            enableNotification()
                        } else {
                            PollService.setServiceAlarm(this, false)  // 关闭闹钟
                            Log.d("Notification", "Notification disabled")
                        }
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        val unitRadioGroup = findViewById<RadioGroup>(R.id.rgTemperatureUnit)

        // 设置单选按钮监听器
        unitRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            unitValue = when (checkedId) {
                R.id.rbFahrenheit -> 1  // 华氏度对应值为1
                else -> 0  // 摄氏度对应值为0
            }

            unitValue_ = when (checkedId) {
                R.id.rbFahrenheit -> 0  // 华氏度对应值为1
                else -> 1  // 摄氏度对应值为0
            }
            UpdataWeather(Setting.currentCityID,Setting.currentCityName)
        }

    }



    //更新前端界面
    fun UpdataWeather(cityId:String,cityName:String){
        currentCityText.setText(cityName)

        lifecycleScope.launch {
            try {
                // 直接调用挂起函数，确保等待网络请求完成
                val nowResult = weatherViewModel.fetchNowWeather(cityId, cityName)
                val dailyResult = weatherViewModel.fetchDailyWeather(cityId, cityName)

                Log.d("WeatherService", "Successfully fetch data, update next")
                UpdateCurWeather(nowResult)
                UpdateDailyWeather(dailyResult)
            } catch (e: Exception) {
                Log.e("WeatherService", "Failed to fetch weather data", e)
            }
        }
    }

    private fun UpdateCurWeather(result: NowWeatherResult?) {
        //返回UnifiedCurrentWeather
        result?.let { result->
            when (result) {
                is NowWeatherResult.Success -> {
                    // 使用网络获取的最新数据更新UI
                    DisplayNowWeather(result.weather)
                    Log.i(TAG, "successful update current weather through http request")
                }

                is NowWeatherResult.Cached -> {
                    // 使用缓存数据更新UI
                    DisplayNowWeather(result.weather)
                    Log.i(TAG, "successful update current weather through cache")
                }

                is NowWeatherResult.Error -> {
                    // 显示错误或占位符
                    DisplayNowWeatherPlaceholder()
                    Log.e(TAG, "failure to update current weather")
                }
            }
        }
    }

    private fun UpdateDailyWeather(result: DailyWeatherResult?) {
        result?.let { result->
            when (result) {
                is DailyWeatherResult.Success -> {
                    if (result.daily.size != 7) {
                        DisplayDailyWeatherPlaceholder()
                        return
                    }
                    days = result.daily.toTypedArray()
                    DisplayDailyWeather(result.daily)
                }

                is DailyWeatherResult.Cached -> {
                    if (result.daily.size != 7) {
                        DisplayDailyWeatherPlaceholder()
                        return
                    }
                    days = result.daily.toTypedArray()
                    DisplayDailyWeather(result.daily)
                }

                is DailyWeatherResult.Error -> {
                    DisplayDailyWeatherPlaceholder()
                }
            }
        }
    }

    fun DisplayNowWeather(now: UnifiedCurrentWeather) {
        val feelsLike = formatSingleTemp(now.feelsLike)
        val temp = formatSingleTemp(now.temp)

        findViewById<TextView>(R.id.feelsLikeTemp).text = "体感温度：$feelsLike"
        findViewById<TextView>(R.id.curTemp).text = temp
        content_ = "当前温度：$temp"
    }

    /*** 格式化单个温度值*/
    private fun formatSingleTemp(celsius: String): String {
        return if (unitValue == 1) {
            val f = String.format("%.1f", celsius.toDouble() * 1.8 + 32)
            "${f}℉"
        } else {
            "${celsius}℃"
        }
    }


    fun DisplayDailyWeather(daily: List<UnifiedWeatherForecast>) {
        val today = daily[0]
        val sampleText = findViewById<TextView>(R.id.sampleWeather)
        sampleText.text = "${today.textDay} ${formatTemp(today.tempMin, today.tempMax)}"

        // 数据完整性检查
        if (daily.size != 7) {
            Log.w(TAG, "数据不完整，无法更新UI")
            return
        }

        // 7天的View ID
        val weekIds = intArrayOf(
            R.id.day1Week, R.id.day2Week, R.id.day3Week, R.id.day4Week, R.id.day5Week, R.id.day6Week, R.id.day7Week)
        val dateIds = intArrayOf(
            R.id.day1Date, R.id.day2Date, R.id.day3Date, R.id.day4Date, R.id.day5Date, R.id.day6Date, R.id.day7Date)
        val tempIds = intArrayOf(
            R.id.day1Temp, R.id.day2Temp, R.id.day3Temp, R.id.day4Temp, R.id.day5Temp, R.id.day6Temp, R.id.day7Temp)
        val picIds = intArrayOf(
            R.id.day1Pic, R.id.day2Pic, R.id.day3Pic, R.id.day4Pic, R.id.day5Pic, R.id.day6Pic, R.id.day7Pic)

        //7 天
        var currentWeek = (CalculateWeekday(daily[0].fxDate) - 1) % 7

        for (i in 0 until 7) {
            days[i] = daily[i]
            val d = daily[i]

            findViewById<TextView>(weekIds[i]).text = weekdayMap[currentWeek]!!
            findViewById<TextView>(dateIds[i]).text = ChangeDate(d.fxDate)
            findViewById<TextView>(tempIds[i]).text = formatTemp(d.tempMin, d.tempMax)
            findViewById<TextView>(picIds[i]).text = d.textDay

            currentWeek = (currentWeek + 1) % 7
        }
    }

    /*** 根据当前温度单位设置，格式化温度显示*/
    private fun formatTemp(minCelsius: String, maxCelsius: String): String {
        return "${formatSingleTemp(minCelsius)}~${formatSingleTemp(maxCelsius)}"
    }

    fun DisplayDailyWeatherPlaceholder(){
        Toast.makeText(this, "获取失败！请检查网络设置", Toast.LENGTH_SHORT).show()
    }

    fun DisplayNowWeatherPlaceholder(){
        Toast.makeText(this, "获取失败！请检查网络设置", Toast.LENGTH_SHORT).show()
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

    //获取Change City Activity返回的参数
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ActivityRequest.REQUEST_CHANGE_CITY && resultCode == RESULT_OK) {
            val cityID = data?.getStringExtra("CITY_ID") ?: ""
            val cityName = data?.getStringExtra("CITY_NAME") ?: ""
            UpdataWeather(Setting.currentCityID, Setting.currentCityName)
        } else {
            Toast.makeText(this, "操作失败", Toast.LENGTH_SHORT).show()
        }
    }

    // 跳转到天气详情页的函数
    private fun toWeatherInfoActivity(weather: UnifiedWeatherForecast) {
        val intent = Intent(this, WeatherInfoActivity::class.java)
        intent.putExtra("weather", weather)
        startActivity(intent)
    }

    // 启动消息提醒
    private fun enableNotification() {
        // 申请通知权限(Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!= PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
                return
            }
        }
        // 权限已有
        startNotificationService()
    }

    private fun startNotificationService() {
        Log.d("Notification", "setServiceAlarm enabled")
    }

    // 权限回调
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户同意
                startNotificationService()
                Toast.makeText(this, "通知已开启", Toast.LENGTH_SHORT).show()
            } else {
                // 用户拒绝 → 回滚设置
                Setting.notificationSetting = false
                Setting.saveSettings()
                pref.edit().putBoolean("notification_enabled", false).apply()
                Toast.makeText(this, "需要通知权限才能推送天气", Toast.LENGTH_SHORT).show()
            }
        }
    }


    //初始化控件
    fun initWidget(){

        Setting.initialize(database.weatherDao())
        WeatherService.initialize(weatherDao = database.weatherDao())

        weatherViewModel = ViewModelProvider(this).get(WeatherViewModel::class.java)


        settingBtn=findViewById(R.id.settingBtn)
        changeCityBtn=findViewById(R.id.changeCityBtn)
        currentCityText=findViewById(R.id.currentCityText)
        day1=findViewById(R.id.day1LinearLayout)
        day2=findViewById(R.id.day2LinearLayout)
        day3=findViewById(R.id.day3LinearLayout)
        day4=findViewById(R.id.day4LinearLayout)
        day5=findViewById(R.id.day5LinearLayout)
        day6=findViewById(R.id.day6LinearLayout)
        day7=findViewById(R.id.day7LinearLayout)

        /*
        day1.setOnClickListener{
            days[0]?.let {weather -> // weather 自动推断为非空类型(学这个)
                toWeatherInfoActivity(weather)
            }
        }
         */
        val dayButtons = listOf(day1, day2, day3, day4, day5, day6, day7)

        dayButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                days[index]?.let { weather ->
                    toWeatherInfoActivity(weather)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG,"onDestroy,Last location:${Setting.currentCityID}")
        Setting.saveSettings()
    }

    /*
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        menu.findItem(R.id.menu_notification)?.isChecked = Setting.notificationSetting
        return true
    }*/

    fun shareText() {
        // 创建分享意图
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"  // 设置分享内容类型为纯文本

        // 添加分享内容
        sharingIntent.putExtra(Intent.EXTRA_TEXT, content_)

        // 设置分享标题
        val title = "分享到"

        // 创建分享选择器
        val chooser = Intent.createChooser(sharingIntent, title)

        // 检查是否有应用可以处理此分享意图
        if (sharingIntent.resolveActivity(packageManager) != null) {
            startActivity(chooser)
        } else {
            Toast.makeText(this, "没有找到可以分享的应用", Toast.LENGTH_SHORT).show()
        }
    }




}