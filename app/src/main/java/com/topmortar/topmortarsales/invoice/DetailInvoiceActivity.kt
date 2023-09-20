package com.topmortar.topmortarsales.invoice

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.CONST_INVOICE_ID
import com.topmortar.topmortarsales.commons.CONST_INVOICE_NUMBER
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.changeStatusBarColor

class DetailInvoiceActivity : AppCompatActivity() {

    // Global
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)
        changeStatusBarColor(this, R.color.primary_200)

        setContentView(R.layout.activity_detail_invoice)

        initVariable()
        initClickHandler()
        dataActivityValidation()
    }

    private fun dataActivityValidation() {

        val iInvoiceNumber = intent.getStringExtra(CONST_INVOICE_NUMBER)

        if (!iInvoiceNumber.isNullOrEmpty()) {
            invoiceNumber = iInvoiceNumber.toString()
            tvTitleBar.text = "${tvTitleBar.text} - $invoiceNumber"
        }

    }

    private fun initVariable() {

        icBack = findViewById(R.id.ic_back)
        tvTitleBar = findViewById(R.id.tv_title_bar)

        icBack.setImageDrawable(getDrawable(R.drawable.arrow_back_white))
        tvTitleBar.text = "Detail Invoice"
        tvTitleBar.setTextColor(getColor(R.color.white))

    }

    private fun initClickHandler() {

        icBack.setOnClickListener { finish() }

    }

    private lateinit var icBack: ImageView
    private lateinit var tvTitleBar: TextView

    private var invoiceNumber = ""

}