package com.topmortar.topmortarsales.view.tukang

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.GET_COORDINATE
import com.topmortar.topmortarsales.commons.LOCATION_PERMISSION_REQUEST_CODE
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.utils.CustomEtHandler.setMaxLength
import com.topmortar.topmortarsales.commons.utils.CustomEtHandler.updateTxtMaxLength
import com.topmortar.topmortarsales.commons.utils.CustomProgressBar
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.PhoneHandler.formatPhoneNumber
import com.topmortar.topmortarsales.commons.utils.PhoneHandler.phoneValidation
import com.topmortar.topmortarsales.commons.utils.ResponseMessage.generateFailedRunServiceMessage
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.URLUtility
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityAddTukangBinding
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.ModalSearchModel
import com.topmortar.topmortarsales.model.SkillModel
import com.topmortar.topmortarsales.view.MapsActivity
import kotlinx.coroutines.launch
import java.util.Calendar

class AddTukangActivity : AppCompatActivity() {

    private lateinit var icBack: ImageView
    private lateinit var tvTitleBar: TextView
    private lateinit var tvMaxMessage: TextView
    private lateinit var btnSubmit: Button
    private lateinit var etPhone: EditText
    private lateinit var etName: EditText
    private lateinit var etOwner: EditText
    private lateinit var etBirthday: EditText
    private lateinit var etSkill: EditText
    private lateinit var etMapsUrl: EditText
    private lateinit var etMessage: EditText

    private lateinit var binding: ActivityAddTukangBinding
    private lateinit var apiService: ApiService
    private lateinit var progressBar: CustomProgressBar
    private lateinit var sessionManager: SessionManager
    private val userKind get() = sessionManager.userKind().toString()
    private val userDistributorId get() = sessionManager.userDistributor().toString()

    private lateinit var datePicker: DatePickerDialog
    private lateinit var searchModal: SearchModal
    private lateinit var searchModalCities: SearchModal

    private var isLoaded = false
    private var activityRequestCode = MAIN_ACTIVITY_REQUEST_CODE
    private val msgMaxLines = 5
    private val msgMaxLength = 200
    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedSkill: ModalSearchModel? = null
    private var skillResults: ArrayList<SkillModel> = ArrayList()
    private var selectedCity: ModalSearchModel? = null

    private var iLocation: String? = null

    private val coordinateResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val latitude = it.data?.getDoubleExtra("latitude", 0.0)
            val longitude = it.data?.getDoubleExtra("longitude", 0.0)
            val latLng = "$latitude,$longitude"
            if (latitude != null && longitude != null) etMapsUrl.setText(latLng)
            etMapsUrl.error = null
            etMapsUrl.clearFocus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge(isPrimary = false)

        binding = ActivityAddTukangBinding.inflate(layoutInflater)
        apiService = HttpClient.create()
        progressBar = CustomProgressBar(this)
        progressBar.setMessage(getString(R.string.txt_loading))
        progressBar.setCancelable(false)
        sessionManager = SessionManager(this)

        setContentView(binding.root)

        initVariable()
        initClickHandler()
//        dataActivityValidation()
        etMessageListener()
        checkLocationPermission()

