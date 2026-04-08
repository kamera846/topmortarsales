package com.topmortar.topmortarsales.commons.workers

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_ABSENT
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_DELIVERY
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.data.HttpClient
import kotlinx.coroutines.tasks.await

class BackupLocationUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val HEARTBEAT_TIME_LIMIT = 15 * 60 * 1000L // 15 Menit
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override suspend fun doWork(): Result {

        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(applicationContext)

        return try {

            val location = fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .await()

            val pref = applicationContext.getSharedPreferences("tracking_prefs", Context.MODE_PRIVATE)
            val lastHeartbeat = pref.getLong("lastHeartbeat", 0L)
            val now = System.currentTimeMillis()

            // Jika Service baru saja update (<15 menit), skip worker API
            if (now - lastHeartbeat < HEARTBEAT_TIME_LIMIT) {
                return Result.success() // Service masih jalan, tidak perlu kirim
            }

            location?.let {
                val sharedPref = applicationContext.getSharedPreferences(
                    "tracking_prefs",
                    Context.MODE_PRIVATE
                )

                val userId = sharedPref.getString("userId", "0") ?: "0"
                val contactId = sharedPref.getString("contactId", "0") ?: "0"

                // Update Firebase
                updateFirebase(it, sharedPref)

                val apiService = HttpClient.create()

                val response = apiService.savePosition(
                    idUser = createPartFromString(userId),
                    idContact = createPartFromString(contactId),
                    type = createPartFromString("Default-Worker"),
                    lat = createPartFromString(it.latitude.toString()),
                    lng = createPartFromString(it.longitude.toString())
                )

                if (!response.isSuccessful) {
                    return Result.retry()
                }
            }

            Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun updateFirebase(location: Location, pref: SharedPreferences) {

        val userId = pref.getString("userId", null) ?: return
        val distributorId = pref.getString("distributorId", null)
        val deliveryId = pref.getString("deliveryId", null)

        val firebaseReference = FirebaseUtils.getReference(distributorId = distributorId ?: "-firebase-001")

        val childAbsent = firebaseReference
            .child(FIREBASE_CHILD_ABSENT)
            .child(userId)

        val childDelivery = firebaseReference.child(FIREBASE_CHILD_DELIVERY)

        val childDriver = deliveryId?.let {
            childDelivery.child(it)
        }

        // update absent
        childAbsent.updateChildren(
            mapOf(
                "lat" to location.latitude,
                "lng" to location.longitude,
                "lastTracking" to DateFormat.now()
            )
        )

        // update driver
        childDriver?.updateChildren(
            mapOf(
                "lat" to location.latitude,
                "lng" to location.longitude
            )
        )
    }
}