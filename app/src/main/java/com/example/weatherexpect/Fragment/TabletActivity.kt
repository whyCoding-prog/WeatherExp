package com.example.weatherexpect.Fragment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherexpect.R

class TabletActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table)

        // 添加列表Fragment到左侧容器
        supportFragmentManager.beginTransaction()
            .replace(R.id.list_container, WeatherListFragment())
            .commit()

        // 添加详情Fragment到右侧容器
        supportFragmentManager.beginTransaction()
            .replace(R.id.detail_container, WeatherDetailFragment())
            .commit()
    }
}