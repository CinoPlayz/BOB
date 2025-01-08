package si.bob.zpmobileapp.utils.backendAuth

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import si.bob.zpmobileapp.MyApp

private val json = Json { ignoreUnknownKeys = true }

@Serializable
data class LoginData(val username: String, val password: String)

@Serializable
data class LoginDataTwoFA(val loginToken: String, val otpCode: String)

@Serializable
data class LoginResponse(val message: String? = null, val token: String? = null, val loginToken: String? = null)

suspend fun loginUser(
    app: MyApp,
    username: String,
    password: String,
    onSuccess: (String) -> Unit,
    onTwoFA: (String) -> Unit,
    onFailure: (String) -> Unit,
) {
    try {
        withContext(Dispatchers.IO) {
            val url = app.sharedPrefs.getString(MyApp.BACKEND_URL_KEY, "") + "/users/login"
            val loginData = LoginData(username, password)
            val body = json.encodeToString(loginData)

            val result = Fuel.post(url)
                .header("Accept-Language", "en")
                .header(
                    "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
                )
                .header(Headers.CONTENT_TYPE, "application/json")
                .jsonBody(body)
                .responseString()
                .third

            when (result) {
                is Result.Success -> {
                    val responseBody = result.get()
                    val loginResponse = json.decodeFromString<LoginResponse>(responseBody)
                    withContext(Dispatchers.Main) {
                        when {
                            loginResponse.token != null -> {
                                // Save token and username to SharedPreferences
                                app.sharedPrefs.edit().apply {
                                    putString(MyApp.TOKEN_KEY, loginResponse.token)
                                    putString(MyApp.USERNAME_KEY, username)
                                    apply()
                                }
                                onSuccess(loginResponse.token)
                            }

                            loginResponse.loginToken != null -> {
                                // Save username for TFA continuation
                                app.sharedPrefs.edit().apply {
                                    putString(MyApp.USERNAME_KEY, username)
                                    apply()
                                }
                                onTwoFA(loginResponse.loginToken)
                            }

                            else -> onFailure("Internal Error: Unexpected response format.")
                        }
                    }
                }

                is Result.Failure -> {
                    withContext(Dispatchers.Main) {
                        // Clear saved username on failure
                        app.sharedPrefs.edit().apply {
                            remove(MyApp.USERNAME_KEY)
                            apply()
                        }

                        when (result.error.response.statusCode) {
                            -1 -> onFailure("Error: Backend Server Offline")
                            500 -> onFailure("Invalid credentials")
                            else -> onFailure("Error: ${result.error.response.statusCode}")
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            // Clear saved username on exception
            app.sharedPrefs.edit().apply {
                remove(MyApp.USERNAME_KEY)
                apply()
            }
            onFailure("An unexpected error occurred: ${e.message}")
        }
        Log.e("LoginUser", "Exception: ${e.message}", e)
    }
}

suspend fun loginUserTwoFA(
    app: MyApp,
    loginToken: String,
    totp: String,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    try {
        withContext(Dispatchers.IO) {
            val url = app.sharedPrefs.getString(MyApp.BACKEND_URL_KEY, "") + "/users/twoFaLogin"
            val loginData = LoginDataTwoFA(loginToken, totp)
            val body = json.encodeToString(loginData)

            val result = Fuel.post(url)
                .header("Accept-Language", "en")
                .header(
                    "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
                )
                .header(Headers.CONTENT_TYPE, "application/json")
                .jsonBody(body)
                .responseString()
                .third

            when (result) {
                is Result.Success -> {
                    val responseBody = result.get()
                    val loginResponse = json.decodeFromString<LoginResponse>(responseBody)
                    withContext(Dispatchers.Main) {
                        when {
                            loginResponse.token != null -> {
                                // Save token to SharedPreferences
                                app.sharedPrefs.edit().apply {
                                    putString(MyApp.TOKEN_KEY, loginResponse.token)
                                    apply()
                                }
                                onSuccess(loginResponse.token)
                            }

                            else -> onFailure("Internal Error: Unexpected response format.")
                        }
                    }
                }

                is Result.Failure -> {
                    withContext(Dispatchers.Main) {
                        // Clear saved username on failure
                        app.sharedPrefs.edit().apply {
                            remove(MyApp.USERNAME_KEY)
                            apply()
                        }

                        when (result.error.response.statusCode) {
                            -1 -> onFailure("Error: Backend Server Offline")
                            500 -> onFailure("Invalid credentials")
                            else -> onFailure("Error: ${result.error.response.statusCode}")
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            // Clear saved username on exception
            app.sharedPrefs.edit().apply {
                remove(MyApp.USERNAME_KEY)
                apply()
            }
            onFailure("An unexpected error occurred: ${e.message}")
        }
        Log.e("LoginUserTwoFA", "Exception: ${e.message}", e)
    }
}