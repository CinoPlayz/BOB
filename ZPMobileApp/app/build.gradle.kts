plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("plugin.serialization") version "2.1.0"
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "si.bob.zpmobileapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "si.bob.zpmobileapp"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }
    println(env.BASE_URL.value)
    println(env.AUTH_TOKEN.value)
    println(env.MQTT_URL.value)

    buildTypes {
        debug {
            isMinifyEnabled = false
            buildConfigField("String", "BASE_URL", "\"${env.BASE_URL.value}\"")
            buildConfigField("String", "AUTH_TOKEN", "\"${env.AUTH_TOKEN.value}\"")
            buildConfigField("String", "MQTT_BROKER_URL", "\"${env.MQTT_URL.value}\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "BASE_URL", "\"${env.BASE_URL.value}\"")
            buildConfigField("String", "AUTH_TOKEN", "\"${env.AUTH_TOKEN.value}\"")
            buildConfigField("String", "MQTT_BROKER_URL", "\"${env.MQTT_URL.value}\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("org.osmdroid:osmdroid-android:6.1.8")
    implementation(libs.converter.gson)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.firebase.messaging)
    implementation(libs.org.eclipse.paho.client.mqttv3)
    implementation(libs.org.eclipse.paho.android.service)
    implementation(libs.fuel)
    implementation(libs.fuel.coroutines)
    implementation(libs.dotenv.kotlin)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.play.services.location)
    implementation(libs.androidx.preference.ktx)
    implementation("androidx.camera:camera-core:1.5.0-alpha05")
    implementation("androidx.camera:camera-camera2:1.5.0-alpha05")
    implementation("androidx.camera:camera-lifecycle:1.5.0-alpha05")
    implementation("androidx.camera:camera-view:1.5.0-alpha05")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}