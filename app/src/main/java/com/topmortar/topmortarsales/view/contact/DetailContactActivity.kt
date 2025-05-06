package com.topmortar.topmortarsales.view.contact

import android.Manifest
import android.animation.ValueAnimator
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
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
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
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_COURIER
import com.topmortar.topmortarsales.commons.BASE_URL
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_DELIVERY_ID
import com.topmortar.topmortarsales.commons.CONST_INVOICE_ID
import com.topmortar.topmortarsales.commons.CONST_IS_TRACKING
import com.topmortar.topmortarsales.commons.CONST_KTP
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_MAPS_NAME
import com.topmortar.topmortarsales.commons.CONST_MAPS_STATUS
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.DETAIL_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.EMPTY_FIELD_VALUE
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_DELIVERY
import com.topmortar.topmortarsales.commons.GET_COORDINATE
import com.topmortar.topmortarsales.commons.IS_CLOSING
import com.topmortar.topmortarsales.commons.LOCATION_PERMISSION_REQUEST_CODE
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.NORMAL_REPORT
import com.topmortar.topmortarsales.commons.PAYMENT_NOT_SET
import com.topmortar.topmortarsales.commons.PAYMENT_TRANSFER
import com.topmortar.topmortarsales.commons.PAYMENT_TUNAI
import com.topmortar.topmortarsales.commons.PHONE_CATEGORIES
import com.topmortar.topmortarsales.commons.PING_HOST
import com.topmortar.topmortarsales.commons.PING_MEDIUM
import com.topmortar.topmortarsales.commons.PING_NORMAL
import com.topmortar.topmortarsales.commons.RENVI_SOURCE
import com.topmortar.topmortarsales.commons.REPORT_SOURCE
import com.topmortar.topmortarsales.commons.REPORT_TYPE_IS_PAYMENT
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
import com.topmortar.topmortarsales.commons.USER_KIND_MARKETING
import com.topmortar.topmortarsales.commons.USER_KIND_PENAGIHAN
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.services.TrackingService
import com.topmortar.topmortarsales.commons.utils.CompressImageUtil
import com.topmortar.topmortarsales.commons.utils.CustomProgressBar
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.PhoneHandler
import com.topmortar.topmortarsales.commons.utils.PhoneHandler.formatPhoneNumber
import com.topmortar.topmortarsales.commons.utils.PingUtility
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.URLUtility
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityDetailContactBinding
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.modal.SendMessageModal
import com.topmortar.topmortarsales.model.ContactModel
import com.topmortar.topmortarsales.model.ContactSales
import com.topmortar.topmortarsales.model.DeliveryModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import com.topmortar.topmortarsales.view.MapsActivity
import com.topmortar.topmortarsales.view.reports.ChecklistReportActivity
import com.topmortar.topmortarsales.view.reports.NewReportActivity
import com.topmortar.topmortarsales.view.reports.ReportsActivity
import com.topmortar.topmortarsales.view.reports.UsersReportActivity
import com.topmortar.topmortarsales.view.suratJalan.ListSuratJalanActivity
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

