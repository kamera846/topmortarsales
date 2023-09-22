package com.topmortar.topmortarsales.view.invoice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.InvoicePaymentRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.CONST_DATE_INVOICE
import com.topmortar.topmortarsales.commons.CONST_INVOICE_ID
import com.topmortar.topmortarsales.commons.CONST_INVOICE_NUMBER
import com.topmortar.topmortarsales.commons.CONST_STATUS_INVOICE
import com.topmortar.topmortarsales.commons.CONST_TOTAL_INVOICE
import com.topmortar.topmortarsales.commons.INVOICE_PAID
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.utils.CurrencyFormat
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.changeStatusBarColor
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.model.InvoicePaymentModel
import kotlinx.coroutines.launch

class DetailInvoiceActivity : AppCompatActivity() {

    // Global
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)
//        changeStatusBarColor(this, R.color.primary_200)

        setContentView(R.layout.activity_detail_invoice)

        apiService = HttpClient.create()

        initVariable()
        initClickHandler()
        dataActivityValidation()
    }

    private fun loadingState(state: Boolean, message: String = getString(R.string.txt_loading)) {

        tvLoading.text = message

        if (state) {

            tvLoading.visibility = View.VISIBLE
            rvPayments.visibility = View.GONE

        } else {

            tvLoading.visibility = View.GONE
            rvPayments.visibility = View.VISIBLE

        }

    }

    private fun getList() {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val response = apiService.getPayment(iInvoiceID)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        setRecyclerView(response.results)
                        loadingState(false)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "There is no payment history yet")

                    }
                    else -> {

                        handleMessage(this@DetailInvoiceActivity, TAG_RESPONSE_CONTACT, "Failed get data")
                        loadingState(true, getString(R.string.failed_request))

                    }
                }

            } catch (e: Exception) {

                handleMessage(this@DetailInvoiceActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))

            }

        }

    }

    private fun setRecyclerView(listItem: ArrayList<InvoicePaymentModel>) {

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

        iInvoiceID = intent.getStringExtra(CONST_INVOICE_ID)!!
        val iInvoiceNumber = intent.getStringExtra(CONST_INVOICE_NUMBER)
        val iStatusInvoice = intent.getStringExtra(CONST_STATUS_INVOICE)
        val iTotalInvoice = intent.getStringExtra(CONST_TOTAL_INVOICE)
        val iDateInvoice = intent.getStringExtra(CONST_DATE_INVOICE)

        if (!iDateInvoice.isNullOrEmpty()) tvDateInvoice.text = DateFormat.format(dateString = iDateInvoice, input = "yyyy-MM-dd hh:mm:ss", format = "dd MMMM yyyy, hh:mm")
        else tvDateInvoice.text = "Status"

        if (!iTotalInvoice.isNullOrEmpty()) tvTotalInvoice.text = CurrencyFormat.format(iTotalInvoice.toDouble())
        else tvTotalInvoice.text = CurrencyFormat.format(0.0)

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

        getList()

    }

    private fun initVariable() {

        icBack = findViewById(R.id.ic_back)
        tvTitleBar = findViewById(R.id.tv_title_bar)
        tvTotalInvoice = findViewById(R.id.tv_total_invoice)
        tvStatus = findViewById(R.id.tv_status)
        tvDateInvoice = findViewById(R.id.tv_date_invoce)
        tvLoading = findViewById(R.id.text_loading)
        rvPayments = findViewById(R.id.rv_payments)

        icBack.setImageDrawable(getDrawable(R.drawable.arrow_back_white))
        tvTitleBar.text = "Detail Invoice"
        tvTitleBar.setTextColor(getColor(R.color.white))

    }

    private fun initClickHandler() {

        icBack.setOnClickListener { finish() }

    }

    private lateinit var icBack: ImageView
    private lateinit var tvTitleBar: TextView
    private lateinit var tvTotalInvoice: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvDateInvoice: TextView
    private lateinit var tvLoading: TextView
    private lateinit var rvPayments: RecyclerView

    private var invoiceNumber = ""
    private var iInvoiceID = ""

    private lateinit var apiService: ApiService

}