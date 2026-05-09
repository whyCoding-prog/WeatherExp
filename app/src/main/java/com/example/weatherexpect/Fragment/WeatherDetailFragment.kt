package com.example.weatherexpect.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.weatherexpect.Data.Entity.UnifiedWeatherForecast
import com.example.weatherexpect.Service.WeatherViewModel
import com.example.weatherexpect.databinding.ActivityWeatherInfoBinding

class WeatherDetailFragment : Fragment() {
    private var _binding: ActivityWeatherInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WeatherViewModel by viewModels(ownerProducer = { requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityWeatherInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 应用窗口Insets（适配刘海屏等）
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // 观察选中的日期索引变化，更新详情页
        viewModel.selectedDayIndex.observe(viewLifecycleOwner) { index ->
            val weather = viewModel.getSelectedWeather()
            weather?.let { updateDetailUI(it) }
        }

        // 初始加载时显示第一天
        val weather = viewModel.getSelectedWeather()
        weather?.let { updateDetailUI(it) }

    }

    private fun updateDetailUI(weather: UnifiedWeatherForecast) {
        // 直接复用Activity中的UI更新逻辑，通过binding访问控件
        binding.weekInfo.text = weekdayMap[CalculateWeekday(weather.fxDate) % 7]
        binding.dateInfo.text = ChangeDate(weather.fxDate)
        binding.maxTempInfo.text = "${weather.tempMax}℃"
        binding.minTempInfo.text = "${weather.tempMin}℃"
        binding.windDirDay.text = "风向：${weather.windDirDay}"
        binding.windScaleDay.text = "风力等级：${weather.windScaleDay}"
        binding.precip.text = "总降水量：${weather.precip}"
        binding.uvIndex.text = "紫外线强度：${weather.uvIndex}"
        binding.humidity.text = "相对湿度：${weather.humidity}"
        binding.pressure.text = "大气压强：${weather.pressure}"
    }



    private val weekdayMap = mapOf(
        0 to "星期日", 1 to "星期一", 2 to "星期二",
        3 to "星期三", 4 to "星期四", 5 to "星期五", 6 to "星期六"
    )

    private fun ChangeDate(inputDate: String): String {
        val parts = inputDate.split("-")
        return "${parts[1]}/${parts[2]}"
    }

    private fun CalculateWeekday(dateString: String): Int {
        val (year, month, day) = dateString.split("-").map { it.toInt() }
        val m = if (month < 3) month + 12 else month
        val y = if (month < 3) year - 1 else year
        val h = (day + 13 * (m + 1) / 5 + y + y / 4 - y / 100 + y / 400) % 7
        return if (h == 0) 7 else h
    }

    private fun shareWeatherInfo(weather: UnifiedWeatherForecast) {
        val shareContent = buildString {
            append("【${weather.location}天气预报】\n")
            append("日期：${weather.fxDate}\n")
            append("天气：${weather.textDay}\n")
            append("温度：${weather.tempMin}°C ~ ${weather.tempMax}°C\n")
            append("湿度：${weather.humidity}%\n")
            append("风力：${weather.windDirDay} ${weather.windScaleDay}级\n")
            append("更新时间：${weather.updateTime}")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareContent)
        }

        startActivity(Intent.createChooser(shareIntent, "分享天气信息"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}