package com.topmortar.topmortarsales.invoice

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.InvoicePaymentRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.CONST_INVOICE_ID
import com.topmortar.topmortarsales.commons.CONST_INVOICE_NUMBER
import com.topmortar.topmortarsales.commons.CONST_STATUS_INVOICE
import com.topmortar.topmortarsales.commons.INVOICE_PAID
import com.topmortar.topmortarsales.commons.utils.CurrencyFormat
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.changeStatusBarColor
import com.topmortar.topmortarsales.model.InvoicePaymentModel

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

    private fun setRecyclerView() {
        val listItem: ArrayList<InvoicePaymentModel> = arrayListOf()
        listItem.add(InvoicePaymentModel(date = "2023-09-20 20:36:00", price = "390000", debt = "0"))
        listItem.add(InvoicePaymentModel(date = "2023-09-19 09:17:00", price = "1000000", debt = "390000"))
        listItem.add(InvoicePaymentModel(date = "2023-09-18 14:20:00", price = "300000", debt = "1390000"))
        listItem.add(InvoicePaymentModel(date = "2023-09-15 11:05:00", price = "700000", debt = "1690000"))
        listItem.add(InvoicePaymentModel(date = "2023-09-10 16:40:00", price = "500000", debt = "2390000"))

        val rvAdapter = InvoicePaymentRecyclerViewAdapter()
        rvAdapter.setListItem(listItem)

        rvPayments.apply {
            layoutManager = LinearLayoutManager(this@DetailInvoiceActivity)
            adapter = rvAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                private var lastScrollPosition = 0

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy < 0) {
                        // Scrolled up
                        val firstVisibleItemPosition =
                            (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        if (lastScrollPosition != firstVisibleItemPosition) {
                            recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition)?.itemView?.startAnimation(
                                AnimationUtils.loadAnimation(
                                    recyclerView.context,
                                    R.anim.rv_item_fade_slide_down
                                )
                            )
                            lastScrollPosition = firstVisibleItemPosition
                        }
                    } else lastScrollPosition = -1
                }
            })
        }

    }

    private fun dataActivityValidation() {

        val iInvoiceNumber = intent.getStringExtra(CONST_INVOICE_NUMBER)
        val iStatusInvoice = intent.getStringExtra(CONST_STATUS_INVOICE)

        if (!iInvoiceNumber.isNullOrEmpty()) {
            invoiceNumber = iInvoiceNumber.toString()
            tvTitleBar.text = "${tvTitleBar.text} - $invoiceNumber"
        }
        if (!iStatusInvoice.isNullOrEmpty()) {
            if (iStatusInvoice == INVOICE_PAID) {
                tvStatus.text = "paid"
                tvStatus.setTextColor(getColor(R.color.white))
                tvStatus.setBackgroundDrawable(getDrawable(R.drawable.bg_active_round))
            } else {
                tvStatus.text = "unpaid"
                tvStatus.setTextColor(getColor(R.color.black_200))
                tvStatus.setBackgroundDrawable(getDrawable(R.drawable.bg_data_round))
            }
        }

        setRecyclerView()

    }

    private fun initVariable() {

        icBack = findViewById(R.id.ic_back)
        tvTitleBar = findViewById(R.id.tv_title_bar)
        tvTotalInvoice = findViewById(R.id.tv_total_invoice)
        tvStatus = findViewById(R.id.tv_status)
        rvPayments = findViewById(R.id.rv_payments)

        icBack.setImageDrawable(getDrawable(R.drawable.arrow_back_white))
        tvTitleBar.text = "Detail Invoice"
        tvTitleBar.setTextColor(getColor(R.color.white))

        tvTotalInvoice.text = CurrencyFormat.format(2890000.0)

    }

    private fun initClickHandler() {

        icBack.setOnClickListener { finish() }

    }

    private lateinit var icBack: ImageView
    private lateinit var tvTitleBar: TextView
    private lateinit var tvTotalInvoice: TextView
    private lateinit var tvStatus: TextView
    private lateinit var rvPayments: RecyclerView

    private var invoiceNumber = ""

}