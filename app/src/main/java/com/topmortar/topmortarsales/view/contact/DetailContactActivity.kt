package com.topmortar.topmortarsales.view.contact

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_COURIER
import com.topmortar.topmortarsales.commons.BASE_URL
import com.topmortar.topmortarsales.commons.CONST_ADDRESS
import com.topmortar.topmortarsales.commons.CONST_BIRTHDAY
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_DATE
import com.topmortar.topmortarsales.commons.CONST_DELIVERY_ID
import com.topmortar.topmortarsales.commons.CONST_IS_TRACKING
import com.topmortar.topmortarsales.commons.CONST_KTP
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_MAPS_NAME
import com.topmortar.topmortarsales.commons.CONST_MAPS_STATUS
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_OWNER
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.CONST_PROMO
import com.topmortar.topmortarsales.commons.CONST_REPUTATION
import com.topmortar.topmortarsales.commons.CONST_STATUS
import com.topmortar.topmortarsales.commons.CONST_TERMIN
import com.topmortar.topmortarsales.commons.CONST_URI
import com.topmortar.topmortarsales.commons.DETAIL_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.EMPTY_FIELD_VALUE
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_DELIVERY
import com.topmortar.topmortarsales.commons.GET_COORDINATE
import com.topmortar.topmortarsales.commons.IMG_PREVIEW_STATE
import com.topmortar.topmortarsales.commons.IS_CLOSING
import com.topmortar.topmortarsales.commons.LOCATION_PERMISSION_REQUEST_CODE
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
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_BID
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_BLACKLIST
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_DATA
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_PASSIVE
import com.topmortar.topmortarsales.commons.STATUS_TERMIN_30
import com.topmortar.topmortarsales.commons.STATUS_TERMIN_45
import com.topmortar.topmortarsales.commons.STATUS_TERMIN_60
import com.topmortar.topmortarsales.commons.STATUS_TERMIN_COD
import com.topmortar.topmortarsales.commons.STATUS_TERMIN_COD_TF
import com.topmortar.topmortarsales.commons.STATUS_TERMIN_COD_TUNAI
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN_CITY
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.services.TrackingService
import com.topmortar.topmortarsales.commons.utils.CompressImageUtil
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.PhoneHandler
import com.topmortar.topmortarsales.commons.utils.PhoneHandler.formatPhoneNumber
import com.topmortar.topmortarsales.commons.utils.PingUtility
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.URLUtility
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityDetailContactBinding
import com.topmortar.topmortarsales.modal.AddVoucherModal
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.modal.SendMessageModal
import com.topmortar.topmortarsales.model.ContactModel
import com.topmortar.topmortarsales.model.DeliveryModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import com.topmortar.topmortarsales.view.MapsActivity
import com.topmortar.topmortarsales.view.reports.NewReportActivity
import com.topmortar.topmortarsales.view.reports.ReportsActivity
import com.topmortar.topmortarsales.view.reports.UsersReportActivity
import com.topmortar.topmortarsales.view.suratJalan.ListSuratJalanActivity
import com.topmortar.topmortarsales.view.suratJalan.PreviewClosingActivity
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
import java.util.Timer
import java.util.TimerTask


