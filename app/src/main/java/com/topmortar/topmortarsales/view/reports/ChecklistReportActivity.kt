package com.topmortar.topmortarsales.view.reports

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.recyclerview.QnAFormReportRVA
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.MAX_REPORT_DISTANCE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityChecklistReportBinding
import com.topmortar.topmortarsales.model.QnAFormReportModel
import kotlinx.coroutines.launch

class ChecklistReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChecklistReportBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var rvAdapter: QnAFormReportRVA
    private lateinit var questions: ArrayList<QnAFormReportModel>

    private var iName: String? = null
    private var shortDistance = 0.5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityChecklistReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBar.icBack.setOnClickListener { finish() }
        binding.titleBar.tvTitleBar.text = "Form Laporan"
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Memperbarui jarak lokasi...")
        progressDialog.setCancelable(false)

        iName = intent.getStringExtra(CONST_NAME)
        if (iName != null) binding.textStoreName.text = iName

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
        progressDialog.show()
        Handler(Looper.getMainLooper()).postDelayed({
            progressDialog.dismiss()
            if (isSubmit) {
                shortDistance = 0.2
                submitForm()
            } else {
                shortDistance = 0.4
            }
            binding.tvDistance.text =
                "Jarak anda dengan toko " + shortDistance.toString() + "km."
            binding.tvDistanceBottom.text =
                "Jarak anda dengan toko " + shortDistance.toString() + "km."
        }, 1000)
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
        if (shortDistance <= MAX_REPORT_DISTANCE) {
            val questionSubmission: ArrayList<QuestionSubmission> = arrayListOf()
            val items = rvAdapter.submitForm()
            items.forEachIndexed { index, item ->
                println("${index + 1}. Question: ${item.text_question}")
                when (item.answer_type) {
                    "checkbox" -> {
                        println("Answer: ${item.selected_answer}")
                    } else -> {
                    println("Answer: ${item.text_answer}")
                }
                }
                questionSubmission.add(QuestionSubmission(id_visit_question = item.id_visit_question, text_answer = item.text_answer, selected_answer = item.selected_answer))
            }
            println(Gson().toJson(questionSubmission))
        } else {
            handleMessage(this, "SUBMIT FORM VISIT", "Cobalah untuk lebih dekat dengan toko")
        }
    }

}