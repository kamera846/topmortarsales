package com.topmortar.topmortarsales.commons.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
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
import com.topmortar.topmortarsales.view.SplashScreenActivity

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
    private var nGroupId: String = "report_feedback"
    private var nGroupName: String = "Grup Notifikasi Umum"
    private var nChannelName: String = "Notifikasi Umum"
    private var nChannelDescription: String = "Notifikasi untuk kategori umum"
    private var soundUri: Uri? = null

    private var notificationId: Int = 2020

    companion object {
//        const val NOTIFICATION_ID = 2020
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        try {
            if (sessionManager == null) sessionManager = SessionManager(this)
            if (soundUri == null) soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            notificationId = System.currentTimeMillis().toInt()
//            println("notificationData: $remoteMessage")
//            println("notificationData: $notificationId")
            remoteMessage.data.let {
                nTitle = it["title"].toString()
                nBody = it["body"].toString()
                nUserId = it["id_user"]
                nVisitId = it["id_visit"].toString()
                nImageUrl = it["image_url"]
                nChannelId = it["id_channel"].toString()
                nChannelName = it["channel_name"].toString()
                nGroupId = it["id_group"].toString()
                nGroupName = it["group_name"].toString()
                nChannelDescription = it["channel_description"].toString()
                nIntent = it["notification_intent"].toString()
            }

            if (nChannelId == "report_feedback") soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.notification_sound)

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

        val nIntent = Intent(this, SplashScreenActivity::class.java).apply {
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

        val notificationBuilder = NotificationCompat.Builder(this, channelId).apply {
            setSmallIcon(R.drawable.notification_icon)
            setContentTitle(title)
            setContentText(message)
            setContentIntent(pendingIntent)
            setAutoCancel(true)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setSound(soundUri)
            setGroup(nGroupId)
            if (!nImageUrl.isNullOrEmpty()) {
                setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(Picasso.get().load(nImageUrl).get())
                )
            }
        }

        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
//        manualRefreshToken()
    }

    private fun createNotificationChannel(
        context: Context,
        channelId: String,
        channelName: String
    ): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channelGroup = NotificationChannelGroup(nGroupId, nGroupName)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                description = nChannelDescription
                lightColor = Color.RED
//                group = nGroupId
                enableLights(true)
                enableVibration(true)
                setSound(soundUri, audioAttributes)
            }

            val service =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(channel)
//            service.createNotificationChannelGroup(channelGroup)
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
