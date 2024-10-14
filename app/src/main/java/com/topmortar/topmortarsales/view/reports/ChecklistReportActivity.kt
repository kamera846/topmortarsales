package com.topmortar.topmortarsales.view.reports

import android.Manifest
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.recyclerview.QnAFormReportRVA
import com.topmortar.topmortarsales.commons.CONST_IS_BASE_CAMP
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_MAPS_NAME
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.LOCATION_PERMISSION_REQUEST_CODE
import com.topmortar.topmortarsales.commons.MAX_REPORT_DISTANCE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.URLUtility
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityChecklistReportBinding
import com.topmortar.topmortarsales.model.QnAFormReportModel
import com.topmortar.topmortarsales.view.MapsActivity
import kotlinx.coroutines.launch

class ChecklistReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChecklistReportBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var progressDialog: ProgressDialog
    private lateinit var rvAdapter: QnAFormReportRVA
    private lateinit var questions: ArrayList<QnAFormReportModel>

    private var iName: String? = null
    private var iCoordinate: String? = null
    private var iShortDistance = 0.5

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityChecklistReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage(getString(R.string.txt_loading))
        progressDialog.setCancelable(false)

        binding.titleBar.icBack.setOnClickListener { finish() }
        binding.titleBar.tvTitleBar.text = "Form Laporan"

        iName = intent.getStringExtra(CONST_NAME)
        if (iName != null) binding.textStoreName.text = iName
        iCoordinate = intent.getStringExtra(CONST_MAPS)

        getQuestions()

        binding.textDistance.setOnClickListener{ getDistance() }
        binding.ivDistance.setOnClickListener { getDistance() }
        binding.textDistanceBottom.setOnClickListener{ getDistance() }
        binding.ivDistanceBottom.setOnClickListener { getDistance() }
        binding.submitReport.setOnClickListener {
            getDistance(isSubmit = true)
        }

    }

    private data class QuestionSubmission(
        val id_visit_question: String = "",
        val text_answer: String = "",
        val selected_answer: MutableList<String>? = null
    )

    private fun loadingState(state: Boolean, message: String = getString(R.string.txt_loading)) {

        binding.txtLoading.text = message

        if (state) {

            binding.cardLoading.visibility = View.VISIBLE
            binding.cardInformation.visibility = View.GONE
            binding.recyclerView.visibility = View.GONE
            binding.cardSubmit.visibility = View.GONE

        } else {

            binding.cardLoading.visibility = View.GONE
            binding.cardInformation.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.VISIBLE
            binding.cardSubmit.visibility = View.VISIBLE

        }

    }

    private fun getDistance(isSubmit: Boolean = false) {

        if (iCoordinate == null) {
            handleMessage(this, "SUBMIT FORM VISIT", "Tidak dapa menemukan lokasi toko")
            return
        }

        progressDialog.show()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        Handler(Looper.getMainLooper()).postDelayed({

            val mapsUrl = iCoordinate!!
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
                                    iShortDistance = shortDistance.toDouble()

                                    if (distance > MAX_REPORT_DISTANCE) {
                                        val builder = AlertDialog.Builder(this)
                                        builder.setCancelable(false)
                                        builder.setOnDismissListener { progressDialog.dismiss() }
                                        builder.setOnCancelListener { progressDialog.dismiss() }
                                        builder.setTitle("Peringatan!")
                                            .setMessage("Titik anda saat ini $shortDistance km dari titik toko. Cobalah untuk lebih dekat dengan toko!")
                                            .setPositiveButton("Oke") { dialog, _ ->
                                                progressDialog.dismiss()

                                                binding.tvDistance.setTextColor(getColor(R.color.primary))
                                                binding.tvDistance.text = "Jarak anda dengan toko $shortDistance km."
                                                binding.tvDistanceBottom.setTextColor(getColor(R.color.primary))
                                                binding.tvDistanceBottom.text = "Jarak anda dengan toko $shortDistance km."
                                                dialog.dismiss()
                                            }
                                            .setNegativeButton("Buka Maps") { dialog, _ ->
                                                val intent = Intent(
                                                    this,
                                                    MapsActivity::class.java
                                                )
                                                intent.putExtra(CONST_MAPS, mapsUrl)
                                                intent.putExtra(CONST_MAPS_NAME, iName)
                                                startActivity(intent)

                                                progressDialog.dismiss()

                                                binding.tvDistance.setTextColor(getColor(R.color.primary))
                                                binding.tvDistance.text = "Jarak anda dengan toko $shortDistance km."
                                                binding.tvDistanceBottom.setTextColor(getColor(R.color.primary))
                                                binding.tvDistanceBottom.text = "Jarak anda dengan toko $shortDistance km."

                                                dialog.dismiss()
                                            }
                                        builder.show()
                                    } else {
                                        progressDialog.dismiss()

                                        var textColor = getColor(R.color.black_200)
                                        if (CustomUtility(this).isDarkMode()) textColor =
                                            getColor(R.color.black_600)

                                        binding.tvDistance.setTextColor(textColor)
                                        binding.tvDistance.text = "Jarak anda dengan toko $shortDistance km."
                                        binding.tvDistanceBottom.setTextColor(textColor)
                                        binding.tvDistanceBottom.text = "Jarak anda dengan toko $shortDistance km."

                                        if (isSubmit) submitForm()
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
                        val message = "Anda tidak dapat membuat laporan untuk saat ini, silakan hubungi admin untuk memperbarui koordinat toko ini"
                        val actionTitle = "Hubungi Sekarang"
                        CustomUtility(this).showPermissionDeniedSnackbar(message, actionTitle) { navigateChatAdmin() }
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
        val message = "*#Courier Service*\nHalo admin, tolong bantu saya untuk memperbarui koordinat pada toko *${ iName }*"

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}")

        try {
            startActivity(intent)
            finishAffinity()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Gagal menghubungkan ke whatsapp", TOAST_SHORT).show()
        }

    }



    private fun getQuestions() {
        loadingState(true)

        val apiService = HttpClient.create()
        lifecycleScope.launch {
            try {
                val response = apiService.getVisitQuestion()
                when(response.status) {
                    RESPONSE_STATUS_OK -> {

                        questions = response.results
                        setupRecyclerView()
                        loadingState(false)
                        getDistance()

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Belum ada pertanyaan")

                    }
                    else -> {

                        loadingState(true, getString(R.string.failed_get_data))

                    }
                }
            } catch (e: Exception) {

                loadingState(true, "Failed run service. Exception " + e.message)

            }
        }
    }

    private fun setupRecyclerView() {
        rvAdapter = QnAFormReportRVA()
        rvAdapter.items = questions
        binding.recyclerView.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(this@ChecklistReportActivity)
        }
    }

    private fun submitForm() {
        if (iShortDistance <= MAX_REPORT_DISTANCE) {
            val questionSubmission: ArrayList<QuestionSubmission> = arrayListOf()
            val items = rvAdapter.submitForm()
            var isAnswerValid = false

            for (i in 0 until items.size) {
                val item = items[i]
                val isTextEmpty = (item.answer_type == "text" || item.answer_type == "date" || item.answer_type == "radio") && item.text_answer.isEmpty()
                val isSelectedEmpty = item.answer_type == "checkbox" && item.selected_answer.isNullOrEmpty()
                if (item.is_required == "1" && (isTextEmpty || isSelectedEmpty)) {
                    AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle("Perhatian!")
                        .setMessage("Pertanyaan no ${i + 1} harus dijawab")
                        .setPositiveButton("Oke") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                    isAnswerValid = false
                    break

                } else {
                    println("${i + 1}. Question: ${item.text_question}")
                    when (item.answer_type) {
                        "checkbox" -> {
                            println("Answer: ${item.selected_answer}")
                        } else -> {
                            println("Answer: ${item.text_answer}")
                        }
                    }
                    questionSubmission.add(QuestionSubmission(id_visit_question = item.id_visit_question, text_answer = item.text_answer, selected_answer = item.selected_answer))
                    isAnswerValid = true
                }
            }
            if (isAnswerValid) {
                println(Gson().toJson(questionSubmission))
                handleMessage(this, "SUBMIT FORM VISIT", "Berhasil mengirim laporan")
                finish()
            }
        } else {
            handleMessage(this, "SUBMIT FORM VISIT", "Cobalah untuk lebih dekat dengan toko")
        }
    }

}