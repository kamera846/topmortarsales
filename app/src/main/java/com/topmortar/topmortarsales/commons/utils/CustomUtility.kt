package com.topmortar.topmortarsales.commons.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_ABSENT
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.USER_KIND_MARKETING
import com.topmortar.topmortarsales.commons.USER_KIND_PENAGIHAN
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import java.util.Calendar

class CustomUtility(private val context: Context) {
    private lateinit var sessionManager: SessionManager

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

    fun setUserStatusOnline(status: Boolean, userDistributorId: String, userId: String) {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR).toString().padStart(2, '0')
        val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val day = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')

        val hour = calendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val minute = calendar.get(Calendar.MINUTE).toString().padStart(2, '0')
        val second = calendar.get(Calendar.SECOND).toString().padStart(2, '0')

        val firebaseReference = FirebaseUtils.getReference(distributorId = userDistributorId)
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

    fun navigateChatAdmin(message: String, distributorNumber: String) {
        val phoneNumber = distributorNumber.ifEmpty { context.getString(R.string.topmortar_wa_number) }

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}")

        try {
            val context = (context as Activity)
            context.startActivity(intent)
            context.finishAffinity()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context as Activity, "Gagal menghubungkan ke whatsapp", TOAST_SHORT).show()
        }

    }

    fun getInitials(fullName: String, length: Int = 2): String {
        return if (fullName.isNotEmpty()) {
            val cleanFullName = fullName.trim()
            val names = cleanFullName.split(" ")
            var initials = ""
            for ((i, name) in names.withIndex()) {
                if (i < length) initials += name[0]
            }
            initials
        } else ""
    }

    fun isUserWithOnlineStatus(): Boolean {
        sessionManager = SessionManager(context)
        return sessionManager.userKind() == USER_KIND_COURIER || sessionManager.userKind() == USER_KIND_SALES || sessionManager.userKind() == USER_KIND_PENAGIHAN || sessionManager.userKind() == USER_KIND_MARKETING
    }
}