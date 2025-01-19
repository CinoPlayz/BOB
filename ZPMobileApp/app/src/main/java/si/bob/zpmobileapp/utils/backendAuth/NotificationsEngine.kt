package si.bob.zpmobileapp.utils.backendAuth

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessaging
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import si.bob.zpmobileapp.MyApp
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject

fun getPushTokenAndSendToMQTT(app: MyApp) {
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            // Successfully retrieved the token
            val pushToken = task.result
            // Send push token to MQTT
            sendPushTokenToMQTT(app, pushToken)
        } else {
            // Failed to get the token
            Log.e("FCM", "Error getting push token", task.exception)
        }
    }
}

private fun sendPushTokenToMQTT(app: MyApp, pushToken: String) {
    val username = app.sharedPrefs.getString(MyApp.USERNAME_KEY, null)
    val token = app.sharedPrefs.getString(MyApp.TOKEN_KEY, null)

    try {
        val mqttClient = app.mqttClient

        if (mqttClient == null || !mqttClient.isConnected) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(app.applicationContext, "MQTT connection lost", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val requestPayload = JSONObject().apply {
            put("token", token)
            put("pushToken", pushToken)
        }

        val publishTopic = "app/notifications/register/request"
        val subscribeTopic = "app/notifications/register/response/$username"

        mqttClient.publish(publishTopic, MqttMessage(requestPayload.toString().toByteArray()))
        mqttClient.subscribe(subscribeTopic)

        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                // Handle connection loss
                // If you are in an Activity/Fragment, replace this with the proper context
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(app.applicationContext, "MQTT connection lost: ${cause?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d("MQTT", "Message arrived on topic: $topic")
                if (topic == subscribeTopic) {
                    val response = message?.toString()
                    val jsonResponse = JSONObject(response ?: "{}")
                    val success = jsonResponse.optBoolean("success", false)

                    Handler(Looper.getMainLooper()).post {
                        if (success) {
                            app.sharedPrefs.edit().apply {
                                putString(MyApp.NOTIFICATION_TOKEN_KEY, pushToken)
                                apply()
                            }
                            Toast.makeText(app.applicationContext, "Extreme event notifications enabled", Toast.LENGTH_SHORT).show()
                        } else {
                            val errorMessage = jsonResponse.optString("message", "Unknown error")
                            Toast.makeText(app.applicationContext, "Failed: $errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                // Optional: Handle delivery confirmation
            }
        })

        Log.d("MQTT", "Push token sent successfully")

    } catch (e: Exception) {
        Log.e("MQTT", "Failed to send push token", e)
    }
}


fun deregisterPushTokenAndRemoveFromDevice(app: MyApp) {
    val pushToken = app.sharedPrefs.getString(MyApp.NOTIFICATION_TOKEN_KEY, null)

    if (pushToken != null) {
        sendDeregisterPushTokenToMQTT(app, pushToken)
        removePushTokenFromSharedPrefs(app)
        deregisterFromFirebase(pushToken)
    } else {
        Log.e("FCM", "No push token found in SharedPreferences")
    }
}

private fun removePushTokenFromSharedPrefs(app: MyApp) {
    try {
        app.sharedPrefs.edit().apply {
            remove(MyApp.NOTIFICATION_TOKEN_KEY)
            apply()
        }
        Log.d("FCM", "Push token removed from SharedPreferences")
    } catch (e: Exception) {
        Log.e("FCM", "Failed to remove push token from SharedPreferences", e)
    }
}

private fun sendDeregisterPushTokenToMQTT(app: MyApp, pushToken: String) {
    val username = app.sharedPrefs.getString(MyApp.USERNAME_KEY, null)
    val token = app.sharedPrefs.getString(MyApp.TOKEN_KEY, null)

    try {
        val mqttClient = app.mqttClient

        if (mqttClient == null || !mqttClient.isConnected) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(app.applicationContext, "MQTT connection lost", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val requestPayload = JSONObject().apply {
            put("token", token)
            put("pushToken", pushToken)
        }

        val publishTopic = "app/notifications/deregister/request"
        val subscribeTopic = "app/notifications/deregister/response/$username"

        mqttClient.publish(publishTopic, MqttMessage(requestPayload.toString().toByteArray()))
        mqttClient.subscribe(subscribeTopic)

        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                // Handle connection loss
                Handler(Looper.getMainLooper()).post {
                    // Toast.makeText(app.applicationContext, "MQTT connection lost: ${cause?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d("MQTT", "Message arrived on topic: $topic")
                if (topic == subscribeTopic) {
                    val response = message?.toString()
                    val jsonResponse = JSONObject(response ?: "{}")
                    val success = jsonResponse.optBoolean("success", false)

                    Handler(Looper.getMainLooper()).post {
                        if (success) {
                            // Toast.makeText(app.applicationContext, "Push token deregistered successfully", Toast.LENGTH_SHORT).show()
                            Toast.makeText(app.applicationContext, "Extreme event notifications disabled", Toast.LENGTH_SHORT).show()

                        } else {
                            val errorMessage = jsonResponse.optString("message", "Unknown error")
                            Toast.makeText(app.applicationContext, "Failed: $errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
            }
        })

        Log.d("MQTT", "Deregistration request sent successfully")

    } catch (e: Exception) {
        Log.e("MQTT", "Failed to send deregistration request", e)
    }
}

private fun deregisterFromFirebase(pushToken: String) {
    FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("FCM", "Push token deregistered from Firebase successfully")
        } else {
            Log.e("FCM", "Failed to deregister push token from Firebase", task.exception)
        }
    }
}