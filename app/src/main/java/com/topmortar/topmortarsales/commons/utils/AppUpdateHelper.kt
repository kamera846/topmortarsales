package com.topmortar.topmortarsales.commons.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

object AppUpdateHelper {

    fun checkForUpdates(context: Context) {
        val appUpdateManager = AppUpdateManagerFactory.create(context)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                showUpdateDialog(context)
            }
        }
    }

    private fun showUpdateDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Update Required")
            .setMessage("A new version of the app is available. Please update to the latest version.")
            .setCancelable(false)
            .setPositiveButton("Update") { dialog, _ ->
                openPlayStore(context)
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun openPlayStore(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
        context.startActivity(intent)
    }
}
