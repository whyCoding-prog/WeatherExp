package com.example.weatherexpect.Service

import android.util.Log
//import com.baidu.location.BuildConfig
import com.example.weatherexpect.BuildConfig

import com.example.weatherexpect.Dao.WeatherDao
import com.example.weatherexpect.Data.Entity.UnifiedCurrentWeather
import com.example.weatherexpect.Data.Entity.UnifiedWeatherForecast
import com.example.weatherexpect.Data.Response.Response.LocationResponse
import com.example.weatherexpect.Tool.Result.DailyWeatherResult
import com.example.weatherexpect.Tool.Result.NowWeatherResult
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

private val API_KEY= BuildConfig.QWEATHER_API_KEY
private const val BASE_URL="https://na52qatguf.re.qweatherapi.com/"


class WeatherService private constructor(
    private val weatherDao: WeatherDao
) {
    companion object {
        @Volatile
        private var instance: WeatherService? = null

        fun isInitialized(): Boolean = instance != null

        // 初始化方法，需要传入 WeatherDao
        fun initialize(weatherDao: WeatherDao) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = WeatherService(weatherDao)
                    }
                }
            }
        }

        fun getInstance(): WeatherService {
            checkNotNull(instance) { "WeatherService not initialized. Call initialize() first." }
            return instance!!
        }
    }

    private val retrofit= Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(createHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherApi = retrofit.create(WeatherAPI::class.java)

    //配置 HTTP 客户端
    private fun createHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)   // 连接服务器的超时时间（15秒）
            .readTimeout(15, TimeUnit.SECONDS)      // 读取响应数据的超时时间（15秒）
            .writeTimeout(15, TimeUnit.SECONDS)     // 发送请求数据的超时时间（15秒）
            .addInterceptor{ chain ->
                val original = chain.request()
                val url = original.url.newBuilder()
                    .addQueryParameter("key", API_KEY) // API Key作为查询参数
                    .build()

                val request: Request = original.newBuilder()
                    .url(url)
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    suspend fun fetchNowWeather(location:String,cityName:String):NowWeatherResult{
        return try {
            val response = weatherApi.getNowWeather(location)

            weatherDao.deleteByLocation(cityName)

            // 保存到数据库
            val unifiedCurrentWeather = UnifiedCurrentWeather(
                location = cityName,
                obsTime = response.now.obsTime,
                updateTime = response.updateTime,
                temp = response.now.temp,
                feelsLike = response.now.feelsLike,
                icon = response.now.icon,
                text = response.now.text
            )
            weatherDao.insertCurrentWeather(unifiedCurrentWeather.toEntity())
            Log.d("WeatherService", "API Response Code: ${response.code}")
            NowWeatherResult.Success(unifiedCurrentWeather)
        }
        catch (e: Exception) {
            // 网络请求失败，尝试从数据库获取
            val cachedEntity = weatherDao.getCurrentWeather(cityName).firstOrNull()
            if (cachedEntity != null) {
                val cachedWeather = UnifiedCurrentWeather.fromEntity(cachedEntity)
                NowWeatherResult.Cached(cachedWeather)
            }
            else {
                // 数据库也没有数据
                NowWeatherResult.Error
            }
        }
    }

    suspend fun fetchDailyWeather(location:String,cityName: String):DailyWeatherResult{
        return try {
            val response = weatherApi.getDailyWeather(location)
            val daily = response.daily

            weatherDao.deleteAllForecastsByLocation(cityName)

            //创建统一预报列表
            val unifiedForecasts = daily.map { day ->
                UnifiedWeatherForecast(
                    location = cityName,
                    fxDate = day.fxDate,
                    updateTime = response.updateTime,
                    tempMax = day.tempMax,
                    tempMin = day.tempMin,
                    iconDay = day.iconDay,
                    textDay = day.textDay,
                    iconNight = day.iconNight,
                    textNight = day.textNight,
                    windDirDay = day.windDirDay,
                    windScaleDay = day.windScaleDay,
                    precip = day.precip,
                    uvIndex = day.uvIndex,
                    humidity = day.humidity,
                    pressure = day.pressure
                )
            }.toMutableList()

            //批量插入数据库
            weatherDao.insertAllForecasts(unifiedForecasts.map { it.toEntity() })

            Log.d("WeatherService", "API Response Code: ${response.code}")
            DailyWeatherResult.Success(unifiedForecasts)

        } catch (e: Exception) {
            Log.e("WeatherService", "API Error: ${e.message}", e)

            //尝试从数据库获取缓存
            try {
                val cachedEntities = weatherDao.getForecasts(cityName).firstOrNull()
                if (cachedEntities != null && cachedEntities.isNotEmpty()) {
                    //转换为统一格式
                    val cachedForecasts = cachedEntities.map { entity ->
                        UnifiedWeatherForecast.fromEntity(entity)
                    }.toMutableList()

                    DailyWeatherResult.Cached(cachedForecasts)
                } else {
                    DailyWeatherResult.Error
                }
            } catch (dbEx: Exception) {
                Log.e("WeatherService", "Database Error: ${dbEx.message}", dbEx)
                DailyWeatherResult.Error
            }
        }
    }

    suspend fun fetchLocation(location: String):LocationResponse?{
        try {
            val response = weatherApi.getLocation(location)
            Log.d("WeatherService", "API Response Code: ${response.code}")
            return response
        } catch (e: Exception) {
            Log.e("WeatherService", "API Error: ${e.message}", e)
            return null
        }
    }
}