        progressBar.show()
        Handler(Looper.getMainLooper()).postDelayed({
            if (userKind == USER_KIND_ADMIN) getCities()
            else getSkills()
        }, 500)

    }

    private fun sendMessage() {

        val phone = "${ etPhone.text }"
        val name = "${ etName.text }"
        var birthday = "${ etBirthday.text }"
        val cityID = if (userKind == USER_KIND_ADMIN) {
            selectedCity?.id ?: "0"
        } else {
            sessionManager.userCityID().let { if (!it.isNullOrEmpty()) it else "0" }
        }
        val skillID = if (selectedSkill != null) "${ selectedSkill!!.id }" else "0"
        val mapsUrl = "${ etMapsUrl.text }"
        val message = "${ etMessage.text }"
        val userId = sessionManager.userID().let { if (!it.isNullOrEmpty()) it else "" }
        val currentName = sessionManager.fullName().let { fullName -> if (!fullName.isNullOrEmpty()) fullName else sessionManager.userName().let { username -> if (!username.isNullOrEmpty()) username else "" } }

        if (!formValidation(phone = phone, name = name, skill = skillID, mapsUrl = mapsUrl)) return

        birthday = if (birthday.isEmpty()) "0000-00-00"
        else DateFormat.format("${ etBirthday.text }", "dd MMMM yyyy", "yyyy-MM-dd")

        loadingState(true)

//        Handler(Looper.getMainLooper()).postDelayed({
//            handleMessage(this, "Submit -> ", "$phone : $name : $owner : $birthday : $skillID : $message")
//            loadingState(false)
//        }, 1000)
//
//        return

        lifecycleScope.launch {
            try {

                val rbPhone = createPartFromString(formatPhoneNumber(phone))
                val rbName = createPartFromString(name)
                val rbLocation = createPartFromString(cityID)
                val rbSkill = createPartFromString(skillID)
                val rbBirthday = createPartFromString(birthday)
                val rbMapsUrl = createPartFromString(mapsUrl)
                val rbMessage = createPartFromString(message)
                val rbUserId = createPartFromString(userId)
                val rbCurrentName = createPartFromString(currentName)

                val apiService: ApiService = HttpClient.create()
                val response = message.let {
                    if (it.isEmpty()) {
                        apiService.insertTukang(
                            name = rbName,
                            phone = rbPhone,
                            birthday = rbBirthday,
                            cityId = rbLocation,
                            skillId = rbSkill,
                            mapsUrl = rbMapsUrl,
                            userId = rbUserId,
                            currentName = rbCurrentName)
                    } else {
                        apiService.sendMessageTukang(
                            name = rbName,
                            phone = rbPhone,
                            birthday = rbBirthday,
                            cityId = rbLocation,
                            skillId = rbSkill,
                            mapsUrl = rbMapsUrl,
                            currentName = rbCurrentName,
                            userId = rbUserId,
                            message = rbMessage)
                    }
                }

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            handleMessage(this@AddTukangActivity, TAG_RESPONSE_MESSAGE, message.let { if (it.isNotEmpty()) "Berhasil menyimpan & mengirim pesan!" else "Berhasil menyimpan kontak!" })
                            loadingState(false)

                            val resultIntent = Intent()
                            resultIntent.putExtra("$activityRequestCode", SYNC_NOW)
                            setResult(RESULT_OK, resultIntent)
                            finish()

                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            handleMessage(this@AddTukangActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim pesan: ${ responseBody.message }")
                            loadingState(false)

                        }
                        else -> {

                            handleMessage(this@AddTukangActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim pesan!")
                            loadingState(false)

                        }
                    }

                } else {

                    handleMessage(this@AddTukangActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim pesan! Error: " + response.message())
                    loadingState(false)

                }

            } catch (e: Exception) {

                FirebaseUtils.logErr(this@AddTukangActivity, "Failed AddTukangActivity on sendMessage(). Catch: ${e.message}")
                handleMessage(this@AddTukangActivity, TAG_RESPONSE_MESSAGE, generateFailedRunServiceMessage(e.message.toString()))
                loadingState(false)

            }

        }

    }

    private fun etMessageListener() {

        etMessage.maxLines = msgMaxLines
        etMessage.setMaxLength(msgMaxLength)

        etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateTxtMaxLength(tvMaxMessage, msgMaxLength, etMessage.text.length)
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isEmpty()) btnSubmit.text = "Simpan Kontak"
                else btnSubmit.text = "Simpan & Kirim Pesan"
            }
        })

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

    private fun getCoordinate() {
        val data = "${ etMapsUrl.text }"

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra(CONST_MAPS, data)
            intent.putExtra(GET_COORDINATE, true)
            coordinateResultLauncher.launch(intent)
        } else checkLocationPermission()
    }

    private fun initVariable() {

        icBack = findViewById(R.id.ic_back)
        tvTitleBar = findViewById(R.id.tv_title_bar)
        tvMaxMessage = findViewById(R.id.tv_max_message)
        btnSubmit = findViewById(R.id.btn_submit)
        etPhone = findViewById(R.id.et_phone)
        etName = findViewById(R.id.et_name)
        etOwner = findViewById(R.id.et_owner)
        etBirthday = findViewById(R.id.et_birthday)
        etSkill = findViewById(R.id.et_skill)
        etMessage = findViewById(R.id.et_message)
        etMapsUrl = findViewById(R.id.et_maps_url)

        // Set Title Bar
        tvTitleBar.text = "Tambah Tukang Baru"
        tvTitleBar.setPadding(0, 0, convertDpToPx(16, this), 0)

        if (userKind == USER_KIND_ADMIN) binding.locationContainer.visibility = View.VISIBLE
        else binding.locationContainer.visibility = View.GONE

        // Setup Date Picker Dialog
        setDatePickerDialog()

        // Setup Dialog Search
        setupDialogSearch()

    }

    private fun initClickHandler() {

        icBack.setOnClickListener { finish() }
        btnSubmit.setOnClickListener { if (isLoaded) sendMessage() }
        etBirthday.setOnClickListener { datePicker.show() }
        etMapsUrl.setOnClickListener { getCoordinate() }
        etSkill.setOnClickListener { showSearchModal() }
        binding.etLocation.setOnClickListener{ showSearchModalCities() }

        // Focus Listener
        etName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) etName.setSelection(etName.length())
        }
        etOwner.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) etOwner.setSelection(etOwner.length())
        }
        etBirthday.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                datePicker.show()
                etBirthday.setSelection(etBirthday.length())
            } else etBirthday.clearFocus()
        }
        etMapsUrl.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                getCoordinate()
                etMapsUrl.setSelection(etMapsUrl.length())
            } else etMapsUrl.clearFocus()
        }
        etSkill.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showSearchModal()
                etSkill.setSelection(etSkill.length())
            } else etSkill.clearFocus()
        }
        binding.etLocation.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showSearchModalCities()
                binding.etLocation.setSelection(binding.etLocation.length())
            } else binding.etLocation.clearFocus()
        }

    }

    private fun loadingState(state: Boolean) {

        btnSubmit.setTextColor(ContextCompat.getColor(this, R.color.white))
        btnSubmit.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_200))

        if (state) {

            btnSubmit.isEnabled = false
            btnSubmit.text = getString(R.string.txt_sending)

        } else {

            btnSubmit.isEnabled = true
            btnSubmit.text = if (etMessage.text.isNullOrEmpty()) "Simpan Kontak" else "Simpan & Kirim Pesan"
            btnSubmit.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))

        }

    }

    private fun formValidation(phone: String, name: String, skill: String = "", mapsUrl: String = ""): Boolean {
        return if (phone.isEmpty()) {
            etPhone.error = "Nomor telpon wajib diisi!"
            etPhone.requestFocus()
            false
        } else if (!phoneValidation(phone, etPhone)) {
            etPhone.requestFocus()
            false
        } else if (name.isEmpty()) {
            etPhone.error = null
            etPhone.clearFocus()
            etName.error = "Nama wajib diisi!"
            etName.requestFocus()
            false
//        } else if (owner.isEmpty()) {
//            etPhone.error = null
//            etPhone.clearFocus()
//            etOwner.error = "Full Name cannot be empty!"
//            etOwner.requestFocus()
//            false
//        } else if (birthday.isEmpty()) {
//            etOwner.error = null
//            etOwner.clearFocus()
//            etBirthday.error = "Choose owner birthday!"
//            etBirthday.requestFocus()
//            handleMessage(this, "ERROR EDIT CONTACT", "Choose owner birthday!")
//            false
        } else if (mapsUrl.isEmpty()) {
            etBirthday.error = null
            etBirthday.clearFocus()
            etMapsUrl.error = "Pilih koordinat!"
//            etMapsUrl.requestFocus()
            false
        } else if (skill.isEmpty()) {
            etMapsUrl.error = null
            etMapsUrl.clearFocus()
            etSkill.error = "Pilih keahlian tukang!"
            etSkill.requestFocus()
            false
//        } else if (message.isEmpty()) {
//            etMapsUrl.error = null
//            etMapsUrl.clearFocus()
//            etMessage.error = "Message cannot be empty!"
//            etMessage.requestFocus()
//            false
        } else {
            etPhone.error = null
            etName.error = null
            etOwner.error = null
            etBirthday.error = null
            etMapsUrl.error = null
            etMessage.error = null
            etSkill.error = null
            etPhone.clearFocus()
            etName.clearFocus()
            etSkill.clearFocus()
            etBirthday.clearFocus()
            etOwner.clearFocus()
            etMapsUrl.clearFocus()
            etMessage.clearFocus()
            true
        }
    }

    private fun setDatePickerDialog() {

        datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, day)

                // Do something with the selected date
                val formattedDate = DateFormat.format(selectedDate)
                etBirthday.setText(formattedDate)
                etBirthday.clearFocus()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.setOnDismissListener {
            etBirthday.clearFocus()
            etMessage.requestFocus()
        }

    }

    private fun setupDialogSearch(items: ArrayList<ModalSearchModel> = ArrayList()) {

        searchModal = SearchModal(this, items)
        searchModal.setCustomDialogListener(object : SearchModal.SearchModalListener {
            override fun onDataReceived(data: ModalSearchModel) {
                etSkill.setText(data.title)
                selectedSkill = data
            }

        })
        searchModal.label = "Pilih Opsi Keahlian"
        searchModal.searchHint = "Ketik nama keahlian…"
        searchModal.setOnDismissListener {
            etSkill.clearFocus()
        }
    }

    private fun showSearchModal() {
        val searchKey = etSkill.text.toString()
        if (searchKey.isNotEmpty()) searchModal.setSearchKey(searchKey)
        searchModal.show()
    }

    private fun setupDialogSearchCities(items: ArrayList<ModalSearchModel> = ArrayList()) {

        searchModalCities = SearchModal(this, items)
        searchModalCities.setCustomDialogListener(object : SearchModal.SearchModalListener {
            override fun onDataReceived(data: ModalSearchModel) {
                binding.etLocation.setText(data.title)
                selectedCity = data
            }

        })
        searchModalCities.label = "Pilih Opsi Kota"
        searchModalCities.searchHint = "Ketik nama kota…"
        searchModalCities.setOnDismissListener {
            binding.etLocation.clearFocus()
        }
    }

    private fun showSearchModalCities() {
        searchModalCities.show()
    }

    private fun getCities() {

        // Get Cities
        lifecycleScope.launch {
            try {

                val response = apiService.getCities(distributorID = userDistributorId)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        val results = response.results
                        val items: ArrayList<ModalSearchModel> = ArrayList()

                        for (i in 0 until results.size) {
                            val data = results[i]
                            items.add(ModalSearchModel(data.id_city, "${data.nama_city} - ${data.kode_city}"))
                        }

                        setupDialogSearchCities(items)
                        getSkills()
                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(this@AddTukangActivity, "LIST CITY", "Daftar kota kosong!")
                        getSkills()

                    }
                    else -> {

                        handleMessage(this@AddTukangActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        getSkills()

                    }
                }


            } catch (e: Exception) {

                FirebaseUtils.logErr(this@AddTukangActivity, "Failed AddTukangActivity on getCities(). Catch: ${e.message}")
                handleMessage(this@AddTukangActivity, TAG_RESPONSE_CONTACT, generateFailedRunServiceMessage(e.message.toString()))
                getSkills()

            }

        }
    }

    private fun getSkills() {
        // Get Cities
        etSkill.setText(getString(R.string.txt_loading))
        etSkill.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = apiService.getSkills(distributorID = userDistributorId)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        skillResults = response.results
                        val items: ArrayList<ModalSearchModel> = ArrayList()

                        for (i in 0 until skillResults.size) {
                            val data = skillResults[i]
                            items.add(ModalSearchModel(data.id_skill, "${data.nama_skill} - ${data.kode_skill}"))
                        }

                        setupDialogSearch(items)

                        val foundItem = skillResults.find { it.id_skill == iLocation }
                        if (foundItem != null) {
                            selectedSkill = ModalSearchModel(foundItem.id_skill, "${foundItem.nama_skill} - ${foundItem.kode_skill}")
                            etSkill.setText("${foundItem.nama_skill} - ${foundItem.kode_skill}")
                        } else {
                            selectedSkill = null
                            etSkill.setText("")
                        }

                        etSkill.isEnabled = true
                        progressBar.dismiss()
                        isLoaded = true

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(this@AddTukangActivity, "LIST CITY", "Daftar keahlian kosong!")
                        etSkill.isEnabled = true
                        progressBar.dismiss()

                    }
                    else -> {

                        handleMessage(this@AddTukangActivity, TAG_RESPONSE_CONTACT, "Gagal memuat data")
                        etSkill.isEnabled = true
                        progressBar.dismiss()

                    }
                }


            } catch (e: Exception) {

                FirebaseUtils.logErr(this@AddTukangActivity, "Failed AddTukangActivity on getSkill(). Catch: ${e.message}")
                handleMessage(this@AddTukangActivity, TAG_RESPONSE_CONTACT, generateFailedRunServiceMessage(e.message.toString()))
                etSkill.isEnabled = true
                progressBar.dismiss()

            }

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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

    override fun onDestroy() {
        super.onDestroy()
        if (::progressBar.isInitialized && progressBar.isShowing()) {
            progressBar.dismiss()
        }
    }

}