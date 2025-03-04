package com.topmortar.topmortarsales.view.tukang

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.BASE_URL
import com.topmortar.topmortarsales.commons.CONST_ADDRESS
import com.topmortar.topmortarsales.commons.CONST_BIRTHDAY
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_DATE
import com.topmortar.topmortarsales.commons.CONST_KTP
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_OWNER
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.CONST_POSTED_NAME
import com.topmortar.topmortarsales.commons.CONST_SKILL
import com.topmortar.topmortarsales.commons.CONST_STATUS
import com.topmortar.topmortarsales.commons.DETAIL_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.EMPTY_FIELD_VALUE
import com.topmortar.topmortarsales.commons.GET_COORDINATE
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.PING_HOST
import com.topmortar.topmortarsales.commons.PING_MEDIUM
import com.topmortar.topmortarsales.commons.PING_NORMAL
import com.topmortar.topmortarsales.commons.REQUEST_EDIT_CONTACT_COORDINATE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_ACTIVE
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_BLACKLIST
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_DATA
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_PASSIVE
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN_CITY
import com.topmortar.topmortarsales.commons.USER_KIND_BA
import com.topmortar.topmortarsales.commons.utils.CompressImageUtil
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.PhoneHandler
import com.topmortar.topmortarsales.commons.utils.PhoneHandler.formatPhoneNumber
import com.topmortar.topmortarsales.commons.utils.PingUtility
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityDetailContactBinding
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.modal.SendMessageTukangModal
import com.topmortar.topmortarsales.model.ModalSearchModel
import com.topmortar.topmortarsales.model.TukangModel
import com.topmortar.topmortarsales.view.MapsActivity
import com.topmortar.topmortarsales.view.contact.PreviewKtpActivity
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@Suppress("DEPRECATION")
@SuppressLint("SetTextI18n")
class DetailTukangActivity : AppCompatActivity(), SearchModal.SearchModalListener,
    PingUtility.PingResultInterface {

    private lateinit var sessionManager: SessionManager
    private val userDistributorId get() = sessionManager.userDistributor().toString()
    private lateinit var binding: ActivityDetailContactBinding
    private var pingUtility: PingUtility? = null

    private lateinit var tvPhoneContainer: LinearLayout
    private lateinit var etPhoneContainer: LinearLayout
    private lateinit var tvBirthdayContainer: LinearLayout
    private lateinit var etBirthdayContainer: LinearLayout
    private lateinit var tvKtpContainer: LinearLayout
    private lateinit var etKtpContainer: LinearLayout
    private lateinit var tvLocationContainer: LinearLayout
    private lateinit var etLocationContainer: LinearLayout
    private lateinit var skillContainer: LinearLayout
    private lateinit var tvSkillContainer: LinearLayout
    private lateinit var etSkillContainer: LinearLayout
    private lateinit var ownerSection: LinearLayout
    private lateinit var tvOwnerContainer: LinearLayout
    private lateinit var etOwnerContainer: LinearLayout
    private lateinit var tvMapsContainer: RelativeLayout
    private lateinit var etMapsContainer: LinearLayout
    private lateinit var overlayMaps: View

    private lateinit var statusContainer: LinearLayout
    private lateinit var terminContainer: LinearLayout
    private lateinit var addressContainer: LinearLayout

    private lateinit var icBack: ImageView
    private lateinit var icEdit: ImageView
    private lateinit var icClose: ImageView

    private lateinit var tooltipPhone: ImageView
    private lateinit var tooltipOwner: ImageView
    private lateinit var tooltipBirthday: ImageView
    private lateinit var tooltipKtp: ImageView
    private lateinit var tooltipLocation: ImageView
    private lateinit var tooltipSkill: ImageView
    private lateinit var tooltipMaps: ImageView

    private lateinit var tooltipStatus: ImageView

    private lateinit var tvTitleBar: TextView
    private lateinit var tvName: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvBirthday: TextView
//    private lateinit var tvKtp: TextView
//    private lateinit var tvSelectedKtp: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvSkill: TextView
    private lateinit var tvOwner: TextView
    private lateinit var tvMaps: TextView
    private lateinit var etName: EditText
    private lateinit var etOwner: EditText
    private lateinit var etPhone: EditText
    private lateinit var etBirthday: EditText
//    private lateinit var etKtp: EditText
    private lateinit var etLocation: EditText
    private lateinit var etSkill: EditText
    private lateinit var etMaps: EditText

    private lateinit var tvStatus: TextView
    private lateinit var tvTermin: TextView
    private lateinit var spinStatus: Spinner
    private lateinit var spinTermin: Spinner
    private lateinit var etAddress: EditText

    private lateinit var btnSendMessage: Button
    private lateinit var btnSaveEdit: Button
    private lateinit var btnInvoice: LinearLayout

    private var activityRequestCode = MAIN_ACTIVITY_REQUEST_CODE
    private var contactId: String? = null
    private var isEdit: Boolean = false
    private var isSearchCity: Boolean = false
    private var isSearchSkill: Boolean = false
    private var hasEdited: Boolean = false
    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedCity: ModalSearchModel? = null
    private var selectedSkill: ModalSearchModel? = null
    private var itemSendMessage: TukangModel? = null

    private var statusItem: List<String> = listOf("Pilih Status", "Data - New Customer", "Passive - Long time no visit", "Active - Need a visit", "Blacklist - Cannot be visited", "Bid - Customers are being Bargained")
    private var selectedStatus: String = ""
    private var cameraPermissionLauncher: ActivityResultLauncher<String>? = null
    private var imagePicker: ActivityResultLauncher<Intent>? = null
    private var selectedUri: Uri? = null
    private var currentPhotoUri: Uri? = null

    private var iLocation: String? = null
    private var iSkill: String? = null
    private var iStatus: String? = null
    private var iAddress: String? = null
    private var iMapsUrl: String? = null
    private var iKtp: String? = null

    private lateinit var datePicker: DatePickerDialog
    private lateinit var searchModal: SearchModal
    private lateinit var searchModalSkill: SearchModal
    private lateinit var sendMessageModal: SendMessageTukangModal

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge(isPrimary = false)

        sessionManager = SessionManager(this)
        binding = ActivityDetailContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVariable()
        initClickHandler()
        dataActivityValidation()

        getCities()
        getSkills()

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
        tvKtpContainer = findViewById(R.id.tv_ktp_container)
        etKtpContainer = findViewById(R.id.et_ktp_container)
        tvLocationContainer = findViewById(R.id.tv_location_container)
        etLocationContainer = findViewById(R.id.et_location_container)
        skillContainer = findViewById(R.id.skill_container)
        tvSkillContainer = findViewById(R.id.tv_skill_container)
        etSkillContainer = findViewById(R.id.et_skill_container)
        ownerSection = findViewById(R.id.owner_section)
        tvOwnerContainer = findViewById(R.id.tv_owner_container)
        etOwnerContainer = findViewById(R.id.et_owner_container)
        tvMapsContainer = findViewById(R.id.tv_maps_container)
        etMapsContainer = findViewById(R.id.et_maps_container)
        overlayMaps = findViewById(R.id.overlay_maps)

        statusContainer = findViewById(R.id.status_container)
        terminContainer = findViewById(R.id.termin_container)
        addressContainer = findViewById(R.id.address_container)

        tooltipPhone = findViewById(R.id.tooltip_phone)
        tooltipOwner = findViewById(R.id.tooltip_owner)
        tooltipLocation = findViewById(R.id.tooltip_location)
        tooltipSkill = findViewById(R.id.tooltip_skill)
        tooltipMaps = findViewById(R.id.tooltip_maps)
        tooltipBirthday = findViewById(R.id.tooltip_birthday)
        tooltipKtp = findViewById(R.id.tooltip_ktp)

        tooltipStatus = findViewById(R.id.tooltip_status)

        tvName = findViewById(R.id.tv_name)
        etName = findViewById(R.id.et_name)
        tvDescription = findViewById(R.id.tv_description)

        tvPhone = findViewById(R.id.tv_phone)
        etPhone = findViewById(R.id.et_phone)
        tvOwner = findViewById(R.id.tv_owner)
        etOwner = findViewById(R.id.et_owner)
        tvLocation = findViewById(R.id.tv_location)
        etLocation = findViewById(R.id.et_location)
        tvSkill = findViewById(R.id.tv_skill)
        etSkill = findViewById(R.id.et_skill)
        tvMaps = findViewById(R.id.tv_maps)
        etMaps = findViewById(R.id.et_maps)
        tvBirthday = findViewById(R.id.tv_birthday)
        etBirthday = findViewById(R.id.et_birthday)
//        tvKtp = findViewById(R.id.tv_ktp)
//        tvSelectedKtp = findViewById(R.id.tv_selected_ktp)
//        etKtp = findViewById(R.id.et_ktp)

        tvStatus = findViewById(R.id.tv_status)
        spinStatus = findViewById(R.id.spin_status)
        tvTermin = findViewById(R.id.tv_termin)
        spinTermin = findViewById(R.id.spin_termin)
        etAddress = findViewById(R.id.et_address)

        btnSendMessage = findViewById(R.id.btn_send_message)
        btnSaveEdit = findViewById(R.id.btn_save_edit)
        btnInvoice = findViewById(R.id.btn_invoice)

        // Setup Title Bar
        tvTitleBar.text = "Detail Tukang"
        tvDescription.text = "Top Mortar Tukang"

        etName.hint = "Masukkan Nama Tukang"
        etOwner.hint = "Masukkan Nama Lengkap Tukang"
        etBirthday.hint = "Atur tanggal lahir tukang"
        btnInvoice.visibility = View.GONE
        skillContainer.visibility = View.VISIBLE
        binding.paymentMethodContainer.visibility = View.GONE
        binding.bottomAction.visibility = View.GONE
        binding.textLoading.visibility = View.GONE
        binding.btnDeliveryContainer.visibility = View.GONE
        terminContainer.visibility = View.GONE
        ownerSection.visibility = View.GONE
        binding.promoContainer.visibility = View.GONE
        binding.reputationContainer.visibility = View.GONE
        binding.etPhoneContainer2.visibility = View.GONE
        binding.etPhoneLine.visibility = View.GONE

        // Setup Date Picker Dialog
        setDatePickerDialog()

        // Setup Dialog Search
        setupDialogSearch()
        setupDialogSearchSkill()

        // Setup Dialog Send Message
        setupDialogSendMessage()

        // Setup KTP Image Picker
        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                chooseFile()
            } else {
                handleMessage(this@DetailTukangActivity, "CAMERA ACCESS DENIED", "Izin kamera ditolak")
            }
            binding.etKtp.clearFocus()
        }
        imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                selectedUri = if (data == null || data.data == null) currentPhotoUri else data.data
                binding.tvSelectedKtp.text = "File terpilih: " + selectedUri?.let { getFileNameFromUri(it) }
