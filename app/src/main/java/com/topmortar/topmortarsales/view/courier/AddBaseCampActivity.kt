@file:Suppress("DEPRECATION")

package com.topmortar.topmortarsales.view.courier

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.ABSENT_MODE_BASECAMP
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.EDIT_CONTACT
import com.topmortar.topmortarsales.commons.GET_COORDINATE
import com.topmortar.topmortarsales.commons.LOCATION_PERMISSION_REQUEST_CODE
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.REQUEST_BASECAMP_FRAGMENT
import com.topmortar.topmortarsales.commons.REQUEST_EDIT_CONTACT_COORDINATE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.RESULT_BASECAMP_FRAGMENT
import com.topmortar.topmortarsales.commons.SELECTED_ABSENT_MODE
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.USER_KIND_PENAGIHAN
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.PhoneHandler
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.URLUtility
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityAddBaseCampBinding
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.CityModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import com.topmortar.topmortarsales.view.MapsActivity
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
class AddBaseCampActivity : AppCompatActivity() {

    private var _binding: ActivityAddBaseCampBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val userID get() = sessionManager.userID()
    private val userCityID get() = sessionManager.userCityID()
    private val userKind get() = sessionManager.userKind()
    private val userDistributorId get() = sessionManager.userDistributor().toString()
    private val userDistributorIds get() = sessionManager.userDistributor()
    private lateinit var searchModal: SearchModal
    private var selectedCity: ModalSearchModel? = null
    private var citiesResults: ArrayList<CityModel> = ArrayList()
    private var iCities: String? = null
    private var isEdit = false
    private var name = ""
    private var idBasecamp = "-1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        _binding = ActivityAddBaseCampBinding.inflate(layoutInflater)
        sessionManager = SessionManager(this)
        setContentView(binding.root)

        isEdit = intent.getBooleanExtra(EDIT_CONTACT, false)
        if (isEdit) setDataEdit()

        binding.titleBar.tvTitleBar.text = if (isEdit) "Edit Basecamp" else "Buat Basecamp Baru"
        binding.titleBar.icBack.setOnClickListener { finish() }
        binding.btnSubmit.setOnClickListener { submitForm() }

