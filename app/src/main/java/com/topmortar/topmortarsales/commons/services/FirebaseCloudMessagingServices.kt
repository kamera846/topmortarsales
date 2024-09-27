package com.topmortar.topmortarsales.commons.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.NOTIFICATION_LEVEL
import com.topmortar.topmortarsales.commons.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class FirebaseCloudMessagingServices : FirebaseMessagingService() {
    var sessionManager: SessionManager? = null
    private val userId get() = sessionManager?.userID()
    private lateinit var nNotificationLevel: String
    private lateinit var nUserId: String
    private var nChannelId: String = "general_notifications"
    private var nChannelName: String = "Notifikasi Umum"
    private var nChannelDescription: String = "Notifikasi untuk kategori umum"

    companion object {
        const val NOTIFICATION_ID = 2020
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        try {
            if (sessionManager == null) sessionManager = SessionManager(this)
            remoteMessage.data.let {
                nNotificationLevel = it["notification_level"].toString()
                nUserId = it["id_user"].toString()
                nChannelId = it["id_channel"].toString()
                nChannelName = it["channel_name"].toString()
                nChannelDescription = it["channel_description"].toString()
            }

            if (userId != null && userId.toString().isNotEmpty() && userId.toString() == nUserId && NOTIFICATION_LEVEL == nNotificationLevel) {
                remoteMessage.notification?.let {
                    showNotification(it.title, it.body, it.imageUrl)
                }
            }
        } catch (e: Exception) {
            Log.d("FCM", "Error fcm received message exception: $e")
        }
    }

    private fun showNotification(title: String?, message: String?, imgUrl: Uri?) {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(
                this,
                nChannelId,
                nChannelName
            )
        } else {
            ""
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    this.resources,
                    R.mipmap.logo_topmortar_circle
                )
            )
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (imgUrl != null) {
            GlobalScope.launch {
                val bitmap = downloadImage(imgUrl.toString())
                if (bitmap != null) {
                    notificationBuilder.setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                    )
                }
                val notificationManager =
                    NotificationManagerCompat.from(this@FirebaseCloudMessagingServices)
                if (ActivityCompat.checkSelfPermission(
                        this@FirebaseCloudMessagingServices,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@launch
                }
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
            }
        } else {
            val notificationManager = NotificationManagerCompat.from(this)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    private fun createNotificationChannel(
        context: Context,
        channelId: String,
        channelName: String
    ): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = nChannelDescription
                enableLights(true)
                lightColor = Color.BLUE
                setSound(soundUri, null)
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }

            val service =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(channel)
            channelId
        } else {
            ""
        }
    }

    private suspend fun downloadImage(url: String?): Bitmap? {
        return try {
            val connection = withContext(Dispatchers.IO) {
                URL(url).openConnection()
            } as HttpURLConnection
            connection.doInput = true
            withContext(Dispatchers.IO) {
                connection.connect()
            }
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onNewToken(token: String) {
        try {
            if (sessionManager == null) sessionManager = SessionManager(this)
            if (userId != null && userId.toString().isNotEmpty()) {
                Log.d("FCM", "Refreshed token: $token")
            }
        } catch (e: Exception) {
            Log.e("FCM", "Error fcm new token exception: $e")
        }
    }
}
