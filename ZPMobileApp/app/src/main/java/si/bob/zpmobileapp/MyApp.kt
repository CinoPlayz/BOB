package si.bob.zpmobileapp

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.FirebaseApp
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.UUID

class MyApp : Application() {
    lateinit var sharedPrefs: SharedPreferences
    var mqttClient: MqttClient? = null

    companion object {
        const val PREFS_NAME = "bobMobileApp_preferences"
        const val UUID_KEY = "bobMobileApp_uuid"
        const val BACKEND_URL_KEY = "bobMobileApp_backend_url"
        const val USERNAME_KEY = "bobMobileApp_username"
        const val TOKEN_KEY = "bobMobileApp_token"
        const val NOTIFICATION_TOKEN_KEY = "bobMobileApp_notification_token"
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

        initializeMQTT()
        FirebaseApp.initializeApp(this)
    }

    private fun initializeMQTT() {
        try {
            mqttClient = MqttClient(BuildConfig.MQTT_BROKER_URL, MqttClient.generateClientId(), null)

            val options = MqttConnectOptions().apply {
                isCleanSession = false // Keep subscriptions after reconnect
                isAutomaticReconnect = true // Enable automatic reconnect
                connectionTimeout = 10 // Timeout for connection
                keepAliveInterval = 20 // Heartbeat interval
            }

            mqttClient?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e("MQTT", "Connection lost: ${cause?.message}")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    Log.d("MQTT", "Message arrived: [$topic] ${message.toString()}")
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d("MQTT", "Delivery complete: ${token?.message}")
                }
            })

            mqttClient?.connect(options)
            Log.d("MQTT", "Connected to MQTT broker")

        } catch (e: MqttException) {
            Log.e("MQTT", "Error initializing MQTT: ${e.message}")
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        disconnectMQTT()
    }

    private fun disconnectMQTT() {
        try {
            mqttClient?.disconnect()
            Log.d("MQTT", "Disconnected from MQTT broker")
        } catch (e: MqttException) {
            Log.e("MQTT", "Error disconnecting MQTT: ${e.message}")
        }
    }
}