class DetailContactActivity : AppCompatActivity(), SearchModal.SearchModalListener,
    PingUtility.PingResultInterface, SendMessageModal.SendMessageModalInterface {

    private lateinit var progressBar: CustomProgressBar
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager
    private val userKind get() = sessionManager.userKind().toString()
    private val userID get() = sessionManager.userID().toString()
    private val username get() = sessionManager.userName().toString()
    private val fulllName get() = sessionManager.fullName().toString()
    private val userDistributorId get() = sessionManager.userDistributor().toString()
    private val userDistributorIds get() = sessionManager.userDistributor()
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
    private var statusWeeklyVisitItem: List<String> = listOf("Pilih Status", "Active")
    private var terminItem: List<String> = listOf("Pilih Termin Payment", "COD", "COD + Transfer", "COD + Tunai", "30 Hari", "45 Hari", "60 Hari")
    private var paymentMethodItem: List<String> = listOf("Pilih Metode Pembayaran", "Tunai", "Transfer")
    private var reputationItem: List<String> = listOf("Pilih Reputasi Toko", "Good", "Bad")
    private var hariBayarItem: List<String> = listOf("Pilih Hari Bayar", "Bebas", "Senin", "Selasa", "Rabu", "Kamis", "Jum'at", "Sabtu")
    private var spinPhoneCatItems: List<String> = listOf()
    private var selectedStatus: String = ""
    private var selectedWeeklyVisitStatus: String = ""
    private var selectedPaymentMethod: String = ""
    private var selectedTermin: String = ""
    private var selectedReputation: String = ""
    private var selectedHariBayar: String = ""
    private var selectedUri: Uri? = null
    private var currentPhotoUri: Uri? = null

    private var titlePage = "Detail Contact"
    private var iName: String? = null
    private var iLocation: String? = null
    private var iStatus: String? = null
    private var iWeeklyVisitStatus: String? = null
    private var iPaymentMethod: String? = null
    private var iTermin: String? = null
    private var iReputation: String? = null
    private var iHariBayar: String? = null
    private var iAddress: String? = null
    private var iMapsUrl: String? = null
    private var iKtp: String? = null
    private var iPromo: String? = null
    private var iReportSource: String? = NORMAL_REPORT
    private var iRenviSource: String? = NORMAL_REPORT
    private var iInvoiceId: String? = null
    private var iReportPaymentStatus: Boolean? = false

    private var isSearchCity = false
    private var isSearchPromo = false
    private var isCitiesSuccessfullyLoad = false
    private var isPhoneFieldOpened = true

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

    private lateinit var phoneCategoriesFRC: FirebaseRemoteConfig

    private val detailLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val resultData = data?.getStringExtra("$DETAIL_ACTIVITY_REQUEST_CODE")
            isClosingAction = data?.getBooleanExtra(IS_CLOSING, false) ?: false

            if (isClosingAction) setupDelivery()
            if (resultData == SYNC_NOW) hasEdited = true
        }
    }

    private val coordinateLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val latitude = data?.getDoubleExtra("latitude", 0.0)
            val longitude = data?.getDoubleExtra("longitude", 0.0)

            if (!::etMaps.isInitialized) etMaps = findViewById(R.id.et_maps)
            etMaps.let {
                if (latitude != null && longitude != null) it.setText("$latitude,$longitude")
                it.error = null
                it.clearFocus()
            }
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            chooseFile()
        } else {
            handleMessage(this@DetailContactActivity, "CAMERA ACCESS DENIED", "Izin kamera ditolak")
        }
        etKtp.clearFocus()
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            selectedUri = if (data == null || data.data == null) currentPhotoUri else data.data
            tvSelectedKtp.text = "File terpilih: " + selectedUri?.let { getFileNameFromUri(it) }
        }
        etKtp.clearFocus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge(isPrimary = false)

        sessionManager = SessionManager(this)
        binding = ActivityDetailContactBinding.inflate(layoutInflater)

        setContentView(binding.root)

        progressBar = CustomProgressBar(this)
        progressBar.setCancelable(false)
        progressBar.setMessage(getString(R.string.txt_loading))

        apiService = HttpClient.create()

        if (CustomUtility(this).isUserWithOnlineStatus()) {
            CustomUtility(this).setUserStatusOnline(true, userDistributorIds ?: "-custom-003", userID)
        }

        phoneCategoriesFRC = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        phoneCategoriesFRC.setConfigSettingsAsync(configSettings)
        phoneCategoriesFRC.setDefaultsAsync(R.xml.default_phone_categories)

        phoneCategoriesFRC.fetchAndActivate()
            .addOnCompleteListener(this) {
                    val itemsJson = phoneCategoriesFRC.getString(PHONE_CATEGORIES)
                    val itemsArray = JSONArray(itemsJson)
                    val items = Array(itemsArray.length()) { i -> itemsArray.getString(i) }

                    spinPhoneCatItems = items.toList()

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinPhoneCatItems)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                binding.spinPhoneCategories1.adapter = adapter
                binding.spinPhoneCategories2.adapter = adapter

                initView()
            }

    }

    private fun initView() {
        initVariable()
        initClickHandler()
        getContact()
//        dataActivityValidation()
        checkLocationPermission()
        if (userKind == USER_KIND_COURIER) {
            setupDelivery()
        } else {
            binding.textLoading.visibility = View.GONE
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
        tvTitleBar.text = titlePage

        // Setup Phone Toggle
        binding.itemPhone2.visibility = View.VISIBLE
        binding.tvPhoneContainer.setPadding(0,0,0, convertDpToPx(12, this))
        binding.togglePhoneSize.visibility = View.VISIBLE

        // Setup Date Picker Dialog
        setDatePickerDialog()

        // Setup Dialog Search
        setupDialogSearch()
        setupDialogSearchPromo()

        // Setup Dialog Send Message
        setupDialogSendMessage()

        bottomSheetDialog = BottomSheetDialog(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        scrollViewListener()

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

        binding.togglePhoneSize.setOnClickListener {
            if (!isPhoneFieldOpened) {
                isPhoneFieldOpened = true
                val startHeight = binding.tvPhoneContainer.height
                binding.tvPhoneContainer.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val targetHeight = binding.tvPhoneContainer.measuredHeight
                phoneAnimation(startHeight, targetHeight)
            } else {
                isPhoneFieldOpened = false
                val startHeight = binding.tvPhoneContainer.height
                val targetHeight = binding.tvPhoneContainer.height - binding.itemPhone2.height
                phoneAnimation(startHeight, targetHeight)
            }
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
        tooltipHandler(binding.tooltipPhone2, "Nomor Telpon ke 2 (WA)")
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
        val data = "${ etMaps.text }"

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra(CONST_MAPS, data)
            intent.putExtra(GET_COORDINATE, true)
            coordinateLauncher.launch(intent)
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
                binding.togglePhoneSize.visibility = View.GONE

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

                // Status
                statusContainer.visibility = if (userKind == USER_KIND_ADMIN) View.VISIBLE else View.GONE
                statusContainer.setBackgroundResource(R.drawable.et_background)
                tooltipStatus.visibility = View.GONE
                tvStatus.visibility = View.GONE
                spinStatus.visibility = View.VISIBLE

                // Weekly Visit Status
                binding.weeklyVisitContainer.visibility = if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_ADMIN_CITY) View.VISIBLE else View.GONE
                binding.weeklyVisitContainer.setBackgroundResource(R.drawable.et_background)
                binding.tooltipWeeklyVisit.visibility = View.GONE
                binding.tvWeeklyVisit.visibility = View.GONE
                binding.spinWeeklyVisit.visibility = View.VISIBLE

                // Payment Method
                binding.paymentMethodContainer.setBackgroundResource(R.drawable.et_background)
                binding.tvPaymentMethod.visibility = View.GONE
                binding.spinPaymentMethod.visibility = View.VISIBLE

                // Termin
                terminContainer.setBackgroundResource(R.drawable.et_background)
                tvTermin.visibility = View.GONE
                spinTermin.visibility = View.VISIBLE

                // Reputation
                reputationContainer.setBackgroundResource(R.drawable.et_background)
                tvReputation.visibility = View.GONE
                spinReputation.visibility = View.VISIBLE

                // Hari Bayar
                binding.hariBayarContainer.setBackgroundResource(R.drawable.et_background)
                binding.tvHariBayar.visibility = View.GONE
                binding.spinHariBayar.visibility = View.VISIBLE

                // Promo
                tvPromo.visibility = View.GONE
                etPromo.visibility = View.VISIBLE
                promoContainer.setBackgroundResource(R.drawable.et_background)

                btnSaveEdit.visibility = View.VISIBLE

                etName.requestFocus()
                etName.setSelection(etName.text.length)
            }

        } else {

            if (sessionManager.userKind() == USER_KIND_ADMIN || sessionManager.userKind() == USER_KIND_ADMIN_CITY) {
                // Show Case
                icEdit.visibility = View.VISIBLE

                tvName.visibility = View.VISIBLE
                tvDescription.visibility = View.VISIBLE

                tvPhoneContainer.visibility = View.VISIBLE
                binding.togglePhoneSize.visibility = View.VISIBLE

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

                binding.weeklyVisitContainer.visibility = View.VISIBLE
                binding.weeklyVisitContainer.setBackgroundResource(R.drawable.background_rounded_16)

                tooltipStatus.visibility = View.VISIBLE
                binding.tooltipWeeklyVisit.visibility = View.VISIBLE
//                if (iStatus == STATUS_CONTACT_BLACKLIST) btnInvoice.visibility = View.GONE
//                else btnInvoice.visibility = View.VISIBLE
                btnInvoice.visibility = View.VISIBLE

                // Status
                tvStatus.visibility = View.VISIBLE
                spinStatus.visibility = View.GONE

                // Weekly Visit Status
                binding.tvWeeklyVisit.visibility = View.VISIBLE
                binding.spinWeeklyVisit.visibility = View.GONE

                // Payment Method
                binding.paymentMethodContainer.setBackgroundResource(R.drawable.background_rounded_16)
                binding.tvPaymentMethod.visibility = View.VISIBLE
                binding.spinPaymentMethod.visibility = View.GONE

                // Termin
                terminContainer.setBackgroundResource(R.drawable.background_rounded_16)
                tvTermin.visibility = View.VISIBLE
                spinTermin.visibility = View.GONE

                // Reputation
                reputationContainer.setBackgroundResource(R.drawable.background_rounded_16)
                tvReputation.visibility = View.VISIBLE
                spinReputation.visibility = View.GONE

                // Hari Bayar
                binding.hariBayarContainer.setBackgroundResource(R.drawable.background_rounded_16)
                binding.tvHariBayar.visibility = View.VISIBLE
                binding.spinHariBayar.visibility = View.GONE

                // Promo
                tvPromo.visibility = View.VISIBLE
                etPromo.visibility = View.GONE
                promoContainer.setBackgroundResource(R.drawable.background_rounded_16)

                btnSaveEdit.visibility = View.GONE

                etName.clearFocus()
            }

        }

    }

    private fun editConfirmation() {

        if (!formValidation("${ etPhone.text }","${ binding.etPhone2.text }","${ etName.text }")) return

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

        val pPhoneCategory1 = binding.spinPhoneCategories1.let {
            if (it.selectedItemPosition < 1) "" else "${ it.selectedItem }"
        }
        val pPhone = "${ etPhone.text }"
        val pPhoneCategory2 = binding.spinPhoneCategories2.let {
            if (it.selectedItemPosition < 1) "" else "${ it.selectedItem }"
        }
        val pPhone2 = binding.etPhone2.text.let { if (it.toString().isEmpty()) "0" else "$it" }
        val pName = "${ etName.text }"
        val pOwner = "${ etOwner.text }"
        var pBirthday = "${ etBirthday.text }"
        val pMapsUrl = "${ etMaps.text }"
        val pAddress = "${ etAddress.text }"
        val pStatus = if (selectedStatus.isEmpty()) "" else selectedStatus.substringBefore(" - ").toLowerCase(Locale.getDefault())
        val pWeeklyVisitStatus = if (selectedWeeklyVisitStatus.isEmpty()) "0" else "1"
        val pPaymentMethod = when (selectedPaymentMethod) {
            paymentMethodItem[1] -> PAYMENT_TUNAI
            paymentMethodItem[2] -> PAYMENT_TRANSFER
            else -> PAYMENT_NOT_SET
        }
        val pTermin = if (selectedTermin.isEmpty()) "-1" else {
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
        val pReputation = if (selectedReputation.isEmpty()) "-1" else {
            when (selectedReputation) {
                reputationItem[1] -> "good"
                reputationItem[2] -> "bad"
                else -> "-1"
            }
        }
        val pHariBayar = if (selectedHariBayar.isEmpty()) "-1" else {
            when (selectedHariBayar) {
                hariBayarItem[1] -> "bebas"
                hariBayarItem[2] -> "senin"
                hariBayarItem[3] -> "selasa"
                hariBayarItem[4] -> "rabu"
                hariBayarItem[5] -> "kamis"
                hariBayarItem[6] -> "jumat"
                hariBayarItem[7] -> "sabtu"
                else -> "-1"
            }
        }

        var imagePart: MultipartBody.Part? = null

        if (iKtp.isNullOrEmpty() && selectedUri != null || !iKtp.isNullOrEmpty() && selectedUri != null) {

            val imgUri = CompressImageUtil.compressImageOptimized(this, selectedUri!!)

            val contentResolver = contentResolver
            val inputStream = contentResolver.openInputStream(imgUri)
            val byteArray = inputStream?.readBytes()

            if (byteArray != null) {
                val requestFile: RequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), byteArray)
                imagePart = MultipartBody.Part.createFormData("ktp", "image.jpg", requestFile)
            } else {
                handleMessage(this, TAG_RESPONSE_CONTACT, "Gambar tidak ditemukan")
            }
        }

        pBirthday = if (pBirthday.isEmpty() || pBirthday == EMPTY_FIELD_VALUE) "0000-00-00"
        else DateFormat.format("${ etBirthday.text }", "dd MMMM yyyy", "yyyy-MM-dd")

        val pCityID = if (selectedCity != null) selectedCity!!.id else "0"
        val pPromoID = if (selectedPromo != null) selectedPromo!!.id else "0"

        loadingState(true)
        progressBar.show()

