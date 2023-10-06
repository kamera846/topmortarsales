package com.topmortar.topmortarsales.view.reports

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.recyclerview.ReportsRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.databinding.ActivityReportsBinding
import com.topmortar.topmortarsales.model.ReportModel

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding

    private var contactID = ""
    private var contactName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar!!.hide()
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contactID = intent.getStringExtra(CONST_CONTACT_ID).toString()
        contactName = intent.getStringExtra(CONST_NAME).toString()

        binding.titleBarLight.tvTitleBar.text = "Reports"
        binding.titleBarLight.tvTitleBarDescription.visibility = View.VISIBLE
        binding.titleBarLight.tvTitleBarDescription.text = "List report pada toko $contactName"

        setRecyclerView()

    }

    private fun setRecyclerView() {
        val items = arrayListOf<ReportModel>()

        val title = getString(R.string.lorem_title)
        val desc = getString(R.string.lorem_desc)
        val date = getString(R.string.dummy_date)

        for (i in 0..24) {
            if (i == 0 || i == 5 || i == 9 || i == 24) items.add(ReportModel(title = title, description = "Kunjungan penagihan periode ke 3", date = date))
            else items.add(ReportModel(title = title, description = desc, date = date))
        }

        val mAdapter = ReportsRecyclerViewAdapter()
        mAdapter.setList(items)

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@ReportsActivity, 2)
            adapter = mAdapter
        }
    }
}