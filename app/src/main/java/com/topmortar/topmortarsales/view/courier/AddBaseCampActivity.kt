package com.topmortar.topmortarsales.view.courier

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.GET_COORDINATE
import com.topmortar.topmortarsales.commons.LOCATION_PERMISSION_REQUEST_CODE
import com.topmortar.topmortarsales.commons.REQUEST_BASECAMP_FRAGMENT
import com.topmortar.topmortarsales.commons.REQUEST_EDIT_CONTACT_COORDINATE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.RESULT_BASECAMP_FRAGMENT
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.PhoneHandler
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.URLUtility
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityAddBaseCampBinding
import com.topmortar.topmortarsales.view.MapsActivity
import kotlinx.coroutines.launch

class AddBaseCampActivity : AppCompatActivity() {

    private var _binding: ActivityAddBaseCampBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val userCityID get() = sessionManager.userCityID()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        _binding = ActivityAddBaseCampBinding.inflate(layoutInflater)
        sessionManager = SessionManager(this)
        setContentView(binding.root)

        binding.titleBar.tvTitleBar.text = "Buat Basecamp Baru"
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

        val phone = "${ binding.etPhone.text }"
        val name = "${ binding.etName.text }"
        val cityId = userCityID.let { if (!it.isNullOrEmpty()) it else "0" }
        val mapsUrl = "${ binding.etMapsUrl.text }"

        lifecycleScope.launch {
            try {

                val rbPhone = createPartFromString(PhoneHandler.formatPhoneNumber(phone))
                val rbName = createPartFromString(name)
                val rbLocation = createPartFromString(cityId)
                val rbMapsUrl = createPartFromString(mapsUrl)

                val apiService: ApiService = HttpClient.create()
                val response = if (phone.isNullOrEmpty()) {
                    apiService.addBaseCamp(
                        name = rbName,
                        cityId = rbLocation,
                        mapsUrl = rbMapsUrl
                    )
                } else {
                    apiService.addBaseCamp(
                        name = rbName,
                        phone = rbPhone,
                        cityId = rbLocation,
                        mapsUrl = rbMapsUrl
                    )
                }

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        handleMessage(this@AddBaseCampActivity, TAG_RESPONSE_MESSAGE, "Berhasil menyimpan")

                        val resultIntent = Intent()
                        resultIntent.putExtra(REQUEST_BASECAMP_FRAGMENT, "new_basecamp")
                        setResult(RESULT_BASECAMP_FRAGMENT, resultIntent)
                        finish()
                        loadingState.dismiss()

                    }
                    RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                        handleMessage(this@AddBaseCampActivity, TAG_RESPONSE_MESSAGE, "Gagal menyimpan basecamp: ${ response.message }")
                        loadingState.dismiss()

                    }
                    else -> {

                        handleMessage(this@AddBaseCampActivity, TAG_RESPONSE_MESSAGE, "Gagal menyimpan!")
                        loadingState.dismiss()

                    }
                }


            } catch (e: Exception) {

                handleMessage(this@AddBaseCampActivity, TAG_RESPONSE_MESSAGE, "Failed run service. Exception " + e.message)
                loadingState.dismiss()

            }

        }
    }

    private fun isValidForm(): Boolean {
        if (binding.etPhone.text.isNotEmpty()) {
            if (!PhoneHandler.phoneValidation(binding.etPhone.text.toString(), binding.etPhone)) {
                binding.etPhone.requestFocus()
                return false
            }
        }

        if (binding.etName.text.isNullOrEmpty()) {
            binding.etName.error = "Nama basecamp wajib diisi!"
            binding.etName.requestFocus()
            return false
        } else if (binding.etMapsUrl.text.isNullOrEmpty()) {
            binding.etMapsUrl.error = "Koordinat basecamp wajib diisi!"
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