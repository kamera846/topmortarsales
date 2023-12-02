package com.topmortar.topmortarsales.view.user

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_ADMIN
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_BA
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_COURIER
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_SALES
import com.topmortar.topmortarsales.commons.CONST_FULL_NAME
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.CONST_USER_ID
import com.topmortar.topmortarsales.commons.CONST_USER_LEVEL
import com.topmortar.topmortarsales.commons.MANAGE_USER_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.commons.utils.PhoneHandler.formatPhoneNumber
import com.topmortar.topmortarsales.commons.utils.PhoneHandler.phoneValidation
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.CityModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import kotlinx.coroutines.launch
import org.w3c.dom.Text

@SuppressLint("SetTextI18n")
class AddUserActivity : AppCompatActivity(), SearchModal.SearchModalListener {

    private lateinit var icBack: ImageView
    private lateinit var tvTitleBar: TextView
    private lateinit var btnSubmit: Button
    private lateinit var spinLevel: Spinner
    private lateinit var etUserCity: EditText
    private lateinit var etUsername: EditText
    private lateinit var tvUsernameGenerated: TextView
    private lateinit var etFullName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var passwordContainer: LinearLayout
    private lateinit var confirmPasswordContainer: LinearLayout

    // Global
    private lateinit var sessionManager: SessionManager
    private val userDistributorId get() = sessionManager.userDistributor().toString()
    private lateinit var searchModal: SearchModal

    private var isLoaded = false
    private var isCitiesLoaded = false
    private var activityRequestCode = MANAGE_USER_ACTIVITY_REQUEST_CODE
    private var selectedLevel: String? = null
    private var selectedCity: ModalSearchModel? = null
    private var citiesResults: ArrayList<CityModel> = ArrayList()

    private var userID: String? = null
    private var iUserLevel: String? = null
    private var iLocation: String? = null