@Suppress("DEPRECATION")
class DetailContactActivity : AppCompatActivity(), SearchModal.SearchModalListener,
    PingUtility.PingResultInterface, SendMessageModal.SendMessageModalInterface {

    private lateinit var sessionManager: SessionManager
    private val userKind get() = sessionManager.userKind().toString()
//    private val userID = "8"
    private val userID get() = sessionManager.userID().toString()
    private val username get() = sessionManager.userName().toString()
//    private val fulllName = "Ple Courier"
    private val fulllName get() = sessionManager.fullName().toString()
    private val userCityID get() = sessionManager.userCityID().toString()
    private val userDistributorId get() = sessionManager.userDistributor().toString()
    private val userCity get() = sessionManager.userCityID().toString()
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
    private lateinit var tvOwnerContainer: LinearLayout
    private lateinit var etOwnerContainer: LinearLayout
    private lateinit var tvMapsContainer: RelativeLayout
    private lateinit var etMapsContainer: LinearLayout
    private lateinit var overlayMaps: View

    private lateinit var statusContainer: LinearLayout
    private lateinit var terminContainer: LinearLayout
    private lateinit var reputationContainer: LinearLayout
    private lateinit var promoContainer: LinearLayout
    private lateinit var addressContainer: LinearLayout

    private lateinit var icBack: ImageView
    private lateinit var icEdit: ImageView
    private lateinit var icClose: ImageView

    private lateinit var tooltipPhone: ImageView
    private lateinit var tooltipOwner: ImageView
    private lateinit var tooltipBirthday: ImageView
    private lateinit var tooltipKtp: ImageView
    private lateinit var tooltipLocation: ImageView
    private lateinit var tooltipMaps: ImageView

    private lateinit var tooltipStatus: ImageView

    private lateinit var tvTitleBar: TextView
    private lateinit var tvName: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvBirthday: TextView
    private lateinit var tvKtp: TextView
    private lateinit var tvSelectedKtp: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvOwner: TextView
    private lateinit var tvMaps: TextView
    private lateinit var etName: EditText
    private lateinit var etOwner: EditText
    private lateinit var etPhone: EditText
    private lateinit var etBirthday: EditText
    private lateinit var etKtp: EditText
    private lateinit var etLocation: EditText
    private lateinit var etMaps: EditText

    private lateinit var tvStatus: TextView
    private lateinit var tvTermin: TextView
    private lateinit var tvReputation: TextView
    private lateinit var tvPromo: TextView
    private lateinit var etPromo: EditText
    private lateinit var spinStatus: Spinner
    private lateinit var spinTermin: Spinner
    private lateinit var spinReputation: Spinner
    private lateinit var etAddress: EditText

    private lateinit var btnSendMessage: Button
    private lateinit var btnSaveEdit: Button
    private lateinit var btnInvoice: LinearLayout

    private var activityRequestCode = MAIN_ACTIVITY_REQUEST_CODE
    private var contactId: String? = null
    private var isEdit: Boolean = false
    private var hasEdited: Boolean = false
    private var isClosingAction: Boolean = false
    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedCity: ModalSearchModel? = null
    private var selectedPromo: ModalSearchModel? = null
    private var itemSendMessage: ContactModel? = null

    private var statusItem: List<String> = listOf("Pilih Status", "Data - New Customer", "Passive - Long time no visit", "Active - Need a visit", "Blacklist - Cannot be visited", "Bid - Customers are being Bargained")
    private var terminItem: List<String> = listOf("Pilih Termin Payment", "COD", "COD + Transfer", "COD + Tunai", "30 Hari", "45 Hari", "60 Hari")
    private var reputationItem: List<String> = listOf("Pilih Reputasi Toko", "Good", "Bad")
    private var selectedStatus: String = ""
    private var selectedTermin: String = ""
    private var selectedReputation: String = ""
    private var cameraPermissionLauncher: ActivityResultLauncher<String>? = null
    private var imagePicker: ActivityResultLauncher<Intent>? = null
    private var selectedUri: Uri? = null
    private var currentPhotoUri: Uri? = null

    private var iLocation: String? = null
    private var iStatus: String? = null
    private var iTermin: String? = null
    private var iReputation: String? = null
    private var iAddress: String? = null
    private var iMapsUrl: String? = null
    private var iKtp: String? = null
    private var iPromo: String? = null

    private var isSearchCity = false
    private var isSearchPromo = false

    private lateinit var datePicker: DatePickerDialog
    private lateinit var searchModal: SearchModal
    private lateinit var searchPromoModal: SearchModal
    private lateinit var sendMessageModal: SendMessageModal
    private lateinit var bottomSheetDialog: BottomSheetDialog

    // Tracking
    private var isDeliveryLoading = false
    private var firebaseReference: DatabaseReference? = null
    private var childDelivery: DatabaseReference? = null
    private var childDriver: DatabaseReference? = null
    private var deliveryId: String = ""
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)
        binding = ActivityDetailContactBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initVariable()
        initClickHandler()
        dataActivityValidation()
        checkLocationPermission()

        // Get List City
        getCities()
        if (userKind == USER_KIND_COURIER) {
            setupDelivery()
        } else {
            binding.btnDeliveryContainer.visibility = View.GONE
            binding.contactAction.visibility = View.VISIBLE
        }
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
        tvOwnerContainer = findViewById(R.id.tv_owner_container)
        etOwnerContainer = findViewById(R.id.et_owner_container)
        tvMapsContainer = findViewById(R.id.tv_maps_container)
        etMapsContainer = findViewById(R.id.et_maps_container)
        overlayMaps = findViewById(R.id.overlay_maps)

        statusContainer = findViewById(R.id.status_container)
        terminContainer = findViewById(R.id.termin_container)
        reputationContainer = findViewById(R.id.reputation_container)
        promoContainer = findViewById(R.id.promo_container)
        addressContainer = findViewById(R.id.address_container)

        tooltipPhone = findViewById(R.id.tooltip_phone)
        tooltipOwner = findViewById(R.id.tooltip_owner)
        tooltipLocation = findViewById(R.id.tooltip_location)
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
        tvMaps = findViewById(R.id.tv_maps)
        etMaps = findViewById(R.id.et_maps)
        tvBirthday = findViewById(R.id.tv_birthday)
        etBirthday = findViewById(R.id.et_birthday)
        tvKtp = findViewById(R.id.tv_ktp)
        tvSelectedKtp = findViewById(R.id.tv_selected_ktp)
        etKtp = findViewById(R.id.et_ktp)

        tvStatus = findViewById(R.id.tv_status)
        spinStatus = findViewById(R.id.spin_status)
        tvTermin = findViewById(R.id.tv_termin)
        tvReputation = findViewById(R.id.tv_reputation)
        tvPromo = findViewById(R.id.tv_promo)
        etPromo = findViewById(R.id.et_promo)
        spinTermin = findViewById(R.id.spin_termin)
        spinReputation = findViewById(R.id.spin_reputation)
        etAddress = findViewById(R.id.et_address)

        btnSendMessage = findViewById(R.id.btn_send_message)
        btnSaveEdit = findViewById(R.id.btn_save_edit)
        btnInvoice = findViewById(R.id.btn_invoice)

        // Setup Title Bar
        tvTitleBar.text = "Detail Contact"

        // Setup Date Picker Dialog
        setDatePickerDialog()

        // Setup Dialog Search
        setupDialogSearch()
        setupDialogSearchPromo()

        // Setup Dialog Send Message
        setupDialogSendMessage()

        // Setup KTP Image Picker
        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                chooseFile()
            } else {
                handleMessage(this@DetailContactActivity, "CAMERA ACCESS DENIED", "Izin kamera ditolak")
            }
            etKtp.clearFocus()
        }
        imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                selectedUri = if (data == null || data.data == null) currentPhotoUri else data.data
                tvSelectedKtp.text = "File terpilih: " + selectedUri?.let { getFileNameFromUri(it) }
