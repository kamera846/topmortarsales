package com.topmortar.topmortarsales.commons.services

import android.content.Context
import android.content.Intent
import android.os.Build

fun Context.startTrackingService(
    userId: String? = null,
    distributorId: String? = null,
    deliveryId: String? = null
) {

    val intent = Intent(this, TrackingService::class.java).apply {
        putExtra("userId", userId ?: "anonymous")
        putExtra("userDistributorId", distributorId ?: "-start-service-${userId}")

        if (!deliveryId.isNullOrEmpty()) {
            putExtra("deliveryId", deliveryId)
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}

fun Context.updateTrackingServiceNow() {

    val intent = Intent(this, TrackingService::class.java).apply {
        action = TrackingService.ACTION_UPDATE_LOCATION_NOW
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}

fun Context.stopTrackingService() {

    val intent = Intent(this, TrackingService::class.java).apply {
        action = TrackingService.ACTION_STOP
    }

    startService(intent)
}