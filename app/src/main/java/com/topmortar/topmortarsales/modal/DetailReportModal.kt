package com.topmortar.topmortarsales.modal

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.IS_PAY_STATUS_NOT_PAY
import com.topmortar.topmortarsales.commons.IS_PAY_STATUS_PAY
import com.topmortar.topmortarsales.commons.IS_PAY_STATUS_PAY_LATER
import com.topmortar.topmortarsales.commons.utils.CurrencyFormat
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.databinding.ModalDetailReportBinding
import com.topmortar.topmortarsales.model.ReportVisitModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetailReportModal(private val context: Context) : Dialog(context) {

    private lateinit var binding: ModalDetailReportBinding
    private lateinit var data: ReportVisitModel
    private var withName = false
    private var isCourier = false

    fun setData(data: ReportVisitModel) {
        this.data = data
    }
    fun setIsCourier(data: Boolean) {
        this.isCourier = data
    }
    fun setWithName(withName: Boolean) {
        this.withName = withName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ModalDetailReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setLayout()
    }

    private fun setLayout() {
        val displayMetrics = DisplayMetrics()
        window?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels

        val widthPercentage = 0.9f // Set the width percentage (e.g., 90%)

        val width = (screenWidth * widthPercentage).toInt()

        val layoutParams = window?.attributes
        layoutParams?.width = width
        layoutParams?.height = WindowManager.LayoutParams.WRAP_CONTENT // Set height to wrap content
        window?.attributes = layoutParams as WindowManager.LayoutParams

        val titleBar = binding.titleBarLight
        titleBar.tvTitleBar.text = if (withName) if (isCourier) data.nama_gudang else data.nama else "Detail Laporan"
        titleBar.tvTitleBar.setPadding(convertDpToPx(16, context),0,0,0)

        titleBar.icBack.visibility = View.GONE
        titleBar.icClose.visibility = View.VISIBLE
        titleBar.icClose.setOnClickListener { this.dismiss() }

        binding.tvDistance.text = " ${if (data.is_approved == "1") "Approved" else "Menunggu"} | ${data.distance_visit} km dari titik"
        binding.date.text = data.date_visit
        setDateReport()
        binding.description.text = data.laporan_visit
        setReportType()
        binding.approveMessage.text = data.approve_message.let { if (!it.isNullOrEmpty()) it else "Tidak ada feedback" }

    }

    private fun setDateReport() {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(data.date_visit)
        val dateFormat = if (date != null) {
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val dateYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)

            if (currentYear == dateYear.toInt()) DateFormat.format(data.date_visit, "yyyy-MM-dd HH:mm:ss", "dd MMM, HH:mm")
            else DateFormat.format(data.date_visit, "yyyy-MM-dd HH:mm:ss", "dd MMM yyyy, HH:mm")
        } else DateFormat.format(data.date_visit, "yyyy-MM-dd HH:mm:ss", "dd MMM, HH:mm")
        binding.date.text = " $dateFormat"

        if (data.is_approved == "1") binding.icStatus.setImageDrawable(context.getDrawable(R.drawable.checkbox_circle_green))
    }

    private fun setReportType() {
        when (data.is_pay) {
            IS_PAY_STATUS_PAY -> {
                binding.reportTypeContainer.visibility = View.VISIBLE
                binding.reportTypeTitle.text = "Tagihan yang dibayarkan"
                val formattedCurrency = data.pay_value.let {
                    if (it.isNullOrEmpty()) it
                    else CurrencyFormat.format(it.toDouble())
                }
                binding.reportTypeDescription.text = formattedCurrency
            }
            IS_PAY_STATUS_PAY_LATER -> {
                binding.reportTypeContainer.visibility = View.VISIBLE
                binding.reportTypeTitle.text = "Tanggal yang dijanjikan"
                val formattedDate = data.pay_date.let {
                    if (it.isNullOrEmpty()) it
                    else DateFormat.format(it, outputFormat = "EEEE, dd MMMM yyyy")
                }
                binding.reportTypeDescription.text = formattedDate
            }
            IS_PAY_STATUS_NOT_PAY -> {
                binding.reportTypeContainer.visibility = View.VISIBLE
                binding.reportTypeTitle.text = "Tagihan yang dibayarkan"
                binding.reportTypeDescription.text = "Toko tidak membayar"
            }
            else -> {
                binding.reportTypeContainer.visibility = View.GONE
            }
        }
    }

}