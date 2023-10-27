package com.topmortar.topmortarsales.view.reports

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.topmortar.topmortarsales.adapter.recyclerview.ReportsRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityReportsBinding
import com.topmortar.topmortarsales.model.ReportVisitModel
import kotlinx.coroutines.launch

class ReportsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var binding: ActivityReportsBinding

    private val userID get() = sessionManager.userID().toString()
    private var contactID = ""
    private var contactName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar!!.hide()
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        contactID = intent.getStringExtra(CONST_CONTACT_ID).toString()
        contactName = intent.getStringExtra(CONST_NAME).toString()

        binding.titleBarDark.tvTitleBar.text = contactName
        binding.titleBarDark.tvTitleBarDescription.visibility = View.VISIBLE
        binding.titleBarDark.tvTitleBarDescription.text = "Daftar laporan saya pada toko ini"

        getList()
        initClickHandler()

    }

    private fun getList() {

        lifecycleScope.launch {
            try {
                val apiService: ApiService = HttpClient.create()
                val response = apiService.listReport(idUser = userID, idContact = contactID)

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            setRecyclerView(responseBody.results)

                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            handleMessage(this@ReportsActivity, TAG_RESPONSE_MESSAGE, "Gagal memuat laporan! Message: ${ responseBody.message }")

                        }
                        else -> {

                            handleMessage(this@ReportsActivity, TAG_RESPONSE_MESSAGE, "Gagal memuat laporan!: ${ responseBody.message }")

                        }
                    }

                } else {

                    handleMessage(this@ReportsActivity, TAG_RESPONSE_MESSAGE, "Gagal memuat laporan! Error: " + response.message())

                }


            } catch (e: Exception) {

                handleMessage(this@ReportsActivity, TAG_RESPONSE_MESSAGE, "Failed run service. Exception " + e.message)

            }

        }

    }

    private fun setRecyclerView(items: ArrayList<ReportVisitModel>) {
//        val items = arrayListOf<ReportModel>()
//
//        val title = getString(R.string.lorem_title)
//        val desc = getString(R.string.lorem_desc)
//        val date = getString(R.string.dummy_date)
//
//        for (i in 0..24) {
//            if (i == 0 || i == 5 || i == 9 || i == 24) items.add(ReportModel(title = title, description = "Kunjungan penagihan periode ke 3", date = date))
//            else items.add(ReportModel(title = title, description = desc, date = date))
//        }

        val mAdapter = ReportsRecyclerViewAdapter()
        mAdapter.setList(items)

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@ReportsActivity, 2)
            adapter = mAdapter
        }
    }

    private fun initClickHandler() {

        binding.titleBarDark.icBack.setOnClickListener { finish() }

    }
}