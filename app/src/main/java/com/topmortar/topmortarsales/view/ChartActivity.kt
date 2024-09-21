package com.topmortar.topmortarsales.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.databinding.ActivityChartBinding

class ChartActivity : AppCompatActivity() {

    private var _binding: ActivityChartBinding? = null
    private lateinit var sessionManager: SessionManager
    private val binding get() = _binding!!

    private var textColor: Int?= null
    private var startMonthIndex = 0 // (0 = Januari)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)
        _binding = ActivityChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBarDark.tvTitleBar.text = "Data Grafik"
        binding.titleBarDark.icBack.setOnClickListener { finish() }
        textColor = getColor(R.color.black_200)
        if (CustomUtility(this).isDarkMode()) textColor = getColor(R.color.white)

        loadBarChartData()
        setupBarChart()
    }

    private fun setupBarChart() {

        // Mengkonfigurasi chart
        binding.barChart.setDrawBarShadow(false)
        binding.barChart.setDrawValueAboveBar(true)
        binding.barChart.description.isEnabled = false
        binding.barChart.setPinchZoom(false)
        binding.barChart.setDrawGridBackground(false)
        binding.barChart.legend.isEnabled = true
        binding.barChart.legend.textColor = textColor!!
        binding.barChart.legend.textSize = 12f

        // Mengatur sumbu X
        val dynamicMonths = getDynamicMonths(startMonthIndex)
        val xAxis: XAxis = binding.barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.labelCount = 12
        xAxis.textColor = textColor!!
        xAxis.valueFormatter = MonthValueFormatter(dynamicMonths)

        // Mengatur sumbu Y kiri
        val leftAxis: YAxis = binding.barChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.textColor = textColor!!
        leftAxis.axisMinimum = 0f // Memastikan Y dimulai dari 0

        // Mengatur sumbu Y kanan
        val rightAxis: YAxis = binding.barChart.axisRight
        rightAxis.isEnabled = false // Menonaktifkan sumbu Y kanan

        binding.barChart.setFitBars(true) // Mengatur agar bar chart menyesuaikan sumbu X
        binding.barChart.invalidate() // Refresh chart

        // Animasi (opsional)
        binding.barChart.animateY(1000)
    }

    private fun loadBarChartData() {
        // Contoh data penjualan per bulan
        val salesData = listOf(
            10f to 0f,
            11f to 0f,
            12f to 0f,
            1f to 150f,
            2f to 200f,
            3f to 180f,
            4f to 220f,
            5f to 170f,
            6f to 210f,
            7f to 190f,
            8f to 230f,
            9f to 250f,
        )

        startMonthIndex = salesData[0].first.toInt() - 1
        val entries = ArrayList<BarEntry>()
        for ((i, item) in salesData.withIndex()) {
            // BarEntry membutuhkan posisi X (0-11) dan nilai Y
            entries.add(BarEntry(i.toFloat(), item.second))
        }

        val barDataSet = BarDataSet(entries, "Toko Aktif (2023 - 2024)")
        barDataSet.color = getColor(R.color.status_active)
        barDataSet.valueTextColor = textColor!!
        barDataSet.valueTextSize = 10f

        val data = BarData(barDataSet)
        data.barWidth = 0.8f
        binding.barChart.data = data
    }

    private fun getDynamicMonths(startIndex: Int): Array<String> {
        val allMonths = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        val dynamicMonths = Array(12) { "" }
        for (i in 0 until 12) {
            dynamicMonths[i] = allMonths[(startIndex + i) % 12]
        }
        return dynamicMonths
    }

    inner class MonthValueFormatter(private val months: Array<String>) : com.github.mikephil.charting.formatter.ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val index = value.toInt()
            return if (index in 0..11) months[index] else ""
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}