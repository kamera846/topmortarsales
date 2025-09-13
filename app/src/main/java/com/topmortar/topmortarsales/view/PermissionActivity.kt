package com.topmortar.topmortarsales.view

import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.utils.PermissionsHandler
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.databinding.ActivityPermissionBinding

class PermissionActivity : AppCompatActivity() {

    lateinit var binding: ActivityPermissionBinding
    private val locationResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        checkPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyMyEdgeToEdge()
        supportActionBar?.title = "Perizinan Lokasi"
        checkPermissions()

        binding.btnOpenLocationSettings.setOnClickListener {
            openSettings()
        }
        binding.switchLocation.setOnClickListener {
            if (!binding.switchLocation.isChecked) {
                binding.switchLocation.isChecked = true
            } else {
                turnOnGPS()
            }
        }
        binding.switchPermissions.setOnClickListener {
            if (!binding.switchPermissions.isChecked) {
                binding.switchPermissions.isChecked = true
            } else {
                openSettings()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.imgGuide.setImageResource(R.drawable.background_permissions)
            binding.textGuide.text = "Pada halaman perizinan:\n" +
                    "1. Tekan 'Permissions' (Perizinan/Izin)\n" +
                    "2. Pilih 'Location' (Lokasi)\n" +
                    "3. Pilih 'Allow all it' (Selalu izinkan)\n\n" +
                    "Setelah selesai, kembali ke aplikasi."
        } else {
            binding.imgGuide.setImageResource(R.drawable.permissions)
            binding.textGuide.text = "Pada halaman perizinan:\n" +
                    "1. Tekan 'Permissions' (Perizinan)\n" +
                    "2. Pilih 'Location' (Lokasi)\n\n" +
                    "Setelah selesai, kembali ke aplikasi."
        }
    }

    private fun checkPermissions() {

        val locationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isForegroundPermissionGranted = PermissionsHandler.hasForegroundLocationPermission(this)
        val isBackgroundPermissionGranted = PermissionsHandler.hasBackgroundLocationPermission(this)

        binding.switchLocation.isChecked = isGpsEnabled
        binding.switchPermissions.isChecked = isForegroundPermissionGranted && isBackgroundPermissionGranted

        if (isGpsEnabled && isForegroundPermissionGranted && isBackgroundPermissionGranted) {
            finish()
        }
    }

    private fun turnOnGPS() {
        locationResultLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        locationResultLauncher.launch(intent)
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }
}