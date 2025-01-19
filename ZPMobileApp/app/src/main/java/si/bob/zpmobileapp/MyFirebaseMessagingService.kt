package si.bob.zpmobileapp

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "notifications_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = "Extreme Notifications"
        val descriptionText = "Channel for extreme events"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "Remote Message: $remoteMessage")

        // Check if there is data in the message payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message Data Payload: ${remoteMessage.data}")

            val title = remoteMessage.data["title"] ?: "New Notification"
            val body = remoteMessage.data["body"] ?: "No message body"
            val navigateTo = remoteMessage.data["navigate_to"] ?: ""

            // Show notification with the data
            showNotification(title, body, navigateTo)
        } else {
            Log.d("FCM", "No data in message")
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(title: String, body: String, navigateTo: String) {
        Log.d("FCM", "showNotification called with title: $title, body: $body, navigate_to: $navigateTo")

        // Create an intent to navigate to the desired fragment (Messages in this case)
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", navigateTo)  // Pass the navigate_to value
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create notification
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error) // Icon
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss the notification when clicked
            .build()

        Log.d("FCM", "Notification: $notification")

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, notification)
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "New token: $token")
    }
}
