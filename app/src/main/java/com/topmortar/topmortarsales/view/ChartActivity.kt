package com.topmortar.topmortarsales.view

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityChartBinding
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.CityModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChartActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private val userDistributorId get() = sessionManager.userDistributor()
    private val userKind get() = sessionManager.userKind()
    private val userCity get() = sessionManager.userCityID()
    private val userCityName get() = sessionManager.userCityName()
    private var _binding: ActivityChartBinding? = null
    private val binding get() = _binding!!

    private lateinit var apiService: ApiService
    private lateinit var searchModal: SearchModal
    private var citiesResults: ArrayList<CityModel>? = null
    private var selectedCity: ModalSearchModel? = null

    private var chartTextColor: Int = 0
    private var startMonthIndex = 0 // (0 = Januari)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)
        _binding = ActivityChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = HttpClient.create()

        binding.titleBarDark.tvTitleBar.text = "Grafik Status Toko"
        binding.titleBarDark.icBack.setOnClickListener { finish() }
        chartTextColor = getColor(R.color.black_200)
        if (CustomUtility(this).isDarkMode()) chartTextColor = getColor(R.color.white)

        if (userKind == USER_KIND_ADMIN) getCities()
        loadBarChartData()
    }

    private fun loadBarChartData() {

        var salesData = listOf(
            1f to 0f,
            2f to 0f,
            3f to 0f,
            4f to 0f,
            5f to 0f,
            6f to 0f,
            7f to 0f,
            8f to 0f,
            9f to 0f,
            10f to 0f,
            11f to 0f,
            12f to 0f,
        )

        lifecycleScope.launch {
            try {
                val response = when (selectedCity) {
                    null -> {
                        if (userKind == USER_KIND_ADMIN) apiService.getActiveStore(idDistributor = userDistributorId ?: "")
                        else {
                            binding.chartDesc.text = "Menampilkan data di kota $userCityName"
                            apiService.getActiveStore(idCity = userCity ?: "", idDistributor = userDistributorId ?: "")
                        }
                    } else -> apiService.getActiveStore(idCity = selectedCity?.id!!, idDistributor = userDistributorId ?: "")
                }
                when(response.status) {
                    RESPONSE_STATUS_OK -> {

                        val listResponse = response.results

                        salesData.forEach { item ->
                            val findMatch = listResponse.firstOrNull { data -> data.month_active ==  item.first.toInt().toString() }
                            if (findMatch != null) {
                                salesData = salesData.map {
                                    when (it.first) {
                                        item.first -> it.copy(second = findMatch.jml_active.toFloat())
                                        else -> it
                                    }
                                }
                            }
                        }

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(this@ChartActivity, "TAG_CHART_ACTIVE", "Data tidak ditemukan")

                    }
                    else -> {

                        handleMessage(this@ChartActivity, "TAG_CHART_ACTIVE",  getString(R.string.failed_get_data))

                    }
                }
            } catch (e: Exception) {

                handleMessage(this@ChartActivity, "TAG_CHART_ACTIVE", "Failed run service. Exception " + e.message)

            } finally {

                val monthFormat = SimpleDateFormat("MM", Locale.getDefault())
                val yearFormat = SimpleDateFormat("YYYY", Locale.getDefault())

                val currentMonth = monthFormat.format(Date()).toInt()

                val beforeYear = (yearFormat.format(Date()).toInt() - 1).toString()
                val currentYear = yearFormat.format(Date())

                val sortedSalesData = salesData.sortedBy {
                    if (it.first > currentMonth) it.first else it.first + 12
                }

                startMonthIndex = sortedSalesData[0].first.toInt() - 1

                val entries = ArrayList<BarEntry>()
                for ((i, item) in sortedSalesData.withIndex()) {
                    entries.add(BarEntry(i.toFloat(), item.second))
                }

                val barDataSet = BarDataSet(entries, null)
                val colorList = mutableListOf<Int>()

                for (item in sortedSalesData) {
                    colorList.add(if (item.first > currentMonth) getColor(R.color.status_passive) else getColor(R.color.status_active))
                }

                barDataSet.setValueTextColors(colorList)
                barDataSet.colors = colorList
                barDataSet.valueTextSize = 10f

                val data = BarData(barDataSet)
                data.barWidth = 0.8f
                binding.barChart.data = data
                binding.currentYear.text = currentYear

                if (currentMonth < 12) {
                    binding.beforeYearContainer.visibility = View.VISIBLE
                    binding.beforeYear.text = beforeYear
                }

                setupBarChart()

            }
        }
    }

    private fun setupBarChart() {

        binding.barChart.apply {
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            description.isEnabled = false
            setPinchZoom(false)
            setDrawGridBackground(false)
            setTouchEnabled(false)
            isDoubleTapToZoomEnabled = false
            legend.isEnabled = false
            legend.textColor = chartTextColor
            legend.textSize = 12f
        }

        // sumbu X
        val dynamicMonths = getDynamicMonths(startMonthIndex)
        binding.barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            setDrawAxisLine(true)
            labelCount = 12
            textColor = chartTextColor
            valueFormatter = MonthValueFormatter(dynamicMonths)
        }


        // sumbu Y kiri
        binding.barChart.axisLeft.apply {
            setDrawGridLines(true)
            textColor = chartTextColor
            axisMinimum = 0f // Y dimulai dari 0
        }

        // sumbu Y kanan
        binding.barChart.axisRight.apply {
            isEnabled = false // Menonaktifkan sumbu Y kanan
        }

        binding.barChart.apply {
            setFitBars(true) // bar chart menyesuaikan sumbu X
            invalidate() // Refresh chart

            animateY(1000)
        }
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

    private fun getCities() {

        lifecycleScope.launch {
            try {

                val response = apiService.getCities(distributorID = userDistributorId ?: "")

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        citiesResults = response.results
                        val items: ArrayList<ModalSearchModel> = ArrayList()

                        for (i in 0 until citiesResults!!.size) {
                            val data = citiesResults!![i]
                            items.add(ModalSearchModel(data.id_city, "${data.nama_city} - ${data.kode_city}"))
                        }
                        items.add(0, ModalSearchModel("-1", "Hapus Filter"))

                        setupDialogSearch(items)
                        binding.filterContainer.visibility = View.VISIBLE

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(this@ChartActivity, "LIST CITY", "Daftar kota kosong!")

                    }
                    else -> {

                        handleMessage(this@ChartActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))

                    }
                }


            } catch (e: Exception) {

                handleMessage(this@ChartActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)

            }

        }
    }

    private fun setupDialogSearch(items: ArrayList<ModalSearchModel> = ArrayList()) {

        searchModal = SearchModal(this, items)
        searchModal.setCustomDialogListener(object: SearchModal.SearchModalListener{
            override fun onDataReceived(data: ModalSearchModel) {
                if (data.id == "-1") {
                    selectedCity = null
                    binding.filterCities.text = "pilih kota"
                    binding.chartDesc.text = "Menampilkan data di semua kota"
                    binding.filterChange.visibility = View.GONE
                } else {
                    selectedCity = data
                    binding.filterCities.text = "ganti"
                    binding.chartDesc.text = "Menampilkan data di kota ${data.title}"
                    binding.filterChange.visibility = View.VISIBLE
                }
                loadBarChartData()
            }

        })
        searchModal.searchHint = "Ketik untuk mencari…"

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) binding.filterCities.setTextColor(getColor(R.color.white))
        else binding.filterCities.setTextColor(getColor(R.color.black_200))
        binding.filterContainer.visibility = View.GONE
        binding.filterCities.setOnClickListener {
            searchModal.show()
        }
        binding.filterCities.text = selectedCity?.title ?: "pilih kota"

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}