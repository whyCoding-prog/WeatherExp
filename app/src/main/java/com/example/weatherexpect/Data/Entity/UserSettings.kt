package com.example.weatherexpect.Data.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: String = "101010100",
    val lastLocation: String = "北京",
    val notification: Boolean = false // 新增字段，默认值为 false
)