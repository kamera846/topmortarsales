package com.topmortar.topmortarsales.view.contact

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.CONST_BIRTHDAY
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_OWNER
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.formatPhoneNumber
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.CityModel
import com.topmortar.topmortarsales.model.ModalSearchModel
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
    private lateinit var etMessage: EditText
    private lateinit var spinnerSearchBox: AutoCompleteTextView

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
    private var citiesResults: ArrayList<CityModel> = ArrayList()
    private var cities = listOf("Malang", "Gresik", "Sidoarjo", "Blitar", "Surabaya", "Jakarta", "Bandung", "Yogyakarta", "Kediri")

    private var iLocation: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        setContentView(R.layout.activity_new_room_chat_form)

        initVariable()
        initClickHandler()
        dataActivityValidation()
        etMessageListener()

        Handler().postDelayed({
            getCities()
        }, 500)

    }

    private fun sendMessage() {

        val phone = "${ etPhone.text }"
        val name = "${ etName.text }"
        val location = "${ etStoreLocated.text }"
        var birthday = "${ etBirthday.text }"
        val owner = "${ etOwner.text }"
        val message = "${ etMessage.text }"

        if (!formValidation(phone, name, location, birthday, owner, message)) return

        birthday = if (birthday.isEmpty()) "0000-00-00"
        else DateFormat.format("${ etBirthday.text }", "dd MMMM yyyy", "yyyy-MM-dd")

        loadingState(true)

//        Handler().postDelayed({
//            handleMessage(this@NewRoomChatFormActivity, TAG_ACTION_MAIN_ACTIVITY, "$phone : $name : ${selectedCity!!.id} : $birthday : $owner : $message")
//            loadingState(false)
//        }, 1000)
//
//        return

        lifecycleScope.launch {
            try {

                val rbPhone = createPartFromString(formatPhoneNumber(phone))
                val rbName = createPartFromString(name)
                val rbLocation = createPartFromString(selectedCity!!.id)
                val rbBirthday = createPartFromString(birthday)
                val rbOwner = createPartFromString(owner)
                val rbMessage = createPartFromString(message)

                val apiService: ApiService = HttpClient.create()
                val response = apiService.sendMessage(rbName, rbPhone, rbBirthday, rbOwner, rbMessage, rbLocation)

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    if (responseBody.status == RESPONSE_STATUS_OK) {

                        handleMessage(this@NewRoomChatFormActivity, TAG_RESPONSE_MESSAGE, "Successfully added data!")
                        loadingState(false)

                        val resultIntent = Intent()
                        resultIntent.putExtra("$activityRequestCode", SYNC_NOW)
                        setResult(RESULT_OK, resultIntent)
                        finish()

                    } else {

                        handleMessage(this@NewRoomChatFormActivity, TAG_RESPONSE_MESSAGE, "Failed to send message!")
                        loadingState(false)

                    }

                } else {

                    handleMessage(this@NewRoomChatFormActivity, TAG_RESPONSE_MESSAGE, "Failed to send message! Error: " + response.message())
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
                updateTxtMaxLength(etMessage.text.length)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

    }

    private fun dataActivityValidation() {

        val iOwner = intent.getStringExtra(CONST_OWNER)
        val iPhone = intent.getStringExtra(CONST_PHONE)
        val iName = intent.getStringExtra(CONST_NAME)
        val iBirthday = intent.getStringExtra(CONST_BIRTHDAY)
        iLocation = intent.getStringExtra(CONST_LOCATION)
        activityRequestCode = intent.getIntExtra(ACTIVITY_REQUEST_CODE, activityRequestCode)

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
            etStoreLocated.setText("Loading...")
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
        spinnerSearchBox = findViewById(R.id.spinner_searchbox)

        // Set Title Bar
        tvTitleBar.text = getString(R.string.new_chat_room)
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
        btnSubmit.setOnClickListener { sendMessage() }
        etBirthday.setOnClickListener { datePicker.show() }
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
            btnSubmit.text = getString(R.string.btn_submit_new_chat_room)
            btnSubmit.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))

        }

    }

    private fun updateTxtMaxLength(length: Int) {
        tvMaxMessage.text = "$length/$msgMaxLength"
    }

    private fun EditText.setMaxLength(maxLength: Int) {

        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))

    }

    private fun formValidation(phone: String, name: String, location: String, birthday: String, owner: String, message: String): Boolean {
        return if (phone.isEmpty()) {
            etPhone.error = "Phone number cannot be empty!"
            etPhone.requestFocus()
            false
        } else if (!phoneValidation(phone)) {
            etPhone.requestFocus()
            false
        } else if (name.isEmpty()) {
            etPhone.error = null
            etPhone.clearFocus()
            etName.error = "Name cannot be empty!"
            etName.requestFocus()
            false
//        } else if (location.isEmpty()) {
//            etName.error = null
//            etName.clearFocus()
//            etStoreLocated.error = "Choose store location!"
//            etStoreLocated.requestFocus()
//            false
//        } else if (owner.isEmpty()) {
//            etBirthday.error = null
//            etBirthday.clearFocus()
//            etOwner.error = "Owner Name cannot be empty!"
//            etOwner.requestFocus()
//            false
//        } else if (birthday.isEmpty()) {
//            etName.error = null
//            etName.clearFocus()
//            etBirthday.error = "Choose owner birthday!"
//            etBirthday.requestFocus()
//            false
        } else if (message.isEmpty()) {
            etOwner.error = null
            etOwner.clearFocus()
            etMessage.error = "Message cannot be empty!"
            etMessage.requestFocus()
            false
        } else {
            etPhone.error = null
            etName.error = null
            etStoreLocated.error = null
            etBirthday.error = null
            etOwner.error = null
            etMessage.error = null
            etPhone.clearFocus()
            etName.clearFocus()
            etStoreLocated.clearFocus()
            etBirthday.clearFocus()
            etOwner.clearFocus()
            etMessage.clearFocus()
            true
        }
    }

    private fun phoneValidation(input: String): Boolean {
        val pattern = Regex("^\\d{10,16}$")
        val trimmedInput = input.trim()

        return if (!trimmedInput.startsWith("0") && !trimmedInput.startsWith("8") && !trimmedInput.startsWith("62")) {
            etPhone.error = "Phone number must consist of starting with: 0, 8, 62"
            false
        } else if (!pattern.matches(input)) {
            etPhone.error = "Phone number must be 10 to 16 digits long"
            false
        } else true
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
        searchModal.searchHint = "Enter city name..."
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

                        handleMessage(this@NewRoomChatFormActivity, "LIST CITY", "Empty cities data!")
                        isCitiesLoaded = false
//                        searchModal.isLoading(true)

                    }
                    else -> {

                        handleMessage(this@NewRoomChatFormActivity, TAG_RESPONSE_CONTACT, "Failed get data")
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

    override fun onDataReceived(data: ModalSearchModel) {
        etStoreLocated.setText(data.title)
        selectedCity = data
    }

}