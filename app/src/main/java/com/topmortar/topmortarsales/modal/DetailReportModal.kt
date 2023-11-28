package com.topmortar.topmortarsales.modal

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.utils.CurrencyFormat
import com.topmortar.topmortarsales.commons.utils.CustomUtility
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

        binding.description.text = data.laporan_visit
        binding.date.text = data.date_visit
        binding.tvDistance.text = " ${if (data.is_approved == "1") "Approved" else "Menunggu"} | ${data.distance_visit} km dari titik"

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

}