//                navigateToPreviewKtp()
            }
            etKtp.clearFocus()
        }

        bottomSheetDialog = BottomSheetDialog(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    private fun initClickHandler() {

        icBack.setOnClickListener { backHandler() }
//        icEdit.setOnClickListener { if (sessionManager.userKind() == USER_KIND_ADMIN || sessionManager.userKind() == USER_KIND_SALES) showMoreOption() else toggleEdit(true) }
        icEdit.setOnClickListener { toggleEdit(true) }
        icClose.setOnClickListener { toggleEdit(false) }
//        btnSendMessage.setOnClickListener { navigateAddNewRoom() }
        btnSendMessage.setOnClickListener { sendMessageModal.show() }
        btnSaveEdit.setOnClickListener { editConfirmation() }
        btnInvoice.setOnClickListener { navigateToDetailInvoice() }
        etBirthdayContainer.setOnClickListener { datePicker.show() }
        etBirthday.setOnClickListener { datePicker.show() }
        etMapsContainer.setOnClickListener { getCoordinate() }
        etMaps.setOnClickListener { getCoordinate() }
        etLocationContainer.setOnClickListener { showSearchModal() }
        etLocation.setOnClickListener { showSearchModal() }
        etPromo.setOnClickListener { showSearchPromoModal() }
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
        etPromo.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showSearchPromoModal()
                etPromo.setSelection(etPromo.length())
            } else etPromo.clearFocus()
        }
        etKtp.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                chooseFile()
                etKtp.setSelection(etKtp.length())
            } else etKtp.clearFocus()
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
//        etMaps.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                if (isEdit) getCoordinate()
//            }
//
//        })
        etLocation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (isEdit) showSearchModal()
            }

        })
        etPromo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (isEdit) showSearchPromoModal()
            }

        })
        etKtp.addTextChangedListener(object : TextWatcher {
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
        tooltipHandler(tooltipOwner, "Nama Pemilik")
        tooltipHandler(tooltipLocation, "Kota Pelanggan")
        tooltipHandler(tooltipBirthday, "Tanggal Lahir Pemilik")

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
        val tooltipKtpText = "Ktp File"
        val tooltipKtpTextOpen = "Tekan untuk menampilkan KTP dan melihat detailnya"
        tooltipKtp.setOnClickListener {
            if (tvKtp.text != EMPTY_FIELD_VALUE) TooltipCompat.setTooltipText(tooltipKtp, tooltipKtpTextOpen)
            else TooltipCompat.setTooltipText(tooltipKtp, tooltipKtpText)
        }
        tooltipKtp.setOnLongClickListener {
            if (tvKtp.text != EMPTY_FIELD_VALUE) TooltipCompat.setTooltipText(tooltipKtp, tooltipKtpTextOpen)
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

        if (iDate.isNullOrEmpty()) {
            binding.dateSeparator.visibility = View.GONE
            binding.line.visibility = View.VISIBLE
        } else {
            val date = DateFormat.format(iDate, format = "dd MMM yyyy")

            binding.tvDate.text = date
            binding.dateSeparator.visibility = View.VISIBLE
            binding.line.visibility = View.GONE
        }

        iKtp = intent.getStringExtra(CONST_KTP)
        iMapsUrl = intent.getStringExtra(CONST_MAPS)
        iStatus = intent.getStringExtra(CONST_STATUS)
        iTermin = intent.getStringExtra(CONST_TERMIN)
        iReputation = intent.getStringExtra(CONST_REPUTATION)
//        if (!iStatus.isNullOrEmpty()) {
            tooltipStatus.visibility = View.VISIBLE
            if (iStatus == STATUS_CONTACT_BLACKLIST) btnInvoice.visibility = View.GONE
            else btnInvoice.visibility = View.VISIBLE
//        }
        iAddress = intent.getStringExtra(CONST_ADDRESS)
        iLocation = intent.getStringExtra(CONST_LOCATION)
        iPromo = intent.getStringExtra(CONST_PROMO)

        activityRequestCode = intent.getIntExtra(ACTIVITY_REQUEST_CODE, activityRequestCode)

        itemSendMessage = ContactModel(id_contact = iContactId!!, nama = iName!!, nomorhp = iPhone!!, store_owner = iOwner!!, tgl_lahir = iBirthday!!, maps_url = iMapsUrl!!, id_city = iLocation!!)
        setupDialogSendMessage(itemSendMessage)

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
            tvLocation.text = getString(R.string.txt_loading)
            etLocation.setText(getString(R.string.txt_loading))
        } else {
            tvLocation.text = EMPTY_FIELD_VALUE
            etLocation.setText("")
        }
        if (!iPromo.isNullOrEmpty()) {
            tvPromo.text = getString(R.string.txt_loading)
            etPromo.setText(getString(R.string.txt_loading))
        } else {
            tvPromo.text = EMPTY_FIELD_VALUE
            etPromo.setText("")
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
            tvMaps.text = "Tekan untuk menampilkan lokasi"
            etMaps.setText(iMapsUrl)
        } else {
            iMapsUrl = EMPTY_FIELD_VALUE
            tvMaps.text = EMPTY_FIELD_VALUE
            etMaps.setText("")
        }
        if (!iKtp.isNullOrEmpty()) {
            tvKtp.text = "Tekan untuk menampilkan KTP"
            etKtp.setText("")
        } else {
            iKtp = EMPTY_FIELD_VALUE
            tvKtp.text = EMPTY_FIELD_VALUE
            etKtp.setText("")
        }

        // Other columns handle
        if (!iAddress.isNullOrEmpty()) etAddress.setText(iAddress)
        else etAddress.setText(EMPTY_FIELD_VALUE)

        // Set Spinner
        setupStatusSpinner()
        setupTerminSpinner()
        setupReputationSpinner()

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
        val data = "${ etMaps.text }"

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra(CONST_MAPS, data)
            intent.putExtra(GET_COORDINATE, true)
            startActivityForResult(intent, REQUEST_EDIT_CONTACT_COORDINATE)
        } else checkLocationPermission()
    }

    private fun toggleEdit(value: Boolean? = null) {

        isEdit = if (value!!) value else !isEdit

        if (isEdit) {

            if (sessionManager.userKind() == USER_KIND_ADMIN || sessionManager.userKind() == USER_KIND_ADMIN_CITY) {
                // Hide Case
                icEdit.visibility = View.GONE

                tvName.visibility = View.GONE
                tvDescription.visibility = View.GONE

                tvPhoneContainer.visibility = View.GONE
                tvOwnerContainer.visibility = View.GONE
                tvLocationContainer.visibility = View.GONE
                tvMapsContainer.visibility = View.GONE
                tvBirthdayContainer.visibility = View.GONE
                tvKtpContainer.visibility = View.GONE

                btnSendMessage.visibility = View.GONE
                btnInvoice.visibility = View.GONE

                // Show Case
                tvTitleBar.text = "Edit Contact"
                icClose.visibility = View.VISIBLE

                etName.visibility = View.VISIBLE

                etPhoneContainer.visibility = View.VISIBLE
                etOwnerContainer.visibility = View.VISIBLE
                if (sessionManager.userKind() == USER_KIND_ADMIN_CITY) etLocationContainer.visibility = View.GONE
                else etLocationContainer.visibility = View.VISIBLE
                etMapsContainer.visibility = View.VISIBLE
                etBirthdayContainer.visibility = View.VISIBLE
                etKtpContainer.visibility = View.VISIBLE
                tvSelectedKtp.visibility = View.VISIBLE
                tvSelectedKtp.text = "File terpilih: "

                // Other Columns Handle
                addressContainer.setBackgroundResource(R.drawable.et_background)
                etAddress.isEnabled = true
                if (iAddress.isNullOrEmpty()) etAddress.setText("")

                statusContainer.visibility = View.GONE
                statusContainer.setBackgroundResource(R.drawable.et_background)
                tooltipStatus.visibility = View.GONE
                tvStatus.visibility = View.GONE
                spinStatus.visibility = View.VISIBLE
                terminContainer.setBackgroundResource(R.drawable.et_background)
                tvTermin.visibility = View.GONE
                spinTermin.visibility = View.VISIBLE
                reputationContainer.setBackgroundResource(R.drawable.et_background)
                tvReputation.visibility = View.GONE
                spinReputation.visibility = View.VISIBLE
                tvPromo.visibility = View.GONE
                etPromo.visibility = View.VISIBLE
                promoContainer.setBackgroundResource(R.drawable.et_background)

                btnSaveEdit.visibility = View.VISIBLE

                etName.requestFocus()
                etName.setSelection(etName.text.length)
            } else if (sessionManager.userKind() == USER_KIND_SALES) {
                icEdit.visibility = View.GONE
                tvMapsContainer.visibility = View.GONE
                btnSendMessage.visibility = View.GONE
                btnInvoice.visibility = View.GONE

                tvTitleBar.text = "Edit Contact"
                icClose.visibility = View.VISIBLE
                etMapsContainer.visibility = View.VISIBLE
                btnSaveEdit.visibility = View.VISIBLE
            }

        } else {

            if (sessionManager.userKind() == USER_KIND_ADMIN || sessionManager.userKind() == USER_KIND_ADMIN_CITY) {
                // Show Case
                icEdit.visibility = View.VISIBLE

                tvName.visibility = View.VISIBLE
                tvDescription.visibility = View.VISIBLE

                tvPhoneContainer.visibility = View.VISIBLE
                tvOwnerContainer.visibility = View.VISIBLE
                tvLocationContainer.visibility = View.VISIBLE
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
                etMapsContainer.visibility = View.GONE
                etBirthdayContainer.visibility = View.GONE
                etKtpContainer.visibility = View.GONE
                tvSelectedKtp.visibility = View.GONE
                selectedUri = null

                // Other Columns Handle
                addressContainer.setBackgroundResource(R.drawable.background_rounded_16)
                etAddress.isEnabled = false
                if (iAddress.isNullOrEmpty()) etAddress.setText(EMPTY_FIELD_VALUE)

                statusContainer.visibility = View.VISIBLE
                statusContainer.setBackgroundResource(R.drawable.background_rounded_16)
//            if (!iStatus.isNullOrEmpty()) {
                tooltipStatus.visibility = View.VISIBLE
                if (iStatus == STATUS_CONTACT_BLACKLIST) btnInvoice.visibility = View.GONE
                else btnInvoice.visibility = View.VISIBLE
//            }
                tvStatus.visibility = View.VISIBLE
                spinStatus.visibility = View.GONE
                terminContainer.setBackgroundResource(R.drawable.background_rounded_16)
                tvTermin.visibility = View.VISIBLE
                spinTermin.visibility = View.GONE
                reputationContainer.setBackgroundResource(R.drawable.background_rounded_16)
                tvReputation.visibility = View.VISIBLE
                spinReputation.visibility = View.GONE
                tvPromo.visibility = View.VISIBLE
                etPromo.visibility = View.GONE
                promoContainer.setBackgroundResource(R.drawable.background_rounded_16)

                btnSaveEdit.visibility = View.GONE

                etName.clearFocus()
            } else if (sessionManager.userKind() == USER_KIND_SALES) {
                icEdit.visibility = View.VISIBLE
                tvMapsContainer.visibility = View.VISIBLE
                btnSendMessage.visibility = View.VISIBLE
                tooltipStatus.visibility = View.VISIBLE
                if (iStatus == STATUS_CONTACT_BLACKLIST) btnInvoice.visibility = View.GONE
                else btnInvoice.visibility = View.VISIBLE

                tvTitleBar.text = "Detail Contact"
                icClose.visibility = View.GONE
                etMapsContainer.visibility = View.GONE
                btnSaveEdit.visibility = View.GONE
            }

        }

    }

    private fun editConfirmation() {

        if (!formValidation("${ etPhone.text }","${ etName.text }", "${ etOwner.text }", "${ etMaps.text }", "${ etLocation.text }", "${ etBirthday.text }", "${ etAddress.text }")) return

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
        val pStatus = if (selectedStatus.isNullOrEmpty()) "" else selectedStatus.substringBefore(" - ").toLowerCase()
        val pTermin = if (selectedTermin.isNullOrEmpty()) "-1" else {
            when (selectedTermin) {
                terminItem[1] -> STATUS_TERMIN_COD
                terminItem[2] -> STATUS_TERMIN_COD_TF
                terminItem[3] -> STATUS_TERMIN_COD_TUNAI
                terminItem[4] -> STATUS_TERMIN_30
                terminItem[5] -> STATUS_TERMIN_45
                terminItem[6] -> STATUS_TERMIN_60
                else -> "-1"
            }
        }
        val pReputation = if (selectedReputation.isNullOrEmpty()) "-1" else {
            when (selectedReputation) {
                reputationItem[1] -> "good"
                reputationItem[2] -> "bad"
                else -> "-1"
            }
        }

        var imagePart: MultipartBody.Part? = null

        if (iKtp.isNullOrEmpty() && selectedUri != null || !iKtp.isNullOrEmpty() && selectedUri != null) {
            val imgUri = CompressImageUtil.compressImage(this@DetailContactActivity, selectedUri!!, 50)
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
        val pPromoID = if (selectedPromo != null) selectedPromo!!.id else "0"

        loadingState(true)

//        Handler().postDelayed({
//            handleMessage(this, "TAG SAVE", "${contactId!!}, ${formatPhoneNumber(pPhone)}, $pName, $pOwner, $pBirthday, $pMapsUrl, ${pCityID!!}, $pAddress, $pStatus, $imagePart, $pTermin, $pReputation, $pPromoID")
//            loadingState(false)
//        }, 1000)
//
//        return

        lifecycleScope.launch {
            try {

                val rbId = createPartFromString(contactId!!)
                val rbPhone = createPartFromString(formatPhoneNumber(pPhone))
                val rbName = createPartFromString(pName)
                val rbOwner = createPartFromString(pOwner)
                val rbBirthday = createPartFromString(pBirthday)
                val rbMapsUrl = createPartFromString(pMapsUrl)
                val rbLocation = createPartFromString(pCityID!!)
                val rbAddress = createPartFromString(pAddress)
                val rbStatus = createPartFromString(pStatus)
                val rbTermin = createPartFromString(pTermin)
                val rbReputation = createPartFromString(pReputation)
                val rbPromoId = createPartFromString(pPromoID!!)

                val apiService: ApiService = HttpClient.create()
                val response = apiService.editContact(
                    id = rbId,
                    phone = rbPhone,
                    name = rbName,
                    ownerName = rbOwner,
                    birthday = rbBirthday,
                    cityId = rbLocation,
                    mapsUrl = rbMapsUrl,
                    address = rbAddress,
                    status = rbStatus,
                    termin = rbTermin,
                    reputation = rbReputation,
                    promoId = rbPromoId,
                    ktp = imagePart?.let { imagePart }
                )

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            itemSendMessage = ContactModel(
                                id_contact = contactId!!,
                                nomorhp = pPhone,
                                nama = pName,
                                store_owner = pOwner,
                                id_city = pCityID,
                                tgl_lahir = pBirthday,
                                maps_url = pMapsUrl,
                                termin_payment = pTermin,
                            )
                            setupDialogSendMessage(itemSendMessage)

                            tvName.text = "${ etName.text }"
                            tvPhone.text = "+" + formatPhoneNumber("${ etPhone.text }")
                            etPhone.setText(formatPhoneNumber("${ etPhone.text }"))
                            iAddress = "${ etAddress.text }"

//                            handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Successfully edit data!")
//                            loadingState(false)
//                            toggleEdit(false)

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
                            if (selectedPromo != null) {
                                if (selectedPromo!!.id != "0") tvPromo.text = "${ etPromo.text }"
                                else tvPromo.text = EMPTY_FIELD_VALUE
                            } else tvPromo.text = EMPTY_FIELD_VALUE

                            iStatus = if (!pStatus.isNullOrEmpty()) pStatus else null
                            iReputation = if (!pReputation.isNullOrEmpty()) pReputation else null
////                            if (!iStatus.isNullOrEmpty()) {
//                                if (iStatus == STATUS_CONTACT_BLACKL) {
//                                    btnInvoice.visibility = View.GONE
//                                } else btnInvoice.visibility = View.VISIBLE
////                            }
//                            setupStatus(iStatus)

                            iTermin = if (!pTermin.isNullOrEmpty()) pTermin else null
//                            setupTermin(iTermin)

                            // Remove image temp
                            currentPhotoUri?.let {
                                val contentResolver = contentResolver
                                contentResolver.delete(it, null, null)
                            }

                            getDetailContact()

                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Gagal mengubah! Message: ${ responseBody.message }")
                            loadingState(false)
                            toggleEdit(false)

                        }
                        else -> {

                            handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Gagal mengubah data!")
                            loadingState(false)
                            toggleEdit(false)

                        }
                    }

                } else {

                    handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Gagal mengubah data! Error: " + response.message())
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

    private fun getDetailContact(withToggleEdit: Boolean = true) {
        loadingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = contactId?.let { apiService.getContactDetail(contactId = it) }

                if (response!!.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            val data = responseBody.results[0]

                            if (withToggleEdit) {
                                if (!data.ktp_owner.isNullOrEmpty()) {
                                    tvKtp.text = "Tekan untuk menampilkan KTP"
                                    iKtp = data.ktp_owner
                                }

                                handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Berhasil mengubah data!")
                                toggleEdit(false)

                                if (iStatus == STATUS_CONTACT_BLACKLIST) {
                                    btnInvoice.visibility = View.GONE
                                } else btnInvoice.visibility = View.VISIBLE

                                setupStatus(iStatus)
                                setupTermin(iTermin)
                                setupReputation(iReputation)
                            } else {

                                if (data.store_status == STATUS_CONTACT_BLACKLIST) {
                                    btnInvoice.visibility = View.GONE
                                } else btnInvoice.visibility = View.VISIBLE

                                setupStatus(data.store_status)
                            }

                            hasEdited = true
                            loadingState(false)

                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Gagal memuat kontak! Message: Response status $RESPONSE_STATUS_FAIL or $RESPONSE_STATUS_FAILED")
                            loadingState(false)
                            toggleEdit(false)

                        }
                        else -> {

                            handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Gagal memuat kontak!")
                            loadingState(false)
                            toggleEdit(false)

                        }
                    }

                } else {

                    handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Gagal memuat kontak! Error: " + response.message())
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
            btnSaveEdit.text = getString(R.string.txt_loading)

        } else {

            btnSaveEdit.isEnabled = true
            btnSaveEdit.text = getString(R.string.save)
            btnSaveEdit.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))

        }

    }

    private fun formValidation(phone: String, name: String, owner: String = "", mapsUrl: String = "", location: String = "", birthday: String = "", address: String = ""): Boolean {
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

        if (tvPromo.text == EMPTY_FIELD_VALUE) intent.putExtra(CONST_PROMO, "")
        else intent.putExtra(CONST_PROMO, selectedPromo!!.id)

        if (iMapsUrl == EMPTY_FIELD_VALUE) intent.putExtra(CONST_MAPS, "")
        else intent.putExtra(CONST_MAPS, iMapsUrl)

        startActivityForResult(intent, DETAIL_ACTIVITY_REQUEST_CODE)

    }

    private fun navigateToDetailInvoice() {

        if (sessionManager.userKind() == USER_KIND_COURIER) {

            val intent = Intent(this@DetailContactActivity, ListSuratJalanActivity::class.java)

            intent.putExtra(CONST_CONTACT_ID, contactId)
            if (tvName.text == EMPTY_FIELD_VALUE) intent.putExtra(CONST_NAME, "")
            else intent.putExtra(CONST_NAME, tvName.text)

            startActivityForResult(intent, DETAIL_ACTIVITY_REQUEST_CODE)

        } else {
            val bottomSheetLayout = layoutInflater.inflate(R.layout.fragment_bottom_sheet_detail_contact, null)

            val invoiceOption = bottomSheetLayout.findViewById<LinearLayout>(R.id.invoiceOption)
            val reportOption = bottomSheetLayout.findViewById<LinearLayout>(R.id.reportOption)
            val btnNewReport = bottomSheetLayout.findViewById<Button>(R.id.btnNewReport)
            val reportsTitle = bottomSheetLayout.findViewById<TextView>(R.id.reportsTitle)
            val voucherOption = bottomSheetLayout.findViewById<LinearLayout>(R.id.voucherOption)

            if (sessionManager.userKind() == USER_KIND_COURIER) {
                invoiceOption.visibility = View.GONE
                reportOption.visibility = View.GONE
                btnNewReport.visibility = View.GONE
                voucherOption.visibility = View.GONE
            } else if (sessionManager.userKind() == USER_KIND_ADMIN || sessionManager.userKind() == USER_KIND_ADMIN_CITY) {
                reportsTitle.text = "Lihat Laporan Sales"
                reportOption.visibility = View.VISIBLE
                btnNewReport.visibility = View.GONE
            }

            bottomSheetDialog.setContentView(bottomSheetLayout)
            bottomSheetDialog.show()
        }

    }

    fun onBottomSheetOptionClick(view: View) {

        when (view.id) {
            R.id.voucherOption -> {

                val intent = Intent(this@DetailContactActivity, VoucherActivity::class.java)

                intent.putExtra(CONST_CONTACT_ID, contactId)
                if (tvName.text == EMPTY_FIELD_VALUE) intent.putExtra(CONST_NAME, "")
                else intent.putExtra(CONST_NAME, tvName.text)

                startActivityForResult(intent, DETAIL_ACTIVITY_REQUEST_CODE)

            } R.id.suratJalanOption, R.id.invoiceOption -> {

                val intent = Intent(this@DetailContactActivity, ListSuratJalanActivity::class.java)

                intent.putExtra(CONST_CONTACT_ID, contactId)
                if (view.id == R.id.invoiceOption) intent.putExtra("type_list", "list_invoice")
                if (tvName.text == EMPTY_FIELD_VALUE) intent.putExtra(CONST_NAME, "")
                else intent.putExtra(CONST_NAME, tvName.text)

                startActivityForResult(intent, DETAIL_ACTIVITY_REQUEST_CODE)

            } R.id.reportOption -> {

                var intent = Intent(this@DetailContactActivity, ReportsActivity::class.java)

                if (sessionManager.userKind() == USER_KIND_ADMIN || sessionManager.userKind() == USER_KIND_ADMIN_CITY) intent = Intent(this@DetailContactActivity, UsersReportActivity::class.java)

                intent.putExtra(CONST_CONTACT_ID, contactId)
                if (tvName.text == EMPTY_FIELD_VALUE) intent.putExtra(CONST_NAME, "")
                else intent.putExtra(CONST_NAME, tvName.text)

                startActivityForResult(intent, DETAIL_ACTIVITY_REQUEST_CODE)

            } else -> {

                val intent = Intent(this@DetailContactActivity, NewReportActivity::class.java)

                intent.putExtra(CONST_CONTACT_ID, contactId)
                if (tvName.text == EMPTY_FIELD_VALUE) intent.putExtra(CONST_NAME, "")
                else intent.putExtra(CONST_NAME, tvName.text)
                if (iMapsUrl == EMPTY_FIELD_VALUE) intent.putExtra(CONST_MAPS, "")
                else intent.putExtra(CONST_MAPS, iMapsUrl)

                startActivityForResult(intent, DETAIL_ACTIVITY_REQUEST_CODE)

            }
        }

        Handler().postDelayed({
            bottomSheetDialog.dismiss()
        }, 500)

    }

    // Helper function to show a toast message
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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

    private fun setupDialogSearchPromo(items: ArrayList<ModalSearchModel> = ArrayList()) {

        searchPromoModal = SearchModal(this, items)
        searchPromoModal.setCustomDialogListener(this)
        searchPromoModal.label = "Pilih Opsi Promo"
        searchPromoModal.searchHint = "Masukkan nama promo…"
        searchPromoModal.setOnDismissListener {
            etPromo.clearFocus()
            isSearchPromo = false
        }

    }

    private fun setupDialogSendMessage(item: ContactModel? = null) {

        sendMessageModal = SendMessageModal(this, lifecycleScope)
        sendMessageModal.initializeInterface(this)
        if (item != null) sendMessageModal.setItem(item)

        // Setup Indicator
        // setupNetworkIndicator()

        // Panggil layanan untuk memulai operasi ping di latar belakang
        if (pingUtility == null) {
            pingUtility = PingUtility()
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
        val searchKey = etLocation.text.toString()
        if (searchKey.isNotEmpty()) searchModal.setSearchKey(searchKey)
        searchModal.show()
    }

    private fun showSearchPromoModal() {
        isSearchPromo = true
        val searchKey = etPromo.text.toString()
        if (searchKey.isNotEmpty()) searchPromoModal.setSearchKey(searchKey)
        searchPromoModal.show()
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

                        val foundItem = results.find { it.id_city == iLocation }
                        if (foundItem != null) {
                            tvLocation.text = "${foundItem.nama_city} - ${foundItem.kode_city}"
                            etLocation.setText("${foundItem.nama_city} - ${foundItem.kode_city}")
                            selectedCity = ModalSearchModel(foundItem.id_city, foundItem.nama_city)
                        } else {
                            tvLocation.text = EMPTY_FIELD_VALUE
                            etLocation.setText("")
                        }

                        // Admin Access
//                        if (sessionManager.userKind() == USER_KIND_ADMIN || sessionManager.userKind() == USER_KIND_SALES) {
//                            icEdit.visibility = View.VISIBLE
//                            val indicatorImageView = findViewById<View>(R.id.indicatorView)
//                            indicatorImageView.visibility = View.VISIBLE
//                            if (sessionManager.userKind() == USER_KIND_ADMIN) tvKtpContainer.visibility = View.VISIBLE
//                        }
                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(this@DetailContactActivity, "LIST CITY", "Daftar kota kosong!")

                    }
                    else -> {

                        handleMessage(this@DetailContactActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))

                    }
                }


            } catch (e: Exception) {

                handleMessage(this@DetailContactActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)

            }

            getPromo()

        }
    }

    private fun getPromo() {

        // Get Cities
        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getPromo()

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        val results = response.results
                        val items: ArrayList<ModalSearchModel> = ArrayList()
                        items.add(ModalSearchModel("0", EMPTY_FIELD_VALUE))

                        for (i in 0 until results.size) {
                            val data = results[i]
                            items.add(ModalSearchModel(data.id_promo, "${data.nama_promo}"))
                        }

                        setupDialogSearchPromo(items)

                        val foundItem = results.find { it.id_promo == iPromo }
                        if (foundItem != null) {
                            tvPromo.text = "${foundItem.nama_promo}"
                            etPromo.setText("${foundItem.nama_promo}")
                            selectedPromo = ModalSearchModel(foundItem.id_promo, foundItem.nama_promo)
                        } else {
                            tvPromo.text = EMPTY_FIELD_VALUE
                            etPromo.setText("")
                        }

                        // Admin Access
                        if (sessionManager.userKind() == USER_KIND_ADMIN || sessionManager.userKind() == USER_KIND_ADMIN_CITY || sessionManager.userKind() == USER_KIND_SALES) {
                            icEdit.visibility = View.VISIBLE
                            val indicatorImageView = findViewById<View>(R.id.indicatorView)
                            indicatorImageView.visibility = View.VISIBLE
                            if (sessionManager.userKind() == USER_KIND_ADMIN || sessionManager.userKind() == USER_KIND_ADMIN_CITY) tvKtpContainer.visibility = View.VISIBLE
                        }
                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(this@DetailContactActivity, "LIST PROMO", "Daftar promo kosong!")

                    }
                    else -> {

                        handleMessage(this@DetailContactActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))

                    }
                }


            } catch (e: Exception) {

                handleMessage(this@DetailContactActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)

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

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(this@DetailContactActivity, MapsActivity::class.java)
                    intent.putExtra(CONST_MAPS, iMapsUrl)
                    intent.putExtra(CONST_MAPS_NAME, tvName.text)
                    intent.putExtra(CONST_MAPS_STATUS, iStatus)
                    intent.putExtra(CONST_CONTACT_ID, contactId)
                    startActivity(intent)
                } else checkLocationPermission()
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

    private fun tooltipHandler(content: View, text: String) {
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
        when (status) {
            STATUS_CONTACT_DATA -> {
                tooltipStatus.setImageDrawable(getDrawable(R.drawable.status_data))
                tooltipHandler(tooltipStatus, "Customer Status is Data")
                tvStatus.text = statusItem[1]
                spinStatus.setSelection(1)
            }
            STATUS_CONTACT_PASSIVE -> {
                tooltipStatus.setImageDrawable(getDrawable(R.drawable.status_passive))
                tooltipHandler(tooltipStatus, "Customer Status is Passive")
                tvStatus.text = statusItem[2]
                spinStatus.setSelection(2)
            }
            STATUS_CONTACT_ACTIVE -> {
                tooltipStatus.setImageDrawable(getDrawable(R.drawable.status_active))
                tooltipHandler(tooltipStatus, "Customer Status is Active")
                tvStatus.text = statusItem[3]
                spinStatus.setSelection(3)
            }
            STATUS_CONTACT_BLACKLIST -> {
                tooltipStatus.setImageDrawable(getDrawable(R.drawable.status_blacklist))
                tooltipHandler(tooltipStatus, "Customer Status is Blacklist")
                tvStatus.text = statusItem[4]
                spinStatus.setSelection(4)
            }
            STATUS_CONTACT_BID -> {
                tooltipStatus.setImageDrawable(getDrawable(R.drawable.status_bid))
                tooltipHandler(tooltipStatus, "Customer Status is Bargained")
                tvStatus.text = statusItem[5]
                spinStatus.setSelection(5)
            }
            else -> {
                tooltipStatus.visibility = View.GONE
                tvStatus.text = EMPTY_FIELD_VALUE
                spinStatus.setSelection(0)
            }
        }
    }

    private fun setupTermin(termin: String? = null) {
        when (termin) {
            STATUS_TERMIN_COD -> {
                tvTermin.text = terminItem[1]
                spinTermin.setSelection(1)
            }
            STATUS_TERMIN_COD_TF -> {
                tvTermin.text = terminItem[2]
                spinTermin.setSelection(2)
            }
            STATUS_TERMIN_COD_TUNAI -> {
                tvTermin.text = terminItem[3]
                spinTermin.setSelection(3)
            }
            STATUS_TERMIN_30 -> {
                tvTermin.text = terminItem[4]
                spinTermin.setSelection(4)
            }
            STATUS_TERMIN_45 -> {
                tvTermin.text = terminItem[5]
                spinTermin.setSelection(5)
            }
            STATUS_TERMIN_60 -> {
                tvTermin.text = terminItem[6]
                spinTermin.setSelection(6)
            }
            else -> {
                tvTermin.text = EMPTY_FIELD_VALUE
                spinTermin.setSelection(0)
            }
        }
    }

    private fun setupReputation(reputation: String? = null) {
        when (reputation) {
            "good" -> {
                tvReputation.text = reputationItem[1]
                spinReputation.setSelection(1)
            }
            "bad" -> {
                tvReputation.text = reputationItem[2]
                spinReputation.setSelection(2)
            }
            else -> {
                tvReputation.text = EMPTY_FIELD_VALUE
                spinReputation.setSelection(0)
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

        selectedStatus = iStatus!!
        setupStatus(iStatus)
    }

    private fun setupTerminSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, terminItem)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinTermin.adapter = adapter
        spinTermin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTermin = if (position != 0) terminItem[position]
                else ""
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        selectedTermin = when (iTermin) {
             STATUS_TERMIN_COD -> terminItem[1]
             STATUS_TERMIN_COD_TF -> terminItem[2]
             STATUS_TERMIN_COD_TUNAI -> terminItem[3]
             STATUS_TERMIN_30 -> terminItem[4]
             STATUS_TERMIN_45 -> terminItem[5]
             STATUS_TERMIN_60 -> terminItem[6]
            else -> "-1"
        }
        setupTermin(iTermin)
    }

    private fun setupReputationSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reputationItem)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinReputation.adapter = adapter
        spinReputation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedReputation = if (position != 0) reputationItem[position]
                else ""
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        selectedReputation = when (iReputation) {
             "good" -> reputationItem[1]
             "bad" -> reputationItem[2]
            else -> "-1"
        }
        setupReputation(iReputation)
    }

    private fun setupNetworkIndicator() {
        val indicatorImageView = findViewById<View>(R.id.indicatorView)
        if (sessionManager.userKind() == USER_KIND_ADMIN || sessionManager.userKind() == USER_KIND_SALES) {
            val layoutParams = indicatorImageView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.marginEnd = 0
            layoutParams.marginStart = 16
        }
        val pingIntervalMillis = 1000L // milidetik

        val pingTimer = Timer()
        pingTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val pingTask = PingUtility(indicatorImageView)
                pingTask.setInterface(this@DetailContactActivity)
                pingTask.execute()
            }
        }, 0, pingIntervalMillis)

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

    private fun navigateToPreviewKtp() {
        val uriList = ArrayList<Uri>()
        selectedUri?.let { uriList.add(it) }

        val intent = Intent(this, PreviewClosingActivity::class.java)
        intent.putExtra(CONST_CONTACT_ID, contactId)
        intent.putParcelableArrayListExtra(CONST_URI, uriList)
        startActivityForResult(intent, IMG_PREVIEW_STATE)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DETAIL_ACTIVITY_REQUEST_CODE) {

            val resultData = data?.getStringExtra("$DETAIL_ACTIVITY_REQUEST_CODE")
            isClosingAction = data?.getBooleanExtra(IS_CLOSING, false) ?: false

            if (isClosingAction) setupDelivery()
            if (resultData == SYNC_NOW) hasEdited = true

        } else if (requestCode == REQUEST_EDIT_CONTACT_COORDINATE) {
            val latitude = data?.getDoubleExtra("latitude", 0.0)
            val longitude = data?.getDoubleExtra("longitude", 0.0)
            if (latitude != null && longitude != null) etMaps.setText("$latitude,$longitude")
            etMaps.error = null
            etMaps.clearFocus()
        }

    }

    override fun onBackPressed() {
//      return super.onBackPressed()
        backHandler()
    }

    // Interfade Search Modal
    override fun onDataReceived(data: ModalSearchModel) {
        if (isSearchCity) {
            etLocation.setText(data.title)
            selectedCity = data
        } else if (isSearchPromo) {
            etPromo.setText(data.title)
            selectedPromo = data
        }
        isSearchCity = false
        isSearchPromo = false
    }

    override fun onPingResult(pingResult: Int?) {
        sendMessageModal.setPingStatus(pingResult)
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

    override fun onSubmitMessage(status: Boolean) {
        getDetailContact(false)
        setupDialogSendMessage(itemSendMessage)
    }

    private fun showMoreOption() {
        val titleBar = findViewById<ImageView>(R.id.ic_edit)
        val popupMenu = PopupMenu(this, titleBar, Gravity.END)
        popupMenu.menuInflater.inflate(R.menu.option_detail_contact, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
            when (item?.itemId) {
                R.id.option_edit -> {
                    toggleEdit(true)
                    return@setOnMenuItemClickListener  true
                }
                R.id.option_voucher -> {
                    showFormVoucherModal()
                    return@setOnMenuItemClickListener  true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }

        popupMenu.show()
    }

    private fun showFormVoucherModal() {
        val modal = AddVoucherModal(this, lifecycleScope)
        modal.setVoucherId(contactId.toString())
        modal.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (pingUtility != null) pingUtility!!.stopPingMonitoring()
    }

    private fun setupDelivery() {

        if (isClosingAction) {
            isDeliveryLoading = false
            binding.btnDeliveryContainer.visibility = View.GONE
            binding.contactAction.visibility = View.VISIBLE
            binding.textDelivery.visibility = View.VISIBLE
            binding.btnDirection.visibility = View.GONE

            binding.textDeliveryTitle.text = "Berhasil Closing"
            binding.textDeliveryDesc.text = "Segera selesaikan pengiriman lainnya."
            return
        }

        deliveryId = "$AUTH_LEVEL_COURIER$userID"
        firebaseReference = FirebaseUtils().getReference(distributorId = userDistributorId)
        childDelivery = firebaseReference?.child(FIREBASE_CHILD_DELIVERY)
        childDriver = childDelivery?.child(deliveryId)

        childDriver?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    childDriver?.child("stores")?.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                var isStoreAvailable = false
                                for (item in snapshot.children) {
                                    val data = item.getValue(DeliveryModel.Store::class.java)!!
                                    if (data.id == contactId) {
                                        isStoreAvailable = true
                                        break
                                    }
                                }
                                if (isStoreAvailable) {
                                    binding.btnDeliveryContainer.visibility = View.GONE
                                    binding.contactAction.visibility = View.VISIBLE
                                    binding.textDelivery.visibility = View.VISIBLE
                                    binding.btnDirection.visibility = View.VISIBLE
                                    binding.btnDirection.setOnClickListener {
                                        val intent = Intent(this@DetailContactActivity, MapsActivity::class.java)
                                        intent.putExtra(CONST_IS_TRACKING, true)
                                        intent.putExtra(CONST_DELIVERY_ID, deliveryId)
                                        intent.putExtra(CONST_CONTACT_ID, contactId)
                                        startActivity(intent)
                                    }
                                    checkServiceStatus()
                                } else setupBtnDelivery()
                            } else setupBtnDelivery()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            handleMessage(this@DetailContactActivity, "onSetupDelivery", "Failed get store child")
                            Log.e("onSetupDelivery", error.message)
                        }

                    })
                } else {
                    setupBtnDelivery()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                handleMessage(this@DetailContactActivity, "onSetupDelivery", "Failed get driver child")
                Log.e("onSetupDelivery", error.message)
            }

        })
    }

    @SuppressLint("MissingPermission")
    private fun startDelivery() {

        val targetLatLng = latLngConverter(iMapsUrl!!)
        if (targetLatLng != null) {

            isDeliveryLoading = true

            val btnDelivery = binding.btnDeliveryTitle
            btnDelivery.text = getString(R.string.txt_loading)
            Handler().postDelayed({
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { currentLatLng: Location ->

                        val courierModel = DeliveryModel.Courier(
                            id = userID,
                            name = fulllName
                        )

                        val store = DeliveryModel.Store(
                            id = contactId!!,
                            name = "${tvName.text}",
                            lat = targetLatLng.latitude,
                            lng = targetLatLng.longitude,
                            startDatetime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) DateFormat.now() else "",
                            startLat = currentLatLng.latitude,
                            startLng = currentLatLng.longitude,
                        )

//                        val deliveryModel = DeliveryModel.Delivery(
//                            id = deliveryId,
////                            stores = deliveryStores,
//                            courier = courierModel
//                        )

                        childDriver?.child("id")?.setValue(deliveryId)
                        childDriver?.child("lat")?.setValue(currentLatLng.latitude)
                        childDriver?.child("lng")?.setValue(currentLatLng.longitude)
                        childDriver?.child("courier")?.setValue(courierModel)
                        childDriver?.child("stores/${store.id}")?.setValue(store)

                        checkServiceStatus()

                        // Change state
                        isDeliveryLoading = false
                        binding.btnDeliveryContainer.visibility = View.GONE
                        binding.contactAction.visibility = View.VISIBLE
                        binding.textDelivery.visibility = View.VISIBLE
                        binding.btnDirection.visibility = View.VISIBLE
                        binding.btnDirection.setOnClickListener {
                            val intent = Intent(this@DetailContactActivity, MapsActivity::class.java)
                            intent.putExtra(CONST_IS_TRACKING, true)
                            intent.putExtra(CONST_DELIVERY_ID, deliveryId)
                            intent.putExtra(CONST_CONTACT_ID, contactId)
                            startActivity(intent)
                        }

                        hasEdited = true

                    }.addOnFailureListener {
                        isDeliveryLoading = false
                        handleMessage(this, "onStartDelivery", "Failed get user lastLocation")
                        Log.e("onStartDelivery", "Failed get user lastLocation: $it")
                        btnDelivery.text = getString(R.string.start_delivery)
                    }
            }, 500)
        } else {
            handleMessage(this, "onStartDelivery", "Latitude Longitude converter has returned null")
        }
    }

    private fun latLngConverter(stringLatLng: String): LatLng? {
        return if (stringLatLng.isNotEmpty()) {

            val urlUtility = URLUtility(this)

            if (!urlUtility.isUrl(stringLatLng)) {

                val coordinates = stringLatLng.trim().split(",")
                return if (coordinates.size == 2) {
                    val latitude = coordinates[0].toDoubleOrNull()
                    val longitude = coordinates[1].toDoubleOrNull()

                    if (latitude != null && longitude != null) {
                        LatLng(latitude, longitude)
                    } else {
                        handleMessage(this, "latLngConverter", "Null latitude or longitude")
                        null
                    }
                } else {
                    handleMessage(this, "latLngConverter", "Failed processed coordinate")
                    null
                }
            } else {
                handleMessage(this, "latLngConverter", "Wrong coordinate value")
                null
            }

        } else {
            handleMessage(this, "latLngConverter", "Parameter is empty")
            null
        }
    }

    private fun setupBtnDelivery() {

        binding.btnDeliveryContainer.visibility = View.VISIBLE
        binding.contactAction.visibility = View.GONE

        if (!isDeliveryLoading) {
            binding.btnDeliveryContainer.setOnClickListener {
                if (ContextCompat.checkSelfPermission(this@DetailContactActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // Meminta izin background location
                        if (ContextCompat.checkSelfPermission(
                                this@DetailContactActivity,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {

                            startDelivery()

                        } else {
                            val message =
                                "Izin background lokasi diperlukan untuk fitur ini. Mohon untuk memilih opsi berikut \"${
                                    getString(R.string.yes_bg_location)
                                }\""
                            val customUtility = CustomUtility(this@DetailContactActivity)
                            customUtility.showPermissionDeniedDialog(message) {
                                ActivityCompat.requestPermissions(
                                    this@DetailContactActivity,
                                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                                    LOCATION_PERMISSION_REQUEST_CODE
                                )
                            }
                        }
                    } else startDelivery()
                } else {
                    // Meminta izin lokasi jika belum diberikan
                    ActivityCompat.requestPermissions(this@DetailContactActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
                }
            }
        }
    }

    private fun checkServiceStatus() {
        val isTracking = CustomUtility(this@DetailContactActivity).isServiceRunning(TrackingService::class.java)
        if (!isTracking) {
            val serviceIntent = Intent(this@DetailContactActivity, TrackingService::class.java)
            serviceIntent.putExtra("userDistributorId", userDistributorId)
            serviceIntent.putExtra("deliveryId", deliveryId)
            this@DetailContactActivity.startService(serviceIntent)
        }
    }

}