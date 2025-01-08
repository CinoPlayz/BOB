package si.bob.zpmobileapp

import android.app.Application
import android.content.SharedPreferences
import java.util.UUID

class MyApp : Application() {
    lateinit var sharedPrefs: SharedPreferences

    companion object {
        const val PREFS_NAME = "bobMobileApp_preferences"
        const val UUID_KEY = "bobMobileApp_uuid"
        const val BACKEND_URL_KEY = "bobMobileApp_backend_url"
        const val USERNAME_KEY = "bobMobileApp_username"
        const val TOKEN_KEY = "bobMobileApp_token"
    }
    override fun onCreate() {
        super.onCreate()

        sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        if (!sharedPrefs.contains(UUID_KEY)) {
            val uuid = UUID.randomUUID().toString()
            sharedPrefs.edit().putString(UUID_KEY, uuid).apply()
        }

        if (!sharedPrefs.contains(BACKEND_URL_KEY)) {
            sharedPrefs.edit().putString(BACKEND_URL_KEY, BuildConfig.BASE_URL).apply()
        }
    }
}