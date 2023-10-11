package com.topmortar.topmortarsales.modal

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.databinding.ModalChartSalesPricingBinding

class ChartSalesPricingModal(private val context: Context) : Dialog(context) {

    private lateinit var customUtility: CustomUtility
    private lateinit var binding: ModalChartSalesPricingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ModalChartSalesPricingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        customUtility = CustomUtility(context)

        setLayout()
        initClickHandler()
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
        titleBar.tvTitleBar.text = "Rincian Pendapatan"
        titleBar.tvTitleBar.setPadding(convertDpToPx(16, context),0,0,0)
        titleBar.tvTitleBarDescription.text = "Ini adalah rincian anda pada bulan ini"

        setupBarChart()
    }

    private fun initClickHandler() {
        val titleBar = binding.titleBarLight
        titleBar.icBack.visibility = View.GONE
        titleBar.icClose.visibility = View.VISIBLE
        titleBar.icClose.setOnClickListener { this.dismiss() }
    }

    private fun setupBarChart() {
        val pricingChart = binding.pricingChart
//        pricingChart.visibility = View.VISIBLE

        // Create some sample data
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(34f, "Data"))
        entries.add(PieEntry(50f, "Passive"))

        val color1 = ContextCompat.getColor(context, R.color.status_data)
        val color2 = ContextCompat.getColor(context, R.color.status_passive)

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(color1, color2) // Set colors for the slices

        val data = PieData(dataSet)
        pricingChart.data = data

        // Customize the chart (optional)
        pricingChart.description.text = ""
        pricingChart.animateY(1000)
    }
}