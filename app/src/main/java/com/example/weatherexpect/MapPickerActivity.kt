package com.example.weatherexpect

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MapPoi
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.geocode.GeoCodeResult
import com.baidu.mapapi.search.geocode.GeoCoder
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult

class MapPickerActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var baiduMap: BaiduMap
    private lateinit var tvAddress: TextView
    private lateinit var tvLatLng: TextView

    private var selectedLat: Double = 0.0
    private var selectedLng: Double = 0.0
    private var selectedAddress: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map_picker)

        mapView = findViewById(R.id.mapView)
        tvAddress = findViewById(R.id.tv_address)
        tvLatLng = findViewById(R.id.tv_latlng)

        baiduMap = mapView.map


        // 开启定位图层
        baiduMap.isMyLocationEnabled = true

        // 监听地图点击事件
        baiduMap.setOnMapClickListener(object : BaiduMap.OnMapClickListener {
            override fun onMapClick(latLng: LatLng) {
                // 记录选中的经纬度
                selectedLat = latLng.latitude
                selectedLng = latLng.longitude

                // 在点击位置添加标记
                baiduMap.clear()


                val icon = getMarkerIcon()

                val marker = MarkerOptions().position(latLng).icon(icon)
                baiduMap.addOverlay(marker)

                // 更新显示
                tvLatLng.text = "经度: ${selectedLng}  纬度: ${selectedLat}"

                // 逆地理编码获取地址
                reverseGeoCode(latLng)
            }

            override fun onMapPoiClick(mapPoi: MapPoi) {}
        })

        // 确认按钮
        findViewById<Button>(R.id.btn_confirm).setOnClickListener {
            if (selectedLat != 0.0 && selectedLng != 0.0) {
                val data = Intent().apply {
                    putExtra("latitude", selectedLat)
                    putExtra("longitude", selectedLng)
                    putExtra("address", selectedAddress)
                }
                setResult(RESULT_OK, data)
                finish()
            } else {
                Toast.makeText(this, "请先在地图上选择位置", Toast.LENGTH_SHORT).show()
            }
        }

        // 启动定位
        startLocation()
    }

    // 把 Vector XML 转成BitmapDescriptor
    private fun getMarkerIcon(): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_location_pin)!!
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    // 逆地理编码：经纬度 - 地址文字
    private fun reverseGeoCode(latLng: LatLng) {
        val geoCoder = GeoCoder.newInstance()
        geoCoder.setOnGetGeoCodeResultListener(object : OnGetGeoCoderResultListener {
            override fun onGetGeoCodeResult(result: GeoCodeResult?) {}

            override fun onGetReverseGeoCodeResult(result: ReverseGeoCodeResult?) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    tvAddress.text = "未知地址"
                    return
                }
                selectedAddress = result.address
                tvAddress.text = "地址: $selectedAddress"
            }
        })

        geoCoder.reverseGeoCode(
            ReverseGeoCodeOption()
                .location(latLng)
                .newVersion(1)
        )
    }

    // 定位到当前位置
    private fun startLocation() {
        val locationClient = LocationClient(applicationContext)
        val option = LocationClientOption().apply {
            isOpenGps = true
            setCoorType("bd09ll")  // 百度坐标系
            setScanSpan(0)         // 只定位一次
        }
        locationClient.locOption = option

        locationClient.registerLocationListener(object : BDAbstractLocationListener() {
            override fun onReceiveLocation(location: BDLocation) {
                // 移动地图到当前位置
                val latLng = LatLng(location.latitude, location.longitude)
                val update = MapStatusUpdateFactory.newLatLngZoom(latLng, 16f)
                baiduMap.animateMapStatus(update)

                locationClient.stop()
            }
        })
        locationClient.start()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        baiduMap.isMyLocationEnabled = false
        mapView.onDestroy()
    }
}
