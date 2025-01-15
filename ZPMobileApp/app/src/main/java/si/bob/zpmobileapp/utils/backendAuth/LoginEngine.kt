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
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
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
            val mqttClient = MqttClient("tcp://broker.hivemq.com:1883", MqttClient.generateClientId(), null)

            val options = MqttConnectOptions()
            options.isCleanSession = true // Optional: set any other options as required

            try {
                mqttClient.connect(options) // Just connect without passing listener here
                Log.d("MQTT", "Connected successfully")
            } catch (e: MqttException) {
                Log.e("MQTT", "Connection failed: ${e.message}")
                Handler(Looper.getMainLooper()).post {
                    onFailure("Failed to connect to MQTT broker")
                }
                return@withContext
            }

            val loginData = LoginData(username, password)
            val body = json.encodeToString(loginData)

            mqttClient.publish("app/login/request", MqttMessage(body.toByteArray()))

            // Subscribe to the response topic for the specific username
            val subscribeTopic = "app/login/response/$username"
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
                        // Assuming the response body contains either a 'token' or 'loginToken'
                        val loginResponse = responseBody?.let { json.decodeFromString<LoginResponse>(it) }

                        Handler(Looper.getMainLooper()).post {
                            when {
                                loginResponse?.token != null -> {
                                    // Success - Store the token
                                    app.sharedPrefs.edit().apply {
                                        putString(MyApp.TOKEN_KEY, loginResponse.token)
                                        putString(MyApp.USERNAME_KEY, username)
                                        apply()
                                    }
                                    onSuccess(loginResponse.token)
                                    mqttClient.disconnect()
                                }
                                loginResponse?.loginToken != null -> {
                                    // Two-Factor Authentication case
                                    app.sharedPrefs.edit().apply {
                                        putString(MyApp.USERNAME_KEY, username)
                                        apply()
                                    }
                                    onTwoFA(loginResponse.loginToken)
                                    mqttClient.disconnect()
                                }
                                loginResponse?.message != null -> {
                                    // Handle login failure
                                    onFailure("Invalid username or password")
                                    mqttClient.disconnect()
                                }
                                else -> {
                                    // Invalid response or failure
                                    onFailure("Internal Error: Unexpected response format.")
                                    mqttClient.disconnect()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MQTT", "Error parsing response: ${e.message}")
                        onFailure("Error parsing server response")
                        mqttClient.disconnect()
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    // Optional: handle message delivery completion
                }
            })
        }
    } catch (e: Exception) {
        Handler(Looper.getMainLooper()).post {
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
    Log.d("LoginUserTwoFA", "loginToken: $loginToken, totp: $totp")

    try {
        withContext(Dispatchers.IO) {
            val mqttClient = MqttClient("tcp://broker.hivemq.com:1883", MqttClient.generateClientId(), null)
            mqttClient.connect()

            val loginData = LoginDataTwoFA(loginToken, totp)
            val body = json.encodeToString(loginData)

            // Publish the OTP request to the backend
            mqttClient.publish("app/twofa/request", MqttMessage(body.toByteArray()))

            // Subscribe to the 2FA response topic to receive the result
            mqttClient.subscribe("app/twofa/response/$loginToken")

            mqttClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Handler(Looper.getMainLooper()).post {
                        onFailure("Connection to server lost: ${cause?.message}")
                    }
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val responseBody = message?.toString()
                    val loginResponse = responseBody?.let { json.decodeFromString<LoginResponse>(it) }

                    Log.d("MQTT", "Message received on topic: $topic, message: $responseBody")

                    Handler(Looper.getMainLooper()).post {
                        when {
                            loginResponse?.token != null -> {
                                // Successful 2FA login - Store the token
                                app.sharedPrefs.edit().apply {
                                    putString(MyApp.TOKEN_KEY, loginResponse.token)
                                    apply()
                                }
                                onSuccess(loginResponse.token)
                                mqttClient.disconnect()
                            }
                            loginResponse?.message != null -> {
                                // If message contains "Invalid OTP code"
                                if (loginResponse.message == "Invalid OTP code") {
                                    onFailure("The OTP code you entered is invalid. Please try again.")
                                } else {
                                    // Handle other failure messages
                                    onFailure(loginResponse.message)
                                }
                                mqttClient.disconnect()
                            }
                            else -> {
                                onFailure("Internal Error: Unexpected response format.")
                                mqttClient.disconnect()
                            }
                        }
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })
        }
    } catch (e: Exception) {
        Handler(Looper.getMainLooper()).post {
            app.sharedPrefs.edit().apply {
                remove(MyApp.USERNAME_KEY)
                apply()
            }
            onFailure("An unexpected error occurred: ${e.message}")
        }
        Log.e("LoginUserTwoFA", "Exception: ${e.message}", e)
    }
}