//        Handler(Looper.getMainLooper()).postDelayed({
//            handleMessage(this, "TAG SAVE", "${contactId!!}, ${formatPhoneNumber(pPhone)}, $pName, $pOwner, $pBirthday, $pMapsUrl, ${pCityID!!}, $pAddress, $pStatus, $imagePart, $pTermin, $pReputation, $pPromoID, $pWeeklyVisitStatus, $pHariBayar")
//            loadingState(false)
//            progressBar.dismiss()
//        }, 1000)
//
//        return

        lifecycleScope.launch {
            try {

                val rbId = createPartFromString(contactId!!)
                val rbPhoneCategory1 = createPartFromString(formatPhoneNumber(pPhoneCategory1))
                val rbPhone = createPartFromString(formatPhoneNumber(pPhone))
                val rbPhoneCategory2 = createPartFromString(pPhoneCategory2)
                val rbPhone2 = createPartFromString(pPhone2.let{ if (it == "0") it else formatPhoneNumber(it) })
                val rbName = createPartFromString(pName)
                val rbOwner = createPartFromString(pOwner)
                val rbBirthday = createPartFromString(pBirthday)
                val rbMapsUrl = createPartFromString(pMapsUrl)
                val rbLocation = createPartFromString(pCityID!!)
                val rbAddress = createPartFromString(pAddress)
                val rbStatus = createPartFromString(pStatus)
                val rbWeeklyVisitStatus = createPartFromString(pWeeklyVisitStatus)
                val rbPaymentMethod = createPartFromString(pPaymentMethod)
                val rbTermin = createPartFromString(pTermin)
                val rbReputation = createPartFromString(pReputation)
                val rbHariBayar = createPartFromString(pHariBayar)
                val rbPromoId = createPartFromString(pPromoID!!)

                val response = apiService.editContact(
                    id = rbId,
                    phoneCategory1 = rbPhoneCategory1,
                    phone = rbPhone,
                    phoneCategory2 = rbPhoneCategory2,
                    phone2 = rbPhone2,
                    name = rbName,
                    ownerName = rbOwner,
                    birthday = rbBirthday,
                    cityId = rbLocation,
                    mapsUrl = rbMapsUrl,
                    address = rbAddress,
                    status = rbStatus,
                    tagihanMingguan = rbWeeklyVisitStatus,
                    paymentMethod = rbPaymentMethod,
                    termin = rbTermin,
                    reputation = rbReputation,
                    promoId = rbPromoId,
                    ktp = imagePart?.let { imagePart },
                    hariBayar = rbHariBayar
                )

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            itemSendMessage = ContactModel(
                                id_contact = contactId!!,
                                nomor_cat_1 = pPhoneCategory1,
                                nomorhp = pPhone,
                                nomor_cat_2 = pPhoneCategory2,
                                nomorhp_2 = pPhone2,
                                nama = pName,
                                store_owner = pOwner,
                                id_city = pCityID,
                                tgl_lahir = pBirthday,
                                maps_url = pMapsUrl,
                                termin_payment = pTermin,
                            )
                            setupDialogSendMessage(itemSendMessage)

                            tvName.text = "${ etName.text }"

                            val iPhoneCat1Position = binding.spinPhoneCategories1.selectedItemPosition
                            if (iPhoneCat1Position == 0) binding.tvPhoneCat1.text = "Nomor 1"
                            else binding.tvPhoneCat1.text = "${ binding.spinPhoneCategories1.selectedItem }"

                            tvPhone.text = "+" + formatPhoneNumber("${ etPhone.text }")
                            etPhone.setText(formatPhoneNumber("${ etPhone.text }"))

                            val iPhoneCat2Position = binding.spinPhoneCategories2.selectedItemPosition
                            if (iPhoneCat2Position == 0) binding.tvPhoneCat2.text = "Nomor 2"
                            else binding.tvPhoneCat2.text = "${ binding.spinPhoneCategories2.selectedItem }"

                            val iPhone2 = binding.etPhone2.text.toString()
                            if (iPhone2.isNotEmpty() && iPhone2 != "0") {
                                binding.tvPhone2.text = "+" + formatPhoneNumber(iPhone2)
                                binding.etPhone2.setText(formatPhoneNumber(iPhone2))
                            } else {
                                binding.tvPhone2.text = EMPTY_FIELD_VALUE
                                binding.etPhone2.setText("")
                            }

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

                            iStatus = pStatus.ifEmpty { null }
                            iWeeklyVisitStatus = pWeeklyVisitStatus.ifEmpty { null }
                            iReputation = pReputation.ifEmpty { null }
                            iHariBayar = pHariBayar.ifEmpty { null }

                            iPaymentMethod = pPaymentMethod.ifEmpty { null }
                            iTermin = pTermin.ifEmpty { null }

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
                            progressBar.dismiss()
                            toggleEdit(false)

                        }
                        else -> {

                            handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Gagal mengubah data!")
                            loadingState(false)
                            progressBar.dismiss()
                            toggleEdit(false)

                        }
                    }

                } else {

                    handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Gagal mengubah data! Error: " + response.message())
                    loadingState(false)
                    progressBar.dismiss()
                    toggleEdit(false)

                }


            } catch (e: Exception) {

                if (e is CancellationException) {
                    return@launch
                }
                FirebaseUtils.logErr(this@DetailContactActivity, "Failed DetailContactActivity on saveEdit(). Catch: ${e.message}")
                handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Failed run service. Exception " + e.message)
                loadingState(false)
                progressBar.dismiss()
                toggleEdit(false)

            }

        }

    }

    private fun getContact() {

        contactId = intent.getStringExtra(CONST_CONTACT_ID) ?: "0"
        loadingState(true)
        progressBar.show()

        lifecycleScope.launch {
            try {

                val response = contactId?.let { apiService.getContactDetail(contactId = it) }

                if (response!!.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            val data = responseBody.results[0]
                            setupAllField(data)

                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Gagal memuat kontak! Message: Response status $RESPONSE_STATUS_FAIL or $RESPONSE_STATUS_FAILED")
//                            loadingState(false)
//                            progressBar.dismiss()
                            toggleEdit(false)
                            setToGetCities()

                        }
                        else -> {

                            handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Gagal memuat kontak!")
//                            loadingState(false)
//                            progressBar.dismiss()
                            toggleEdit(false)
                            setToGetCities()

                        }
                    }

                } else {

                    handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Gagal memuat kontak! Error: " + response.message())
//                    loadingState(false)
//                    progressBar.dismiss()
                    toggleEdit(false)
                    setToGetCities()

                }


            } catch (e: Exception) {

                if (e is CancellationException) {
                    return@launch
                }
                FirebaseUtils.logErr(this@DetailContactActivity, "Failed DetailContactActivity on getContact(). Catch: ${e.message}")
                handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Failed run service. Exception " + e.message)
                loadingState(false)
                progressBar.dismiss()
                toggleEdit(false)
                setToGetCities()

            }

        }
    }

    private fun setToGetCities() {
        // Get List City
        if (!isCitiesSuccessfullyLoad) {
            if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_ADMIN_CITY) getContactSales()
            else getCities()
        }
    }

    private fun getDetailContact(withToggleEdit: Boolean = true) {
        loadingState(true)
        if (!progressBar.isShowing()) progressBar.show()

        lifecycleScope.launch {
            try {

                val response = contactId?.let { apiService.getContactDetail(contactId = it) }

                if (response!!.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            val data = responseBody.results[0]

                            if (withToggleEdit) {
                                if (data.ktp_owner.isNotEmpty()) {
                                    tvKtp.text = "Tekan untuk menampilkan KTP"
                                    iKtp = data.ktp_owner
                                }

                                handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Berhasil mengubah data!")
                                toggleEdit(false)

//                                if (iStatus == STATUS_CONTACT_BLACKLIST) {
//                                    btnInvoice.visibility = View.GONE
//                                } else btnInvoice.visibility = View.VISIBLE
                                btnInvoice.visibility = View.VISIBLE

                                setupStatus(iStatus)
                                setupWeeklyVisitStatus(iWeeklyVisitStatus)
                                setupPaymentMethod(iPaymentMethod)
                                setupTermin(iTermin)
                                setupReputation(iReputation)
                                setupHariBayar(iHariBayar)
                                setupAllField(data)
                            } else {

//                                if (data.store_status == STATUS_CONTACT_BLACKLIST) {
//                                    btnInvoice.visibility = View.GONE
//                                } else btnInvoice.visibility = View.VISIBLE
                                btnInvoice.visibility = View.VISIBLE

                                setupStatus(data.store_status)
                                setupWeeklyVisitStatus(data.tagih_mingguan)
//                                setupPaymentMethod(data.payment_method)
//                                setupTermin(data.termin_payment)
//                                setupReputation(data.reputation)
                            }

                            hasEdited = true
                            loadingState(false)
                            progressBar.dismiss()

                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Gagal memuat kontak! Message: Response status $RESPONSE_STATUS_FAIL or $RESPONSE_STATUS_FAILED")
                            loadingState(false)
                            progressBar.dismiss()
                            toggleEdit(false)

                        }
                        else -> {

                            handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Gagal memuat kontak!")
                            loadingState(false)
                            progressBar.dismiss()
                            toggleEdit(false)

                        }
                    }

                } else {

                    handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Gagal memuat kontak! Error: " + response.message())
                    loadingState(false)
                    progressBar.dismiss()
                    toggleEdit(false)

                }

            } catch (e: Exception) {

                if (e is CancellationException) {
                    return@launch
                }
                FirebaseUtils.logErr(this@DetailContactActivity, "Failed DetailContactActivity on getDetailContact(). Catch: ${e.message}")
                handleMessage(this@DetailContactActivity, TAG_RESPONSE_MESSAGE, "Failed run service. Exception " + e.message)
                loadingState(false)
                progressBar.dismiss()
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

    private fun formValidation(phone: String, phone2: String, name: String): Boolean {
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
//        } else if (binding.spinPhoneCategories1.selectedItemPosition < 1) {
//            Toast.makeText(this, "Pilih kategori untuk nomor ke 1", TOAST_LONG).show()
//            false
        } else if (phone2.isNotEmpty()) {
            if (!PhoneHandler.phoneValidation(phone2, binding.etPhone2)) {
                binding.etPhone2.requestFocus()
                false
//            } else if (binding.spinPhoneCategories2.selectedItemPosition < 1) {
//                Toast.makeText(this, "Pilih kategori untuk nomor ke 2", TOAST_LONG).show()
//                false
            } else {
                binding.etPhone2.clearFocus()
                binding.etPhone2.error = null
                true
            }
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

    private fun navigateToDetailInvoice() {

        if (sessionManager.userKind() == USER_KIND_COURIER) {

            val intent = Intent(this@DetailContactActivity, ListSuratJalanActivity::class.java)

            intent.putExtra(CONST_CONTACT_ID, contactId)
            if (tvName.text == EMPTY_FIELD_VALUE) intent.putExtra(CONST_NAME, "")
            else intent.putExtra(CONST_NAME, tvName.text)

            detailLauncher.launch(intent)

        } else {
            val parentLayout: ViewGroup = findViewById(R.id.detail_contact_activity)
            val bottomSheetLayout = layoutInflater.inflate(R.layout.fragment_bottom_sheet_detail_contact, parentLayout, false)

            val invoiceOption = bottomSheetLayout.findViewById<LinearLayout>(R.id.invoiceOption)
            val reportOption = bottomSheetLayout.findViewById<LinearLayout>(R.id.reportOption)
//            val checklistReportOption = bottomSheetLayout.findViewById<LinearLayout>(R.id.btnChecklistReport)
            val btnNewReport = bottomSheetLayout.findViewById<Button>(R.id.btnNewReport)
            val reportsTitle = bottomSheetLayout.findViewById<TextView>(R.id.reportsTitle)
            val voucherOption = bottomSheetLayout.findViewById<LinearLayout>(R.id.voucherOption)
//            checklistReportOption.visibility = View.GONE

            if (sessionManager.userKind() == USER_KIND_COURIER) {
                invoiceOption.visibility = View.GONE
                reportOption.visibility = View.GONE
                btnNewReport.visibility = View.GONE
                voucherOption.visibility = View.GONE
//                checklistReportOption.visibility = View.GONE
            } else if (sessionManager.userKind() == USER_KIND_ADMIN || sessionManager.userKind() == USER_KIND_ADMIN_CITY) {
                reportsTitle.text = "Lihat Laporan Sales"
                reportOption.visibility = View.VISIBLE
//                checklistReportOption.visibility = View.GONE
                btnNewReport.visibility = View.GONE
            } else if (sessionManager.userKind() == USER_KIND_SALES || sessionManager.userKind() == USER_KIND_PENAGIHAN || sessionManager.userKind() == USER_KIND_MARKETING) {
                if (iReportSource == NORMAL_REPORT) {
                    btnNewReport.visibility = View.GONE
//                    checklistReportOption.visibility = View.GONE
                }
//                if (userDistributorId == "1" && iReportSource == NORMAL_REPORT) btnNewReport.visibility = View.GONE
//                else {
//                    if (iStatus == STATUS_CONTACT_BLACKLIST) {
//                        invoiceOption.visibility = View.GONE
//                        sjOption.visibility = View.GONE
//                        voucherOption.visibility = View.GONE
//                    }
//                }
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
                if (iMapsUrl == EMPTY_FIELD_VALUE) intent.putExtra(CONST_MAPS, "")
                else intent.putExtra(CONST_MAPS, iMapsUrl)

                detailLauncher.launch(intent)

            } R.id.suratJalanOption, R.id.invoiceOption -> {

                val intent = Intent(this@DetailContactActivity, ListSuratJalanActivity::class.java)

                intent.putExtra(CONST_CONTACT_ID, contactId)
                if (view.id == R.id.invoiceOption) intent.putExtra("type_list", "list_invoice")
                if (tvName.text == EMPTY_FIELD_VALUE) intent.putExtra(CONST_NAME, "")
                else intent.putExtra(CONST_NAME, tvName.text)

                detailLauncher.launch(intent)

            } R.id.reportOption -> {

                var intent = Intent(this@DetailContactActivity, ReportsActivity::class.java)

                if (sessionManager.userKind() == USER_KIND_ADMIN || sessionManager.userKind() == USER_KIND_ADMIN_CITY) intent = Intent(this@DetailContactActivity, UsersReportActivity::class.java)

                intent.putExtra(CONST_CONTACT_ID, contactId)
                if (tvName.text == EMPTY_FIELD_VALUE) intent.putExtra(CONST_NAME, "")
                else intent.putExtra(CONST_NAME, tvName.text)

                detailLauncher.launch(intent)

//            } R.id.btnChecklistReport -> {
//
//                val intent = Intent(this@DetailContactActivity, ChecklistReportActivity::class.java)
//                intent.putExtra(CONST_NAME, iName)
//                intent.putExtra(CONST_MAPS, iMapsUrl)
//                detailLauncher.launch(intent)

            } else -> {

                var intent = Intent(this@DetailContactActivity, NewReportActivity::class.java)

                if (iRenviSource == "voucher" || iRenviSource == "passive" || iRenviSource == "mg") {
                    intent = Intent(this@DetailContactActivity, ChecklistReportActivity::class.java)
                }

                intent.putExtra(REPORT_SOURCE, iReportSource)
                intent.putExtra(RENVI_SOURCE, iRenviSource)
                intent.putExtra(CONST_CONTACT_ID, contactId)
                intent.putExtra(CONST_INVOICE_ID, iInvoiceId)
                intent.putExtra(REPORT_TYPE_IS_PAYMENT, iReportPaymentStatus)
                if (tvName.text == EMPTY_FIELD_VALUE) intent.putExtra(CONST_NAME, "")
                else intent.putExtra(CONST_NAME, tvName.text)
                if (iMapsUrl == EMPTY_FIELD_VALUE) intent.putExtra(CONST_MAPS, "")
                else intent.putExtra(CONST_MAPS, iMapsUrl)

                detailLauncher.launch(intent)

            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetDialog.dismiss()
        }, 500)

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

    private fun getContactSales() {

        // Get Cities
        lifecycleScope.launch {
            try {

                val response = apiService.getContactSales(idContact = contactId ?: "0")

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        val data = response.results

                        if (data is Map<*, *>) {
                            // Melakukan casting ke tipe Map<String, Any>
                            val resultMap = data as? Map<*, *>

                            // Membuat objek ContactSales baru
                            val contactSales = ContactSales(username = resultMap?.get("username").toString())

                            binding.textBy.visibility = View.VISIBLE
                            binding.textBy.text = " " + getString(R.string.text_by) + " " + contactSales.username
                        }
                        getCities()

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        // Empty creator name
                        getCities()

                    }
                    else -> {

                        handleMessage(this@DetailContactActivity, "CONTACT SALES", getString(R.string.failed_get_data))
                        getCities()

                    }
                }


            } catch (e: Exception) {

                if (e is CancellationException) {
                    return@launch
                }
                FirebaseUtils.logErr(this@DetailContactActivity, "Failed DetailContactActivity on getContactSales(). Catch: ${e.message}")
                handleMessage(this@DetailContactActivity, "CONTACT SALES", "Failed run service. Exception " + e.message)
                getCities()

            }

        }
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

                        getPromo()

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
                        getPromo()

                    }
                    else -> {

                        handleMessage(this@DetailContactActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        getPromo()

                    }
                }


            } catch (e: Exception) {

                if (e is CancellationException) {
                    return@launch
                }
                FirebaseUtils.logErr(this@DetailContactActivity, "Failed DetailContactActivity on getCities(). Catch: ${e.message}")
                handleMessage(this@DetailContactActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                getPromo()

            }

        }
    }

    private fun getPromo() {

        // Get Cities
        lifecycleScope.launch {
            try {

                val response = apiService.getPromo()

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        val results = response.results
                        val items: ArrayList<ModalSearchModel> = ArrayList()
                        items.add(ModalSearchModel("0", EMPTY_FIELD_VALUE))

                        for (i in 0 until results.size) {
                            val data = results[i]
                            items.add(ModalSearchModel(data.id_promo, data.nama_promo))
                        }

                        setupDialogSearchPromo(items)

                        val foundItem = results.find { it.id_promo == iPromo }
                        if (foundItem != null) {
                            tvPromo.text = foundItem.nama_promo
                            etPromo.setText(foundItem.nama_promo)
                            selectedPromo = ModalSearchModel(foundItem.id_promo, foundItem.nama_promo)
                        } else {
                            tvPromo.text = EMPTY_FIELD_VALUE
                            etPromo.setText("")
                        }

                        // Admin Access
//                        if (sessionManager.userKind() == USER_KIND_ADMIN || sessionManager.userKind() == USER_KIND_ADMIN_CITY || sessionManager.userKind() == USER_KIND_SALES) {
                        if (sessionManager.userKind() == USER_KIND_ADMIN || sessionManager.userKind() == USER_KIND_ADMIN_CITY) {
                            icEdit.visibility = View.VISIBLE
                            val indicatorImageView = findViewById<View>(R.id.indicatorView)
                            indicatorImageView.visibility = View.VISIBLE
                            if (sessionManager.userKind() == USER_KIND_ADMIN || sessionManager.userKind() == USER_KIND_ADMIN_CITY) tvKtpContainer.visibility = View.VISIBLE
                        }

                        loadingState(false)
                        progressBar.dismiss()
                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(this@DetailContactActivity, "LIST PROMO", "Daftar promo kosong!")

                        loadingState(false)
                        progressBar.dismiss()

                    }
                    else -> {

                        handleMessage(this@DetailContactActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))

                        loadingState(false)
                        progressBar.dismiss()

                    }
                }


            } catch (e: Exception) {

                if (e is CancellationException) {
                    return@launch
                }
                FirebaseUtils.logErr(this@DetailContactActivity, "Failed DetailContactActivity on getPromo(). Catch: ${e.message}")
                handleMessage(this@DetailContactActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)

                loadingState(false)
                progressBar.dismiss()

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
            STATUS_CONTACT_BID -> {
                tooltipStatus.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.status_bid))
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

    private fun setupWeeklyVisitStatus(status: String? = null) {
        binding.tooltipWeeklyVisit.visibility = View.VISIBLE
        when (status) {
            "1" -> {
                binding.tooltipWeeklyVisit.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.status_active))
                tooltipHandler(binding.tooltipWeeklyVisit, "Customer Status is active")
                binding.tvWeeklyVisit.text = statusWeeklyVisitItem[1]
                binding.spinWeeklyVisit.setSelection(1)
            }
            else -> {
                binding.tooltipWeeklyVisit.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.status_passive))
                tooltipHandler(binding.tooltipWeeklyVisit, "Customer Status is not set")
                binding.tvWeeklyVisit.text = EMPTY_FIELD_VALUE
                binding.spinWeeklyVisit.setSelection(0)
            }
        }
    }

    private fun setupPaymentMethod(paymentMethod: String? = null) {
        when (paymentMethod) {
            PAYMENT_TUNAI -> {
                binding.tvPaymentMethod.text = paymentMethodItem[1]
                binding.spinPaymentMethod.setSelection(1)
            }
            PAYMENT_TRANSFER -> {
                binding.tvPaymentMethod.text = paymentMethodItem[2]
                binding.spinPaymentMethod.setSelection(2)
            }
            else -> {
                binding.tvPaymentMethod.text = EMPTY_FIELD_VALUE
                binding.spinPaymentMethod.setSelection(0)
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

    private fun setupHariBayar(hariBayar: String? = null) {
        when (hariBayar) {
            "bebas" -> {
                binding.tvHariBayar.text = hariBayarItem[1]
                binding.spinHariBayar.setSelection(1)
            }"senin" -> {
                binding.tvHariBayar.text = hariBayarItem[2]
                binding.spinHariBayar.setSelection(2)
            }"selasa" -> {
                binding.tvHariBayar.text = hariBayarItem[3]
                binding.spinHariBayar.setSelection(3)
            }"rabu" -> {
                binding.tvHariBayar.text = hariBayarItem[4]
                binding.spinHariBayar.setSelection(4)
            }"kamis" -> {
                binding.tvHariBayar.text = hariBayarItem[5]
                binding.spinHariBayar.setSelection(5)
            }"jumat" -> {
                binding.tvHariBayar.text = hariBayarItem[6]
                binding.spinHariBayar.setSelection(6)
            }"sabtu" -> {
                binding.tvHariBayar.text = hariBayarItem[7]
                binding.spinHariBayar.setSelection(7)
            }
            else -> {
                binding.tvHariBayar.text = EMPTY_FIELD_VALUE
                binding.spinHariBayar.setSelection(0)
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

    private fun setupWeekliVisitStatusSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusWeeklyVisitItem)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinWeeklyVisit.adapter = adapter
        binding.spinWeeklyVisit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedWeeklyVisitStatus = if (position != 0) statusWeeklyVisitItem[position]
                else ""
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        selectedWeeklyVisitStatus = iStatus!!
        setupWeeklyVisitStatus(iWeeklyVisitStatus)
    }

    private fun setupPaymentMethodSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paymentMethodItem)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinPaymentMethod.adapter = adapter
        binding.spinPaymentMethod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedPaymentMethod = paymentMethodItem[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        selectedPaymentMethod = when (iPaymentMethod) {
            PAYMENT_TUNAI -> paymentMethodItem[1]
            PAYMENT_TRANSFER -> paymentMethodItem[2]
            else -> paymentMethodItem[0]
        }
        setupPaymentMethod(iPaymentMethod)
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

    private fun setupHariBayarSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, hariBayarItem)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinHariBayar.adapter = adapter
        binding.spinHariBayar.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedHariBayar = if (position != 0) hariBayarItem[position]
                else ""
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        selectedHariBayar = when (iHariBayar) {
             "bebas" -> hariBayarItem[1]
             "senin" -> hariBayarItem[2]
             "selasa" -> hariBayarItem[3]
             "rabu" -> hariBayarItem[4]
             "kamis" -> hariBayarItem[5]
             "jumat" -> hariBayarItem[6]
             "sabtu" -> hariBayarItem[7]
            else -> "-1"
        }
        setupHariBayar(iHariBayar)
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

            imagePickerLauncher!!.launch(chooserIntent)
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

    private fun setupDelivery() {

        if (isClosingAction) {
            isDeliveryLoading = false
            binding.btnDeliveryContainer.visibility = View.GONE
            binding.contactAction.visibility = View.VISIBLE
            binding.textDelivery.visibility = View.VISIBLE
            binding.btnDirection.visibility = View.GONE

            binding.textDeliveryTitle.text = "Berhasil Closing"
            binding.textDeliveryDesc.text = "Segera selesaikan pengiriman lainnya."
            binding.textLoading.visibility = View.GONE
            return
        }

        deliveryId = "$AUTH_LEVEL_COURIER$userID"
        val userDistributorIds = sessionManager.userDistributor()
        firebaseReference = FirebaseUtils.getReference(distributorId = userDistributorIds ?: "-firebase-007")
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
                                    binding.textLoading.visibility = View.GONE
                                    binding.btnDeliveryContainer.visibility = View.GONE
                                    binding.contactAction.visibility = View.VISIBLE
                                    binding.textDelivery.visibility = View.VISIBLE
                                    binding.btnDirection.visibility = View.VISIBLE
                                    binding.btnDirection.setOnClickListener {
                                        val intent = Intent(this@DetailContactActivity, MapsActivity::class.java)
                                        intent.putExtra(CONST_IS_TRACKING, true)
                                        intent.putExtra(CONST_DELIVERY_ID, deliveryId)
                                        intent.putExtra(CONST_CONTACT_ID, contactId)
                                        detailLauncher.launch(intent)
                                    }
                                    checkServiceStatus()
                                } else setupBtnDelivery()
                            } else setupBtnDelivery()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            binding.textLoading.visibility = View.GONE
                            handleMessage(this@DetailContactActivity, "onSetupDelivery", "Failed get store child")
                            Log.e("onSetupDelivery", error.message)
                        }

                    })
                } else {
                    setupBtnDelivery()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.textLoading.visibility = View.GONE
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
            Handler(Looper.getMainLooper()).postDelayed({
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
                            startDatetime = DateFormat.now(),
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
                            detailLauncher.launch(intent)
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
        binding.textLoading.visibility = View.GONE

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
//                            val message =
//                                "Izin Diperlukan: Izin lokasi latar belakang diperlukan untuk fitur ini. Lokasi akan digunakan ketika aplikasi ditutup untuk mendukung fungsi latar belakang.\n" +
//                                "\n" +
//                                "Fitur yang Menggunakan Lokasi di Latar Belakang:\n" +
//                                "1. Pembaruan otomatis lokasi pengguna.\n" +
//                                "2. Notifikasi terkait geolokasi.\n" +
//                                "\n" +
//                                "Mohon untuk memilih opsi berikut: \"${ getString(R.string.yes_bg_location) }\""
                            val message = getString(R.string.bg_service_location_permission_message)
                            val title = getString(R.string.bg_service_location_permission_title)
                            val customUtility = CustomUtility(this@DetailContactActivity)
                            customUtility.showPermissionDeniedDialog(title = title, message = message) {
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
//        val isTracking = CustomUtility(this@DetailContactActivity).isServiceRunning(TrackingService::class.java)
//        if (!isTracking) {
            val serviceIntent = Intent(this@DetailContactActivity, TrackingService::class.java)
            serviceIntent.putExtra("userId", userID)
            serviceIntent.putExtra("userDistributorId", userDistributorIds ?: "-start-002-$username")
            if (userKind == USER_KIND_COURIER) serviceIntent.putExtra("deliveryId", deliveryId)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
//        }
    }

    private fun scrollViewListener() {

        val profileBar = binding.profileBar
        profileBar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val profileBarHeight = profileBar.measuredHeight // Ukuran tinggi LinearLayout

        binding.contentScrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = binding.contentScrollView.scrollY

            if (scrollY < profileBarHeight) {
                binding.titleBar.tvTitleBar.text = titlePage
            } else if (scrollY > profileBarHeight) {
                binding.titleBar.tvTitleBar.text = iName
            }
        }
    }

    private fun phoneAnimation(startHeight: Int, targetHeight: Int) {
        val duration = 500L
        val animator = ValueAnimator.ofInt(startHeight, targetHeight)
        animator.duration = duration
        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Int

            val layoutParams = binding.tvPhoneContainer.layoutParams
            layoutParams.height = animatedValue
            binding.tvPhoneContainer.layoutParams = layoutParams
        }
        animator.start()

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isPhoneFieldOpened) {
                binding.togglePhoneSizeText.text = "Lainnya"
                binding.togglePhoneSizeIcon.setImageResource(R.drawable.chevron_down_only_white)
//                val params = binding.togglePhoneSizeIcon.layoutParams as ViewGroup.MarginLayoutParams
//                params.setMargins(0, convertDpToPx(1, this@DetailContactActivity), 0, 0)
            } else {
                binding.togglePhoneSizeText.text = "Tutup"
                binding.togglePhoneSizeIcon.setImageResource(R.drawable.chevron_up_only_white)
//                val params = binding.togglePhoneSizeIcon.layoutParams as ViewGroup.MarginLayoutParams
//                params.setMargins(0, 0, 0, convertDpToPx(1, this@DetailContactActivity))
            }
        }, duration)
    }

    private fun setupAllField(data: ContactModel) {
        val iContactId = data.id_contact
        val iPhoneCategory1 = data.nomor_cat_1
        val iPhone = data.nomorhp
        val iPhoneCategory2 = data.nomor_cat_2
        val iPhone2 = data.nomorhp_2
        val iOwner = data.store_owner
        val iBirthday = data.tgl_lahir
        val iDate = data.created_at

        if (iDate.isEmpty()) {
            binding.dateSeparator.visibility = View.GONE
            binding.line.visibility = View.VISIBLE
        } else {
            val date = DateFormat.format(iDate, outputFormat = "dd MMM yyyy")

            binding.tvDate.text = date
            binding.dateSeparator.visibility = View.VISIBLE
            binding.line.visibility = View.GONE
        }

        iName = data.nama
        iKtp = data.ktp_owner
        iMapsUrl = data.maps_url
        iStatus = data.store_status
        iWeeklyVisitStatus = data.tagih_mingguan
        iPaymentMethod = data.payment_method
        iTermin = data.termin_payment
        iReputation = data.reputation
        iHariBayar = data.hari_bayar

        tooltipStatus.visibility = View.VISIBLE
        binding.weeklyVisitContainer.visibility = View.VISIBLE
        binding.hariBayarContainer.visibility = View.VISIBLE
        binding.tooltipWeeklyVisit.visibility = View.VISIBLE
//        if (iStatus == STATUS_CONTACT_BLACKLIST) btnInvoice.visibility = View.GONE
//        else btnInvoice.visibility = View.VISIBLE
        btnInvoice.visibility = View.VISIBLE

        iAddress = data.address
        iLocation = data.id_city
        iPromo = data.id_promo
        iReportSource = intent.getStringExtra(REPORT_SOURCE).let { if (it.isNullOrEmpty()) NORMAL_REPORT else it }
        iRenviSource = intent.getStringExtra(RENVI_SOURCE).let { if (it.isNullOrEmpty()) NORMAL_REPORT else it }
        iInvoiceId = intent.getStringExtra(CONST_INVOICE_ID)
        iReportPaymentStatus = intent.getBooleanExtra(REPORT_TYPE_IS_PAYMENT, false)

        activityRequestCode = intent.getIntExtra(ACTIVITY_REQUEST_CODE, activityRequestCode)

        itemSendMessage = ContactModel(
            id_contact = iContactId,
            nama = iName!!,
            nomor_cat_1 = iPhoneCategory1,
            nomorhp = iPhone,
            nomor_cat_2 = iPhoneCategory2,
            nomorhp_2 = iPhone2,
            store_owner = iOwner,
            tgl_lahir = iBirthday,
            maps_url = iMapsUrl!!,
            id_city = iLocation!!
        )
        setupDialogSendMessage(itemSendMessage)

        if (iContactId.isNotEmpty()) {
            contactId = iContactId
        }

        if (iPhoneCategory1.isNotEmpty()) {
            val indexItem = spinPhoneCatItems.indexOf(iPhoneCategory1)
            if (indexItem > 0) {
                binding.spinPhoneCategories1.setSelection(indexItem)
                binding.tvPhoneCat1.text = "${ binding.spinPhoneCategories1.selectedItem }"
            }
        }
        if (iPhone.isNotEmpty()) {
            tvPhone.text = "+$iPhone"
            etPhone.setText(iPhone)
        } else {
            tvPhone.text = EMPTY_FIELD_VALUE
            etPhone.setText("")
        }

        if (iPhoneCategory2.isNotEmpty()) {
            val indexItem = spinPhoneCatItems.indexOf(iPhoneCategory2)
            if (indexItem > 0) {
                binding.spinPhoneCategories2.setSelection(indexItem)
                binding.tvPhoneCat2.text = "${ binding.spinPhoneCategories2.selectedItem }"
            }
        }
        if (iPhone2.isNotEmpty() && iPhone2 != "0") {
            binding.tvPhone2.text = "+$iPhone2"
            binding.etPhone2.setText(iPhone2)
        } else {
            binding.tvPhone2.text = EMPTY_FIELD_VALUE
            binding.etPhone2.setText("")
        }
        if (!iName.isNullOrEmpty()) {
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
        if (!iPromo.isNullOrEmpty()) {
            tvPromo.text = getString(R.string.txt_loading)
            etPromo.setText(getString(R.string.txt_loading))
        } else {
            tvPromo.text = EMPTY_FIELD_VALUE
            etPromo.setText("")
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

//                            loadingState(false)
//                            progressBar.dismiss()

        // Set Spinner
        setupStatusSpinner()
        setupWeekliVisitStatusSpinner()
        setupPaymentMethodSpinner()
        setupTerminSpinner()
        setupReputationSpinner()
        setupHariBayarSpinner()
        setToGetCities()
    }

    override fun onStart() {
        super.onStart()
        Handler(Looper.getMainLooper()).postDelayed({
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(true, userDistributorIds ?: "-custom-003", userID)
            }
        }, 1000)
    }

    override fun onStop() {
        super.onStop()
        if (CustomUtility(this).isUserWithOnlineStatus()) {
            CustomUtility(this).setUserStatusOnline(false, userDistributorIds ?: "-custom-003", userID)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::progressBar.isInitialized && progressBar.isShowing()) {
            progressBar.dismiss()
        }
        if (pingUtility != null) pingUtility!!.stopPingMonitoring()
        if (CustomUtility(this).isUserWithOnlineStatus()) {
            CustomUtility(this).setUserStatusOnline(false, userDistributorIds ?: "-custom-003", userID)
        }
    }

}