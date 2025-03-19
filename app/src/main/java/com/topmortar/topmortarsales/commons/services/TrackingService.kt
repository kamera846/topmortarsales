@file:Suppress("DEPRECATION")

package com.topmortar.topmortarsales.commons.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
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
import java.util.Calendar

class TrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var locationCallback: LocationCallback? = null
    private lateinit var firebaseReference: DatabaseReference
    private lateinit var childDelivery: DatabaseReference
    private lateinit var childAbsent: DatabaseReference
    private lateinit var childDriver: DatabaseReference
    private var isLocationUpdating = false

    companion object {
        const val NOTIFICATION_ID = 1010
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startForegroundService()
        startLocationUpdates(intent)
        scheduleServiceStop()
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
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOnGoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startLocationUpdates(intent: Intent?) {

        val userId = intent?.getStringExtra("userId")
        val userDistributorId = intent?.getStringExtra("userDistributorId")
        val deliveryId = intent?.getStringExtra("deliveryId")

        firebaseReference = FirebaseUtils.getReference(distributorId = userDistributorId ?: "-firebase-001")
        childDelivery = firebaseReference.child(FIREBASE_CHILD_DELIVERY)
        childAbsent = firebaseReference.child(FIREBASE_CHILD_ABSENT).child(userId.toString())
        if (!deliveryId.isNullOrEmpty()) childDriver = childDelivery.child(deliveryId)

        if (isLocationUpdating) {

            stopLocationUpdates()
            startListeningLocation(deliveryId)
            return
        }

        startListeningLocation(deliveryId)
    }

    private fun startListeningLocation(deliveryId: String?) {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(3000)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                val userLocation = locationResult.lastLocation!!

                childAbsent.child("lat").setValue(userLocation.latitude)
                childAbsent.child("lng").setValue(userLocation.longitude)
                childAbsent.child("lastTracking").setValue(DateFormat.now())

                if (!deliveryId.isNullOrEmpty()) {
                    childDriver.child("lat").setValue(userLocation.latitude)
                    childDriver.child("lng").setValue(userLocation.longitude)
                }

            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) return
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())
        isLocationUpdating = true
    }

    private fun scheduleServiceStop() {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 22) // Set to 10 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis < now) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val stopTimeMillis = calendar.timeInMillis
        val delayMillis = stopTimeMillis - now

        Handler(Looper.getMainLooper()).postDelayed({
            stopSelf()
        }, delayMillis)
    }

    private fun stopLocationUpdates() {

        fusedLocationClient.removeLocationUpdates(locationCallback!!)
        locationCallback = null
        isLocationUpdating = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}