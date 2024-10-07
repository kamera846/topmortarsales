package com.topmortar.topmortarsales.view.reports

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.topmortar.topmortarsales.adapter.recyclerview.QnAFormReportRVA
import com.topmortar.topmortarsales.databinding.ActivityChecklistReportBinding
import com.topmortar.topmortarsales.model.QnAFormReportModel

class ChecklistReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChecklistReportBinding
    private val questions = "[{\"id\":\"1\",\"question\":\"Merchandise yang diberikan\",\"is_required\":\"0\",\"answer_type\":\"checkbox\",\"answer_option\":[\"Voucher\",\"Kantong Plastik\",\"Buletin (Flyer Cetak)\",\"Brosur Katalog\",\"Lainnya\"],\"selected_answer\":null,\"keterangan\":\"\"},{\"id\":\"2\",\"question\":\"Tawarkan produk thinbed/perekat dan tunjukan kemasan\",\"is_required\":\"0\",\"answer_type\":\"checkbox\",\"answer_option\":[\"Produk\",\"Kemasan\"],\"selected_answer\":null,\"keterangan\":\"\"},{\"id\":\"3\",\"question\":\"Tanya toko\",\"is_required\":\"0\",\"answer_type\":\"checkbox\",\"answer_option\":[\"Kapan mau order pak/bu\",\"Ada kendala apa pak/bu\",\"Bisa saya temui kembali kapan pak/bu\"],\"selected_answer\":null,\"keterangan\":\"\"},{\"id\":\"4\",\"question\":\"Merchandiseyangdiberikan\",\"is_required\":\"0\",\"answer_type\":\"checkbox\",\"answer_option\":[\"Voucher\",\"KantongPlastik\",\"Buletin(FlyerCetak)\",\"BrosurKatalog\",\"Lainnya\"],\"selected_answer\":null,\"keterangan\":\"\"},{\"id\":\"5\",\"question\":\"Tawarkanprodukthinbed/perekatdantujukankemasan\",\"is_required\":\"0\",\"answer_type\":\"checkbox\",\"answer_option\":[\"Produk\",\"Kemasan\"],\"selected_answer\":null,\"keterangan\":\"\"},{\"id\":\"6\",\"question\":\"Tanyatoko\",\"is_required\":\"0\",\"answer_type\":\"checkbox\",\"answer_option\":[\"Kapanmauorderpak/bu\",\"Adakendalaapapak/bu\",\"Bisasayatemuikembalikapanpak/bu\"],\"selected_answer\":null,\"keterangan\":\"\"}]"
    private lateinit var rvAdapter: QnAFormReportRVA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        binding = ActivityChecklistReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBar.icBack.setOnClickListener { finish() }
        binding.titleBar.tvTitleBar.text = "Form Laporan"

        setupRecyclerView()

        binding.submitReport.setOnClickListener {
            val QuestionSubmission: ArrayList<QuestionSubmission> = arrayListOf()
            val items = rvAdapter.submitForm()
            items.forEach { item ->
                println("Question: ${item.question}")
                println("Selected Options (true/false): ${item.selected_answer}")
                println("User Answer: ${item.keterangan}")
                QuestionSubmission.add(QuestionSubmission(id = item.id, keterangan = item.keterangan, selected_answer = item.selected_answer))
            }
            println(Gson().toJson(QuestionSubmission))
        }

    }

    private data class QuestionSubmission(
        val id: String = "",
        val keterangan: String = "",
        val selected_answer: MutableList<Boolean>? = null
    )

    private fun setupRecyclerView() {
        val gson = Gson()
        val listType = object : TypeToken<List<QnAFormReportModel>>() {}.type
        val questionsList: ArrayList<QnAFormReportModel> = gson.fromJson(questions, listType)

        rvAdapter = QnAFormReportRVA()
        rvAdapter.items = questionsList
        binding.recyclerView.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(this@ChecklistReportActivity)
        }
    }

}