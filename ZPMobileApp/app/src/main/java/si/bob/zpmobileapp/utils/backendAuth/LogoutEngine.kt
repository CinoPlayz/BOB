package si.bob.zpmobileapp.utils.backendAuth

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import si.bob.zpmobileapp.MyApp

suspend fun logoutUserBackend(context: MyApp) {
    withContext(Dispatchers.IO) {
        try {
            val prefs = context.sharedPrefs

            // Retrieve the stored token and username from SharedPreferences
            val token = prefs.getString(MyApp.TOKEN_KEY, "") ?: ""
            val username = prefs.getString(MyApp.USERNAME_KEY, "") ?: ""

            if (token.isEmpty()) {
                Log.e("LogoutUser", "No token found. User is already logged out.")
                return@withContext
            }

            // Construct the logout URL
            val url = "${prefs.getString(MyApp.BACKEND_URL_KEY, "")}/users/token"

            // Make the DELETE request
            val (_, response, result) = Fuel.delete(url)
                .header("Accept-Language", "en")
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
                )
                .header(Headers.AUTHORIZATION, "Bearer $token")
                .header(Headers.CONTENT_TYPE, "application/json")
                .responseString()

            when (result) {
                is Result.Failure -> {
                    Log.e("LogoutUser", "Logout failed: ${result.getException().message}")
                    // Clear the token and username from SharedPreferences even if the logout request fails
                    prefs.edit().apply {
                        remove(MyApp.TOKEN_KEY)
                        remove(MyApp.USERNAME_KEY)
                        apply()
                    }
                }

                is Result.Success -> {
                    Log.i("LogoutUser", "Logout successful: ${response.statusCode}")
                    // Clear the token and username from SharedPreferences upon success
                    prefs.edit().apply {
                        remove(MyApp.TOKEN_KEY)
                        remove(MyApp.USERNAME_KEY)
                        apply()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LogoutUser", "An error occurred during logout: ${e.message}")
            // Ensure SharedPreferences are cleared in case of any exception
            val prefs = context.sharedPrefs
            prefs.edit().apply {
                remove(MyApp.TOKEN_KEY)
                remove(MyApp.USERNAME_KEY)
                apply()
            }
        }
    }
}
