package si.bob.zpmobileapp.utils.backendAuth

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import si.bob.zpmobileapp.MyApp

private val json = Json { ignoreUnknownKeys = true }

suspend fun logoutUserBackend(context: MyApp) {
    withContext(Dispatchers.IO) {
        try {
            val prefs = context.sharedPrefs

            // Retrieve the stored token and username from SharedPreferences
            val token = prefs.getString(MyApp.TOKEN_KEY, "") ?: ""
            val username = prefs.getString(MyApp.USERNAME_KEY, "") ?: ""

            prefs.edit().apply {
                remove(MyApp.TOKEN_KEY)
                remove(MyApp.USERNAME_KEY)
                apply()
            }

            if (token.isEmpty()) {
                Log.e("LogoutUser", "No token found. User is already logged out.")
                return@withContext
            }

            // Connect to the MQTT broker
            val mqttClient = MqttClient("tcp://10.11.12.100:1883", MqttClient.generateClientId(), null)
            mqttClient.connect()

            // Prepare the logout data
            val logoutData = mapOf("username" to username, "token" to token)

            // Convert the data to JSON string
            val body = json.encodeToString(logoutData)

            // Publish the logout message to the MQTT topic
            mqttClient.publish("app/logout/request", MqttMessage(body.toByteArray()))

            // Subscribe to the response topic to know when the logout is complete
            mqttClient.subscribe("app/logout/response/$username") { message, exception ->
                if (exception != null) {
                    Log.e("MQTT", "Error subscribing: $exception")
                    return@subscribe
                }

                // Handle the response message
                val response = message.toString()
                if (response.contains("success")) {
                    Log.i("LogoutUser", "Logout successful via MQTT")

                    // Clear the token and username from SharedPreferences upon success
                    prefs.edit().apply {
                        remove(MyApp.TOKEN_KEY)
                        remove(MyApp.USERNAME_KEY)
                        apply()
                    }
                } else {
                    Log.e("LogoutUser", "Logout failed via MQTT: $response")
                }

                mqttClient.disconnect()
            }

        } catch (e: Exception) {
            Log.e("LogoutUser", "An error occurred during logout: ${e.message}")
        }
    }
}
