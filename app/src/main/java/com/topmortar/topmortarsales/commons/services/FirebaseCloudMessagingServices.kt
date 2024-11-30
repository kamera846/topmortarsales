package com.topmortar.topmortarsales.commons.services

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_SALES
import com.topmortar.topmortarsales.commons.CONST_FULL_NAME
import com.topmortar.topmortarsales.commons.CONST_USER_ID
import com.topmortar.topmortarsales.commons.CONST_USER_LEVEL
import com.topmortar.topmortarsales.commons.utils.CustomNotificationBuilder
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

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        try {
            if (sessionManager == null) sessionManager = SessionManager(this)
            if (soundUri == null) soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            notificationId = System.currentTimeMillis().toInt()

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

            if (nChannelId == "report_feedback") soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.feedback_notification_sound)

            if (!userId.isNullOrEmpty() && userId == nUserId
            ) {
                showNotification(nTitle, nBody)
            }
        } catch (e: Exception) {
            Log.d("FCM", "Error fcm received message exception: $e")
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationIntent = Intent(this, SplashScreenActivity::class.java).apply {
            putExtra(CONST_USER_ID, userId)
            putExtra(CONST_FULL_NAME, userFullName)
            putExtra(CONST_USER_LEVEL, AUTH_LEVEL_SALES)
            putExtra("notification_intent", nIntent)
            putExtra("nVisitId", nVisitId)
        }
        val notification = CustomNotificationBuilder.with(this)
            .setIntent(notificationIntent)
            .setChannelId(nChannelId)
            .setChannelName(nChannelName)
            .setRequestCode(notificationId)
            .setSound(soundUri!!)
            .setGroup(nGroupId)
            .setAutoCancel(true)
            .setContentTitle(title)
            .setContentText(message)
            .setChannelDescription(nChannelDescription)
            .setBigImageUrl(nImageUrl)

        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(notificationId, notification.build())
    }

    override fun onNewToken(token: String) {
    }
}
