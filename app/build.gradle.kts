import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.example.weatherexpect"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.weatherexpect"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "QWEATHER_API_KEY","\"${localProperties.getProperty("QWEATHER_API_KEY", "")}\"")
        buildConfigField("String", "BAIDU_MAP_API_KEY","\"${localProperties.getProperty("BAIDU_MAP_API_KEY", "")}\"")

        manifestPlaceholders["BAIDU_MAP_KEY"] =localProperties.getProperty("BAIDU_MAP_API_KEY", "")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    //implementation(libs.androidx.room3.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    implementation ("com.squareup.okhttp3:okhttp:4.9.3")

    // Kotlin协程
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // 如果需要使用JSON解析
    implementation ("org.json:json:20210307")

    // Retrofit 核心库
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")

    // Gson 解析器
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // AndroidX Fragment 库（包含 viewLifecycleOwner）
    implementation ("androidx.fragment:fragment-ktx:1.6.1")

    // Room 核心运行时依赖
    implementation("androidx.room:room-runtime:2.6.0")

    // Room Kotlin 扩展
    implementation("androidx.room:room-ktx:2.6.0")

    // Room 注解处理器（Kotlin 使用 kapt）
    kapt("androidx.room:room-compiler:2.6.0")

    // 核心协程库
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

        // 百度地图 SDK
    implementation ("com.baidu.lbsyun:BaiduMapSDK_Map:7.6.0")
        // 百度定位 SDK
    implementation ("com.baidu.lbsyun:BaiduMapSDK_Location:9.3.7")
        // 百度搜索（POI检索）

    implementation ("com.baidu.lbsyun:BaiduMapSDK_Search:7.6.0")
}