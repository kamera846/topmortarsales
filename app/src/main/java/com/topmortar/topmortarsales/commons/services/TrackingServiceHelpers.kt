package com.topmortar.topmortarsales.commons.services

import android.content.Context
import android.content.Intent
import android.os.Build

fun Context.startTrackingService(
    userId: String? = null,
    distributorId: String? = null,
    deliveryId: String? = null
) {

    if (TrackingService.isRunning) return

    val intent = Intent(this, TrackingService::class.java).apply {
        putExtra("userId", userId ?: "0")
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

fun Context.saveTrackingServiceLocation(
    userId: String,
    contactId: String,
    actionType: String,
) {

    val intent = Intent(this, TrackingService::class.java).apply {
        action = TrackingService.ACTION_UPDATE_LOCATION_NOW
        putExtra("userId", userId)
        putExtra("contactId", contactId)
        putExtra("actionType", actionType)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}

fun Context.stopTrackingService() {

    if (!TrackingService.isRunning) return

    val intent = Intent(this, TrackingService::class.java).apply {
        action = TrackingService.ACTION_STOP
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}