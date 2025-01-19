package si.bob.zpmobileapp

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.media.VolumeShaper
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

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if the message contains a notification payload
        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            // Display the notification
            showNotification(it.body ?: "No message body", it.title)
        }

        remoteMessage.data.isNotEmpty().let {
            Log.d("FCM", "Message Data Payload: ${remoteMessage.data}")
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(messageBody: String, title: String?) {
        // Create an intent to navigate to the MessagesFragment when the notification is clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "messages")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_MUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.bob) // Icon
            .setContentTitle(title ?: "New Notification")
            .setContentText(messageBody)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss the notification when clicked
            .build()

        // Display the notification
        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, notification)
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "New token: $token")
    }
}
