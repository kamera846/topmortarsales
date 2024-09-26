package com.topmortar.topmortarsales.commons.services
import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.USER_KIND_PENAGIHAN
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.utils.SessionManager

class FirebaseCloudMessagingServices : FirebaseMessagingService() {
    var sessionManager: SessionManager? = null
    private val userId get() = sessionManager?.userID()
    private val userKind get() = sessionManager?.userKind()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        try {
            if (sessionManager == null) sessionManager = SessionManager(this)
//            Log.d("FCM", "Message received: ${Gson().toJson(remoteMessage)}")
            if (userId != null && userId.toString().isNotEmpty() && userKind != null && (userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN)) {
                // Mengambil data dari pesan
//                Log.d("FCM", "From: ${remoteMessage.from}")

                // Jika pesan memiliki payload
                remoteMessage.notification?.let {
                    Log.d("FCM", "Message Notification Body: ${it.body}")
                    showNotification(it.title, it.body)
                }
//            } else {
//                Log.d("FCM", "user not logged in")
//                Log.d("FCM", remoteMessage.toString())
            }
        } catch (e: Exception) {
            Log.e("FCM", "Error exception: $e")
        }
    }

    private fun showNotification(title: String?, message: String?) {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(this, "topmortar_messages_notifications", "Topmortar Messages Notifications")
        } else {
            ""
        }
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.logo_topmortar_circle))
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun createNotificationChannel(context: Context, channelId: String, channelName: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            channel.lightColor = Color.BLUE
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(channel)
            channelId
        } else {
            ""
        }
    }

    override fun onNewToken(token: String) {
        try {
            if (sessionManager == null) sessionManager = SessionManager(this)
            if (userId != null && userId.toString().isNotEmpty() && userKind != null && (userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN)) {
                Log.d("FCM", "Refreshed token: $token")
                // Kirim token ke server atau simpan
            }
        } catch (e: Exception) {
            Log.e("FCM", "Error exception: $e")
        }
    }
}
