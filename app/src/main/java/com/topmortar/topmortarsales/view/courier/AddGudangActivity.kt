package com.topmortar.topmortarsales.view.courier

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.GET_COORDINATE
import com.topmortar.topmortarsales.commons.LOCATION_PERMISSION_REQUEST_CODE
import com.topmortar.topmortarsales.commons.REQUEST_EDIT_CONTACT_COORDINATE
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.PhoneHandler
import com.topmortar.topmortarsales.commons.utils.URLUtility
import com.topmortar.topmortarsales.databinding.ActivityAddGudangBinding
import com.topmortar.topmortarsales.view.MapsActivity

class AddGudangActivity : AppCompatActivity() {

    private var _binding: ActivityAddGudangBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        _binding = ActivityAddGudangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBar.tvTitleBar.text = "Tambah Daftar Gudang"
        binding.titleBar.icBack.setOnClickListener { finish() }
        binding.btnSubmit.setOnClickListener { submitForm() }

        setMapsAction()
    }

    private fun setMapsAction() {
        binding.etMapsUrl.setOnClickListener { getCoordinate() }
        binding.etMapsUrl.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                getCoordinate()
                binding.etMapsUrl.setSelection(binding.etMapsUrl.length())
            } else binding.etMapsUrl.clearFocus()
        }
    }

    private fun getCoordinate() {
        val data = "${ binding.etMapsUrl.text }"

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra(CONST_MAPS, data)
            intent.putExtra(GET_COORDINATE, true)
            startActivityForResult(intent, REQUEST_EDIT_CONTACT_COORDINATE)
        } else checkLocationPermission()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT_CONTACT_COORDINATE) {
            val latitude = data?.getDoubleExtra("latitude", 0.0)
            val longitude = data?.getDoubleExtra("longitude", 0.0)
            if (latitude != null && longitude != null) binding.etMapsUrl.setText("$latitude,$longitude")
            binding.etMapsUrl.error = null
            binding.etMapsUrl.clearFocus()
        }
    }

    private fun checkLocationPermission() {
        val urlUtility = URLUtility(this)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (urlUtility.isLocationEnabled(this)) {

                val urlUtility = URLUtility(this)
                urlUtility.requestLocationUpdate()

            } else {
                val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(enableLocationIntent)
            }
        } else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) checkLocationPermission()
            else {
                val customUtility = CustomUtility(this)
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    val message = "Izin lokasi diperlukan untuk fitur ini. Izinkan aplikasi mengakses lokasi perangkat."
                    customUtility.showPermissionDeniedSnackbar(message) { ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE) }
                } else customUtility.showPermissionDeniedDialog("Izin lokasi diperlukan untuk fitur ini. Harap aktifkan di pengaturan aplikasi.")
            }
        }
    }

    private fun submitForm() {
        if (!isValidForm()) return

        val loadingState = ProgressDialog(this)
        loadingState.setCancelable(false)
        loadingState.setMessage(getString(R.string.txt_saving))
        loadingState.show()

        Handler().postDelayed({
            finish()
            loadingState.dismiss()
        }, 1000)
    }

    private fun isValidForm(): Boolean {
        if (binding.etPhone.text.isNotEmpty()) {
            if (!PhoneHandler.phoneValidation(binding.etPhone.text.toString(), binding.etPhone)) {
                binding.etPhone.requestFocus()
                return false
            }
        }

        if (binding.etName.text.isNullOrEmpty()) {
            binding.etName.error = "Nama gudang wajib diisi!"
            binding.etName.requestFocus()
            return false
        } else if (binding.etMapsUrl.text.isNullOrEmpty()) {
            binding.etMapsUrl.error = "Koordinat gudang wajib diisi!"
            binding.etMapsUrl.requestFocus()
            return false
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}