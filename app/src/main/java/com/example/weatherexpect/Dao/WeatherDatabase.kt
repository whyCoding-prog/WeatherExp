package com.example.weatherexpect.Dao

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.weatherexpect.Data.Entity.CurrentWeather
import com.example.weatherexpect.Data.Entity.UserSettings
import com.example.weatherexpect.Data.Entity.WeatherForecast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Database(
    entities = [CurrentWeather::class, UserSettings::class, WeatherForecast::class],
    version = 2, // 升级到版本 2
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao

    companion object {
        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        // 定义从版本 1 到版本 2 的迁移
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加 notification 字段，默认值为 0（SQLite 中 0 表示 false）
                database.execSQL("ALTER TABLE user_settings ADD COLUMN notification INTEGER NOT NULL DEFAULT 0")
                Log.i("DatabaseMigration", "成功执行从版本 1 到版本 2 的迁移")
            }
        }

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): WeatherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java,
                    "weather_database"
                )
                    .addCallback(WeatherDatabaseCallback(scope))
                    .addMigrations(MIGRATION_1_2) // 添加迁移脚本
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class WeatherDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Log.i("DatabaseService", "数据库初始化成功")
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    DatabaseInitializer.initializeDatabase(database)
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            Log.d("DatabaseService", "数据库已打开")
        }
    }
}