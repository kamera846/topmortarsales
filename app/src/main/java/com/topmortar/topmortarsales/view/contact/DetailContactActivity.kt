package com.topmortar.topmortarsales.view.contact

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.CONST_ADDRESS
import com.topmortar.topmortarsales.commons.CONST_BIRTHDAY
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_OWNER
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.DETAIL_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.EMPTY_FIELD_VALUE
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.commons.utils.phoneHandler
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.ModalSearchModel
import kotlinx.coroutines.launch
import java.util.Calendar


@Suppress("DEPRECATION")
class DetailContactActivity : AppCompatActivity(), SearchModal.SearchModalListener {

    private lateinit var sessionManager: SessionManager

    private lateinit var tvPhoneContainer: LinearLayout
    private lateinit var etPhoneContainer: LinearLayout
    private lateinit var tvBirthdayContainer: LinearLayout
    private lateinit var etBirthdayContainer: LinearLayout
    private lateinit var tvLocationContainer: LinearLayout
    private lateinit var etLocationContainer: LinearLayout
    private lateinit var tvOwnerContainer: LinearLayout
    private lateinit var etOwnerContainer: LinearLayout
    private lateinit var tvMapsContainer: RelativeLayout
    private lateinit var etMapsContainer: LinearLayout
    private lateinit var overlayMaps: View

    private lateinit var addressContainer: LinearLayout

    private lateinit var icBack: ImageView
    private lateinit var icEdit: ImageView
    private lateinit var icClose: ImageView

    private lateinit var tooltipPhone: ImageView
    private lateinit var tooltipOwner: ImageView
    private lateinit var tooltipBirthday: ImageView
    private lateinit var tooltipLocation: ImageView
    private lateinit var tooltipMaps: ImageView

    private lateinit var tvTitleBar: TextView
    private lateinit var tvName: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvBirthday: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvOwner: TextView
    private lateinit var tvMaps: TextView
    private lateinit var etName: EditText
    private lateinit var etOwner: EditText
    private lateinit var etPhone: EditText
    private lateinit var etBirthday: EditText
    private lateinit var etLocation: EditText
    private lateinit var etMaps: EditText

    private lateinit var etAddress: EditText

    private lateinit var btnSendMessage: Button
    private lateinit var btnSaveEdit: Button

    private var activityRequestCode = MAIN_ACTIVITY_REQUEST_CODE
    private var contactId: String? = null
    private var isEdit: Boolean = false
    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedCity: ModalSearchModel? = null
    private var hasEdited: Boolean = false

    private var iLocation: String? = null
    private var iAddress: String? = null
    private var iMapsUrl: String? = null

