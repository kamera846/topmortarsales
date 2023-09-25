package com.topmortar.topmortarsales.commons.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import com.google.android.material.snackbar.Snackbar

class CustomUtility(private val context: Context) {
    fun showPermissionDeniedSnackbar(message: String, actionTitle: String = "Try Again", unit: () -> Unit) {
        Snackbar.make(
            (context as Activity).findViewById(android.R.id.content), // Replace with your root view
            message,
            Snackbar.LENGTH_LONG
        )
            .setAction(actionTitle) { unit() }
            .show()
    }

    fun showPermissionDeniedDialog(message: String) {
        AlertDialog.Builder(context)
            .setTitle("Permission Required")
            .setMessage(message)
            .setPositiveButton("App Settings") { _, _ -> openAppSettings() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", context.packageName, null)
        context.startActivity(intent)
    }
}