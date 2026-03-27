@file:Suppress("MissingPermission")

package com.topmortar.topmortarsales.commons.services

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import android.util.Log
import com.google.android.gms.location.*
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.firebase.database.DatabaseReference
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_ABSENT
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_DELIVERY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.utils.CustomNotificationBuilder
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.view.SplashScreenActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Calendar

class TrackingService : Service() {

    companion object {
        var isRunning = false
        const val NOTIFICATION_ID = 1010
        private const val LOCATION_INTERVAL = 30 * 60 * 1000L // 30 menit
        private const val LOCATION_MIN_DISTANCE = 100f // 100 meter

        const val ACTION_STOP = "STOP_SERVICE"
        const val ACTION_UPDATE_LOCATION_NOW = "UPDATE_LOCATION_NOW"
        const val ACTION_TYPE_DEFAULT = "Default"
        const val ACTION_TYPE_VISIT = "Visit"
        const val ACTION_TYPE_CLOSING = "Closing"
    }

    private var userId : String? = null
    private var distributorId : String? = null
    private var deliveryId : String? = null

    private var lastApiUpdate = 0L

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var locationCallback: LocationCallback? = null

    private lateinit var firebaseReference: DatabaseReference
    private lateinit var childDelivery: DatabaseReference
    private lateinit var childAbsent: DatabaseReference
    private var childDriver: DatabaseReference? = null

    private val handler = Handler(Looper.getMainLooper())

    private var wakeLock: PowerManager.WakeLock? = null

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {

            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_UPDATE_LOCATION_NOW -> {
                requestSingleLocationUpdate(intent)
                return START_STICKY
            }
        }

        intent?.let {
            userId = it.getStringExtra("userId")
            distributorId = it.getStringExtra("userDistributorId")
            deliveryId = it.getStringExtra("deliveryId")
        }

        acquireWakeLock()
//        startForegroundService()
        startForeground(NOTIFICATION_ID, createNotification())
        startLocationUpdates(intent)
        scheduleServiceStop()

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
    }

    override fun onDestroy() {
        wakeLock?.release()
        serviceScope.cancel()
        stopLocationUpdates()
        handler.removeCallbacksAndMessages(null)
        isRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null


    // -------------------------
    // Wakelock Service
    // -------------------------

    private fun acquireWakeLock() {

        if (wakeLock?.isHeld == true) return

        val pm = getSystemService(POWER_SERVICE) as PowerManager

        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "TopMortar:TrackingWakeLock"
        )

        wakeLock?.acquire(15 * 60 * 1000L) // 15 menit
    }

    // -------------------------
    // Foreground Notification
    // -------------------------

//    private fun startForegroundService() {
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForeground(NOTIFICATION_ID, createNotification())
//        }
//    }

    private fun createNotification(): Notification {

        val intent = Intent(this, SplashScreenActivity::class.java)

        return CustomNotificationBuilder.with(this)
            .setIntent(intent)
            .setChannelId("topmortar_delivery_notification")
            .setChannelName("Topmortar Absent Notification")
            .setContentTitle("Kehadiran Sudah Tercatat")
            .setContentText("Pastikan untuk menyelesaikan target anda sebelum pulang...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOnGoing(true)
            .build()
    }


    // -------------------------
    // Location Setup
    // -------------------------

    private fun startLocationUpdates(intent: Intent?) {


        val uid = userId ?: return
        val distributor = distributorId ?: "-firebase-001"

        firebaseReference = FirebaseUtils.getReference(distributorId = distributor)
        childDelivery = firebaseReference.child(FIREBASE_CHILD_DELIVERY)
        childAbsent = firebaseReference.child(FIREBASE_CHILD_ABSENT).child(uid)

        if (!deliveryId.isNullOrEmpty()) {
            childDriver = childDelivery.child(deliveryId!!)
        }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_INTERVAL
        )
            .setMinUpdateIntervalMillis(LOCATION_INTERVAL / 2)
            .setMinUpdateDistanceMeters(LOCATION_MIN_DISTANCE)
            .build()

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(result: LocationResult) {

                result.lastLocation?.let {
                        updateAbsentLocation(it)
                        updateDriverLocation(it)
                        if (intent != null && shouldSendUpdate()) {
                            sendPositionToServer(it, intent)
                        }
                }

            }
        }

        if (!hasLocationPermission()) return

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }


    // -------------------------
    // Firebase Update
    // -------------------------

    private fun requestSingleLocationUpdate(intent: Intent) {

        if (!hasLocationPermission()) return

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->

            location?.let {

                updateAbsentLocation(it)
                updateDriverLocation(it)
                sendPositionToServer(it, intent)
            }
        }
    }

    private fun updateAbsentLocation(location: Location) {

        childAbsent.updateChildren(
            mapOf(
                "lat" to location.latitude,
                "lng" to location.longitude,
                "lastTracking" to DateFormat.now()
            )
        )
    }

    private fun updateDriverLocation(location: Location) {

        if (childDriver == null) return;
        childDriver?.updateChildren(
            mapOf(
                "lat" to location.latitude,
                "lng" to location.longitude
            )
        )
    }

    // -------------------------
    // Api Service
    // -------------------------

    private fun shouldSendUpdate(): Boolean {

        val now = System.currentTimeMillis()

        if (now - lastApiUpdate > (LOCATION_INTERVAL / 2)) { // 15 menit
            lastApiUpdate = now
            return true
        }

        return false
    }

    private fun sendPositionToServer(location: Location, intent: Intent) {

        val uid = intent.getStringExtra("userId") ?: userId ?: "0"
        val contactId = intent.getStringExtra("contactId") ?: "0"
        val actionType = intent.getStringExtra("actionType") ?: ACTION_TYPE_DEFAULT

        serviceScope.launch {

            try {

                val apiService = HttpClient.create()
                val response = apiService.savePosition(
                    idUser = createPartFromString(uid),
                    idContact = createPartFromString(contactId),
                    type = createPartFromString(actionType),
                    lat = createPartFromString(location.latitude.toString()),
                    lng = createPartFromString(location.longitude.toString())
                )

                if (!response.isSuccessful) {
                    Log.e("Save Tracking Location", response.message())
                    return@launch
                }

                val responseBody = response.body()

                when (responseBody?.status) {
                    RESPONSE_STATUS_OK -> {
                        Log.i("Save Tracking Location", responseBody.message)
                    } else -> {
                        Log.e("Save Tracking Location", responseBody?.message ?: "")
                    }
                }

            } catch (e: Exception) {
                Log.e("Save Tracking Location", e.message.toString())
                e.printStackTrace()
            }

        }
    }

    // -------------------------
    // Stop Updates
    // -------------------------

    private fun stopLocationUpdates() {

        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }

        locationCallback = null
    }


    // -------------------------
    // Permission Check
    // -------------------------

    private fun hasLocationPermission(): Boolean {

        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    // -------------------------
    // Auto Stop Service 22:00
    // -------------------------

    private fun scheduleServiceStop() {

        val now = System.currentTimeMillis()

        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis < now) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val delay = calendar.timeInMillis - now

        handler.postDelayed({
            stopSelf()
        }, delay)
    }
}