    private var txtSubmit = ""
    private var txtSave = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)

        setContentView(R.layout.activity_add_user)

        txtSubmit = getString(R.string.submit)
        txtSave = getString(R.string.save)

        initVariable()
        initClickHandler()
        dataActivityHandler()

        Handler().postDelayed({

            // Setup Spinner
            setSpinner()

            // Setup Dialog Search
            setupDialogSearch()

            // Get List of City
            getCities()
        }, 100)

    }

    private fun submit() {

        val level = selectedLevel
        var city = "${ etUserCity.text }"
        val phone = "${ etPhone.text }"
        val username = "${ etUsername.text }".trim().replace(" ", "").toLowerCase()
        val fullName = "${ etFullName.text }"
        val password = "${ etPassword.text }"
        val confirmPassword = "${ etConfirmPassword.text }"

        if (!formValidation(level = level, city = city, phone = phone, username = username, fullName = fullName, password = password, confirmPassword = confirmPassword)) return

        city = if (selectedCity == null) "0" else selectedCity!!.id!!

        loadingState(true)

//        Handler().postDelayed({
//            handleMessage(this, "Add User", "$level : $city : $phone : $username : $password : $confirmPassword")
//            loadingState(false)
//        }, 1000)
//
//        return

        lifecycleScope.launch {
            try {

                val rbLevel = createPartFromString(level!!)
                val rbCityId = createPartFromString(city)
                val rbPhone = createPartFromString(formatPhoneNumber(phone))
                val rbUsername = createPartFromString(username)
                val rbFullName = createPartFromString(fullName)
                val rbPassword = createPartFromString(password)
                val rbDistributorId = createPartFromString(userDistributorId)

                val apiService: ApiService = HttpClient.create()
                val response = if (userID == null) {
                    apiService.addUser(level = rbLevel, cityId = rbCityId, phone = rbPhone, username = rbUsername, fullName = rbFullName, password = rbPassword, distributorID = rbDistributorId)
                } else {
                    val rbUserID = createPartFromString(userID!!)
                    apiService.editUser(ID = rbUserID, level = rbLevel, cityId = rbCityId, phone = rbPhone, username = rbUsername, fullName = rbFullName, distributorID = rbDistributorId)
                }

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            handleMessage(this@AddUserActivity, TAG_RESPONSE_MESSAGE, "Successfully ${ if (userID == null) "added" else "edit" } data!")
                            loadingState(false)

                            val resultIntent = Intent()
                            resultIntent.putExtra("$activityRequestCode", SYNC_NOW)
                            setResult(RESULT_OK, resultIntent)
                            finish()

                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            handleMessage(this@AddUserActivity, TAG_RESPONSE_MESSAGE, "Failed to ${ if (userID == null) "added" else "edit" }! Message: ${ responseBody.message }")
                            loadingState(false)

                        }
                        else -> {

                            handleMessage(this@AddUserActivity, TAG_RESPONSE_MESSAGE, "Failed ${ if (userID == null) "added" else "edit" } data!: ${ responseBody.message }")
                            loadingState(false)

                        }
                    }

                } else {

                    handleMessage(this@AddUserActivity, TAG_RESPONSE_MESSAGE, "Failed ${ if (userID == null) "added" else "edit" } data! Error: " + response.message())
                    loadingState(false)

                }


            } catch (e: Exception) {

                handleMessage(this@AddUserActivity, TAG_RESPONSE_MESSAGE, "Failed run service. Exception " + e.message)
                loadingState(false)

            }

        }

    }

    private fun initVariable() {

        icBack = findViewById(R.id.ic_back)
        tvTitleBar = findViewById(R.id.tv_title_bar)
        btnSubmit = findViewById(R.id.btn_submit)
        spinLevel = findViewById(R.id.spin_level)
        etUserCity = findViewById(R.id.et_user_city)
        etPhone = findViewById(R.id.et_phone)
        etUsername = findViewById(R.id.et_username)
        tvUsernameGenerated = findViewById(R.id.tv_username_generated)
        etFullName = findViewById(R.id.et_full_name)
        etPassword = findViewById(R.id.et_user_password)
        etConfirmPassword = findViewById(R.id.et_confirm_user_password)
        passwordContainer = findViewById(R.id.password_container)
        confirmPasswordContainer = findViewById(R.id.confirm_password_container)

        // Set Title Bar
        tvTitleBar.text = "Buat Pengguna Baru"

        // Text View Generated Username
        val usernameGeneratedDescription = "Username akan dibuat menjadi:"
        etUsername.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val currentText = s.toString().trim()

                if (currentText.isNotEmpty()) {
                    tvUsernameGenerated.visibility = View.VISIBLE
                    tvUsernameGenerated.text = "$usernameGeneratedDescription <b><i>${currentText.replace(" ", "").toLowerCase()}</i></b>"
                    tvUsernameGenerated.text = Html.fromHtml("${ tvUsernameGenerated.text }", Html.FROM_HTML_MODE_COMPACT)
                } else tvUsernameGenerated.visibility = View.GONE
            }

        })

    }

    private fun initClickHandler() {

        icBack.setOnClickListener { finish() }
        btnSubmit.setOnClickListener { if (isLoaded) submit() }
        etUserCity.setOnClickListener { showSearchModal() }

        // Focus Listener
        etUserCity.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showSearchModal()
                etUserCity.setSelection(etUserCity.length())
            } else etUserCity.clearFocus()
        }

        // Change Listener
        etUserCity.addTextChangedListener (object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (isLoaded) {
                    if (s.toString().isNotEmpty()) etUserCity.error = null
                    showSearchModal()
                }
            }

        })

    }

    private fun dataActivityHandler() {
        val iUserID = intent.getStringExtra(CONST_USER_ID)
        val iPhone = intent.getStringExtra(CONST_PHONE)
        val iName = intent.getStringExtra(CONST_NAME)
        val iFullName = intent.getStringExtra(CONST_FULL_NAME)
        iUserLevel = intent.getStringExtra(CONST_USER_LEVEL)
        iLocation = intent.getStringExtra(CONST_LOCATION)

        if (!iUserID.isNullOrEmpty()) {
            userID = iUserID
            // Set Title Bar
            tvTitleBar.text = "Edit User"
            // Set Button Submit
            btnSubmit.text = txtSave
            // Remove Field Password
            passwordContainer.visibility = View.GONE
            confirmPasswordContainer.visibility = View.GONE
        }
        if (!iPhone.isNullOrEmpty()) etPhone.setText(iPhone)
        if (!iName.isNullOrEmpty()) {
            etUsername.setText(iName)
            etUsername.setTextColor(getColor(R.color.black_500))
            etUsername.setBackgroundResource(R.drawable.et_background_disabled)
            etUsername.isEnabled = false
            tvUsernameGenerated.text = "Username tidak bisa diganti."
            tvUsernameGenerated.setTypeface(null, Typeface.ITALIC)
        }
        if (!iFullName.isNullOrEmpty()) etFullName.setText(iFullName)
        if (!iLocation.isNullOrEmpty()) etUserCity.setText(getString(R.string.txt_loading))
        else etUserCity.setText("")
    }

    private fun loadingState(state: Boolean) {

        btnSubmit.setTextColor(ContextCompat.getColor(this, R.color.white))
        btnSubmit.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_200))

        if (state) {

            btnSubmit.isEnabled = false
            btnSubmit.text = getString(R.string.txt_loading)

        } else {

            btnSubmit.isEnabled = true
            if (userID != null) btnSubmit.text = txtSave else btnSubmit.text = txtSubmit
            btnSubmit.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))

        }

    }

    private fun formValidation(level: String? = null, city: String = "", phone: String = "", username: String = "", fullName: String = "", password: String = "", confirmPassword: String = ""): Boolean {
        return if (level == null) {
            handleMessage(this, "ERROR SPINNER", "Pilih level pengguna")
            false
        } else if (city.isEmpty()) {
            etUserCity.error = "Pilih kota pengguna!"
            etUserCity.requestFocus()
            false
        } else if (phone.isEmpty()) {
            etPhone.error = "Nomor telpon wajib diisi!"
            etPhone.requestFocus()
            false
        } else if (!phoneValidation(phone, etPhone)) {
            etPhone.requestFocus()
            false
        } else if (username.isEmpty()) {
            etUserCity.error = null
            etUserCity.clearFocus()
            etUsername.error = "Username wajib diisi!"
            etUsername.requestFocus()
            false
        } else if (fullName.isEmpty()) {
            etUsername.error = null
            etUsername.clearFocus()
            etFullName.error = "Nama lengkap wajib diisi!"
            etFullName.requestFocus()
            false
        } else if (userID == null) {
            if (password.isEmpty()) {
                etUsername.error = null
                etUsername.clearFocus()
                etPassword.error = "Password wajib diisi!"
                etPassword.requestFocus()
                false
            } else if (password.length < 8) {
                etUsername.error = null
                etUsername.clearFocus()
                etPassword.error = "Kata sandi minimum adalah 8 karakter!"
                etPassword.requestFocus()
                false
            } else if (confirmPassword.isEmpty()) {
                etPassword.error = null
                etPassword.clearFocus()
                etConfirmPassword.error = "Konfirmasi kata sandi tidak boleh kosong!"
                etConfirmPassword.requestFocus()
                false
            } else if (confirmPassword.length < 8) {
                etPassword.error = null
                etPassword.clearFocus()
                etConfirmPassword.error = "Kata sandi minimum adalah 8 karakter!"
                etConfirmPassword.requestFocus()
                false
            } else if (password != confirmPassword) {
                etPassword.error = "Password tidak sama"
                etConfirmPassword.error = "password tidak sama"
                etPassword.requestFocus()
                etConfirmPassword.requestFocus()
                false
            } else {
                etPassword.error = null
                etConfirmPassword.error = null
                etPassword.clearFocus()
                etConfirmPassword.clearFocus()
                return true
            }
        } else {
            etPassword.error = null
            etConfirmPassword.error = null
            etPassword.clearFocus()
            etConfirmPassword.clearFocus()
            return true
        }
    }

    private fun setSpinner() {

        // Create an ArrayAdapter using the string array and default spinner layout
        val adapter = object : ArrayAdapter<CharSequence>(
            this,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.user_level_spinner_options)
        ) {
            override fun isEnabled(position: Int): Boolean {
                // Disable the first item (position 0)
                return position != 0
            }
        }

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        spinLevel.adapter = adapter

        val userLevel = when (iUserLevel) {
            AUTH_LEVEL_ADMIN ->  1
            AUTH_LEVEL_SALES ->  2
            AUTH_LEVEL_COURIER ->  3
            AUTH_LEVEL_BA ->  4
            else -> 0
        }

        spinLevel.setSelection(userLevel)

        // Handle the selected option
        spinLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                // Get the selected item value (e.g., "admin" or "sales")
                selectedLevel = when (position) {
                    0 -> null
                    1 -> AUTH_LEVEL_ADMIN
                    2 -> AUTH_LEVEL_SALES
                    3 -> AUTH_LEVEL_COURIER
                    4 -> AUTH_LEVEL_BA
                    else -> null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing here
                selectedLevel = when (iUserLevel) {
                    AUTH_LEVEL_ADMIN -> AUTH_LEVEL_ADMIN
                    AUTH_LEVEL_SALES -> AUTH_LEVEL_SALES
                    AUTH_LEVEL_COURIER -> AUTH_LEVEL_COURIER
                    AUTH_LEVEL_BA -> AUTH_LEVEL_BA
                    else -> null
                }
            }
        }

    }

    private fun setupDialogSearch(items: ArrayList<ModalSearchModel> = ArrayList()) {

        searchModal = SearchModal(this, items)
        searchModal.setCustomDialogListener(this)
        searchModal.searchHint = "Masukkan nama kota…"
        searchModal.setOnDismissListener {
            etUserCity.clearFocus()
            etUsername.requestFocus()
        }

    }

    private fun showSearchModal() {
        val searchKey = etUserCity.text.toString()
        if (searchKey.isNotEmpty()) searchModal.setSearchKey(searchKey)
        searchModal.show()
    }

    private fun getCities() {
        // Get Cities
        isCitiesLoaded = false

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

                        val foundItem = citiesResults.find { it.id_city == iLocation }
                        if (foundItem != null) {
                            etUserCity.setText("${foundItem.nama_city} - ${foundItem.kode_city}")
                            selectedCity = ModalSearchModel(foundItem.id_city, foundItem.nama_city)
                        } else etUserCity.setText("")

                        isCitiesLoaded = true
                        isLoaded = true

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(this@AddUserActivity, "LIST CITY", "Daftar kota kosong!")
                        isCitiesLoaded = false

                    }
                    else -> {

                        handleMessage(this@AddUserActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        isCitiesLoaded = false

                    }
                }


            } catch (e: Exception) {

                handleMessage(this@AddUserActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                isCitiesLoaded = false

            }

        }
    }

    override fun onDataReceived(data: ModalSearchModel) {
        etUserCity.setText(data.title)
        selectedCity = data
    }

}