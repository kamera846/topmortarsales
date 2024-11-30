package com.topmortar.topmortarsales.commons.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.squareup.picasso.Picasso
import com.topmortar.topmortarsales.R

class CustomNotificationBuilder private constructor(private val context: Context) {
    private var channelId: String? = null
    private var channelName: String? = null
    private var autoCancel: Boolean = false
    private var onGoing: Boolean = true
    private var requestCode: Int = 0
    private var smallIconResId: Int = R.drawable.notification_icon
    private var largeIconResId: Int = R.mipmap.logo_topmortar_circle
    private var bigImageUrl: String? = null
    private var contentTitle: String? = null
    private var contentText: String? = null
    private var badgeIconType: Int? = NotificationCompat.BADGE_ICON_SMALL
    private var notificationIntent: Intent? = null

    fun setChannelId(channelId: String): CustomNotificationBuilder {
        this.channelId = channelId
        return this
    }

    fun setChannelName(channelName: String): CustomNotificationBuilder {
        this.channelName = channelName
        return this
    }

    fun setAutoCancel(autoCancel: Boolean): CustomNotificationBuilder {
        this.autoCancel = autoCancel
        return this
    }

    fun setOnGoing(onGoing: Boolean): CustomNotificationBuilder {
        this.onGoing = onGoing
        return this
    }

    fun setRequestCode(requestCode: Int): CustomNotificationBuilder {
        this.requestCode = requestCode
        return this
    }

    fun setSmallIcon(smallIconResId: Int): CustomNotificationBuilder {
        this.smallIconResId = smallIconResId
        return this
    }

    fun setLargeIcon(largeIconResId: Int): CustomNotificationBuilder {
        this.largeIconResId = largeIconResId
        return this
    }

    fun setBigImageUrl(bigImageUrl: String?): CustomNotificationBuilder {
        this.bigImageUrl = bigImageUrl
        return this
    }

    fun setContentTitle(contentTitle: String): CustomNotificationBuilder {
        this.contentTitle = contentTitle
        return this
    }

    fun setContentText(contentText: String): CustomNotificationBuilder {
        this.contentText = contentText
        return this
    }

    fun setBadgeIconType(badgeIconType: Int): CustomNotificationBuilder {
        this.badgeIconType = badgeIconType
        return this
    }

    fun setIntent(notificationIntent: Intent): CustomNotificationBuilder {
        this.notificationIntent = notificationIntent
        return this
    }

    fun build(): Notification {
        val bChannelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(context, channelId ?: "topmortar_notifications", channelName ?: "Topmortar Notifications")
            } else {
                ""
            }

        val largeIconBitmap = BitmapFactory.decodeResource(context.resources, largeIconResId)
        val notificationBuilder = NotificationCompat.Builder(context, bChannelId)

        if (notificationIntent != null) {
            val pendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            notificationBuilder.setContentIntent(pendingIntent)
        }

        if (!contentTitle.isNullOrEmpty()) notificationBuilder.setContentTitle(contentTitle)
        if (!contentText.isNullOrEmpty()) notificationBuilder.setContentText(contentText)

        notificationBuilder.setOngoing(onGoing).apply {
            setSmallIcon(smallIconResId)
            setLargeIcon(largeIconBitmap)
            setPriority(NotificationManager.IMPORTANCE_DEFAULT)
            setCategory(Notification.CATEGORY_SERVICE)
            setAutoCancel(true)
            setChannelId(bChannelId)
            setChannelName(channelName ?: "Topmortar Notifications")
            setBadgeIconType(badgeIconType ?: NotificationCompat.BADGE_ICON_SMALL)
            if (!bigImageUrl.isNullOrEmpty()) {
                setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(Picasso.get().load(bigImageUrl).get())
                )
            }
        }

        return notificationBuilder.build()
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

    companion object {
        fun with(context: Context): CustomNotificationBuilder {
            return CustomNotificationBuilder(context)
        }
    }
}
