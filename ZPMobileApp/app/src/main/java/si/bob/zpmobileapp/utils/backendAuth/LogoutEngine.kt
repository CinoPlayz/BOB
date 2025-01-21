package si.bob.zpmobileapp.utils.backendAuth

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import si.bob.zpmobileapp.MyApp

private val json = Json { ignoreUnknownKeys = true }

@Serializable
data class LogoutResponse(
    val success: Boolean,
    val message: String? = null,
)

suspend fun logoutUserBackend(app: MyApp) {
    withContext(Dispatchers.IO) {
        try {
            val prefs = app.sharedPrefs

            // Retrieve the stored token and username from SharedPreferences
            val token = prefs.getString(MyApp.TOKEN_KEY, "") ?: ""
            val username = prefs.getString(MyApp.USERNAME_KEY, "") ?: ""

            // Delete auth data from SharedPreferences
            prefs.edit().apply {
                remove(MyApp.TOKEN_KEY)
                remove(MyApp.USERNAME_KEY)
                apply()
            }

            if (token.isEmpty()) {
                Log.e("LogoutUser", "No token found. User is already logged out.")
                return@withContext
            }

            val mqttClient = app.mqttClient

            if (mqttClient == null || !mqttClient.isConnected) {
                Handler(Looper.getMainLooper()).post {
                    Log.e("LogoutEngine", "MQTT client not connected")
                }
                return@withContext
            }

            val logoutData = mapOf("username" to username, "token" to token)
            val body = json.encodeToString(logoutData)

            val publishTopic = "app/logout/request"
            val subscribeTopic = "app/logout/response/$username"

            mqttClient.publish(publishTopic, MqttMessage(body.toByteArray()))
            mqttClient.subscribe(subscribeTopic)

            mqttClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Handler(Looper.getMainLooper()).post {
                        Log.e("LogoutEngine", "Connection to server lost: ${cause?.message}")
                    }
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val responseBody = message?.toString()
                    Log.d("MQTT", "Message received on topic: $topic, message: $responseBody")

                    try {
                        val response = responseBody?.let { json.decodeFromString<LogoutResponse>(it) }

                        Handler(Looper.getMainLooper()).post {
                            if (response?.success == true) {
                                Toast.makeText(app, "Logout successful", Toast.LENGTH_SHORT).show()
                            } else {
                                val errorMessage = response?.message ?: "Unknown error occurred"
                                Log.e("LogoutEngine", "Logout failed: $errorMessage")
                            }
                        }
                    } catch (e: Exception) {
                        Handler(Looper.getMainLooper()).post {
                            app.sharedPrefs.edit().apply {
                                remove(MyApp.USERNAME_KEY)
                                apply()
                            }
                        }
                        Log.e("LoginUser", "Exception: ${e.message}", e)
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    // No action needed here
                }
            })

        } catch (e: Exception) {
            Log.e("LogoutUser", "An error occurred during logout: ${e.message}")
        }
    }
}
