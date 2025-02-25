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
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.recyclerview.QnAFormReportRVA
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_FULL_NAME
import com.topmortar.topmortarsales.commons.CONST_INVOICE_ID
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_MAPS_NAME
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.LOCATION_PERMISSION_REQUEST_CODE
import com.topmortar.topmortarsales.commons.MAX_REPORT_DISTANCE
import com.topmortar.topmortarsales.commons.NORMAL_REPORT
import com.topmortar.topmortarsales.commons.RENVI_SOURCE
import com.topmortar.topmortarsales.commons.REPORT_SOURCE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.URLUtility
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
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

    private val idUser get() = sessionManager.userID().toString()
    private val userDistributorId get() = sessionManager.userDistributor().toString()
    private var iContactId: String? = null
    private var iInvoiceId: String? = null
    private var iName: String? = null
    private var iUserFullName: String? = null
    private var iCoordinate: String? = null
    private var iVisitId: String? = null
    private var iDistance: Double? = null
    private var shortDistance: String? = null
    private var isAnswerChecklist: Boolean? = null
    private lateinit var iReportSource: String
    private lateinit var iRenviSource: String

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        applyMyEdgeToEdge(isPrimary = false)

        binding = ActivityChecklistReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage(getString(R.string.txt_loading))
        progressDialog.setCancelable(false)

        binding.titleBar.icBack.visibility = View.VISIBLE
        binding.titleBar.tvTitleBar.text = "Form Visit Checklist"

        isAnswerChecklist = intent.getBooleanExtra("is_answer_checklist", false)
        iName = intent.getStringExtra(CONST_NAME)
        if (iName != null) binding.textStoreName.text = iName
        iUserFullName = intent.getStringExtra(CONST_FULL_NAME)
        iCoordinate = intent.getStringExtra(CONST_MAPS)
        iContactId = intent.getStringExtra(CONST_CONTACT_ID)
        iInvoiceId = intent.getStringExtra(CONST_INVOICE_ID)
        shortDistance = intent.getStringExtra("shortDistance")
        iVisitId = intent.getStringExtra("visitId")
        iReportSource = intent.getStringExtra(REPORT_SOURCE).let { if (it.isNullOrEmpty()) NORMAL_REPORT else it }
        iRenviSource = intent.getStringExtra(RENVI_SOURCE).let { if (it.isNullOrEmpty()) NORMAL_REPORT else it }

        loadContent()

        binding.titleBar.icBack.setOnClickListener { finish() }
        if (isAnswerChecklist != null && isAnswerChecklist == true) {
            binding.textInfoTitle.text = "Laporan $iUserFullName di toko"
            binding.ivDistance.visibility = View.GONE
        } else {
            binding.textDistance.setOnClickListener{ getDistance() }
            binding.ivDistance.setOnClickListener { getDistance() }
            binding.textDistanceBottom.setOnClickListener{ getDistance() }
            binding.ivDistanceBottom.setOnClickListener { getDistance() }
            binding.submitReport.setOnClickListener {
                getDistance(isSubmit = true)
            }
        }
        binding.swipeRefreshLayout.setOnRefreshListener { loadContent() }

    }

    private data class QuestionSubmission(
        val id_visit_question: String = "",
        val text_answer: String = "",
        val selected_answer: MutableList<String>? = null
    )

    private fun loadContent() {
        if (isAnswerChecklist != null && isAnswerChecklist == true) {
            binding.cardSubmit.visibility = View.GONE
            binding.tvDistance.text = "Jarak laporan pengguna di toko $shortDistance km."
            getAnswers()
        } else getQuestions()
    }

    private fun loadingState(state: Boolean, message: String = getString(R.string.txt_loading)) {

        binding.txtLoading.text = message

        if (state) {

            binding.txtLoading.visibility = View.VISIBLE
            binding.cardInformation.visibility = View.GONE
            binding.recyclerView.visibility = View.GONE
            binding.cardSubmit.visibility = View.GONE

            binding.swipeRefreshLayout.isRefreshing = message === getString(R.string.txt_loading)

        } else {

            binding.txtLoading.visibility = View.GONE
            binding.cardInformation.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.VISIBLE
            if (isAnswerChecklist == null || isAnswerChecklist == false) binding.cardSubmit.visibility = View.VISIBLE
            else binding.cardSubmit.visibility = View.GONE

            binding.swipeRefreshLayout.isRefreshing = false

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
                                    shortDistance = "%.3f".format(distance)
                                    iDistance = distance

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

                                        var textColor = getColor(R.color.black_200)
                                        if (CustomUtility(this).isDarkMode()) textColor =
                                            getColor(R.color.black_600)

                                        binding.tvDistance.setTextColor(textColor)
                                        binding.tvDistance.text = "Jarak anda dengan toko $shortDistance km."
                                        binding.tvDistanceBottom.setTextColor(textColor)
                                        binding.tvDistanceBottom.text = "Jarak anda dengan toko $shortDistance km."

                                        if (isSubmit) {
                                            if (iDistance != null && iDistance!! <= MAX_REPORT_DISTANCE) {
                                                submitForm()
                                            } else {
                                                progressDialog.dismiss()
                                                handleMessage(this, "SUBMIT FORM VISIT", "Cobalah untuk lebih dekat dengan toko")
                                            }
                                        }
                                        else progressDialog.dismiss()
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

    private fun getAnswers() {
        loadingState(true)

        val apiService = HttpClient.create()
        lifecycleScope.launch {
            try {
                val response = apiService.getVisitAnswers(idVisit = iVisitId ?: "")
                when(response.status) {
                    RESPONSE_STATUS_OK -> {

                        questions = response.results
                        setupRecyclerView()
                        loadingState(false)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Jawaban tidak ditemukan")

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

    private fun getQuestions() {
        loadingState(true)

        val apiService = HttpClient.create()
        lifecycleScope.launch {
            try {
                val response = apiService.getVisitQuestion(idDistributor = userDistributorId)
                when(response.status) {
                    RESPONSE_STATUS_OK -> {

                        questions = response.results
                        questions.add(QnAFormReportModel(
                            id_visit_question = "-1",
                            text_question = "Pesan Laporan",
                            is_required = "1",
                            answer_type = "text",
                            created_at = "",
                            updated_at = "",
                            answer_option = null,
                            selected_answer = null,
                            text_answer = "",
                            placeholder = getString(R.string.laporan_toko_hint)
                        ))
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
        rvAdapter.isAnswerChecklist = isAnswerChecklist
        binding.recyclerView.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(this@ChecklistReportActivity)
        }
    }

    private fun submitReport(items: ArrayList<QnAFormReportModel>, arrayAnswer: String) {
        lifecycleScope.launch {
            try {
                val rbidContact = createPartFromString(iContactId ?: "")
                val rbidUser = createPartFromString(idUser)
                val rbdistanceVisit = createPartFromString(shortDistance ?: "")
                val rblaporanVisit = createPartFromString(items.last().text_answer)
                val rbSource = createPartFromString(iReportSource)
                val rbRenviSource = createPartFromString(iRenviSource)
                val rbInvoiceId = createPartFromString(iInvoiceId ?: "")

                val apiService: ApiService = HttpClient.create()
                val response = apiService.makeVisitReport(
                    idContact = rbidContact,
                    idUser = rbidUser,
                    distanceVisit = rbdistanceVisit,
                    laporanVisit = rblaporanVisit,
                    source = rbSource,
                    renviSource = rbRenviSource,
                    idInvoice = rbInvoiceId,
                    isPay = createPartFromString("0")
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()!!
                    when(responseBody.status) {
                        RESPONSE_STATUS_OK -> {
                            iVisitId = responseBody.id_visit
                            postSubmitAnswer(arrayAnswer)
                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {
                            progressDialog.dismiss()
                            handleMessage(this@ChecklistReportActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim laporan! Message: ${ responseBody.message }")
                        }
                        else -> {
                            progressDialog.dismiss()
                            handleMessage(this@ChecklistReportActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim laporan!: ${ responseBody.message }")
                        }
                    }
                } else {
                    progressDialog.dismiss()
                    handleMessage(this@ChecklistReportActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim laporan! Error: " + response.message())
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                handleMessage(this@ChecklistReportActivity, TAG_RESPONSE_MESSAGE, "Failed run service. Exception " + e.message)
            }
        }
    }

    private fun submitForm() {
            val questionSubmission: ArrayList<QuestionSubmission> = arrayListOf()
            val items = rvAdapter.submitForm()
            var isAnswerValid = false

            for (i in 0 until items.size) {
                val item = items[i]
                val isTextEmpty = (item.answer_type == "text" || item.answer_type == "date" || item.answer_type == "radio") && item.text_answer.isEmpty()
                val isSelectedEmpty = item.answer_type == "checkbox" && item.selected_answer.isNullOrEmpty()
                if (item.is_required == "1" && (isTextEmpty || isSelectedEmpty)) {
                    progressDialog.dismiss()
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
                    if (item.id_visit_question != "-1") questionSubmission.add(QuestionSubmission(id_visit_question = item.id_visit_question, text_answer = item.text_answer, selected_answer = item.selected_answer))
                    isAnswerValid = true
                }
            }
            if (isAnswerValid) {
                val arrayAnswer = convertToJsonString(questionSubmission)
                submitReport(items, arrayAnswer)
            }
    }

    private fun convertToJsonString(questions: ArrayList<QuestionSubmission>): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("[")

        for ((index, question) in questions.withIndex()) {
            stringBuilder.append("{")
            stringBuilder.append("\"id_visit_question\":\"${question.id_visit_question}\",")
            stringBuilder.append("\"text_answer\":\"${question.text_answer}\"")

            question.selected_answer?.let {
                stringBuilder.append(",\"selected_answer\":[\"${it.joinToString("\",\"")}\"]")
            }

            stringBuilder.append("}")

            if (index < questions.size - 1) {
                stringBuilder.append(",")
            }
        }

        stringBuilder.append("]")
        return "\"" + stringBuilder.toString().replace("\"", "\\\"") + "\""
    }

    private fun postSubmitAnswer(arrayAnswer: String) {

        val apiService = HttpClient.create()
        lifecycleScope.launch {
            try {
                val response = apiService.postVisitQuestion(
                    idVisit = createPartFromString(iVisitId ?: ""),
                    arrayAnswer = createPartFromString(arrayAnswer)
                )
                if (response.isSuccessful) {
                    val responseBody = response.body()!!
                    when(responseBody.status) {
                        RESPONSE_STATUS_OK -> {
                            progressDialog.dismiss()
                            handleMessage(this@ChecklistReportActivity, TAG_RESPONSE_MESSAGE, responseBody.message)
                            finish()
                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {
                            progressDialog.dismiss()
                            handleMessage(this@ChecklistReportActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim laporan checklist! Message: ${ responseBody.message }")
                        }
                        else -> {
                            progressDialog.dismiss()
                            handleMessage(this@ChecklistReportActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim laporan checklist!: ${ responseBody.message }")
                        }
                    }
                } else {
                    progressDialog.dismiss()
                    handleMessage(this@ChecklistReportActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim laporan checklist! Error: " + response.message())
                }
            } catch (e: Exception) {

                progressDialog.dismiss()
                handleMessage(this@ChecklistReportActivity, TAG_RESPONSE_MESSAGE, "Failed run service. Exception " + e.message)

            }
        }
    }

    override fun onStart() {
        super.onStart()
        Handler(Looper.getMainLooper()).postDelayed({
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    true,
                    userDistributorId ?: "-custom-011",
                    idUser ?: ""
                )
            }
        }, 1000)
    }

    override fun onStop() {
        if (sessionManager.isLoggedIn()) {
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    userDistributorId ?: "-custom-011",
                    idUser ?: ""
                )
            }
        }
        super.onStop()
    }

    override fun onDestroy() {
        if (sessionManager.isLoggedIn()) {
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    userDistributorId ?: "-custom-011",
                    idUser ?: ""
                )
            }
        }
        super.onDestroy()
    }

}