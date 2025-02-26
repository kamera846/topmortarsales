@file:Suppress("DEPRECATION")

package com.topmortar.topmortarsales.view.reports

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_COURIER
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_INVOICE_ID
import com.topmortar.topmortarsales.commons.CONST_IS_BASE_CAMP
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_MAPS_NAME
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.IS_PAY_STATUS_NOT_PAY
import com.topmortar.topmortarsales.commons.IS_PAY_STATUS_PAY
import com.topmortar.topmortarsales.commons.IS_PAY_STATUS_PAY_LATER
import com.topmortar.topmortarsales.commons.LOCATION_PERMISSION_REQUEST_CODE
import com.topmortar.topmortarsales.commons.MAX_REPORT_DISTANCE
import com.topmortar.topmortarsales.commons.NORMAL_REPORT
import com.topmortar.topmortarsales.commons.RENVI_SOURCE
import com.topmortar.topmortarsales.commons.REPORT_SOURCE
import com.topmortar.topmortarsales.commons.REPORT_TYPE_IS_PAYMENT
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.USER_KIND_PENAGIHAN
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.services.TrackingService
import com.topmortar.topmortarsales.commons.utils.CustomEtHandler
import com.topmortar.topmortarsales.commons.utils.CustomEtHandler.setMaxLength
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.URLUtility
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityNewReportBinding
import com.topmortar.topmortarsales.view.MapsActivity
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar

