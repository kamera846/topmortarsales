package com.topmortar.topmortarsales.commons.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_ABSENT
import java.util.Calendar

class CustomUtility(private val context: Context) {
    fun showPermissionDeniedSnackbar(message: String, actionTitle: String = "Coba Lagi", unit: () -> Unit) {
        Snackbar.make(
            (context as Activity).findViewById(android.R.id.content), // Replace with your root view
            message,
            Snackbar.LENGTH_LONG
        )
            .setAction(actionTitle) { unit() }
            .show()
    }

    fun showPermissionDeniedDialog(message: String, title: String = "Izin Diperlukan", unit: (() -> Unit)? = null) {
        val openSettings = context.getString(R.string.open_settings)
        val textCancel = context.getString(R.string.cancel)
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(openSettings) { _, _ -> if (unit != null) unit() else openAppSettings() }
            .setNegativeButton(textCancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    fun showDialog(title: String = "Peringatan!", message: String, positiveText: String = "Oke") {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", context.packageName, null)
        context.startActivity(intent)
    }

    fun isDarkMode(): Boolean {
        return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager?.getRunningServices(Int.MAX_VALUE) ?: emptyList()) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun setUserStatusOnline(status: Boolean, userDistributorId: String, userId: String) {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR).toString().padStart(2, '0')
        val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val day = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')

        val hour = calendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val minute = calendar.get(Calendar.MINUTE).toString().padStart(2, '0')
        val second = calendar.get(Calendar.SECOND).toString().padStart(2, '0')

        val firebaseReference = FirebaseUtils().getReference(distributorId = userDistributorId)
        val userChild = firebaseReference.child("$FIREBASE_CHILD_ABSENT/$userId")
        userChild.child("isOnline").setValue(status)
        userChild.child("lastSeen").setValue("$year-$month-$day $hour:$minute:$second")
    }


    fun latLngConverter(stringLatLng: String): LatLng? {
        return if (stringLatLng.isNotEmpty()) {

            val urlUtility = URLUtility(context)

            if (!urlUtility.isUrl(stringLatLng)) {

                val coordinates = stringLatLng.trim().split(",")
                return if (coordinates.size == 2) {
                    val latitude = coordinates[0].toDoubleOrNull()
                    val longitude = coordinates[1].toDoubleOrNull()

                    if (latitude != null && longitude != null) {
                        LatLng(latitude, longitude)
                    } else {
                        handleMessage(context, "latLngConverter", "Null latitude or longitude")
                        null
                    }
                } else {
                    handleMessage(context, "latLngConverter", "Failed processed coordinate")
                    null
                }
            } else {
                handleMessage(context, "latLngConverter", "Wrong coordinate value")
                null
            }

        } else {
            handleMessage(context, "latLngConverter", "Parameter is empty")
            null
        }
    }
}