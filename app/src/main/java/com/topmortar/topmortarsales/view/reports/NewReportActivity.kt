package com.topmortar.topmortarsales.view.reports

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
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
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_IS_BASE_CAMP
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_MAPS_NAME
import com.topmortar.topmortarsales.commons.CONST_MAPS_STATUS
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.LOCATION_PERMISSION_REQUEST_CODE
import com.topmortar.topmortarsales.commons.MAX_REPORT_DISTANCE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.utils.CustomEtHandler
import com.topmortar.topmortarsales.commons.utils.CustomEtHandler.setMaxLength
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.URLUtility
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityNewReportBinding
import com.topmortar.topmortarsales.view.MapsActivity
import kotlinx.coroutines.launch

class NewReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewReportBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var customUtility: CustomUtility
    private lateinit var progressDialog: ProgressDialog

    private val msgMaxLines = 6
    private val msgMaxLength = 500

    private var isDistanceToLong = false
    private var isBaseCamp = false
    private var reportType: String = "toko"
    private var id: String = ""
    private val idUser get() = sessionManager.userID().toString()
    private var name: String = ""
    private var coordinate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        binding = ActivityNewReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        customUtility = CustomUtility(this)

        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Sedang menghitung jarak...")

        initContent()
        initClickHandler()
    }

    private fun initContent() {
        binding.titleBarLight.tvTitleBar.text = "Buat Laporan"

        loadingContent(true)

        etMessageListener()

        id = intent.getStringExtra(CONST_CONTACT_ID).toString()
        name = intent.getStringExtra(CONST_NAME).toString()
        coordinate = intent.getStringExtra(CONST_MAPS).toString()
        isBaseCamp = intent.getBooleanExtra(CONST_IS_BASE_CAMP, false)

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

        Handler().postDelayed({

            binding.etName.text = name
            calculateDistance()

            loadingContent(false)
        }, 500)
    }

    private fun initClickHandler() {
        binding.titleBarLight.icBack.setOnClickListener { finish() }
        binding.btnReport.setOnClickListener { submitValidation() }
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
    fun calculateDistance(view: View? = null) {
        progressDialog.show()

        Handler().postDelayed({

            val mapsUrl = coordinate
            val urlUtility = URLUtility(this)

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (urlUtility.isLocationEnabled(this)) {

                    urlUtility.requestLocationUpdate()

                    if (!urlUtility.isUrl(mapsUrl) && !mapsUrl.isNullOrEmpty()) {
                        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
                        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

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
                                val urlUtility = URLUtility(this)
                                val distance = urlUtility.calculateDistance(currentLatitude, currentLongitude, latitude, longitude)
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
                                            binding.tvDistanceError.text = "Jarak anda lebih dari $MAX_REPORT_DISTANCE km. Cobalah untuk lebih dekat dengan titik $reportType dan refresh jaraknya!"
                                            isDistanceToLong = true

                                            dialog.dismiss()
                                        }
                                        .setNegativeButton("Buka Maps") { dialog, _ ->
                                            val intent = Intent(this@NewReportActivity, MapsActivity::class.java)
                                            intent.putExtra(CONST_MAPS, mapsUrl)
                                            intent.putExtra(CONST_MAPS_NAME, name)
                                            startActivity(intent)

                                            progressDialog.dismiss()

                                            binding.etDistance.setTextColor(getColor(R.color.primary))
                                            binding.etDistance.text = shortDistance
                                            binding.icRefreshDistance.visibility = View.VISIBLE
                                            binding.tvDistanceError.visibility = View.VISIBLE
                                            binding.tvDistanceError.text = "Cobalah untuk lebih dekat dengan titik $reportType dan refresh jaraknya!"
                                            isDistanceToLong = true

                                            dialog.dismiss()
                                        }
                                    builder.show()
                                } else {
                                    progressDialog.dismiss()

                                    var textColor = getColor(R.color.black_200)
                                    if (customUtility.isDarkMode()) textColor = getColor(R.color.black_600)

                                    binding.etDistance.setTextColor(textColor)
                                    binding.etDistance.text = shortDistance
                                    binding.icRefreshDistance.visibility = View.VISIBLE
                                    binding.tvDistanceError.visibility = View.GONE
                                    isDistanceToLong = false
                                }

                            } else {
                                progressDialog.dismiss()
                                Toast.makeText(this, "Gagal memproses koordinat", TOAST_SHORT).show()
                            }

                        } else {
                            progressDialog.dismiss()
                            Toast.makeText(this, "Tidak dapat mengakses lokasi, kembali dan coba lagi", TOAST_SHORT).show()
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
        val phoneNumber = getString(R.string.topmortar_wa_number)
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
        if (coordinate.isNullOrEmpty()) {
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
        etMessage.error = null
        etMessage.clearFocus()
        return true
    }

    private fun submitReport() {
        loadingSubmit(true)

        lifecycleScope.launch {
            try {

                val rbidContact = createPartFromString(id)
                val rbidUser = createPartFromString(idUser)
                val rbdistanceVisit = createPartFromString(binding.etDistance.text.toString())
                val rblaporanVisit = createPartFromString(binding.etMessage.text.toString())

                val apiService: ApiService = HttpClient.create()
                val response = when (isBaseCamp) {
                    true -> apiService.makeVisitCourierReport(
                        idGudang = rbidContact,
                        idUser = rbidUser,
                        distanceVisit = rbdistanceVisit,
                        laporanVisit = rblaporanVisit,
                    ) else -> apiService.makeVisitReport(
                        idContact = rbidContact,
                        idUser = rbidUser,
                        distanceVisit = rbdistanceVisit,
                        laporanVisit = rblaporanVisit,
                    )
                }

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            loadingSubmit(false)
                            Toast.makeText(this@NewReportActivity, responseBody.message, TOAST_SHORT).show()
                            finish()

                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            handleMessage(this@NewReportActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim laporan! Message: ${ responseBody.message }")
                            loadingSubmit(false)

                        }
                        else -> {

                            handleMessage(this@NewReportActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim laporan!: ${ responseBody.message }")
                            loadingSubmit(false)

                        }
                    }

                } else {

                    handleMessage(this@NewReportActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim laporan! Error: " + response.message())
                    loadingSubmit(false)

                }


            } catch (e: Exception) {

                handleMessage(this@NewReportActivity, TAG_RESPONSE_MESSAGE, "Failed run service. Exception " + e.message)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                getDistance()
                calculateDistance()
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    val message = "Izin lokasi diperlukan untuk fitur ini. Tolong berikan izinnya."
                    customUtility.showPermissionDeniedSnackbar(message) { ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE) }
                } else customUtility.showPermissionDeniedDialog("Izin lokasi diperlukan untuk fitur ini. Harap aktifkan di pengaturan aplikasi.")
            }
        }

    }
}