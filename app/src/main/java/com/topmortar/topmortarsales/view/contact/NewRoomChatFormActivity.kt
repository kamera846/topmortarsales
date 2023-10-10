package com.topmortar.topmortarsales.view.contact

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.CONST_BIRTHDAY
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_OWNER
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.CONST_TERMIN
import com.topmortar.topmortarsales.commons.DETAIL_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.GET_COORDINATE
import com.topmortar.topmortarsales.commons.LOCATION_PERMISSION_REQUEST_CODE
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.REQUEST_EDIT_CONTACT_COORDINATE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.STATUS_TERMIN_30
import com.topmortar.topmortarsales.commons.STATUS_TERMIN_45
import com.topmortar.topmortarsales.commons.STATUS_TERMIN_60
import com.topmortar.topmortarsales.commons.STATUS_TERMIN_COD
import com.topmortar.topmortarsales.commons.STATUS_TERMIN_COD_TF
import com.topmortar.topmortarsales.commons.STATUS_TERMIN_COD_TUNAI
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.utils.CustomEtHandler.setMaxLength
import com.topmortar.topmortarsales.commons.utils.CustomEtHandler.updateTxtMaxLength
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.commons.utils.PhoneHandler.formatPhoneNumber
import com.topmortar.topmortarsales.commons.utils.PhoneHandler.phoneValidation
import com.topmortar.topmortarsales.commons.utils.URLUtility
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.CityModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import com.topmortar.topmortarsales.view.MapsActivity
import kotlinx.coroutines.launch
import java.util.Calendar

@SuppressLint("SetTextI18n")
class NewRoomChatFormActivity : AppCompatActivity(), SearchModal.SearchModalListener {

    private lateinit var icBack: ImageView
    private lateinit var tvTitleBar: TextView
    private lateinit var tvMaxMessage: TextView
    private lateinit var btnSubmit: Button
    private lateinit var etPhone: EditText
    private lateinit var etName: EditText
    private lateinit var etOwner: EditText
    private lateinit var etBirthday: EditText
    private lateinit var etStoreLocated: EditText
    private lateinit var etMapsUrl: EditText
    private lateinit var etMessage: EditText
    private lateinit var spinTermin: Spinner
    private lateinit var spinnerSearchBox: AutoCompleteTextView

    private lateinit var sessionManager: SessionManager

    private lateinit var spinnerAdapter: ArrayAdapter<CharSequence>
    private lateinit var datePicker: DatePickerDialog
    private lateinit var searchModal: SearchModal

    private var isLoaded = false
    private var isCitiesLoaded = false
    private var activityRequestCode = MAIN_ACTIVITY_REQUEST_CODE
    private val msgMaxLines = 5
    private val msgMaxLength = 200
    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedCity: ModalSearchModel? = null
    private var selectedTermin: String? = null
    private var citiesResults: ArrayList<CityModel> = ArrayList()
    private var cities = listOf("Malang", "Gresik", "Sidoarjo", "Blitar", "Surabaya", "Jakarta", "Bandung", "Yogyakarta", "Kediri")

    private var iLocation: String? = null
    private var iMapsUrl: String? = null
    private var iTermin: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)

        setContentView(R.layout.activity_new_room_chat_form)

        initVariable()
        initClickHandler()
