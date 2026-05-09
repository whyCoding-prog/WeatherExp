package com.example.weatherexpect.Tool

import android.content.Context
import android.location.Geocoder
import android.util.Log
import java.util.*

/**
 * 地理坐标系转换工具类
 * 负责处理经纬度转换和格式化，以符合API查询要求
 */
object CoordinateConverter {
    private const val TAG = "CoordinateConverter"
    private const val PI = 3.1415926535897932384626
    private const val A = 6378245.0
    private const val EE = 0.00669342162296594323

    /**
     * 判断坐标是否在中国境内
     * @param lat 纬度
     * @param lng 经度
     * @return 是否在中国境内
     */
    fun isInChina(lat: Double, lng: Double): Boolean {
        // 粗略判断：中国领土的经纬度范围
        return lng in 73.66..135.05 && lat in 3.86..53.55
    }

    /**
     * WGS-84坐标系转GCJ-02坐标系（火星坐标）
     * @param wgLat WGS-84纬度
     * @param wgLng WGS-84经度
     * @return GCJ-02坐标对 (纬度, 经度)
     */
    fun wgs84ToGcj02(wgLat: Double, wgLng: Double): Pair<Double, Double> {
        if (!isInChina(wgLat, wgLng)) {
            return Pair(wgLat, wgLng)
        }

        var dLat = transformLat(wgLng - 105.0, wgLat - 35.0)
        var dLng = transformLng(wgLng - 105.0, wgLat - 35.0)

        val radLat = wgLat / 180.0 * PI
        var magic = Math.sin(radLat)
        magic = 1 - EE * magic * magic

        val sqrtMagic = Math.sqrt(magic)
        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI)
        dLng = (dLng * 180.0) / (A / sqrtMagic * Math.cos(radLat) * PI)

        val mgLat = wgLat + dLat
        val mgLng = wgLng + dLng

        return Pair(mgLat, mgLng)
    }

    /**
     * 转换纬度
     */
    private fun transformLat(x: Double, y: Double): Double {
        var ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x))
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0
        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0
        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0
        return ret
    }

    /**
     * 转换经度
     */
    private fun transformLng(x: Double, y: Double): Double {
        var ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x))
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0
        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0
        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0
        return ret
    }

    /**
     * 根据地区自动选择合适的坐标系并格式化输出
     * @param lat 纬度
     * @param lng 经度
     * @return 格式化后的坐标字符串（经度,纬度）
     */
    fun formatCoordinateForApi(lat: Double, lng: Double): String {
        val coordinate = if (isInChina(lat, lng)) {
            val gcj02 = wgs84ToGcj02(lat, lng)
            Pair(gcj02.second, gcj02.first) // API需要经度在前，纬度在后
        } else {
            Pair(lng, lat) // 其他地区直接使用WGS-84
        }

        // 保留小数点后2位，符合常见的地理坐标精度
        return "${String.format("%.2f", coordinate.first)},${String.format("%.2f", coordinate.second)}"
    }

    /**
     * 从位置信息中获取适合API查询的坐标格式
     * @param context 上下文
     * @param lat 纬度
     * @param lng 经度
     * @return 格式化后的坐标字符串（经度,纬度）
     */
    fun getApiLocationFromCoordinates(context: Context, lat: Double, lng: Double): String {
        return try {
            // 尝试通过Geocoder判断所在国家
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)

            if (addresses != null && addresses.isNotEmpty()) {
                val countryCode = addresses[0].countryCode
                // 中国的国家代码是CN
                if ("CN".equals(countryCode, ignoreCase = true)) {
                    // 中国地区使用GCJ-02坐标系
                    val gcj02 = wgs84ToGcj02(lat, lng)
                    "${String.format("%.2f", gcj02.second)},${String.format("%.2f", gcj02.first)}"
                } else {
                    // 其他地区使用WGS-84坐标系
                    "${String.format("%.2f", lng)},${String.format("%.2f", lat)}"
                }
            } else {
                // 如果无法获取地址信息，使用粗略的区域判断
                formatCoordinateForApi(lat, lng)
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取地址信息失败: ${e.message}")
            // 出错时使用粗略的区域判断
            formatCoordinateForApi(lat, lng)
        }
    }
}
