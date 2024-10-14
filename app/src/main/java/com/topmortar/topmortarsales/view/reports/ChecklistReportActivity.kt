package com.topmortar.topmortarsales.view.reports

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.recyclerview.QnAFormReportRVA
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityChecklistReportBinding
import com.topmortar.topmortarsales.model.QnAFormReportModel
import kotlinx.coroutines.launch

class ChecklistReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChecklistReportBinding
    private lateinit var rvAdapter: QnAFormReportRVA
    private lateinit var questions: ArrayList<QnAFormReportModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityChecklistReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBar.icBack.setOnClickListener { finish() }
        binding.titleBar.tvTitleBar.text = "Form Laporan"

        getQuestions()

        binding.submitReport.setOnClickListener {
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
        }

    }

    private data class QuestionSubmission(
        val id_visit_question: String = "",
        val text_answer: String = "",
        val selected_answer: MutableList<String>? = null
    )

    private fun getQuestions() {
        val apiService = HttpClient.create()
        lifecycleScope.launch {
            try {
                val response = apiService.getVisitQuestion()
                when(response.status) {
                    RESPONSE_STATUS_OK -> {
                        questions = response.results
                        setupRecyclerView()
                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(this@ChecklistReportActivity, "GET QUESTION", "Belum ada pertanyaan")

                    }
                    else -> {

                        handleMessage(this@ChecklistReportActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))

                    }
                }
            } catch (e: Exception) {

                handleMessage(this@ChecklistReportActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)

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

}