//        dataActivityValidation()
        etMessageListener()
        checkLocationPermission()

        Handler().postDelayed({
            isLoaded = true
            isCitiesLoaded = true
//            setSpinnerTermin()
//            getCities()
        }, 500)

    }

    private fun sendMessage() {

        val phone = "${ etPhone.text }"
        val name = "${ etName.text }"
        var birthday = "${ etBirthday.text }"
        val owner = "${ etOwner.text }"
        var cityId = sessionManager.userCityID().let { if (!it.isNullOrEmpty()) it else "0" }
        val mapsUrl = "${ etMapsUrl.text }"
        val message = "${ etMessage.text }"
        val termin = "-1"
        val userId = sessionManager.userID().let { if (!it.isNullOrEmpty()) it else "" }
        val currentName = sessionManager.fullName().let { fullName -> if (!fullName.isNullOrEmpty()) fullName else sessionManager.userName().let { username -> if (!username.isNullOrEmpty()) username else "" } }

        if (!formValidation(phone = phone, name = name, owner = owner, message = message, birthday = birthday, termin = termin, mapsUrl = mapsUrl)) return

        birthday = if (birthday.isEmpty()) "0000-00-00"
        else DateFormat.format("${ etBirthday.text }", "dd MMMM yyyy", "yyyy-MM-dd")

//        if (iLocation.isNullOrEmpty()) cityId = "0"

        loadingState(true)

//        Handler().postDelayed({
//            handleMessage(this, "SEND MESSAGE", "$phone : $name : $owner : $birthday : $cityId : $mapsUrl : $userId : $currentName : $termin : $message")
//            loadingState(false)
//        }, 1000)
//
//        return

        lifecycleScope.launch {
            try {

                val rbPhone = createPartFromString(formatPhoneNumber(phone))
                val rbName = createPartFromString(name)
//                val rbLocation = createPartFromString(selectedCity!!.id)
                val rbLocation = createPartFromString(cityId)
                val rbBirthday = createPartFromString(birthday)
                val rbOwner = createPartFromString(owner)
                val rbMapsUrl = createPartFromString(mapsUrl)
                val rbMessage = createPartFromString(message)
                val rbUserId = createPartFromString(userId)
                val rbCurrentName = createPartFromString(currentName)
                val rbTermin = createPartFromString(termin)

                val apiService: ApiService = HttpClient.create()
                val response = message.let {
                    if (it.isNullOrEmpty()) {
                        apiService.insertContact(
                            name = rbName,
                            phone = rbPhone,
                            ownerName = rbOwner,
                            birthday = rbBirthday,
                            cityId = rbLocation,
                            mapsUrl = rbMapsUrl,
                            userId = rbUserId,
                            currentName = rbCurrentName,
                            termin = rbTermin
                        )
                    } else {
                        apiService.sendMessage(
                            name = rbName,
                            phone = rbPhone,
                            ownerName = rbOwner,
                            birthday = rbBirthday,
                            cityId = rbLocation,
                            mapsUrl = rbMapsUrl,
                            userId = rbUserId,
                            currentName = rbCurrentName,
                            termin = rbTermin,
                            message = rbMessage
                        )
                    }
                }

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            handleMessage(this@NewRoomChatFormActivity, TAG_RESPONSE_MESSAGE, message.let { if (!it.isNullOrEmpty()) "Berhasil menyimpan & mengirim pesan!" else "Berhasil menyimpan kontak!" })
                            loadingState(false)

                            val resultIntent = Intent()
                            resultIntent.putExtra("$activityRequestCode", SYNC_NOW)
                            setResult(RESULT_OK, resultIntent)
                            finish()

                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            handleMessage(this@NewRoomChatFormActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim pesan: ${ responseBody.message }")
                            loadingState(false)

                        }
                        else -> {

                            handleMessage(this@NewRoomChatFormActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim pesan!")
                            loadingState(false)

                        }
                    }

                } else {

                    handleMessage(this@NewRoomChatFormActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim pesan. Error: " + response.message())
                    loadingState(false)

                }


            } catch (e: Exception) {

                handleMessage(this@NewRoomChatFormActivity, TAG_RESPONSE_MESSAGE, "Failed run service. Exception " + e.message)
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
                if (s.toString().isNullOrEmpty()) btnSubmit.text = "Simpan Kontak"
                else btnSubmit.text = "Simpan & Kirim Pesan"
            }
        })

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

    private fun getCoordinate() {
        val data = "${ etMapsUrl.text }"

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra(CONST_MAPS, data)
            intent.putExtra(GET_COORDINATE, true)
            startActivityForResult(intent, REQUEST_EDIT_CONTACT_COORDINATE)
        } else checkLocationPermission()
    }

    private fun dataActivityValidation() {

        val iContactId = intent.getStringExtra(CONST_CONTACT_ID)
        val iOwner = intent.getStringExtra(CONST_OWNER)
        val iPhone = intent.getStringExtra(CONST_PHONE)
        val iName = intent.getStringExtra(CONST_NAME)
        val iBirthday = intent.getStringExtra(CONST_BIRTHDAY)
        iLocation = intent.getStringExtra(CONST_LOCATION)
        iMapsUrl = intent.getStringExtra(CONST_MAPS)
        iTermin = intent.getStringExtra(CONST_TERMIN)
        activityRequestCode = intent.getIntExtra(ACTIVITY_REQUEST_CODE, activityRequestCode)

        if (iContactId.isNullOrEmpty()) {
            if (sessionManager.userKind() == USER_KIND_SALES) iLocation = sessionManager.userCityID()
        }
        if (!iPhone.isNullOrEmpty()) {
            etPhone.setText(iPhone)
            etPhone.setTextColor(getColor(R.color.black_500))
            etPhone.setBackgroundResource(R.drawable.et_background_disabled)
            etPhone.isEnabled = false
        }
        if (!iName.isNullOrEmpty()) {
            etName.setText(iName)
            etName.setTextColor(getColor(R.color.black_500))
            etName.setBackgroundResource(R.drawable.et_background_disabled)
            etName.isEnabled = false
        }
        if (!iOwner.isNullOrEmpty()) {
            etOwner.setText(iOwner)
            etOwner.setTextColor(getColor(R.color.black_500))
            etOwner.setBackgroundResource(R.drawable.et_background_disabled)
            etOwner.isEnabled = false
        }
        if (!iLocation.isNullOrEmpty()) {
            etStoreLocated.setText(getString(R.string.txt_loading))
            etStoreLocated.setTextColor(getColor(R.color.black_500))
            etStoreLocated.setBackgroundResource(R.drawable.et_background_disabled)
            etStoreLocated.isEnabled = false
        }
        if (!iBirthday.isNullOrEmpty()) {
            if (iBirthday == "0000-00-00") etBirthday.setText("")
            else {
                etBirthday.setText(DateFormat.format(iBirthday))
                etBirthday.setTextColor(getColor(R.color.black_500))
                etBirthday.setBackgroundResource(R.drawable.et_background_disabled)
                etBirthday.isEnabled = false
            }
        }
        if (!iMapsUrl.isNullOrEmpty()) {
            etMapsUrl.setText(iMapsUrl)
            etMapsUrl.setTextColor(getColor(R.color.black_500))
            etMapsUrl.setBackgroundResource(R.drawable.et_background_disabled)
            etMapsUrl.isEnabled = false
        }

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
        etStoreLocated = findViewById(R.id.et_store_located)
        etMessage = findViewById(R.id.et_message)
        spinTermin = findViewById(R.id.spin_termin)
        spinnerSearchBox = findViewById(R.id.spinner_search_box)
        etMapsUrl = findViewById(R.id.et_maps_url)

        // Set Title Bar
        tvTitleBar.text = "Tambah Kontak Baru"
        tvTitleBar.setPadding(0, 0, convertDpToPx(16, this), 0)

        // Setup Date Picker Dialog
        setDatePickerDialog()

        // Setup Spinner
        setSpinnerCities()

        // Setup Dialog Search
        setupDialogSearch()

    }

    private fun initClickHandler() {

        icBack.setOnClickListener { finish() }
        btnSubmit.setOnClickListener { if (isLoaded) sendMessage() }
        etBirthday.setOnClickListener { datePicker.show() }
        etMapsUrl.setOnClickListener { getCoordinate() }
        etStoreLocated.setOnClickListener { showSearchModal() }

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
        etStoreLocated.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showSearchModal()
                etStoreLocated.setSelection(etStoreLocated.length())
            } else etStoreLocated.clearFocus()
        }

        // Change Listener
        etBirthday.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (isLoaded) datePicker.show()
            }

        })
        etStoreLocated.addTextChangedListener (object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (isLoaded) {
                    if (s.toString().isNotEmpty()) etStoreLocated.error = null
                    showSearchModal()
                }
            }

        })

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

    private fun formValidation(phone: String, name: String, location: String = "", birthday: String = "", owner: String = "", termin: String? = "", mapsUrl: String = "", message: String): Boolean {
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
        } else if (owner.isEmpty()) {
            etPhone.error = null
            etPhone.clearFocus()
            etOwner.error = "Nama pemilik wajib diisi!"
            etOwner.requestFocus()
            false
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
//        } else if (location.isEmpty()) {
//            etMapsUrl.error = null
//            etMapsUrl.clearFocus()
//            etStoreLocated.error = "Choose store location!"
//            etStoreLocated.requestFocus()
//            false
//        } else if (termin == null) {
//            handleMessage(this, "ERROR SPINNER", "Choose termin payment")
//            false
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
            etStoreLocated.error = null
            etPhone.clearFocus()
            etName.clearFocus()
            etStoreLocated.clearFocus()
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

    private fun setSpinnerCities() {

        spinnerAdapter = ArrayAdapter(
            this@NewRoomChatFormActivity,
            android.R.layout.simple_dropdown_item_1line,
            cities
        )

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        spinnerSearchBox.setAdapter(spinnerAdapter)

    }

    private fun setupDialogSearch(items: ArrayList<ModalSearchModel> = ArrayList()) {

        searchModal = SearchModal(this, items)
        searchModal.setCustomDialogListener(this)
        searchModal.searchHint = "Ketik nama kota…"
        searchModal.setOnDismissListener {
            etStoreLocated.clearFocus()
            etOwner.requestFocus()
        }

    }

    private fun showSearchModal() {
        val searchKey = etStoreLocated.text.toString()
        if (searchKey.isNotEmpty()) searchModal.setSearchKey(searchKey)
        searchModal.show()
    }

    private fun getCities() {
        // Get Cities
        isCitiesLoaded = false

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getCities()

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        citiesResults = response.results
                        val items: ArrayList<ModalSearchModel> = ArrayList()

                        for (i in 0 until citiesResults.size) {
                            val data = citiesResults[i]
                            items.add(ModalSearchModel(data.id_city, "${data.nama_city} - ${data.kode_city}"))
                        }

                        setupDialogSearch(items)

                        val foundItem = citiesResults.find { it.id_city == iLocation }
                        if (foundItem != null) {
                            selectedCity = ModalSearchModel(foundItem.id_city, "${foundItem.nama_city} - ${foundItem.kode_city}")
                            etStoreLocated.setText("${foundItem.nama_city} - ${foundItem.kode_city}")
                        } else {
                            selectedCity = null
                            etStoreLocated.setText("")
                            etStoreLocated.setTextColor(getColor(R.color.black_200))
                            etStoreLocated.setBackgroundResource(R.drawable.et_background)
                            etStoreLocated.isEnabled = true
                        }

                        isCitiesLoaded = true
                        isLoaded = true
//                        searchModal.isLoading(false)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(this@NewRoomChatFormActivity, "LIST CITY", "Daftar kota kosong!")
                        isCitiesLoaded = false
//                        searchModal.isLoading(true)

                    }
                    else -> {

                        handleMessage(this@NewRoomChatFormActivity, TAG_RESPONSE_CONTACT, "Gagal memuat data")
                        isCitiesLoaded = false
//                        searchModal.isLoading(true)

                    }
                }


            } catch (e: Exception) {

                handleMessage(this@NewRoomChatFormActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                isCitiesLoaded = false
//                searchModal.isLoading(true)

            }

        }
    }

    private fun setSpinnerTermin() {

        // Create an ArrayAdapter using the string array and default spinner layout
        val adapter = object : ArrayAdapter<CharSequence>(
            this,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.termin_spinner_options)
        ) {
            override fun isEnabled(position: Int): Boolean {
                // Disable the first item (position 0)
                return position != 0
            }
        }

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        spinTermin.adapter = adapter

        val terminItem = when (iTermin) {
            STATUS_TERMIN_COD -> 1
            STATUS_TERMIN_COD_TF -> 2
            STATUS_TERMIN_COD_TUNAI -> 3
            STATUS_TERMIN_30 -> 4
            STATUS_TERMIN_45 -> 5
            STATUS_TERMIN_60 -> 6
            else -> 0
        }

        spinTermin.setSelection(terminItem)

        // Handle the selected option
        spinTermin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                // Get the selected item value (e.g., "admin" or "sales")
                selectedTermin = when (position) {
                    0 -> null
                    1 -> STATUS_TERMIN_COD
                    2 -> STATUS_TERMIN_COD_TF
                    3 -> STATUS_TERMIN_COD_TUNAI
                    4 -> STATUS_TERMIN_30
                    5 -> STATUS_TERMIN_45
                    6 -> STATUS_TERMIN_60
                    else -> null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing here
                selectedTermin = when (iTermin) {
                    STATUS_TERMIN_COD -> STATUS_TERMIN_COD
                    STATUS_TERMIN_COD_TF -> STATUS_TERMIN_COD_TF
                    STATUS_TERMIN_COD_TUNAI -> STATUS_TERMIN_COD_TUNAI
                    STATUS_TERMIN_30 -> STATUS_TERMIN_30
                    STATUS_TERMIN_45 -> STATUS_TERMIN_45
                    STATUS_TERMIN_60 -> STATUS_TERMIN_60
                    else -> null
                }
            }
        }

    }

    override fun onDataReceived(data: ModalSearchModel) {
        etStoreLocated.setText(data.title)
        selectedCity = data
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_EDIT_CONTACT_COORDINATE) {
            val latitude = data?.getDoubleExtra("latitude", 0.0)
            val longitude = data?.getDoubleExtra("longitude", 0.0)
            if (latitude != null && longitude != null) etMapsUrl.setText("$latitude,$longitude")
            etMapsUrl.error = null
            etMapsUrl.clearFocus()
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

}