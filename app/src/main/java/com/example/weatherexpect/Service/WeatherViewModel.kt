package com.example.weatherexpect.Service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherexpect.Data.Entity.UnifiedWeatherForecast
import com.example.weatherexpect.Data.Entity.UserSettings
import com.example.weatherexpect.Data.Response.Response.LocationResponse
import com.example.weatherexpect.Tool.Result.DailyWeatherResult
import com.example.weatherexpect.Tool.Result.NowWeatherResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class WeatherViewModel:ViewModel() {

    val TAG="WeatherService"
    val weatherService=WeatherService.getInstance()

    private val _locationLiveData = MutableLiveData<LocationResponse?>()
    val locationLiveData: LiveData<LocationResponse?> = _locationLiveData

    private val _nowWeatherResult = MutableStateFlow<NowWeatherResult?>(null)
    val nowWeatherResult: StateFlow<NowWeatherResult?> = _nowWeatherResult.asStateFlow()

    private val _dailyWeatherResult = MutableStateFlow<DailyWeatherResult?>(null)
    val dailyWeatherResult: StateFlow<DailyWeatherResult?> = _dailyWeatherResult.asStateFlow()


    private val _dailyWeather = MutableLiveData<Array<UnifiedWeatherForecast?>>(arrayOfNulls(7))
    val dailyWeather: LiveData<Array<UnifiedWeatherForecast?>> = _dailyWeather

    private val _selectedDayIndex = MutableLiveData<Int>(0)
    val selectedDayIndex: LiveData<Int> = _selectedDayIndex


    // 修改为挂起函数，直接返回结果
    suspend fun fetchNowWeather(location: String, cityName: String): NowWeatherResult {
        Log.d(TAG, "Request now weather, location = $location...")
        return weatherService.fetchNowWeather(location, cityName).also {
            _nowWeatherResult.value = it // 更新状态
        }
    }

    suspend fun fetchDailyWeather(location: String, cityName: String): DailyWeatherResult {
        Log.d(TAG, "Request daily weather...")
        return weatherService.fetchDailyWeather(location, cityName).also {  result ->
            _dailyWeatherResult.value = result
            if (result is DailyWeatherResult.Success && result.daily.size >= 7) {
                _dailyWeather.value = result.daily.toTypedArray() // 同步更新数组
            }else if (result is DailyWeatherResult.Cached && result.daily.size >= 7){
                _dailyWeather.value = result.daily.toTypedArray()
            }
        }
    }

    fun fetchLocation(location: String){
        Log.d(TAG,"Request location,location = ${location}...")
        viewModelScope.launch {
            val result = weatherService.fetchLocation(location)
            _locationLiveData.postValue(result)
        }
    }

    // 新增方法
    fun updateDailyWeather(weatherList: List<UnifiedWeatherForecast>) {
        if (weatherList.size >= 7) {
            _dailyWeather.value = weatherList.toTypedArray()
        }
    }

    fun selectDay(index: Int) {
        if (index in 0..6) {
            _selectedDayIndex.value = index
        }
    }

    fun getSelectedWeather(): UnifiedWeatherForecast? {
        val index = _selectedDayIndex.value ?: 0
        return _dailyWeather.value?.getOrNull(index)
    }
}