@SuppressLint("SetTextI18n")
class NewReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewReportBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var customUtility: CustomUtility
    private lateinit var progressDialog: ProgressDialog

    private val userId get() = sessionManager.userID()
    private val username get() = sessionManager.userName()
    private val userKind get() = sessionManager.userKind()
    private val userDistributorId get() = sessionManager.userDistributor()

    private val msgMaxLines = 6
    private val msgMaxLength = 500

    private var isSalesOrPenagihan = false
    private var isDistanceToLong = false
    private var isBaseCamp = false
    private var isReportPaymentStatus = true
    private var iInvoiceId: String? = null
    private var reportType: String = "toko"
    private var id: String = ""
    private var realPaymentValue = ""
    private var selectedDate: Calendar = Calendar.getInstance()
    private var realPaymentDateValue = ""
    private val idUser get() = sessionManager.userID().toString()
    private var name: String = ""
    private var coordinate: String = ""
    private lateinit var iReportSource: String
    private lateinit var iRenviSource: String

    private lateinit var datePicker: DatePickerDialog
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge(isPrimary = false)

        binding = ActivityNewReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        customUtility = CustomUtility(this)

        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Sedang menghitung jarak...")

        if (CustomUtility(this).isUserWithOnlineStatus()) {
            CustomUtility(this).setUserStatusOnline(
                true,
                sessionManager.userDistributor() ?: "-custom-012",
                sessionManager.userID() ?: ""
            )
        }

        iReportSource = intent.getStringExtra(REPORT_SOURCE).let { if (it.isNullOrEmpty()) NORMAL_REPORT else it }
        iRenviSource = intent.getStringExtra(RENVI_SOURCE).let { if (it.isNullOrEmpty()) NORMAL_REPORT else it }

        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkGpsStatus()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun checkGpsStatus() {
        val locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!isGpsEnabled) {
            // GPS tidak aktif, munculkan dialog untuk mengaktifkannya
            showGpsDisabledDialog()
        } else {
//            if (!CustomUtility(this).isServiceRunning(
//                    TrackingService::class.java)) {
                val serviceIntent = Intent(this, TrackingService::class.java)
                serviceIntent.putExtra("userId", userId)
                serviceIntent.putExtra("userDistributorId", userDistributorId ?: "-start-001-$username")
                if (userKind == USER_KIND_COURIER) serviceIntent.putExtra("deliveryId", AUTH_LEVEL_COURIER + userId)
                startService(serviceIntent)
//            }
            initContent()
        }
    }

    private fun showGpsDisabledDialog() {

        val serviceIntent = Intent(this, TrackingService::class.java)
        stopService(serviceIntent)

        AlertDialog.Builder(this)
            .setMessage("Aplikasi memerlukan lokasi untuk berfungsi. Aktifkan lokasi sekarang?")
            .setCancelable(false)
            .setPositiveButton("Ya") { _, _ ->
                // Buka pengaturan untuk mengaktifkan GPS
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(intent, LOCATION_PERMISSION_REQUEST_CODE)
            }
            .show()
    }

    private fun initContent() {
        isSalesOrPenagihan = userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN
        binding.titleBarLight.tvTitleBar.text = "Buat Laporan"
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        loadingContent(true)

        etMessageListener()

        id = intent.getStringExtra(CONST_CONTACT_ID).toString()
        name = intent.getStringExtra(CONST_NAME).toString()
        coordinate = intent.getStringExtra(CONST_MAPS).toString()
        isBaseCamp = intent.getBooleanExtra(CONST_IS_BASE_CAMP, false)
        iInvoiceId = intent.getStringExtra(CONST_INVOICE_ID)
        isReportPaymentStatus = intent.getBooleanExtra(REPORT_TYPE_IS_PAYMENT, false)

        when (isBaseCamp) {
            true -> {
                reportType = "basecamp"
                binding.tvNameLabel.text = getString(R.string.basecamp_name)
                binding.etMessage.hint = getString(R.string.laporan_basecamp_hint)
            }
            else -> {
                reportType = "toko"
                binding.tvNameLabel.text = getString(R.string.store_name)
                binding.etMessage.hint = getString(R.string.laporan_toko_hint)
            }
        }

        if (isSalesOrPenagihan) {
//            binding.reportPaymentContainer.visibility = View.VISIBLE
            binding.reportPaymentTrueContainer.visibility = View.VISIBLE

            binding.etPaymentYes.addTextChangedListener(object: TextWatcher {
                private var current = ""

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString() != current) {
                        binding.etPaymentYes.removeTextChangedListener(this)

                        val cleanString = s.toString().replace("""[Rp,.]""".toRegex(), "")
                        val parsed = cleanString.toDoubleOrNull() ?: 0.0

                        // Custom format without currency symbol and unnecessary zeros
                        val decimalFormatSymbols = DecimalFormatSymbols().apply {
                            groupingSeparator = ','
                        }
                        val decimalFormat = DecimalFormat("#,###", decimalFormatSymbols)
                        val formatted = decimalFormat.format(parsed)

                        current = formatted
                        realPaymentValue = getCurrencyNormalValue(formatted).toString()
                        binding.etPaymentYes.setText(formatted)
                        binding.etPaymentYes.setSelection(formatted.length)

                        binding.etPaymentYes.addTextChangedListener(this)
                    }
                }

            })

            changeReportPaymentTypeUI()
        }

        initClickHandler()
        Handler(Looper.getMainLooper()).postDelayed({

            binding.etName.text = name
            calculateDistance()

            loadingContent(false)
        }, 500)
    }

    private fun getCurrencyNormalValue(formattedString: String): Long {
        val cleanString = formattedString.replace("""[,.]""".toRegex(), "")
        return cleanString.toLongOrNull() ?: -1
    }

    private fun toggleReportPayment(state: Boolean? = null) {
        isReportPaymentStatus = state ?: !isReportPaymentStatus
        changeReportPaymentTypeUI()
    }

    private fun changeReportPaymentTypeUI() {
        binding.etPaymentYesContainer.visibility = View.GONE
        binding.etPaymentLaterContainer.visibility = View.GONE
        binding.rgReportPaymentTrue.clearCheck()
        binding.etPaymentYes.setText("0")
        binding.etPaymentYes.setSelection(binding.etPaymentYes.length())
        realPaymentValue = ""
        binding.etPaymentLater.setText("")
        realPaymentDateValue = ""

        if (!isReportPaymentStatus) {
            binding.reportPaymentFalse.setBackgroundResource(R.drawable.bg_primary_round_8)
            binding.reportPaymentFalse.setTextColor(getColor(R.color.white))
            binding.reportPaymentTrue.setBackgroundResource(R.drawable.et_background_clickable)
            if (!customUtility.isDarkMode()) binding.reportPaymentTrue.setTextColor(getColor(R.color.black_200))
            else binding.reportPaymentTrue.setTextColor(getColor(R.color.black_600))
            binding.reportPaymentTrueContainer.visibility = View.GONE
        } else {
            binding.reportPaymentTrue.setBackgroundResource(R.drawable.bg_primary_round_8)
            binding.reportPaymentTrue.setTextColor(getColor(R.color.white))
            binding.reportPaymentFalse.setBackgroundResource(R.drawable.et_background_clickable)
            if (!customUtility.isDarkMode()) binding.reportPaymentFalse.setTextColor(getColor(R.color.black_200))
            else binding.reportPaymentFalse.setTextColor(getColor(R.color.black_600))
            binding.reportPaymentTrueContainer.visibility = View.VISIBLE
        }
    }

    private fun initClickHandler() {
        binding.titleBarLight.icBack.setOnClickListener { finish() }
        binding.btnReport.setOnClickListener { submitValidation() }
        binding.lnrDistance.setOnClickListener { calculateDistance() }
        binding.reportPaymentTrue.setOnClickListener { toggleReportPayment(true) }
        binding.reportPaymentFalse.setOnClickListener { toggleReportPayment(false) }
        binding.rgReportPaymentTrue.setOnCheckedChangeListener { _, checkedId ->
            binding.etPaymentYes.setText("0")
            binding.etPaymentYes.setSelection(binding.etPaymentYes.length())
            realPaymentValue = ""
            binding.etPaymentLater.setText("")
            realPaymentDateValue = ""
            binding.rgReportPaymentError.visibility = View.GONE

            when (checkedId) {
                R.id.rbPayYes -> {
                    binding.etPaymentYesContainer.visibility = View.VISIBLE
                    binding.etPaymentLaterContainer.visibility = View.GONE
                } R.id.rbNotPay -> {
                    binding.etPaymentYesContainer.visibility = View.GONE
                    binding.etPaymentLaterContainer.visibility = View.GONE
                } R.id.rbPayLater -> {
                    binding.etPaymentYesContainer.visibility = View.GONE
                    binding.etPaymentLaterContainer.visibility = View.VISIBLE
                }
            }
        }
        binding.etPaymentLater.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                setDatePickerDialog()
                datePicker.show()
                binding.etPaymentLater.setSelection(binding.etPaymentLater.length())
            } else binding.etPaymentLater.clearFocus()
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
                realPaymentDateValue =  DateFormat.format(selectedDate, "yyyy-MM-dd")
                binding.etPaymentLater.setText(formattedDate)
                binding.etPaymentLater.clearFocus()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000

        datePicker.setOnDismissListener {
            binding.etPaymentLater.clearFocus()
        }

    }

    private fun etMessageListener() {
        val etMessage = binding.etMessage
        val tvMaxMessage = binding.tvMaxMessage

        etMessage.maxLines = msgMaxLines
        etMessage.setMaxLength(msgMaxLength)

        etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                CustomEtHandler.updateTxtMaxLength(
                    tvMaxMessage,
                    msgMaxLength,
                    etMessage.text.length
                )
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

    }

    @SuppressLint("MissingPermission")
    fun calculateDistance() {
        progressDialog.show()

        Handler(Looper.getMainLooper()).postDelayed({

            val mapsUrl = coordinate
            val urlUtility = URLUtility(this)

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (urlUtility.isLocationEnabled(this)) {

                    urlUtility.requestLocationUpdate()

                    if (!urlUtility.isUrl(mapsUrl) && mapsUrl.isNotEmpty()) {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->

                            if (location != null) {
                                // Courier Location
                                val currentLatitude = location.latitude
                                val currentLongitude = location.longitude

                                // Store Location
                                val coordinate = mapsUrl.split(",")
                                val latitude = coordinate[0].toDoubleOrNull()
                                val longitude = coordinate[1].toDoubleOrNull()

                                if (latitude != null && longitude != null) {

                                    // Calculate Distance
                                    val distance = urlUtility.calculateDistance(
                                        currentLatitude,
                                        currentLongitude,
                                        latitude,
                                        longitude
                                    )
                                    val shortDistance = "%.3f".format(distance)

                                    if (distance > MAX_REPORT_DISTANCE) {
                                        val builder = AlertDialog.Builder(this)
                                        builder.setCancelable(false)
                                        builder.setOnDismissListener { progressDialog.dismiss() }
                                        builder.setOnCancelListener { progressDialog.dismiss() }
                                        builder.setTitle("Peringatan!")
                                            .setMessage("Titik anda saat ini $shortDistance km dari titik $reportType. Cobalah untuk lebih dekat dengan $reportType!")
                                            .setPositiveButton("Oke") { dialog, _ ->
                                                progressDialog.dismiss()

                                                binding.etDistance.setTextColor(getColor(R.color.primary))
                                                binding.etDistance.text = shortDistance
                                                binding.icRefreshDistance.visibility = View.VISIBLE
                                                binding.tvDistanceError.visibility = View.VISIBLE
                                                binding.tvDistanceError.text =
                                                    "Jarak anda lebih dari $MAX_REPORT_DISTANCE km. Cobalah untuk lebih dekat dengan titik $reportType dan refresh jaraknya!"
                                                isDistanceToLong = true

                                                dialog.dismiss()
                                            }
                                            .setNegativeButton("Buka Maps") { dialog, _ ->
                                                val intent = Intent(
                                                    this@NewReportActivity,
                                                    MapsActivity::class.java
                                                )
                                                intent.putExtra(
                                                    CONST_IS_BASE_CAMP,
                                                    reportType == "basecamp"
                                                )
                                                intent.putExtra(CONST_MAPS, mapsUrl)
                                                intent.putExtra(CONST_MAPS_NAME, name)
                                                startActivity(intent)

                                                progressDialog.dismiss()

                                                binding.etDistance.setTextColor(getColor(R.color.primary))
                                                binding.etDistance.text = shortDistance
                                                binding.icRefreshDistance.visibility = View.VISIBLE
                                                binding.tvDistanceError.visibility = View.VISIBLE
                                                binding.tvDistanceError.text =
                                                    "Cobalah untuk lebih dekat dengan titik $reportType dan refresh jaraknya!"
                                                isDistanceToLong = true

                                                dialog.dismiss()
                                            }
                                        builder.show()
                                    } else {
                                        progressDialog.dismiss()

                                        var textColor = getColor(R.color.black_200)
                                        if (customUtility.isDarkMode()) textColor =
                                            getColor(R.color.black_600)

                                        binding.etDistance.setTextColor(textColor)
                                        binding.etDistance.text = shortDistance
                                        binding.icRefreshDistance.visibility = View.VISIBLE
                                        binding.tvDistanceError.visibility = View.GONE
                                        isDistanceToLong = false
                                    }

                                } else {
                                    progressDialog.dismiss()
                                    Toast.makeText(this, "Gagal memproses koordinat", TOAST_SHORT)
                                        .show()
                                }
                            } else {
                                AlertDialog.Builder(this)
                                    .setCancelable(false)
                                    .setTitle("Gagal memproses lokasi")
                                    .setMessage("Cobalah untuk menututup dan membuka ulang aplikasi")
                                    .setPositiveButton("Tutup") { dialog, _ ->
                                        finish()
                                        dialog.dismiss()
                                    }
                                    .show()
                            }

                        }.addOnFailureListener {
                            progressDialog.dismiss()
                            handleMessage(this, "LOG REPORT", "Gagal mendapatkan lokasi anda. Err: " + it.message)
//                            Toast.makeText(this, "Gagal mendapatkan lokasi anda", TOAST_SHORT).show()
                        }

                    } else {
                        progressDialog.dismiss()
                        val message = "Anda tidak dapat membuat laporan untuk saat ini, silakan hubungi admin untuk memperbarui koordinat $reportType ini"
                        val actionTitle = "Hubungi Sekarang"
                        customUtility.showPermissionDeniedSnackbar(message, actionTitle) { navigateChatAdmin() }
                    }

                } else {
                    progressDialog.dismiss()
                    val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(enableLocationIntent)
                }

            } else {
                progressDialog.dismiss()
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            }

        }, 500)
    }

    private fun navigateChatAdmin() {
        val distributorNumber = sessionManager.userDistributorNumber()!!
        val phoneNumber = distributorNumber.ifEmpty { getString(R.string.topmortar_wa_number) }
        val message = "*#Courier Service*\nHalo admin, tolong bantu saya untuk memperbarui koordinat pada $reportType *${ name }*"

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}")

        try {
            startActivity(intent)
            finishAffinity()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Gagal menghubungkan ke whatsapp", TOAST_SHORT).show()
        }

    }

    private fun submitValidation() {

        if (!formValidation()) return

        val dialog = AlertDialog.Builder(this)
            .setTitle("Konfirmasi Laporan!")
            .setMessage("Apakah anda yakin ingin mengirim laporan sekarang?")
            .setNegativeButton("Tidak") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Iya") { _, _ -> submitReport() }
            .create()

        dialog.show()
    }

    private fun formValidation(): Boolean {
        val etMessage = binding.etMessage
        if (coordinate.isEmpty()) {
            val message = "Jarak anda belum terhitung!"
            customUtility.showDialog(message = message)
            return false
        }
        if (isDistanceToLong) {
            val message = "Anda tidak dapat membuat laporan untuk saat ini, Cobalah untuk lebih dekat dengan titik $reportType dan refresh jaraknya!"
            val actionTitle = "Refresh Sekarang"
            customUtility.showPermissionDeniedSnackbar(message, actionTitle) { calculateDistance() }
            return false
        }
        if (etMessage.text.isNullOrEmpty()) {
            etMessage.error = "Pesan laporan wajib diisi"
            etMessage.requestFocus()
            return false
        }
        if (isReportPaymentStatus) {
            val checkedRadio = when (binding.rgReportPaymentTrue.checkedRadioButtonId) {
                R.id.rbPayYes -> IS_PAY_STATUS_PAY
                R.id.rbPayLater -> IS_PAY_STATUS_PAY_LATER
                R.id.rbNotPay -> IS_PAY_STATUS_NOT_PAY
                else -> null
            }
            if (checkedRadio == null) {
                binding.rgReportPaymentTrue.requestFocus()
                binding.rgReportPaymentError.visibility = View.VISIBLE
                return false
            } else if (checkedRadio == IS_PAY_STATUS_PAY && (realPaymentValue.isEmpty() || realPaymentValue == "0")) {
                binding.etPaymentYes.error = "Jumlah yang di bayarkan harus lebih dari 0"
                binding.etPaymentYes.requestFocus()
                return false
            } else if (checkedRadio == IS_PAY_STATUS_PAY_LATER && realPaymentDateValue.isEmpty()) {
                binding.etPaymentLater.error = "Silahkan pilih tanggal terlebih dahulu"
                binding.etPaymentLater.requestFocus()
                return false
            }
        }

        etMessage.error = null
        etMessage.clearFocus()
        binding.rgReportPaymentTrue.clearFocus()
        binding.etPaymentYes.error = null
        binding.etPaymentYes.clearFocus()
        binding.etPaymentLater.error = null
        binding.etPaymentLater.clearFocus()
        return true
    }

    private fun submitReport() {

        val submitDialog = ProgressDialog(this)
        submitDialog.setCancelable(false)
        submitDialog.setMessage("Membuat laporan...")

        submitDialog.show()
        loadingSubmit(true)

//        Handler(Looper.getMainLooper()).postDelayed({
//            Toast.makeText(this@NewReportActivity, "Berhasil membuat laporan", TOAST_SHORT).show()
//            println("===== Form Data =====")
//            println("Form data id: $id")
//            println("Form data id user: $idUser")
//            println("Form data id invoice: $iInvoiceId")
//            println("Form data distance: ${binding.etDistance.text}")
//            println("Form data message: ${binding.etMessage.text}")
//            println("Form data reportSource: $iReportSource")
//            println("Form data renviSource: $iRenviSource")
//            if (!isReportPaymentStatus) {
//                println("Form data is_pay: 0")
//            } else {
//                when (binding.rgReportPaymentTrue.checkedRadioButtonId) {
//                    R.id.rbPayYes -> {
//                        println("Form data is_pay: pay")
//                        println("Form data pay_value: $realPaymentValue")
//                    }
//                    R.id.rbPayLater -> {
//                        println("Form data is_pay: pay_later")
//                        println("Form data pay_date: $realPaymentDateValue")
//                    } else -> println("Form data is_pay: not_pay")
//                }
//            }
//            submitDialog.dismiss()
//            loadingSubmit(false)
//        }, 1000)
//
//        return

        lifecycleScope.launch {
            try {

                val rbidContact = createPartFromString(id)
                val rbidUser = createPartFromString(idUser)
                val rbdistanceVisit = createPartFromString(binding.etDistance.text.toString())
                val rblaporanVisit = createPartFromString(binding.etMessage.text.toString())
                val rbSource = createPartFromString(iReportSource)
                val rbRenviSource = createPartFromString(iRenviSource)
                val rbInvoiceId = createPartFromString(iInvoiceId ?: "")

                val apiService: ApiService = HttpClient.create()
                val response = when (isBaseCamp) {
                    true -> apiService.makeVisitCourierReport(
                        idGudang = rbidContact,
                        idUser = rbidUser,
                        distanceVisit = rbdistanceVisit,
                        laporanVisit = rblaporanVisit,
                        source = rbSource,
                        renviSource = rbRenviSource,
                    ) else -> {
                        if (!isReportPaymentStatus) {
                            apiService.makeVisitReport(
                                idContact = rbidContact,
                                idUser = rbidUser,
                                distanceVisit = rbdistanceVisit,
                                laporanVisit = rblaporanVisit,
                                source = rbSource,
                                renviSource = rbRenviSource,
                                idInvoice = rbInvoiceId,
                                isPay = createPartFromString("0")
                            )
                        } else {
                            when (binding.rgReportPaymentTrue.checkedRadioButtonId) {
                                R.id.rbPayYes -> {
                                    apiService.makeVisitReportPaymentYes(
                                        idContact = rbidContact,
                                        idUser = rbidUser,
                                        distanceVisit = rbdistanceVisit,
                                        laporanVisit = rblaporanVisit,
                                        source = rbSource,
                                        renviSource = rbRenviSource,
                                        idInvoice = rbInvoiceId,
                                        isPay = createPartFromString(IS_PAY_STATUS_PAY),
                                        payValue = createPartFromString(realPaymentValue)
                                    )
                                }
                                R.id.rbPayLater -> {
                                    apiService.makeVisitReportPaymentLater(
                                        idContact = rbidContact,
                                        idUser = rbidUser,
                                        distanceVisit = rbdistanceVisit,
                                        laporanVisit = rblaporanVisit,
                                        source = rbSource,
                                        renviSource = rbRenviSource,
                                        idInvoice = rbInvoiceId,
                                        isPay = createPartFromString(IS_PAY_STATUS_PAY_LATER),
                                        payDate = createPartFromString(realPaymentDateValue)
                                    )
                                }
                                else -> {
                                    apiService.makeVisitReport(
                                        idContact = rbidContact,
                                        idUser = rbidUser,
                                        distanceVisit = rbdistanceVisit,
                                        laporanVisit = rblaporanVisit,
                                        source = rbSource,
                                        renviSource = rbRenviSource,
                                        idInvoice = rbInvoiceId,
                                        isPay = createPartFromString(IS_PAY_STATUS_NOT_PAY)
                                    )
                                }
                            }
                        }
                    }
                }

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            submitDialog.dismiss()
                            loadingSubmit(false)

                            if (iRenviSource == "voucher" || iRenviSource == "passive") {
//                                println("RESPONSE REPORT: ${Gson().toJson(response.body())}")
                                val intent = Intent(
                                    this@NewReportActivity,
                                    ChecklistReportActivity::class.java
                                )
                                intent.putExtra("id_visit", responseBody.id_visit)
                                intent.putExtra(CONST_NAME, name)
                                intent.putExtra(CONST_MAPS, coordinate)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this@NewReportActivity, responseBody.message, TOAST_SHORT).show()
                            }

                            finish()

                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            handleMessage(this@NewReportActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim laporan! Message: ${ responseBody.message }")
                            submitDialog.dismiss()
                            loadingSubmit(false)

                        }
                        else -> {

                            handleMessage(this@NewReportActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim laporan!: ${ responseBody.message }")
                            submitDialog.dismiss()
                            loadingSubmit(false)

                        }
                    }

                } else {

                    handleMessage(this@NewReportActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim laporan! Error: " + response.message())
                    submitDialog.dismiss()
                    loadingSubmit(false)

                }

            } catch (e: Exception) {

                handleMessage(this@NewReportActivity, TAG_RESPONSE_MESSAGE, "Failed run service. Exception " + e.message)
                submitDialog.dismiss()
                loadingSubmit(false)

            }

        }
    }

    private fun loadingSubmit(state: Boolean) {
        if (state) {
            binding.btnReport.text = getString(R.string.txt_loading)
            binding.btnReport.isEnabled = false
        } else {
            binding.btnReport.text = "Kirim Laporan"
            binding.btnReport.isEnabled = true
        }
    }

    private fun loadingContent(state: Boolean) {
        if (state) {
            binding.tvLoading.visibility = View.VISIBLE
            binding.container.visibility = View.GONE
        } else {
            binding.tvLoading.visibility = View.GONE
            binding.container.visibility = View.VISIBLE
        }
    }
    override fun onStart() {
        super.onStart()
//        checkLocationPermission()
        Handler(Looper.getMainLooper()).postDelayed({
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    true,
                    sessionManager.userDistributor() ?: "-custom-012",
                    sessionManager.userID() ?: ""
                )
            }
        }, 1000)
    }

    override fun onStop() {
        super.onStop()

        if (sessionManager.isLoggedIn()) {
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    sessionManager.userDistributor() ?: "-custom-012",
                    sessionManager.userID() ?: ""
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (sessionManager.isLoggedIn()) {
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    sessionManager.userDistributor() ?: "-custom-012",
                    sessionManager.userID() ?: ""
                )
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) checkLocationPermission()
            else {
                AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Izin Diperlukan")
                    .setMessage("Izin lokasi diperlukan untuk fitur ini. Harap aktifkan di pengaturan aplikasi.")
                    .setPositiveButton(getString(R.string.open_settings)) { localDialog, _ ->
                        localDialog.dismiss()
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", packageName, null)
                        startActivityForResult(intent, LOCATION_PERMISSION_REQUEST_CODE)
                    }
                    .show()
            }
        }

    }
}