//                navigateToPreviewKtp()
            }
            binding.etKtp.clearFocus()
        }

    }

    private fun initClickHandler() {

        icBack.setOnClickListener { backHandler() }
        icEdit.setOnClickListener { toggleEdit(true) }
        icClose.setOnClickListener { toggleEdit(false) }
//        btnSendMessage.setOnClickListener { navigateAddNewRoom() }
        btnSendMessage.setOnClickListener { sendMessageModal.show() }
        btnSaveEdit.setOnClickListener { editConfirmation() }
        etBirthdayContainer.setOnClickListener { datePicker.show() }
        etBirthday.setOnClickListener { datePicker.show() }
        etMapsContainer.setOnClickListener { getCoordinate() }
        etMaps.setOnClickListener { getCoordinate() }
        etLocationContainer.setOnClickListener { showSearchModal() }
        etLocation.setOnClickListener { showSearchModal() }
        etSkillContainer.setOnClickListener { showSearchModalSkill() }
        etSkill.setOnClickListener { showSearchModalSkill() }
        addressContainer.setOnClickListener {
            if (isEdit) etAddress.requestFocus()
        }
        tvMapsContainer.setOnClickListener { mapsActionHandler() }
        tvKtpContainer.setOnClickListener { previewKtp() }

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
        etMaps.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                getCoordinate()
                etMaps.setSelection(etMaps.length())
            } else etMaps.clearFocus()
        }
        etLocation.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showSearchModal()
                etLocation.setSelection(etLocation.length())
            } else etLocation.clearFocus()
        }
        etSkill.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showSearchModalSkill()
                etSkill.setSelection(etSkill.length())
            } else etSkill.clearFocus()
        }
        binding.etKtp.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                chooseFile()
                binding.etKtp.setSelection(binding.etKtp.length())
            } else binding.etKtp.clearFocus()
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
        etSkill.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (isEdit) showSearchModalSkill()
            }

        })
        binding.etKtp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (isEdit) chooseFile()
            }

        })
        //////////

        // Tooltip Handler
        tooltipHandler(tooltipPhone, "Nomor Telpon (WA)")
        tooltipHandler(tooltipOwner, "Nama Lengkap Tukang")
        tooltipHandler(tooltipLocation, "Kota Tukang")
        tooltipHandler(tooltipSkill, "Keahlian Tukang")
        tooltipHandler(tooltipBirthday, "Tanggal Lahir Tukang")

        val tooltipMapsText = "Maps URL"
        val tooltipMapsTextOpen = "Tekan untuk menampilkan lokasi pada maps"
        tooltipMaps.setOnClickListener {
            if (tvMaps.text != EMPTY_FIELD_VALUE) TooltipCompat.setTooltipText(tooltipMaps, tooltipMapsTextOpen)
            else TooltipCompat.setTooltipText(tooltipMaps, tooltipMapsText)
        }
        tooltipMaps.setOnLongClickListener {
            if (tvMaps.text != EMPTY_FIELD_VALUE) TooltipCompat.setTooltipText(tooltipMaps, tooltipMapsTextOpen)
            else TooltipCompat.setTooltipText(tooltipMaps, tooltipMapsText)
            false
        }
        val tooltipKtpText = "File Foto"
        val tooltipKtpTextOpen = "Tekan untuk menampilkan Foto"
        tooltipKtp.setOnClickListener {
            if (binding.tvKtp.text != EMPTY_FIELD_VALUE) TooltipCompat.setTooltipText(tooltipKtp, tooltipKtpTextOpen)
            else TooltipCompat.setTooltipText(tooltipKtp, tooltipKtpText)
        }
        tooltipKtp.setOnLongClickListener {
            if (binding.tvKtp.text != EMPTY_FIELD_VALUE) TooltipCompat.setTooltipText(tooltipKtp, tooltipKtpTextOpen)
            else TooltipCompat.setTooltipText(tooltipKtp, tooltipKtpText)
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
        val iDate = intent.getStringExtra(CONST_DATE)
//        var iPostedBy = intent.getStringExtra(CONST_POSTED_BY)
        var iPostedName = intent.getStringExtra(CONST_POSTED_NAME)

        if (iDate.isNullOrEmpty()) {
            binding.dateSeparator.visibility = View.GONE
            binding.line.visibility = View.VISIBLE
            if (!iPostedName.isNullOrEmpty()) {
                binding.textBy.text = "oleh $iPostedName"
                binding.textBy.visibility = View.VISIBLE
                binding.dateSeparator.visibility = View.VISIBLE
                binding.tvDate.visibility = View.GONE
                binding.line.visibility = View.GONE
            }
        } else {
            val date = DateFormat.format(iDate)

            if (!iPostedName.isNullOrEmpty()) {
                binding.textBy.text = "oleh $iPostedName"
                binding.textBy.visibility = View.VISIBLE
            }

            binding.tvDate.text = date
            binding.dateSeparator.visibility = View.VISIBLE
            binding.line.visibility = View.GONE
        }

        iKtp = intent.getStringExtra(CONST_KTP)
        iMapsUrl = intent.getStringExtra(CONST_MAPS)
        iStatus = intent.getStringExtra(CONST_STATUS)
        if (!iStatus.isNullOrEmpty()) {
            tooltipStatus.visibility = View.VISIBLE
        }
        iAddress = intent.getStringExtra(CONST_ADDRESS)
        iLocation = intent.getStringExtra(CONST_LOCATION)
        iSkill = intent.getStringExtra(CONST_SKILL)

        activityRequestCode = intent.getIntExtra(ACTIVITY_REQUEST_CODE, activityRequestCode)

        itemSendMessage = TukangModel(nama = iName!!, nomorhp = iPhone!!, nama_lengkap = iOwner!!, tgl_lahir = iBirthday!!, maps_url = iMapsUrl!!, id_city = iLocation!!)
        setupDialogSendMessage(itemSendMessage)

        if (!iContactId.isNullOrEmpty() ) {
            contactId = iContactId
        }
        if (iPhone.isNotEmpty()) {
            tvPhone.text = "+$iPhone"
            etPhone.setText(iPhone)
        } else {
            tvPhone.text = EMPTY_FIELD_VALUE
            etPhone.setText("")
        }
        if (iName.isNotEmpty()) {
            tvName.text = iName
            etName.setText(iName)
        } else {
            tvName.text = EMPTY_FIELD_VALUE
            etName.setText("")
        }
        if (iOwner.isNotEmpty()) {
            tvOwner.text = iOwner
            etOwner.setText(iOwner)
        } else {
            tvOwner.text = EMPTY_FIELD_VALUE
            etOwner.setText("")
        }
        if (!iLocation.isNullOrEmpty()) {
            tvLocation.text = getString(R.string.txt_loading)
            etLocation.setText(getString(R.string.txt_loading))
        } else {
            tvLocation.text = EMPTY_FIELD_VALUE
            etLocation.setText("")
        }
        if (!iSkill.isNullOrEmpty()) {
            tvSkill.text = getString(R.string.txt_loading)
            etSkill.setText(getString(R.string.txt_loading))
        } else {
            tvSkill.text = EMPTY_FIELD_VALUE
            etSkill.setText("")
        }
        if (iBirthday.isNotEmpty()) {
            if (iBirthday == "0000-00-00") {
                tvBirthday.text = EMPTY_FIELD_VALUE
            } else {
                tvBirthday.text = DateFormat.format(iBirthday)
                etBirthday.setText(DateFormat.format(iBirthday))
            }
        }
        if (!iMapsUrl.isNullOrEmpty()) {
            tvMaps.text = "Tekan untuk menampilkan lokasi"
            etMaps.setText(iMapsUrl)
        } else {
            iMapsUrl = EMPTY_FIELD_VALUE
            tvMaps.text = EMPTY_FIELD_VALUE
            etMaps.setText("")
        }
        if (!iKtp.isNullOrEmpty()) {
            binding.tvKtp.text = "Tekan untuk menampilkan Foto"
            binding.etKtp.setText("")
        } else {
            iKtp = EMPTY_FIELD_VALUE
            binding.tvKtp.text = EMPTY_FIELD_VALUE
            binding.etKtp.setText("")
        }
        binding.tvKtp.visibility = View.VISIBLE

        // Other columns handle
        if (!iAddress.isNullOrEmpty()) etAddress.setText(iAddress)
        else etAddress.setText(EMPTY_FIELD_VALUE)

        // Set Spinner
        setupStatusSpinner()

    }

    private fun getCoordinate() {
        val data = "${ etMaps.text }"

        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra(CONST_MAPS, data)
        intent.putExtra(GET_COORDINATE, true)
        startActivityForResult(intent, REQUEST_EDIT_CONTACT_COORDINATE)
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
            tvSkillContainer.visibility = View.GONE
            tvMapsContainer.visibility = View.GONE
            tvBirthdayContainer.visibility = View.GONE
            tvKtpContainer.visibility = View.GONE
            binding.spinPhoneCategories1.visibility = View.GONE

            btnSendMessage.visibility = View.GONE

            // Show Case
            tvTitleBar.text = "Edit Contact"
            icClose.visibility = View.VISIBLE

            etName.visibility = View.VISIBLE

            etPhoneContainer.visibility = View.VISIBLE
            etOwnerContainer.visibility = View.VISIBLE
            etLocationContainer.visibility = View.VISIBLE
            etSkillContainer.visibility = View.VISIBLE
            etMapsContainer.visibility = View.VISIBLE
            etBirthdayContainer.visibility = View.VISIBLE
            etKtpContainer.visibility = View.VISIBLE
            binding.tvSelectedKtp.visibility = View.VISIBLE
            binding.tvSelectedKtp.text = "File terpilih: "

            // Other Columns Handle
            addressContainer.setBackgroundResource(R.drawable.et_background)
            etAddress.isEnabled = true
            if (iAddress.isNullOrEmpty()) etAddress.setText("")

            statusContainer.setBackgroundResource(R.drawable.et_background)
            tooltipStatus.visibility = View.GONE
            tvStatus.visibility = View.GONE
            spinStatus.visibility = View.VISIBLE


            btnSaveEdit.visibility = View.VISIBLE

            etName.requestFocus()
            etName.setSelection(etName.text.length)

//            binding.bottomAction.visibility = View.VISIBLE
//            binding.contactAction.visibility = View.VISIBLE

        } else {

            // Show Case
            icEdit.visibility = View.VISIBLE

            tvName.visibility = View.VISIBLE
            tvDescription.visibility = View.VISIBLE

            tvPhoneContainer.visibility = View.VISIBLE
            tvOwnerContainer.visibility = View.VISIBLE
            tvLocationContainer.visibility = View.VISIBLE
            tvSkillContainer.visibility = View.VISIBLE
            tvMapsContainer.visibility = View.VISIBLE
            tvBirthdayContainer.visibility = View.VISIBLE
            tvKtpContainer.visibility = View.VISIBLE

            btnSendMessage.visibility = View.VISIBLE

            // Hide Case
            tvTitleBar.text = "Detail Contact"
            icClose.visibility = View.GONE

            etName.visibility = View.GONE

            etPhoneContainer.visibility = View.GONE
            etOwnerContainer.visibility = View.GONE
            etLocationContainer.visibility = View.GONE
            etSkillContainer.visibility = View.GONE
            etMapsContainer.visibility = View.GONE
            etBirthdayContainer.visibility = View.GONE
            etKtpContainer.visibility = View.GONE
            binding.tvSelectedKtp.visibility = View.GONE
            selectedUri = null

            // Other Columns Handle
            addressContainer.setBackgroundResource(R.drawable.background_rounded_16)
            etAddress.isEnabled = false
            if (iAddress.isNullOrEmpty()) etAddress.setText(EMPTY_FIELD_VALUE)

            statusContainer.setBackgroundResource(R.drawable.background_rounded_16)
            if (!iStatus.isNullOrEmpty()) {
                tooltipStatus.visibility = View.VISIBLE
            }
            tvStatus.visibility = View.VISIBLE
            spinStatus.visibility = View.GONE

            btnSaveEdit.visibility = View.GONE

            etName.clearFocus()

//            binding.bottomAction.visibility = View.GONE
//            binding.contactAction.visibility = View.GONE

        }

    }

    private fun editConfirmation() {

        if (!formValidation("${ etPhone.text }","${ etName.text }")) return

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Perubahan")
            .setMessage("Apakah anda yakin ingin menyimpan perubahan?")
            .setNegativeButton("Tidak") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Iya") { dialog, _ ->
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
        val pMapsUrl = "${ etMaps.text }"
        val pAddress = "${ etAddress.text }"
        val pStatus = if (selectedStatus.isEmpty()) "" else selectedStatus.substringBefore(" - ").toLowerCase(
            Locale.getDefault())

        var imagePart: MultipartBody.Part? = null

        if (iKtp.isNullOrEmpty() && selectedUri != null || !iKtp.isNullOrEmpty() && selectedUri != null) {
            val imgUri = CompressImageUtil.compressImage(this@DetailTukangActivity, selectedUri!!, 50)
            val contentResolver = contentResolver
            val inputStream = contentResolver.openInputStream(imgUri!!)
            val byteArray = inputStream?.readBytes()

            if (byteArray != null) {
                val requestFile: RequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), byteArray)
                imagePart = MultipartBody.Part.createFormData("ktp", "image.jpg", requestFile)
            } else handleMessage(this, TAG_RESPONSE_CONTACT, "Gambar tidak ditemukan")
        }

        pBirthday = if (pBirthday.isEmpty() || pBirthday == EMPTY_FIELD_VALUE) "0000-00-00"
        else DateFormat.format("${ etBirthday.text }", "dd MMMM yyyy", "yyyy-MM-dd")

        val pCityID = if (selectedCity != null) selectedCity!!.id else "0"
        val pSkillID = if (selectedSkill != null) selectedSkill!!.id else "0"

        loadingState(true)

