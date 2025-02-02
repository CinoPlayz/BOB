package si.bob.zpmobileapp.utils.backendAuth

import android.os.Handler
import android.os.Looper
import android.util.Log
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
data class RegisterResponse(
    val success: Boolean,
    val message: String? = null,
)

suspend fun registerUser(
    app: MyApp,
    email: String,
    username: String,
    password: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    try {
        withContext(Dispatchers.IO) {
            val mqttClient = app.mqttClient

            if (mqttClient == null || !mqttClient.isConnected) {
                Handler(Looper.getMainLooper()).post {
                    onFailure("MQTT client not connected")
                }
                return@withContext
            }

            val registerData = mapOf(
                "email" to email,
                "username" to username,
                "password" to password
            )
            val body = json.encodeToString(registerData)

            mqttClient.publish("app/register/request", MqttMessage(body.toByteArray()))

            val subscribeTopic = "app/register/response/$username"
            mqttClient.subscribe(subscribeTopic)

            mqttClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Handler(Looper.getMainLooper()).post {
                        onFailure("Connection to server lost: ${cause?.message}")
                    }
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val responseBody = message?.toString()
                    Log.d("MQTT", "Message received on topic: $topic, message: $responseBody")

                    try {
                        val response = responseBody?.let { json.decodeFromString<RegisterResponse>(it) }

                        Handler(Looper.getMainLooper()).post {
                            if (response?.success == true) {
                                onSuccess()
                            } else {
                                val errorMessage = response?.message ?: "Unknown error occurred"
                                onFailure(errorMessage)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MQTT", "Error parsing response: ${e.message}")
                        Handler(Looper.getMainLooper()).post {
                            onFailure("Error parsing server response")
                        }
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    // No action needed here
                }
            })
        }
    } catch (e: Exception) {
        Handler(Looper.getMainLooper()).post {
            onFailure("An unexpected error occurred: ${e.message}")
        }
        Log.e("RegisterUser", "Exception: ${e.message}", e)
    }
}

