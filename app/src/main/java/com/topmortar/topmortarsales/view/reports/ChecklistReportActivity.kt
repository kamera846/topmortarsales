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
    private val questions = "[{\"id\":\"1\",\"question\":\"Nama Toko\",\"is_required\":\"1\",\"answer_type\":\"text\",\"answer_option\":null},{\"id\":\"2\",\"question\":\"Nama customer yang ditemui (owner/karyawan)\",\"is_required\":\"1\",\"answer_type\":\"text\",\"answer_option\":null},{\"id\":\"3\",\"question\":\"Merchandise yang diberikan\",\"is_required\":\"1\",\"answer_type\":\"checkbox\",\"answer_option\":[\"Vouhcer\",\"Kantong Plastik\",\"Buletin (Flyer Cetak)\",\"Brosur Katalog\",\"Yang lainnya\"]},{\"id\":\"4\",\"question\":\"Stock mortar toko dan promonya\",\"is_required\":\"0\",\"answer_type\":\"text\",\"answer_option\":null},{\"id\":\"5\",\"question\":\"Tawarkan produk thinbed/perekat dan tunjukan kemasan\",\"is_required\":\"1\",\"answer_type\":\"checkbox\",\"answer_option\":[\"Produk\",\"Kemasan\"]},{\"id\":\"6\",\"question\":\"Validasi tanggal ultah toko\",\"is_required\":\"0\",\"answer_type\":\"date\",\"answer_option\":null},{\"id\":\"7\",\"question\":\"Validasi instagram toko, Follow topmortar untuk masuk ke giveaway\",\"is_required\":\"0\",\"answer_type\":\"checkbox\",\"answer_option\":[\"Sudah follow\"]},{\"id\":\"8\",\"question\":\"Tanya toko\",\"is_required\":\"1\",\"answer_type\":\"checkbox\",\"answer_option\":[\"Kapan mau order pak/bu?\",\"Ada kendala apa pak/bu?\",\"Kapan bisa saya temui kembali pak/bu?\"]}]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        binding = ActivityChecklistReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBar.icBack.setOnClickListener { finish() }
        binding.titleBar.tvTitleBar.text = "Form Laporan"

        setupRecyclerView()

    }

    private fun setupRecyclerView() {
        val gson = Gson()
        val listType = object : TypeToken<List<QnAFormReportModel>>() {}.type
        val questionsList: ArrayList<QnAFormReportModel> = gson.fromJson(questions, listType)

        val rvAdapter = QnAFormReportRVA()
        rvAdapter.items = questionsList
        binding.recyclerView.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(this@ChecklistReportActivity)
        }
    }

}