//        Handler(Looper.getMainLooper()).postDelayed({
//            Toast.makeText(this, "$pPhone : $pName : $pOwner : $pBirthday : $pMapsUrl : $pAddress : $pStatus : $pCityID : $pSkillID", TOAST_SHORT).show()
//            loadingState(false)
//        }, 2000)
//
//        return

        lifecycleScope.launch {
            try {

                val rbId = createPartFromString(contactId!!)
                val rbPhone = createPartFromString(formatPhoneNumber(pPhone))
                val rbName = createPartFromString(pName)
                val rbBirthday = createPartFromString(pBirthday)
                val rbMapsUrl = createPartFromString(pMapsUrl)
                val rbLocation = createPartFromString(pCityID!!)
                val rbAddress = createPartFromString(pAddress)
                val rbStatus = createPartFromString(pStatus)
                val rbSkill = createPartFromString(pSkillID!!)

                val apiService: ApiService = HttpClient.create()
                val response = apiService.editTukang(
                    id = rbId,
                    phone = rbPhone,
                    name = rbName,
//                    namaLengkap = rbOwner,
                    birthday = rbBirthday,
                    cityId = rbLocation,
                    mapsUrl = rbMapsUrl,
                    address = rbAddress,
                    status = rbStatus,
                    skillId = rbSkill,
                    ktp = imagePart?.let { imagePart }
                )

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            itemSendMessage = TukangModel(
                                nomorhp = pPhone,
                                nama = pName,
                                nama_lengkap = pOwner,
                                id_city = pCityID,
                                id_skill = pSkillID,
                                tgl_lahir = pBirthday,
                                maps_url = pMapsUrl
                            )
                            setupDialogSendMessage(itemSendMessage)

                            tvName.text = "${ etName.text }"
                            tvPhone.text = "+" + formatPhoneNumber("${ etPhone.text }")
                            etPhone.setText(formatPhoneNumber("${ etPhone.text }"))
                            iAddress = "${ etAddress.text }"

                            if (!etOwner.text.isNullOrEmpty()) tvOwner.text = "${ etOwner.text }"
                            else tvOwner.text = EMPTY_FIELD_VALUE
                            if (!etBirthday.text.isNullOrEmpty()) tvBirthday.text = "${ etBirthday.text }"
                            else tvBirthday.text = EMPTY_FIELD_VALUE
                            if (!etMaps.text.isNullOrEmpty()) {
                                tvMaps.text = "Tekan untuk menampilkan lokasi"
                                iMapsUrl = "${ etMaps.text }"
                            } else {
                                tvMaps.text = EMPTY_FIELD_VALUE
                                iMapsUrl = ""
                            }
                            if (selectedCity != null) {
                                if (selectedCity!!.id != "0") tvLocation.text = "${ etLocation.text }"
                                else tvLocation.text = EMPTY_FIELD_VALUE
                            } else tvLocation.text = EMPTY_FIELD_VALUE
                            if (selectedSkill != null) {
                                if (selectedSkill!!.id != "0") tvSkill.text = "${ etSkill.text }"
                                else tvSkill.text = EMPTY_FIELD_VALUE
                            } else tvSkill.text = EMPTY_FIELD_VALUE

                            iStatus = pStatus.ifEmpty { null }

                            getDetailTukang()

                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            handleMessage(this@DetailTukangActivity, TAG_RESPONSE_MESSAGE, "Gagal mengubah! Message: ${ responseBody.message }")
                            loadingState(false)
                            toggleEdit(false)

                        }
                        else -> {

                            handleMessage(this@DetailTukangActivity, TAG_RESPONSE_MESSAGE, "Gagal mengubah data!")
                            loadingState(false)
                            toggleEdit(false)

                        }
                    }

                } else {

                    handleMessage(this@DetailTukangActivity, TAG_RESPONSE_MESSAGE, "Gagal mengubah data! Error: " + response.message())
                    loadingState(false)
                    toggleEdit(false)

                }


            } catch (e: Exception) {

                handleMessage(this@DetailTukangActivity, TAG_RESPONSE_MESSAGE, "Failed run service. Exception " + e.message)
                loadingState(false)
                toggleEdit(false)

            }

        }

    }

    private fun getDetailTukang() {
        loadingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = contactId?.let { apiService.getDetailTukang(tukangId = it) }

                if (response!!.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            val data = responseBody.results[0]
                            if (data.ktp_tukang.isNotEmpty()) {
                                binding.tvKtp.text = "Tekan untuk menampilkan Foto"
                                iKtp = data.ktp_tukang
                            }

                            handleMessage(this@DetailTukangActivity, TAG_RESPONSE_MESSAGE, "Berhasil mengubah data!")
                            loadingState(false)
                            toggleEdit(false)

                            setupStatus(iStatus)

                            hasEdited = true
                            loadingState(false)

                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            handleMessage(this@DetailTukangActivity, TAG_RESPONSE_MESSAGE, "Gagal memuat detail! Message: Response status $RESPONSE_STATUS_FAIL or $RESPONSE_STATUS_FAILED")
                            loadingState(false)
                            toggleEdit(false)

                        }
                        else -> {

                            handleMessage(this@DetailTukangActivity, TAG_RESPONSE_MESSAGE, "Gagal memuat detail!")
                            loadingState(false)
                            toggleEdit(false)

                        }
                    }

                } else {

                    handleMessage(this@DetailTukangActivity, TAG_RESPONSE_MESSAGE, "Gagal memuat detail! Error: " + response.message())
                    loadingState(false)
                    toggleEdit(false)

                }


            } catch (e: Exception) {

                handleMessage(this@DetailTukangActivity, TAG_RESPONSE_MESSAGE, "Failed run service. Exception " + e.message)
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
            btnSaveEdit.text = getString(R.string.txt_loading)

        } else {

            btnSaveEdit.isEnabled = true
            btnSaveEdit.text = getString(R.string.save)
            btnSaveEdit.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))

        }

    }

    private fun formValidation(phone: String, name: String): Boolean {
        return if (name.isEmpty()) {
            etName.error = "Nama wajib diisi!"
            etName.requestFocus()
            false
        } else if (phone.isEmpty()) {
            etPhone.error = "Nomor telpon wajib diisi!"
            etPhone.requestFocus()
            false
        } else if (!PhoneHandler.phoneValidation(phone, etPhone)) {
            etPhone.requestFocus()
            false
//        } else if (mapsUrl.isNotEmpty() && !isValidUrl(mapsUrl)) {
//            etMaps.error = "Please enter a valid URL!"
//            etMaps.requestFocus()
//            false
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
            etSkill.error = null
            etSkill.clearFocus()
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

    private fun setupDialogSearch(items: ArrayList<ModalSearchModel> = ArrayList()) {

        searchModal = SearchModal(this, items)
        searchModal.setCustomDialogListener(this)
        searchModal.searchHint = "Masukkan nama kota…"
        searchModal.setOnDismissListener {
            etLocation.clearFocus()
            isSearchCity = false
        }

    }

    private fun setupDialogSearchSkill(items: ArrayList<ModalSearchModel> = ArrayList()) {

        searchModalSkill = SearchModal(this, items)
        searchModalSkill.setCustomDialogListener(this)
        searchModalSkill.label = "Pilih Opsi Keahlian"
        searchModalSkill.searchHint = "Masukkan nama keahlian…"
        searchModalSkill.setOnDismissListener {
            etSkill.clearFocus()
            isSearchSkill = false
        }

    }

    private fun setupDialogSendMessage(item: TukangModel? = null) {

        sendMessageModal = SendMessageTukangModal(this, lifecycleScope)
        if (item != null) sendMessageModal.setItem(item)

        // Setup Indicator
        // setupNetworkIndicator()

        // Panggil layanan untuk memulai operasi ping di latar belakang
        if (pingUtility == null) {
            pingUtility = PingUtility()
            binding.titleBar.indicatorView.visibility = View.VISIBLE
            pingUtility!!.startPingMonitoring(host = PING_HOST, listener = object: PingUtility.PingResultListener {
                override fun onPingResult(result: Long) {

                    binding.titleBar.tvIndicatorView.text = "$result ms"

                    if (result > 999) {
                        binding.titleBar.indicatorView.setBackgroundResource(R.drawable.bg_primary_round)
                        binding.titleBar.indicatorView.visibility = View.GONE
                        binding.titleBar.tvIndicatorView.text = "999+ ms"
                        binding.titleBar.tvIndicatorView.setTextColor(getColor(R.color.primary))
                        binding.titleBar.tvIndicatorView.visibility = View.VISIBLE
                        sendMessageModal.setPingStatus(PING_MEDIUM)
                    }
                    else if (result > 350) {
                        binding.titleBar.indicatorView.setBackgroundResource(R.drawable.bg_primary_round)
                        binding.titleBar.indicatorView.visibility = View.GONE
                        binding.titleBar.tvIndicatorView.setTextColor(getColor(R.color.primary))
                        binding.titleBar.tvIndicatorView.visibility = View.VISIBLE
                        sendMessageModal.setPingStatus(PING_MEDIUM)
                    }
                    else if (result > 300) {
                        binding.titleBar.indicatorView.setBackgroundResource(R.drawable.bg_data_round)
                        binding.titleBar.indicatorView.visibility = View.GONE
                        binding.titleBar.tvIndicatorView.setTextColor(getColor(R.color.status_data))
                        binding.titleBar.tvIndicatorView.visibility = View.VISIBLE
                        sendMessageModal.setPingStatus(PING_MEDIUM)
                    }
                    else if (result > 0) {
                        binding.titleBar.indicatorView.setBackgroundResource(R.drawable.bg_active_round)
                        binding.titleBar.indicatorView.visibility = View.VISIBLE
                        binding.titleBar.tvIndicatorView.setTextColor(getColor(R.color.status_active))
                        binding.titleBar.tvIndicatorView.visibility = View.GONE
                        sendMessageModal.setPingStatus(PING_NORMAL)
                    } else {
                        binding.titleBar.indicatorView.setBackgroundResource(R.drawable.bg_primary_round)
                        binding.titleBar.indicatorView.visibility = View.GONE
                        binding.titleBar.tvIndicatorView.text = "999+ ms"
                        binding.titleBar.tvIndicatorView.setTextColor(getColor(R.color.primary))
                        binding.titleBar.tvIndicatorView.visibility = View.VISIBLE
                        sendMessageModal.setPingStatus(PING_MEDIUM)
                    }
                }

            })
        }
    }

    private fun showSearchModal() {
        isSearchCity = true
//        val searchKey = etLocation.text.toString()
//        if (searchKey.isNotEmpty()) searchModal.setSearchKey(searchKey)
        searchModal.show()
    }

    private fun showSearchModalSkill() {
        isSearchSkill = true
//        val searchKey = etSkill.text.toString()
//        if (searchKey.isNotEmpty()) searchModalSkill.setSearchKey(searchKey)
        searchModalSkill.show()
    }

    private fun getCities() {

        // Get Cities
        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getCities(distributorID = userDistributorId)

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

                        handleMessage(this@DetailTukangActivity, "LIST CITY", "Daftar kota kosong!")
//                        searchModal.isLoading(true)

                    }
                    else -> {

                        handleMessage(this@DetailTukangActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
//                        searchModal.isLoading(true)

                    }
                }


            } catch (e: Exception) {

                handleMessage(this@DetailTukangActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
//                searchModal.isLoading(true)

            }

        }
    }

    private fun getSkills() {

        // Get Skills
        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getSkills(distributorID = userDistributorId)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        val results = response.results
                        val items: ArrayList<ModalSearchModel> = ArrayList()

                        for (i in 0 until results.size) {
                            val data = results[i]
                            items.add(ModalSearchModel(data.id_skill, "${data.nama_skill} - ${data.kode_skill}"))
                        }

                        setupDialogSearchSkill(items)
//                        searchModal.isLoading(false)

                        val foundItem = results.find { it.id_skill == iSkill }
                        if (foundItem != null) {
                            tvSkill.text = "${foundItem.nama_skill} - ${foundItem.kode_skill}"
                            etSkill.setText("${foundItem.nama_skill} - ${foundItem.kode_skill}")
                            selectedSkill = ModalSearchModel(foundItem.id_skill, foundItem.nama_skill)
                        } else {
                            tvSkill.text = EMPTY_FIELD_VALUE
                            etSkill.setText("")
                        }

                        // Admin Access
                        if (sessionManager.userKind() == USER_KIND_ADMIN || sessionManager.userKind() == USER_KIND_ADMIN_CITY || sessionManager.userKind() == USER_KIND_BA) {
                            icEdit.visibility = View.VISIBLE
                            tvKtpContainer.visibility = View.VISIBLE
                            if (sessionManager.userKind() == USER_KIND_ADMIN || sessionManager.userKind() == USER_KIND_ADMIN_CITY) {
                                binding.bottomAction.visibility = View.VISIBLE
                                binding.contactAction.visibility = View.VISIBLE
                            }
                        }
                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(this@DetailTukangActivity, "LIST SKILL", "Daftar keahlian kosong!")
//                        searchModal.isLoading(true)

                    }
                    else -> {

                        handleMessage(this@DetailTukangActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
//                        searchModal.isLoading(true)

                    }
                }


            } catch (e: Exception) {

                handleMessage(this@DetailTukangActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
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

            Handler(Looper.getMainLooper()).postDelayed({
                overlayMaps.alpha = 0f
                overlayMaps.visibility = View.GONE

                val intent = Intent(this@DetailTukangActivity, MapsActivity::class.java)
                intent.putExtra(CONST_MAPS, iMapsUrl)
                startActivity(intent)
            }, animateDuration)
        }
    }

    private fun previewKtp() {
        if (!iKtp.isNullOrEmpty() && iKtp != EMPTY_FIELD_VALUE && !isEdit) {

            val imageUrl = BASE_URL + "img/" + iKtp
            val intent = Intent(this, PreviewKtpActivity::class.java)
            intent.putExtra(CONST_KTP, imageUrl)
            startActivity(intent)

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

    private fun setupStatus(status: String? = null) {
        tooltipStatus.visibility = View.VISIBLE
        when (status) {STATUS_CONTACT_DATA -> {
            tooltipStatus.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.status_data))
            tooltipHandler(tooltipStatus, "Customer Status is Data")
            tvStatus.text = statusItem[1]
            spinStatus.setSelection(1)
        }
            STATUS_CONTACT_PASSIVE -> {
                tooltipStatus.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.status_passive))
                tooltipHandler(tooltipStatus, "Customer Status is Passive")
                tvStatus.text = statusItem[2]
                spinStatus.setSelection(2)
            }
            STATUS_CONTACT_ACTIVE -> {
                tooltipStatus.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.status_active))
                tooltipHandler(tooltipStatus, "Customer Status is Active")
                tvStatus.text = statusItem[3]
                spinStatus.setSelection(3)
            }
            STATUS_CONTACT_BLACKLIST -> {
                tooltipStatus.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.status_blacklist))
                tooltipHandler(tooltipStatus, "Customer Status is Blacklist")
                tvStatus.text = statusItem[4]
                spinStatus.setSelection(4)
            }
            else -> {
                tooltipStatus.visibility = View.GONE
                tvStatus.text = EMPTY_FIELD_VALUE
                spinStatus.setSelection(0)
            }
        }
    }

    private fun setupStatusSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusItem)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinStatus.adapter = adapter
        spinStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedStatus = if (position != 0) statusItem[position]
                else ""
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        setupStatus(iStatus)
    }

    private fun chooseFile() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            // Create a file to store the captured image
            val photoFile: File? = createImageFile()

            if (photoFile != null) {
                val photoUri: Uri = FileProvider.getUriForFile(this, "com.topmortar.topmortarsales.fileprovider", photoFile)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                currentPhotoUri = photoUri
            }

            val chooserIntent = Intent.createChooser(galleryIntent, "Pilih Gambar")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

            imagePicker!!.launch(chooserIntent)
        } else {
            cameraPermissionLauncher!!.launch(Manifest.permission.CAMERA)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir("Invoices")
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    @SuppressLint("Range")
    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                fileName = displayName
            }
        }
        return fileName
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DETAIL_ACTIVITY_REQUEST_CODE) {

            val resultData = data?.getStringExtra("$DETAIL_ACTIVITY_REQUEST_CODE")

            if (resultData == SYNC_NOW) hasEdited = true

        } else if (requestCode == REQUEST_EDIT_CONTACT_COORDINATE) {
            val latitude = data?.getDoubleExtra("latitude", 0.0)
            val longitude = data?.getDoubleExtra("longitude", 0.0)
            if (latitude != null && longitude != null) etMaps.setText("$latitude,$longitude")
            etMaps.clearFocus()
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isEdit) toggleEdit(false)
        else {

            if (hasEdited) {

                val resultIntent = Intent()
                resultIntent.putExtra("$activityRequestCode", SYNC_NOW)
                setResult(RESULT_OK, resultIntent)

                super.onBackPressed()

            } else super.onBackPressed()

        }
    }

    // Interfade Search Modal
    override fun onDataReceived(data: ModalSearchModel) {
        if (isSearchCity) {
            etLocation.setText(data.title)
            selectedCity = data
        } else if (isSearchSkill) {
            etSkill.setText(data.title)
            selectedSkill = data
        }
        isSearchCity = false
        isSearchSkill = false
    }

    override fun onPingResult(pingResult: Int?) {
        sendMessageModal.setPingStatus(pingResult)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (pingUtility != null) pingUtility!!.stopPingMonitoring()
    }

}