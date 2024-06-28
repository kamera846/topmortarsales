package com.topmortar.topmortarsales.commons.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.LATEST_APP_VERSION
import kotlin.system.exitProcess

object AppUpdateHelper {

    private lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    fun initialize() {
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        firebaseRemoteConfig.setDefaultsAsync(R.xml.default_app_version)
    }

    fun checkForUpdate(activity: Activity, onUpToDate: () -> Unit = {}) {
        fetchRemoteConfig {
            val currentVersion = getCurrentVersionCode(activity)
            val latestVersion = firebaseRemoteConfig.getLong(LATEST_APP_VERSION)
            Log.d("Check Apps Update", "$currentVersion, $latestVersion")
            if (currentVersion < latestVersion) {
                showUpdateDialog(activity)
            } else {
                onUpToDate()
            }
        }
    }

    private fun fetchRemoteConfig(onComplete: () -> Unit) {
        firebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d("UpdateChecker", "Config params updated: $updated")
                } else {
                    Log.e("UpdateChecker", "Fetch failed")
                }
                onComplete()
            }
    }

    private fun getCurrentVersionCode(activity: Activity): Int {
        return try {
            val packageInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
            packageInfo.versionCode
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    private fun showUpdateDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Update Diperlukan")
            .setMessage("Versi baru aplikasi telah tersedia. Harap perbarui ke versi terbaru.")
            .setPositiveButton("Perbarui Sekarang") { _, _ ->
                // Arahkan pengguna ke Google Play Store
                val appPackageName = activity.packageName
                try {
                    activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
                } catch (anfe: android.content.ActivityNotFoundException) {
                    activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
                }
            }
            .setNegativeButton("Nanti dulu") { _, _ ->
                activity.finishAffinity()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    activity.finishAndRemoveTask()
                }
                exitProcess(0)
            }
            .setCancelable(false)
            .show()
    }
}
