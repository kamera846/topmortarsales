@file:Suppress("DEPRECATION")

package com.topmortar.topmortarsales.commons.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DatabaseReference
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_ABSENT
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_DELIVERY
import com.topmortar.topmortarsales.commons.utils.CustomNotificationBuilder
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.view.SplashScreenActivity

class TrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var locationCallback: LocationCallback? = null
    private lateinit var firebaseReference: DatabaseReference
    private lateinit var childDelivery: DatabaseReference
    private lateinit var childAbsent: DatabaseReference
    private lateinit var childDriver: DatabaseReference

    companion object {
        const val NOTIFICATION_ID = 1010
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Log.d("Tracking Service", "On Start Service")
        startForegroundService()
        startLocationUpdates(intent)
        return START_STICKY
    }

    override fun onDestroy() {
        stopLocationUpdates()
        super.onDestroy()
    }

    private fun startForegroundService() {
        val notificationIntent = Intent(this, SplashScreenActivity::class.java)
        val notification = CustomNotificationBuilder.with(this)
            .setIntent(notificationIntent)
            .setChannelId("topmortar_delivery_notification")
            .setChannelName("Topmortar Absent Notification")
            .setContentTitle("Kehadiran Sudah Tercatat")
            .setContentText("Pastikan untuk menyelesaikan target anda sebelum pulang...")
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startLocationUpdates(intent: Intent?) {

        val userId = intent?.getStringExtra("userId")
        val userDistributorId = intent?.getStringExtra("userDistributorId")
        val deliveryId = intent?.getStringExtra("deliveryId").toString()

        firebaseReference = FirebaseUtils().getReference(distributorId = userDistributorId ?: "-firebase-001")
        childDelivery = firebaseReference.child(FIREBASE_CHILD_DELIVERY)
        childAbsent = firebaseReference.child(FIREBASE_CHILD_ABSENT).child(userId.toString())
        childDriver = childDelivery.child(deliveryId)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(3000)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                val driverLocation = locationResult.lastLocation!!
                childDriver.child("lat").setValue(driverLocation.latitude)
                childDriver.child("lng").setValue(driverLocation.longitude)

                childAbsent.child("lat").setValue(driverLocation.latitude)
                childAbsent.child("lng").setValue(driverLocation.longitude)
                childAbsent.child("lastTracking").setValue(DateFormat.now())

            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) return
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback!!)
        if (locationCallback != null) locationCallback = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}