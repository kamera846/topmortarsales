package com.topmortar.topmortarsales.commons.services

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.startTrackingService(
    userId: String? = null,
    distributorId: String? = null,
    deliveryId: String? = null
) {
    if (!hasLocationPermission(this)) {
        stopTrackingService()
        return
    }

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

    if (!hasLocationPermission(this)) {
        stopTrackingService()
        return
    }

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
    if (!hasLocationPermission(this)) {
        stopTrackingService()
        return
    }

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

    stopService(intent);
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        startForegroundService(intent)
//    } else {
//        startService(intent)
//    }
}