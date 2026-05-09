package com.example.weatherexpect.Fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.weatherexpect.Data.Entity.UnifiedCurrentWeather
import com.example.weatherexpect.Data.Entity.UnifiedWeatherForecast
import com.example.weatherexpect.Notification.PollService
import com.example.weatherexpect.R
import com.example.weatherexpect.Service.Setting
import com.example.weatherexpect.Service.WeatherViewModel
import com.example.weatherexpect.Tool.Result.DailyWeatherResult
import com.example.weatherexpect.Tool.Result.NowWeatherResult
import com.example.weatherexpect.databinding.ActivityMainBinding
import kotlinx.coroutines.launch


class WeatherListFragment : Fragment() {
    /*
    // 使用 Activity 级别的 ViewModel
    private val viewModel: WeatherViewModel by viewModels(ownerProducer = { requireActivity() })

    // 视图绑定
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private var days: Array<UnifiedWeatherForecast?> = arrayOfNulls(7)
    val TAG = "WeatherListFragment"

    val weekdayMap = mapOf(
        0 to "星期日",
        1 to "星期一",
        2 to "星期二",
        3 to "星期三",
        4 to "星期四",
        5 to "星期五",
        6 to "星期六"
    )



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 初始化视图绑定
        _binding = ActivityMainBinding.inflate(inflater, container, false)
        return binding.root
    }

     */
    /*
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            initWidget()
            UpdataWeather(Setting.currentCityID,Setting.currentCityName)
        }

        private fun initWidget() {
            // 使用视图绑定直接访问控件
            binding.settingBtn.setOnClickListener {
                // 设置按钮点击处理
            }

            binding.changeCityBtn.setOnClickListener {
                // 城市切换按钮点击处理
            }

            val dayLayouts = listOf(
                binding.day1LinearLayout,
                binding.day2LinearLayout,
                binding.day3LinearLayout,
                binding.day4LinearLayout,
                binding.day5LinearLayout,
                binding.day6LinearLayout,
                binding.day7LinearLayout
            )

            dayLayouts.forEachIndexed { index, layout ->
                layout.setOnClickListener {
                    days[index]?.let { weather ->
                        // 通过 ViewModel 通知选中日期
                        viewModel.selectDay(index)
                    }
                }
            }
        }


        private fun setupViewModelObservers() {
            // 观察当前天气（NowWeatherResult）
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.nowWeatherResult.collect { result ->
                        result?.let {
                            when (it) {
                                is NowWeatherResult.Success -> DisplayNowWeather(it.weather)
                                is NowWeatherResult.Cached -> DisplayNowWeather(it.weather)
                                is NowWeatherResult.Error -> DisplayNowWeatherPlaceholder()
                            }
                        }
                    }
                }
            }

            // 观察每日天气（DailyWeatherResult）
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.dailyWeatherResult.collect { result ->
                        result?.let {
                            when (it) {
                                is DailyWeatherResult.Success -> {
                                    if (it.daily.size >= 7) UpdateDailyWeather(it)
                                    else DisplayDailyWeatherPlaceholder()
                                }
                                is DailyWeatherResult.Cached -> {
                                    if (it.daily.size >= 7) UpdateDailyWeather(it)
                                    else DisplayDailyWeatherPlaceholder()
                                }
                                is DailyWeatherResult.Error -> DisplayDailyWeatherPlaceholder()
                            }
                        }
                    }
                }
            }



        }
        fun UpdataWeather(cityId:String,cityName:String){
            binding.currentCityText.text = cityName

            lifecycleScope.launch {
                try {
                    // 直接调用挂起函数，确保等待网络请求完成
                    val nowResult = viewModel.fetchNowWeather(cityId, cityName)
                    val dailyResult = viewModel.fetchDailyWeather(cityId, cityName)

                    Log.d("WeatherService", "Successfully fetch data, update next")
                    UpdateCurWeather(nowResult)
                    UpdateDailyWeather(dailyResult)
                } catch (e: Exception) {
                    Log.e("WeatherService", "Failed to fetch weather data", e)
                }
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null // 清理视图绑定
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

        fun DisplayDailyWeatherPlaceholder(){
            Log.e(TAG,"failed to fetch data")
        }

        fun DisplayNowWeatherPlaceholder(){
            Log.e(TAG,"failed to fetch data")
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

        fun DisplayNowWeather(now: UnifiedCurrentWeather) {
            // 使用视图绑定更新控件
            binding.feelsLikeTemp.text = "体感温度：${now.feelsLike}℃"
            binding.curTemp.text = "${now.temp}℃"
        }

        fun DisplayDailyWeather(daily: List<UnifiedWeatherForecast>) {
            if (daily.size != 7) {
                Log.w(TAG, "数据不完整，无法更新UI")
                return
            }

            days = daily.toTypedArray()

            // 使用视图绑定更新控件
            val day1 = daily[0]
            binding.sampleWeather.text = "${day1.textDay} ${day1.tempMin}~${day1.tempMax}℃"

            var currentWeek = (CalculateWeekday(day1.fxDate) - 1) % 7

            // 更新每一天的UI
            updateDayUI(0, daily[0], currentWeek)
            currentWeek = (currentWeek + 1) % 7
            updateDayUI(1, daily[1], currentWeek)
            currentWeek = (currentWeek + 1) % 7
            updateDayUI(2, daily[2], currentWeek)
            currentWeek = (currentWeek + 1) % 7
            updateDayUI(3, daily[3], currentWeek)
            currentWeek = (currentWeek + 1) % 7
            updateDayUI(4, daily[4], currentWeek)
            currentWeek = (currentWeek + 1) % 7
            updateDayUI(5, daily[5], currentWeek)
            currentWeek = (currentWeek + 1) % 7
            updateDayUI(6, daily[6], currentWeek)
        }

        private fun updateDayUI(index: Int, day: UnifiedWeatherForecast, weekDay: Int) {
            val weekViews = listOf(
                Triple(binding.day1Week, binding.day1Date, binding.day1Temp),
                Triple(binding.day2Week, binding.day2Date, binding.day2Temp),
                Triple(binding.day3Week, binding.day3Date, binding.day3Temp),
                Triple(binding.day4Week, binding.day4Date, binding.day4Temp),
                Triple(binding.day5Week, binding.day5Date, binding.day5Temp),
                Triple(binding.day6Week, binding.day6Date, binding.day6Temp),
                Triple(binding.day7Week, binding.day7Date, binding.day7Temp)
            )

            val (weekView, dateView, tempView) = weekViews[index]

            weekView.text = weekdayMap[weekDay] ?: ""
            dateView.text = ChangeDate(day.fxDate)
            tempView.text = "${day.tempMin}℃~${day.tempMax}℃"
        }
    */

}