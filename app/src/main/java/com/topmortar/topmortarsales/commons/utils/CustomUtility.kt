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
    fun showPermissionDeniedSnackbar(message: String, actionTitle: String = "Coba Lagi", unit: () -> Unit) {
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
            .setTitle("Izin Diperlukan")
            .setMessage(message)
            .setPositiveButton("Pengaturan aplikasi") { _, _ -> openAppSettings() }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
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
}