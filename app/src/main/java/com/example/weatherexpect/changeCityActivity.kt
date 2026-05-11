package com.example.weatherexpect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherexpect.Data.Response.Data.Location
import com.example.weatherexpect.Service.WeatherViewModel
import com.example.weatherexpect.Tool.CityAdapter
import kotlinx.coroutines.Job
import androidx.lifecycle.viewModelScope
import com.example.weatherexpect.Service.Setting
import com.example.weatherexpect.Service.WeatherService
import com.example.weatherexpect.Tool.ActivityRequest.MAP_REQUEST_CODE
import com.example.weatherexpect.Tool.CoordinateConverter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class changeCityActivity : AppCompatActivity() {

    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var searchEdit:EditText
    private lateinit var rvCities:RecyclerView
    private val adapter = CityAdapter()

    private var searchJob: Job? = null // 声明协程Job用于取消旧任务

    private var isFromMap = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_city)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!WeatherService.isInitialized()) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        initWidget()
        setupRecyclerView()
        setupSearchListener()


        // 监听和风 GeoAPI 返回结果
        weatherViewModel.locationLiveData.observe(this) { response ->
            response?.let { locationResponse ->
                val locations = locationResponse.locations

                if (isFromMap && locations.isNotEmpty()) {
                    returnSelectedCity(locations[0])
                } else if (locations.isNotEmpty()) {
                    adapter.submitList(locations)       // 来自搜索 → 显示列表让用户选
                } else {
                    adapter.submitList(emptyList())
                    Toast.makeText(this, "搜索失败或无结果", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                adapter.submitList(emptyList())
                Toast.makeText(this, "搜索失败或无结果", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.backBtn).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.MapBtn).setOnClickListener {
            openMapForLocation()
        }

    }

    fun initWidget(){
        weatherViewModel=ViewModelProvider(this).get(WeatherViewModel::class.java)
        searchEdit=findViewById(R.id.searchEdit)
        rvCities=findViewById(R.id.rv_cities)
    }

    private fun setupSearchListener(){
        /*TextWatcher是Android框架中的一个 接口，用于监听文本变化事件。它定义了三个方法*/
        searchEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                // 使用 Activity 的 lifecycleScope 启动协程
                searchJob = lifecycleScope.launch {
                    delay(500) // 挂起函数，在协程内调用
                    val query = s.toString().trim()
                    if (query.length >= 2) {
                        isFromMap = false
                        weatherViewModel.fetchLocation(query)
                    } else {
                        adapter.submitList(emptyList())
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupRecyclerView(){
        //设置布局管理器
        rvCities.layoutManager=LinearLayoutManager(this)

        //设置配置器
        rvCities.adapter=adapter
        adapter.setOnItemClickListener { location->
            returnSelectedCity(location)
        }
    }

    private fun returnSelectedCity(location:Location){
        Setting.updateCity(location.id, location.name)
        Setting.saveSettings()
        val intent=Intent().apply {
            putExtra("CITY_ID",location.id)
            putExtra("CITY_NAME",location.name)
        }
        setResult(RESULT_OK,intent)
        finish()
    }


    // 打开百度地图选点
    private fun openMapForLocation() {
        val intent = Intent(this, MapPickerActivity::class.java)
        startActivityForResult(intent, MAP_REQUEST_CODE)
    }

    // 接收地图返回的经纬度 → 传给和风 GeoAPI
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MAP_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val lat = data.getDoubleExtra("latitude", 0.0)
            val lng = data.getDoubleExtra("longitude", 0.0)
            val address = data.getStringExtra("address") ?: ""

            if (lat != 0.0 && lng != 0.0) {
                Toast.makeText(this, "已选择: $address", Toast.LENGTH_SHORT).show()

                // 把经纬度->和风天气GeoAPI："经度,纬度"
                val qweatherLocation = "${String.format("%.2f", lng)},${String.format("%.2f", lat)}"
                Log.d("MapResult", "传给和风GeoAPI: $qweatherLocation")

                isFromMap = true  // 标记来自地图
                weatherViewModel.fetchLocation(qweatherLocation)
            }
        }
    }

}