    private lateinit var datePicker: DatePickerDialog
    private lateinit var searchModal: SearchModal

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)

        setContentView(R.layout.activity_detail_contact)

        initVariable()
        initClickHandler()
        dataActivityValidation()

        // Get List City
        getCities()

    }

    private fun initVariable() {

        tvTitleBar = findViewById(R.id.tv_title_bar)

        icBack = findViewById(R.id.ic_back)
        icEdit = findViewById(R.id.ic_edit)
        icClose = findViewById(R.id.ic_close)

        tvPhoneContainer = findViewById(R.id.tv_phone_container)
        etPhoneContainer = findViewById(R.id.et_phone_container)
        tvBirthdayContainer = findViewById(R.id.tv_birthday_container)
        etBirthdayContainer = findViewById(R.id.et_birthday_container)
        tvLocationContainer = findViewById(R.id.tv_location_container)
        etLocationContainer = findViewById(R.id.et_location_container)
        tvOwnerContainer = findViewById(R.id.tv_owner_container)
        etOwnerContainer = findViewById(R.id.et_owner_container)
        tvMapsContainer = findViewById(R.id.tv_maps_container)
        etMapsContainer = findViewById(R.id.et_maps_container)
        overlayMaps = findViewById(R.id.overlay_maps)

        addressContainer = findViewById(R.id.address_container)

        tooltipPhone = findViewById(R.id.tooltip_phone)
        tooltipOwner = findViewById(R.id.tooltip_owner)
        tooltipLocation = findViewById(R.id.tooltip_location)
        tooltipMaps = findViewById(R.id.tooltip_maps)
        tooltipBirthday = findViewById(R.id.tooltip_birthday)

        tvName = findViewById(R.id.tv_name)
        etName = findViewById(R.id.et_name)
        tvDescription = findViewById(R.id.tv_description)

        tvPhone = findViewById(R.id.tv_phone)
        etPhone = findViewById(R.id.et_phone)
        tvOwner = findViewById(R.id.tv_owner)
        etOwner = findViewById(R.id.et_owner)
        tvLocation = findViewById(R.id.tv_location)
        etLocation = findViewById(R.id.et_location)
        tvMaps = findViewById(R.id.tv_maps)
        etMaps = findViewById(R.id.et_maps)
        tvBirthday = findViewById(R.id.tv_birthday)
        etBirthday = findViewById(R.id.et_birthday)
        etAddress = findViewById(R.id.et_address)

        btnSendMessage = findViewById(R.id.btn_send_message)
        btnSaveEdit = findViewById(R.id.btn_save_edit)

        // Setup Title Bar
        tvTitleBar.text = "Detail Contact"
        tvTitleBar.setPadding(0, 0, convertDpToPx(16, this), 0)
        if (sessionManager.userKind() == USER_KIND_ADMIN) icEdit.visibility = View.VISIBLE

        // Setup Date Picker Dialog
        setDatePickerDialog()

        // Setup Dialog Search
        setupDialogSearch()

    }

    private fun initClickHandler() {

        icBack.setOnClickListener { backHandler() }
        icEdit.setOnClickListener { toggleEdit(true) }
        icClose.setOnClickListener { toggleEdit(false) }
        btnSendMessage.setOnClickListener { navigateAddNewRoom() }
        btnSaveEdit.setOnClickListener { editConfirmation() }
        etBirthdayContainer.setOnClickListener { datePicker.show() }
        etBirthday.setOnClickListener { datePicker.show() }
        etLocationContainer.setOnClickListener { showSearchModal() }
        etLocation.setOnClickListener { showSearchModal() }
        addressContainer.setOnClickListener {
            if (isEdit) etAddress.requestFocus()
        }
        tvMapsContainer.setOnClickListener { mapsActionHandler() }

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
        etLocation.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showSearchModal()
                etLocation.setSelection(etLocation.length())
            } else etLocation.clearFocus()
        }
        //////////

        // Change Listener
        etBirthday.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (isEdit) datePicker.show()
            }

        })
        etLocation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (isEdit) showSearchModal()
            }

        })
        //////////

        // Tooltip Handler
        tooltipHandler(tooltipPhone, "Phone Number (WA)")
        tooltipHandler(tooltipOwner, "Owner Name")
        tooltipHandler(tooltipLocation, "Customer City")
        tooltipHandler(tooltipBirthday, "Owner Birthday")

        val tooltipMapsText = "Maps URL"
        val tooltipMapsTextOpen = "Click to open store location on maps apps"
        tooltipMaps.setOnClickListener {
            if (tvMaps.text != EMPTY_FIELD_VALUE) TooltipCompat.setTooltipText(tooltipMaps, tooltipMapsTextOpen)
            else TooltipCompat.setTooltipText(tooltipMaps, tooltipMapsText)
        }
        tooltipMaps.setOnLongClickListener {
            if (tvMaps.text != EMPTY_FIELD_VALUE) TooltipCompat.setTooltipText(tooltipMaps, tooltipMapsTextOpen)
            else TooltipCompat.setTooltipText(tooltipMaps, tooltipMapsText)
            false
        }
        //////////
    }

    private fun dataActivityValidation() {

        val iContactId = intent.getStringExtra(CONST_CONTACT_ID)
        val iPhone = intent.getStringExtra(CONST_PHONE)
        val iOwner = intent.getStringExtra(CONST_OWNER)
        val iName = intent.getStringExtra(CONST_NAME)
        val iBirthday = intent.getStringExtra(CONST_BIRTHDAY)
        iMapsUrl = intent.getStringExtra(CONST_MAPS)
        iAddress = intent.getStringExtra(CONST_ADDRESS)
        iLocation = intent.getStringExtra(CONST_LOCATION)
        activityRequestCode = intent.getIntExtra(ACTIVITY_REQUEST_CODE, activityRequestCode)

        if (!iContactId.isNullOrEmpty() ) {
            contactId = iContactId
        }
        if (!iPhone.isNullOrEmpty() ) {
            tvPhone.text = "+$iPhone"
            etPhone.setText(iPhone)
        } else {
            tvPhone.text = EMPTY_FIELD_VALUE
            etPhone.setText("")
        }
        if (!iName.isNullOrEmpty() ) {
            tvName.text = iName
            etName.setText(iName)
        } else {
            tvName.text = EMPTY_FIELD_VALUE
            etName.setText("")
        }
        if (!iOwner.isNullOrEmpty() ) {
            tvOwner.text = iOwner
            etOwner.setText(iOwner)
        } else {
            tvOwner.text = EMPTY_FIELD_VALUE
            etOwner.setText("")
        }
        if (!iLocation.isNullOrEmpty()) {
            tvLocation.text = "Loading..."
            etLocation.setText("Loading...")
        } else {
            tvLocation.text = EMPTY_FIELD_VALUE
            etLocation.setText("")
        }
        if (!iBirthday.isNullOrEmpty() ) {
            if (iBirthday == "0000-00-00") {
                tvBirthday.text = EMPTY_FIELD_VALUE
            } else {
                tvBirthday.text = DateFormat.format(iBirthday)
                etBirthday.setText(DateFormat.format(iBirthday))
            }
        }
        if (!iMapsUrl.isNullOrEmpty()) {
            tvMaps.text = "Click to open"
            etMaps.setText(iMapsUrl)
        } else {
            iMapsUrl = EMPTY_FIELD_VALUE
            tvMaps.text = EMPTY_FIELD_VALUE
            etMaps.setText("")
        }
        if (!iAddress.isNullOrEmpty()) {
            etAddress.setText(iAddress)
        } else {
            etAddress.setText(EMPTY_FIELD_VALUE)
        }

    }

    private fun toggleEdit(value: Boolean? = null) {

        isEdit = if (value!!) value else !isEdit

        if (isEdit) {

            // Hide Case
            icEdit.visibility = View.GONE

            tvName.visibility = View.GONE
            tvDescription.visibility = View.GONE

            tvPhoneContainer.visibility = View.GONE
            tvOwnerContainer.visibility = View.GONE
            tvLocationContainer.visibility = View.GONE
            tvMapsContainer.visibility = View.GONE
            tvBirthdayContainer.visibility = View.GONE

            btnSendMessage.visibility = View.GONE

            // Show Case
            tvTitleBar.text = "Edit Contact"
            icClose.visibility = View.VISIBLE

            etName.visibility = View.VISIBLE

            etPhoneContainer.visibility = View.VISIBLE
            etOwnerContainer.visibility = View.VISIBLE
            etLocationContainer.visibility = View.VISIBLE
            etMapsContainer.visibility = View.VISIBLE
            etBirthdayContainer.visibility = View.VISIBLE

            // Other Columns Handle
            addressContainer.setBackgroundResource(R.drawable.et_background)
            etAddress.isEnabled = true
            if (iAddress.isNullOrEmpty()) etAddress.setText("")


            btnSaveEdit.visibility = View.VISIBLE

            etName.requestFocus()
            etName.setSelection(etName.text.length)

        } else {

            // Show Case
            icEdit.visibility = View.VISIBLE

            tvName.visibility = View.VISIBLE
            tvDescription.visibility = View.VISIBLE

            tvPhoneContainer.visibility = View.VISIBLE
            tvOwnerContainer.visibility = View.VISIBLE
            tvLocationContainer.visibility = View.VISIBLE
            tvMapsContainer.visibility = View.VISIBLE
            tvBirthdayContainer.visibility = View.VISIBLE

            btnSendMessage.visibility = View.VISIBLE

            // Hide Case
            tvTitleBar.text = "Detail Contact"
            icClose.visibility = View.GONE

            etName.visibility = View.GONE

            etPhoneContainer.visibility = View.GONE
            etOwnerContainer.visibility = View.GONE
            etLocationContainer.visibility = View.GONE
            etMapsContainer.visibility = View.GONE
            etBirthdayContainer.visibility = View.GONE

            // Other Columns Handle
            addressContainer.setBackgroundResource(R.drawable.background_rounded)
            etAddress.isEnabled = false
            if (iAddress.isNullOrEmpty()) etAddress.setText(EMPTY_FIELD_VALUE)

            btnSaveEdit.visibility = View.GONE

            etName.clearFocus()

        }

    }

    private fun editConfirmation() {

        if (!formValidation("${ etPhone.text }","${ etName.text }", "${ etOwner.text }", "${ etMaps.text }", "${ etLocation.text }", "${ etBirthday.text }", "${ etAddress.text }")) return

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Confirmation")
            .setMessage("Are you sure you want to save changes?")
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                saveEdit()
            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun saveEdit() {

        val pPhone = "${ etPhone.text }"
        val pName = "${ etName.text }"
        val pOwner = "${ etOwner.text }"
        var pBirthday = "${ etBirthday.text }"
        var pMapsUrl = "${ etMaps.text }"
        var pAddress = "${ etAddress.text }"

        pBirthday = if (pBirthday.isEmpty() || pBirthday == EMPTY_FIELD_VALUE) "0000-00-00"
        else DateFormat.format("${ etBirthday.text }", "dd MMMM yyyy", "yyyy-MM-dd")

        val pCityID = if (selectedCity != null) selectedCity!!.id else "0"

        loadingState(true)
//
//        Handler().postDelayed({
//            tvName.text = "${ etName.text }"
//            tvOwner.text = "${ etOwner.text }"
//            tvBirthday.text = "${ etBirthday.text }"
//            tvMaps.text = "Click to open"
//            iMapsUrl = "${ etMaps.text }"
//            tvLocation.text = "${ etLocation.text }"
//            iAddress = "${ etAddress.text }"
//            handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Successfully edit data!")
//            loadingState(false)
//            toggleEdit(false)
//        }, 1000)
//
//        return

        lifecycleScope.launch {
            try {

                val rbId = createPartFromString(contactId!!)
                val rbPhone = createPartFromString(pPhone)
                val rbName = createPartFromString(pName)
                val rbOwner = createPartFromString(pOwner)
                val rbBirthday = createPartFromString(pBirthday)
                val rbMapsUrl = createPartFromString(pMapsUrl)
                val rbLocation = createPartFromString(pCityID)
                val rbAddress = createPartFromString(pAddress)

                val apiService: ApiService = HttpClient.create()
                val response = apiService.editContact(id = rbId, phone = rbPhone, name = rbName, ownerName = rbOwner, birthday = rbBirthday, cityId = rbLocation, mapsUrl = rbMapsUrl, address = rbAddress)

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    if (responseBody.status == RESPONSE_STATUS_OK) {


                        tvName.text = "${ etName.text }"
                        tvPhone.text = "${ etPhone.text }"
                        iAddress = "${ etAddress.text }"

                        handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Successfully edit data!")
                        loadingState(false)
                        toggleEdit(false)

                        if (!etOwner.text.isNullOrEmpty()) tvOwner.text = "${ etOwner.text }"
                        else tvOwner.text = EMPTY_FIELD_VALUE
                        if (!etBirthday.text.isNullOrEmpty()) tvBirthday.text = "${ etBirthday.text }"
                        else tvBirthday.text = EMPTY_FIELD_VALUE
                        if (!etMaps.text.isNullOrEmpty()) {
                            tvMaps.text = "Click to open"
                            iMapsUrl = "${ etMaps.text }"
                        } else {
                            tvMaps.text = EMPTY_FIELD_VALUE
                            iMapsUrl = ""
                        }
                        if (selectedCity != null) {
                            if (selectedCity!!.id != "0") tvLocation.text = "${ etLocation.text }"
                            else tvLocation.text = EMPTY_FIELD_VALUE
                        } else tvLocation.text = EMPTY_FIELD_VALUE
                        hasEdited = true

                    } else {

                        handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Failed to edit data!")
                        loadingState(false)
                        toggleEdit(false)

                    }

                } else {

                    handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Failed to edit data! Error: " + response.message())
                    loadingState(false)
                    toggleEdit(false)

                }


            } catch (e: Exception) {

                handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Failed run service. Exception " + e.message)
                loadingState(false)
                toggleEdit(false)

            }

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

        datePicker.setOnDismissListener { etBirthday.clearFocus() }

    }

    private fun loadingState(state: Boolean) {

        btnSaveEdit.setTextColor(ContextCompat.getColor(this, R.color.white))
        btnSaveEdit.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_200))

        if (state) {

            btnSaveEdit.isEnabled = false
            btnSaveEdit.text = "LOADING..."

        } else {

            btnSaveEdit.isEnabled = true
            btnSaveEdit.text = "SAVE"
            btnSaveEdit.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))

        }

    }

    private fun formValidation(phone: String, name: String, owner: String = "", mapsUrl: String = "", location: String = "", birthday: String = "", address: String = ""): Boolean {
        return if (name.isEmpty()) {
            etName.error = "Name cannot be empty!"
            etName.requestFocus()
            false
        } else if (phone.isEmpty()) {
            etPhone.error = "Phone number cannot be empty!"
            etPhone.requestFocus()
            false
        } else if (!phoneHandler.phoneValidation(phone, etPhone)) {
            etPhone.requestFocus()
            false
//        } else if (owner.isEmpty()) {
//            etName.error = null
//            etName.clearFocus()
//            etOwner.error = "Owner name cannot be empty!"
//            etOwner.requestFocus()
//            false
//        } else if (mapsUrl.isEmpty()) {
//            etOwner.error = null
//            etOwner.clearFocus()
//            etMaps.error = "Maps url cannot be empty!"
//            etMaps.requestFocus()
//            false
//        } else if (location.isEmpty() || location == EMPTY_FIELD_VALUE) {
//            etMaps.error = null
//            etMaps.requestFocus()
//            etLocation.error = "Choose customer city!"
//            etLocation.requestFocus()
//            handleMessage(this, "ERROR EDIT CONTACT", "Choose customer city!")
//            false
//        } else if (birthday.isEmpty() || birthday == EMPTY_FIELD_VALUE) {
//            etLocation.error = null
//            etLocation.clearFocus()
//            etBirthday.error = "Choose owner birthday!"
//            etBirthday.requestFocus()
//            handleMessage(this, "ERROR EDIT CONTACT", "Choose owner birthday!")
//            false
//        } else if (address.isEmpty()) {
//            etBirthday.error = null
//            etBirthday.clearFocus()
//            etAddress.error = "Address cannot be empty!"
//            etAddress.requestFocus()
//            false
        } else {
            etName.error = null
            etName.clearFocus()
            etOwner.error = null
            etOwner.clearFocus()
            etMaps.error = null
            etMaps.clearFocus()
            etLocation.error = null
            etLocation.clearFocus()
            etBirthday.error = null
            etBirthday.clearFocus()
            etAddress.error = null
            etAddress.clearFocus()
            true
        }
    }

    private fun backHandler() {

        if (isEdit) toggleEdit(false)
        else {

            if (hasEdited) {

                val resultIntent = Intent()
                resultIntent.putExtra("$activityRequestCode", SYNC_NOW)
                setResult(RESULT_OK, resultIntent)

                finish()

            } else finish()

        }

    }

    private fun navigateAddNewRoom() {

        val intent = Intent(this@DetailContactActivity, NewRoomChatFormActivity::class.java)

        intent.putExtra(CONST_CONTACT_ID, contactId)
        if (tvName.text == EMPTY_FIELD_VALUE) intent.putExtra(CONST_NAME, "")
        else intent.putExtra(CONST_NAME, tvName.text)

        // Remove "+" on text phone
        val trimmedInput = tvPhone.text.trim()
        if (trimmedInput.startsWith("+")) intent.putExtra(CONST_PHONE, trimmedInput.substring(1))

        if (tvOwner.text == EMPTY_FIELD_VALUE) intent.putExtra(CONST_OWNER, "")
        else intent.putExtra(CONST_OWNER, tvOwner.text)

        if (tvBirthday.text == EMPTY_FIELD_VALUE) intent.putExtra(CONST_BIRTHDAY, "0000-00-00")
        else intent.putExtra(CONST_BIRTHDAY, DateFormat.format("${ tvBirthday.text }", "dd MMMM yyyy", "yyyy-MM-dd"))
        intent.putExtra(ACTIVITY_REQUEST_CODE, DETAIL_ACTIVITY_REQUEST_CODE)

        if (tvLocation.text == EMPTY_FIELD_VALUE) intent.putExtra(CONST_LOCATION, "")
        else intent.putExtra(CONST_LOCATION, selectedCity!!.id)

        if (iMapsUrl == EMPTY_FIELD_VALUE) intent.putExtra(CONST_MAPS, "")
        else intent.putExtra(CONST_MAPS, iMapsUrl)

        startActivityForResult(intent, DETAIL_ACTIVITY_REQUEST_CODE)

    }

    private fun setupDialogSearch(items: ArrayList<ModalSearchModel> = ArrayList()) {

        searchModal = SearchModal(this, items)
        searchModal.setCustomDialogListener(this)
        searchModal.searchHint = "Enter city name..."
        searchModal.setOnDismissListener { etLocation.clearFocus() }

    }

    private fun showSearchModal() {
        val searchKey = etLocation.text.toString()
        if (searchKey.isNotEmpty()) searchModal.setSearchKey(searchKey)
        searchModal.show()
    }

    private fun getCities() {

        // Get Cities
        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getCities()

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        val results = response.results
                        val items: ArrayList<ModalSearchModel> = ArrayList()

                        for (i in 0 until results.size) {
                            val data = results[i]
                            items.add(ModalSearchModel(data.id_city, "${data.nama_city} - ${data.kode_city}"))
                        }

                        setupDialogSearch(items)
//                        searchModal.isLoading(false)

                        val foundItem = results.find { it.id_city == iLocation }
                        if (foundItem != null) {
                            tvLocation.text = "${foundItem.nama_city} - ${foundItem.kode_city}"
                            etLocation.setText("${foundItem.nama_city} - ${foundItem.kode_city}")
                            selectedCity = ModalSearchModel(foundItem.id_city, foundItem.nama_city)
                        } else {
                            tvLocation.text = EMPTY_FIELD_VALUE
                            etLocation.setText("")
                        }
                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(this@DetailContactActivity, "LIST CITY", "Empty cities data!")
//                        searchModal.isLoading(true)

                    }
                    else -> {

                        handleMessage(this@DetailContactActivity, TAG_RESPONSE_CONTACT, "Failed get data")
//                        searchModal.isLoading(true)

                    }
                }


            } catch (e: Exception) {

                handleMessage(this@DetailContactActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
//                searchModal.isLoading(true)

            }

        }
    }

    private fun mapsActionHandler() {
        if (tvMaps.text != EMPTY_FIELD_VALUE && !isEdit) {
            val animateDuration = 200L

            val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            fadeIn.duration = animateDuration

            overlayMaps.alpha = 0.4f
            overlayMaps.visibility = View.VISIBLE
            overlayMaps.startAnimation(fadeIn)

            Handler().postDelayed({
                overlayMaps.alpha = 0f
                overlayMaps.visibility = View.GONE

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(iMapsUrl))
                startActivity(intent)
            }, animateDuration)
        }
    }

    private fun tooltipHandler(content: ImageView, text: String) {
        content.setOnClickListener {
            TooltipCompat.setTooltipText(content, text)
        }
        content.setOnLongClickListener {
            TooltipCompat.setTooltipText(content, text)
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DETAIL_ACTIVITY_REQUEST_CODE) {

            val resultData = data?.getStringExtra("$DETAIL_ACTIVITY_REQUEST_CODE")

            if (resultData == SYNC_NOW) hasEdited = true

        }

    }

    override fun onBackPressed() {
//      return super.onBackPressed()
        backHandler()
    }

    override fun onDataReceived(data: ModalSearchModel) {
        etLocation.setText(data.title)
        selectedCity = data
    }

}