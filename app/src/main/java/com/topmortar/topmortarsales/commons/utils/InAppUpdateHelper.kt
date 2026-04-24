package com.topmortar.topmortarsales.commons.utils

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

fun Context.inAppUpdateHelper(
    appUpdateManager: AppUpdateManager,
    launcher: ActivityResultLauncher<IntentSenderRequest>,
) {
    try {
        val options = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->

            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        launcher,
                        options,
                    )
                } catch (e: Exception) {
                    FirebaseUtils.logErr(this, "Failed to update app. Catch ${e.message}")
                    handleMessage(this, message = "Failed to update app. Catch ${e.message}")
                }
            }
        }.addOnFailureListener { e ->
            if (e.message?.contains("ERROR_APP_NOT_OWNED") == true) {
                FirebaseUtils.logErr(this, "Aplikasi tidak diinstall melalui Google Play")
                handleMessage(this, message = "Aplikasi tidak diinstall melalui Google Play")
            } else {
                FirebaseUtils.logErr(this, "Failed to update app. Err ${e.message}")
                handleMessage(this, message = "Failed to update app. Err ${e.message}")
            }
        }
    } catch (e: Exception) {
        FirebaseUtils.logErr(this, "Failed to update app. Main Catch ${e.message}")
        handleMessage(this, message = "Failed to update app. Main Catch ${e.message}")
    }
}