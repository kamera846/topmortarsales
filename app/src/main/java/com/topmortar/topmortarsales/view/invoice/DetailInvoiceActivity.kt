package com.topmortar.topmortarsales.view.invoice

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.convertDpToPx

class DetailInvoiceActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    private lateinit var icBack: ImageView

    private lateinit var tvTitleBar: TextView

    private var contactId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)

        setContentView(R.layout.activity_detail_invoice)

        initVariable()
        initClickHandler()
        dataActivityValidation()

    }

    private fun initVariable() {

        tvTitleBar = findViewById(R.id.tv_title_bar)

        icBack = findViewById(R.id.ic_back)

        // Setup Title Bar
        tvTitleBar.text = "Detail Contact"
        tvTitleBar.setPadding(0, 0, convertDpToPx(16, this), 0)

    }

    private fun initClickHandler() {
        icBack.setOnClickListener { backHandler() }
    }

    private fun dataActivityValidation() {

        val iContactId = intent.getStringExtra(CONST_CONTACT_ID)

        if (!iContactId.isNullOrEmpty() ) {
            contactId = iContactId
        }

    }

    private fun backHandler() {
        finish()
    }

    override fun onBackPressed() {
        backHandler()
    }
}