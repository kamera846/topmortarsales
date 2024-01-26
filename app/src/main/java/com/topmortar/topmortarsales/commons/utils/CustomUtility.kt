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
import com.google.android.material.snackbar.Snackbar
import com.topmortar.topmortarsales.R

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
}