        if (userKind == USER_KIND_ADMIN) {
//            binding.titleBar.icTrash.visibility = View.VISIBLE
//            binding.titleBar.icTrash.setOnClickListener { deleteValidation() }
            setCitiesOption()
        } else if (
                userKind == USER_KIND_COURIER || sessionManager.userKind() == USER_KIND_COURIER ||
                userKind == USER_KIND_SALES || sessionManager.userKind() == USER_KIND_SALES ||
                userKind == USER_KIND_PENAGIHAN || sessionManager.userKind() == USER_KIND_PENAGIHAN
            ) {
                CustomUtility(this).setUserStatusOnline(true, userDistributorIds ?: "-custom-006", "$userID")
        }
        setMapsAction()
    }

    private fun setDataEdit() {
        idBasecamp = intent.getStringExtra(CONST_CONTACT_ID).toString()
        name = intent.getStringExtra(CONST_NAME).toString()

        binding.etPhone.setText(intent.getStringExtra(CONST_PHONE))
        binding.etName.setText(name)
        binding.etMapsUrl.setText(intent.getStringExtra(CONST_MAPS))
    }

    private fun setCitiesOption() {

        binding.citiesOptionContainer.visibility = View.VISIBLE

        iCities = intent.getStringExtra(CONST_LOCATION)
        if (!iCities.isNullOrEmpty()) binding.etCityOption.setText(getString(R.string.txt_loading))
        else binding.etCityOption.setText("")

        binding.etCityOption.setOnClickListener { showSearchModal() }
        binding.etCityOption.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showSearchModal()
                binding.etCityOption.setSelection(binding.etCityOption.length())
            } else binding.etCityOption.clearFocus()
        }

        // Setup Dialog Search
        setupDialogSearch()
        getCities()

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

    @Deprecated("Deprecated in Java")
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
        val cityId = when (userKind) {
            USER_KIND_ADMIN -> selectedCity?.id!!
            else -> userCityID.let { if (!it.isNullOrEmpty()) it else "0" }
        }
        val mapsUrl = "${ binding.etMapsUrl.text }"

        lifecycleScope.launch {
            try {

                val rbPhone = createPartFromString(PhoneHandler.formatPhoneNumber(phone))
                val rbName = createPartFromString(name)
                val rbLocation = createPartFromString(cityId)
                val rbMapsUrl = createPartFromString(mapsUrl)
                val rbIdBasecamp = createPartFromString(idBasecamp)
                val rbDistributorId = createPartFromString(userDistributorId)

                val apiService: ApiService = HttpClient.create()
                val response = if (isEdit) {
                    apiService.editBaseCamp(
                        name = rbName,
                        phone = rbPhone,
                        cityId = rbLocation,
                        mapsUrl = rbMapsUrl,
                        idBasecamp = rbIdBasecamp,
                        distributorID = rbDistributorId
                    )
                } else {
                    if (phone.isEmpty()) {
                        apiService.addBaseCamp(
                            name = rbName,
                            cityId = rbLocation,
                            mapsUrl = rbMapsUrl,
                            distributorID = rbDistributorId
                        )
                    } else {
                        apiService.addBaseCamp(
                            name = rbName,
                            phone = rbPhone,
                            cityId = rbLocation,
                            mapsUrl = rbMapsUrl,
                            distributorID = rbDistributorId
                        )
                    }
                }

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        handleMessage(this@AddBaseCampActivity, TAG_RESPONSE_MESSAGE, "Berhasil menyimpan")

                        val resultIntent = Intent()
                        resultIntent.putExtra(REQUEST_BASECAMP_FRAGMENT, SYNC_NOW)
                        resultIntent.putExtra("$MAIN_ACTIVITY_REQUEST_CODE", SYNC_NOW)
                        resultIntent.putExtra(SELECTED_ABSENT_MODE, ABSENT_MODE_BASECAMP)
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
        } else if (userKind == USER_KIND_ADMIN && binding.etCityOption.text.isNullOrEmpty()) {
            binding.etCityOption.error = "Kota wajib dipilih!"
            binding.etCityOption.requestFocus()
            return false
        } else if (binding.etMapsUrl.text.isNullOrEmpty()) {
            binding.etMapsUrl.error = "Koordinat basecamp wajib diisi!"
            binding.etMapsUrl.requestFocus()
            return false
        }
        return true
    }

    private fun getCities() {

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getCities(distributorID = userDistributorId)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        citiesResults = response.results
                        val items: ArrayList<ModalSearchModel> = ArrayList()

                        for (i in 0 until citiesResults.size) {
                            val data = citiesResults[i]
                            items.add(ModalSearchModel(data.id_city, "${data.nama_city} - ${data.kode_city}"))
                        }

                        setupDialogSearch(items)

                        val foundItem = citiesResults.find { it.id_city == iCities }
                        if (foundItem != null) {
                            binding.etCityOption.setText("${foundItem.nama_city} - ${foundItem.kode_city}")
                            selectedCity = ModalSearchModel(foundItem.id_city, foundItem.nama_city)
                        } else binding.etCityOption.setText("")

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(this@AddBaseCampActivity, "LIST CITY", "Daftar kota kosong!")

                    }
                    else -> {

                        handleMessage(this@AddBaseCampActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))

                    }
                }


            } catch (e: Exception) {

                handleMessage(this@AddBaseCampActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)

            }

        }
    }

    private fun setupDialogSearch(items: ArrayList<ModalSearchModel> = ArrayList()) {

        searchModal = SearchModal(this, items)
        searchModal.setCustomDialogListener(object: SearchModal.SearchModalListener{
            override fun onDataReceived(data: ModalSearchModel) {
                binding.etCityOption.setText(data.title)
                selectedCity = data
            }

        })
        searchModal.searchHint = "Masukkan nama kota…"
        searchModal.setOnDismissListener {
            binding.etCityOption.clearFocus()
        }

    }

    private fun showSearchModal() {
        val searchKey = binding.etCityOption.text.toString()
        if (searchKey.isNotEmpty()) searchModal.setSearchKey(searchKey)
        searchModal.show()
    }

    override fun onStart() {
        super.onStart()
        Handler(Looper.getMainLooper()).postDelayed({
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(true, userDistributorIds ?: "-custom-006", "$userID")
            }
        }, 1000)
    }

    override fun onStop() {
        super.onStop()
        if (CustomUtility(this).isUserWithOnlineStatus()) {
            CustomUtility(this).setUserStatusOnline(false, userDistributorIds ?: "-custom-006", "$userID")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        if (CustomUtility(this).isUserWithOnlineStatus()) {
            CustomUtility(this).setUserStatusOnline(false, userDistributorIds ?: "-custom-006", "$userID")
        }
    }

}