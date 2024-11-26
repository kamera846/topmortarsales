package com.topmortar.topmortarsales.commons.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.picasso.Picasso
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_SALES
import com.topmortar.topmortarsales.commons.CONST_FULL_NAME
import com.topmortar.topmortarsales.commons.CONST_USER_ID
import com.topmortar.topmortarsales.commons.CONST_USER_LEVEL
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.view.reports.ReportsActivity

class FirebaseCloudMessagingServices : FirebaseMessagingService() {
    var sessionManager: SessionManager? = null
    private val userId get() = sessionManager?.userID()
    private val userFullName get() = sessionManager?.fullName()
    private lateinit var nTitle: String
    private lateinit var nBody: String
    private lateinit var nVisitId: String
    private lateinit var nIntent: String
    private var nUserId: String? = null
    private var nImageUrl: String? = null
    private var nChannelId: String = "general_notifications"
    private var nChannelName: String = "Notifikasi Umum"
    private var nChannelDescription: String = "Notifikasi untuk kategori umum"

    companion object {
        const val NOTIFICATION_ID = 2020
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        try {
            if (sessionManager == null) sessionManager = SessionManager(this)

//            println("Message received")

            remoteMessage.notification?.let {
//                println("Message received from notification")
//                println(it.toString())
                // Tampilkan notifikasi
//                val title = it.title
//                val body = it.body
//                val image = it.imageUrl // URL logo/gambar
//
//                // Menggunakan NotificationManager untuk menampilkan notifikasi
//                val notificationBuilder = NotificationCompat.Builder(this, "top_mortar_sales")
//                    .setContentTitle(title)
//                    .setContentText(body)
//                    .setSmallIcon(R.drawable.notification_icon)
//                    .setStyle(
//                        NotificationCompat.BigPictureStyle()
//                            .bigPicture(Picasso.get().load(image).get()) // Memuat gambar logo
//                    )
//                    .setSound(Uri.parse("android.resource://$packageName/raw/notification_sound")) // Suara kustom
//
//                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                notificationManager.notify(0, notificationBuilder.build())
            }

            remoteMessage.data.let {
//                println("Message received from data")
//                println(it.toString())
                nTitle = it["title"].toString()
                nBody = it["body"].toString()
                nUserId = it["id_user"]
                nChannelId = it["id_channel"].toString()
                nVisitId = it["id_visit"].toString()
                nChannelName = it["channel_name"].toString()
                nChannelDescription = it["channel_description"].toString()
                nImageUrl = it["image_url"]
                nIntent = it["notification_intent"].toString()
            }

            if (!userId.isNullOrEmpty() && userId == nUserId
            ) {
                showNotification(nTitle, nBody)
            }
        } catch (e: Exception) {
            Log.d("FCM", "Error fcm received message exception: $e")
        }
    }

    private fun showNotification(title: String?, message: String?) {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(
                this,
                nChannelId,
                nChannelName
            )
        } else {
            ""
        }

        val nIntent = Intent(this, ReportsActivity::class.java).apply {
            putExtra(CONST_USER_ID, userId)
            putExtra(CONST_FULL_NAME, userFullName)
            putExtra(CONST_USER_LEVEL, AUTH_LEVEL_SALES)
            putExtra("notification_intent", nIntent)
            putExtra("nVisitId", nVisitId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            nIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (!nImageUrl.isNullOrEmpty()) {
            notificationBuilder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(Picasso.get().load(nImageUrl).get())
            )
        }

        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
//        manualRefreshToken()
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

    private fun manualRefreshToken() {

        try {

            FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Manual refresh token: Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                Log.d("FCM", "Manual refresh token: Token deleted!")

                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w("FCM", "Manual refresh token: Fetching FCM registration token failed", task.exception)
                        return@addOnCompleteListener
                    }

                    val token = task.result
                    Log.d("FCM", "Manual refresh token: $token")
                }
            }

        } catch (e: Exception) {
            Log.e("FCM", "Error manual refresh token. Exception: $e")
        }

    }

    override fun onNewToken(token: String) {
//        try {
//            if (sessionManager == null) sessionManager = SessionManager(this)
//            if (userId != null && userId.toString().isNotEmpty() && (userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN)) {
//                Log.d("FCM", "Refreshed token: $token")
//            }
//        } catch (e: Exception) {
//            Log.e("FCM", "Error fcm new token exception: $e")
//        }
    }
}
