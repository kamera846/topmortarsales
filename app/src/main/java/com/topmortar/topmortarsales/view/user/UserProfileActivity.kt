package com.topmortar.topmortarsales.view.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.tabs.TabLayout
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.viewpager.UserProfileViewPagerAdapter
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_SALES
import com.topmortar.topmortarsales.commons.CONST_FULL_NAME
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.CONST_USER_ID
import com.topmortar.topmortarsales.commons.CONST_USER_LEVEL
import com.topmortar.topmortarsales.commons.MANAGE_USER_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.EventBusUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityUserProfileBinding
import com.topmortar.topmortarsales.modal.ChartSalesPricingModal
import com.topmortar.topmortarsales.model.UserModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class UserProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var customUtility: CustomUtility
    private lateinit var modalPricingDetails: ChartSalesPricingModal

    private var iUserName: String? = null; private var iFullName: String? = null; private var iUserLevel: String? = null
    private var iUserID: String? = null; private var iPhone: String? = null; private var iLocation: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initClickHandler()

        iUserID = intent.getStringExtra(CONST_USER_ID)
        iPhone = intent.getStringExtra(CONST_PHONE)
        iUserName = intent.getStringExtra(CONST_NAME)
        iFullName = intent.getStringExtra(CONST_FULL_NAME)
        iUserLevel = intent.getStringExtra(CONST_USER_LEVEL)
        iLocation = intent.getStringExtra(CONST_LOCATION)

        customUtility = CustomUtility(this)
        modalPricingDetails = ChartSalesPricingModal(this)

        dataActivityValidation()

    }

    private fun getUserDetail() {
        binding.titleBarLight.tvTitleBar.text = getString(R.string.txt_loading)
        binding.tvFullName.text = getString(R.string.txt_loading)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                sessionManager.userID()?.let {
                    val response = apiService.detailUser(userId = it)
                    when (response.status) {
                        RESPONSE_STATUS_OK -> {

                            val data = response.results[0]

                            iUserID = data.id_user
                            iPhone = data.phone_user
                            iUserName = data.username
                            iFullName = data.full_name
                            iUserLevel = data.level_user
                            iLocation = data.id_city

                            sessionManager.setUserID(data.id_user)
                            sessionManager.setUserName(data.username)
                            sessionManager.setFullName(data.full_name)
                            sessionManager.setUserCityID(data.id_city)

                            dataActivityValidation()

                        } RESPONSE_STATUS_EMPTY -> handleMessage(this@UserProfileActivity, "GET USER DETAIL", "Gagal memuat detail pengguna: ${response.message}")
                        else -> handleMessage(this@UserProfileActivity, "GET USER DETAIL", "Gagal memuat data. Error: ${response.message}")
                    }
                }

            } catch (e: Exception) {
                handleMessage(this@UserProfileActivity, "GET USER DETAIL", "Failed run service. Error: ${e.message}")
            }

        }
    }

    private fun navigateEditUser() {

        val intent = Intent(this, AddUserActivity::class.java)

        intent.putExtra(CONST_USER_ID, iUserID)
        intent.putExtra(CONST_PHONE, iPhone)
        intent.putExtra(CONST_NAME, iUserName)
        intent.putExtra(CONST_USER_LEVEL, iUserLevel)
        intent.putExtra(CONST_LOCATION, iLocation)
        intent.putExtra(CONST_FULL_NAME, iFullName)

        startActivityForResult(intent, MANAGE_USER_ACTIVITY_REQUEST_CODE)

    }

    private fun initClickHandler() {

        if (sessionManager.userKind() == USER_KIND_ADMIN) {
            binding.titleBarLight.icEdit.visibility = View.VISIBLE
            binding.titleBarLight.icEdit.setOnClickListener { navigateEditUser() }
        }
        binding.toggleBarChart.setOnClickListener { toggleBarChart() }
        binding.priceContainer.setOnClickListener { modalPricingDetails.show() }
        binding.titleBarLight.icBack.setOnClickListener { finish() }

    }

    private fun dataActivityValidation() {

        if (iUserID.isNullOrEmpty()) {
            return getUserDetail()
        }

        if (iUserName!!.isNotEmpty()) {
            binding.titleBarLight.tvTitleBar.text = iUserName
            binding.titleBarLight.tvTitleBarDescription.visibility = View.VISIBLE
            binding.titleBarLight.tvTitleBarDescription.text = iUserLevel
        }
        if (iFullName!!.isNotEmpty()) binding.tvFullName.text = iFullName
        if (iUserLevel!!.isNotEmpty()) binding.tvLevel.text = iUserLevel

        if (iUserLevel == AUTH_LEVEL_SALES) {

            if (sessionManager.userKind() == USER_KIND_ADMIN) binding.titleBarLight.icEdit.visibility = View.VISIBLE
            binding.priceContainer.visibility = View.VISIBLE
            binding.tabContainer.visibility = View.VISIBLE
//            binding.counterContainer.visibility = View.VISIBLE

            val tabLayout: TabLayout = binding.tabLayout
            val viewPager: ViewPager = binding.viewPager // If using ViewPager

            val pagerAdapter = UserProfileViewPagerAdapter(supportFragmentManager) // Create your PagerAdapter
            viewPager.adapter = pagerAdapter

            // Connect TabLayout and ViewPager
            tabLayout.setupWithViewPager(viewPager)

        }

        setupBarChart()

    }

    private fun setupBarChart() {
        binding.barChartContainer.visibility = View.VISIBLE
        val barChart = binding.storeBarChart

        // Create some sample data
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, 100f))
        entries.add(BarEntry(1f, 150f))
        entries.add(BarEntry(2f, 80f))
        entries.add(BarEntry(3f, 120f))
        entries.add(BarEntry(4f, 60f))
        entries.add(BarEntry(5f, 90f))

        // Access the custom colors from resources
        val color1 = ContextCompat.getColor(this, R.color.status_active)
        val color2 = ContextCompat.getColor(this, R.color.status_passive)
        val color3 = ContextCompat.getColor(this, R.color.status_data)
        val color4 = ContextCompat.getColor(this, R.color.status_bid)
        val color5 = ContextCompat.getColor(this, R.color.black_200)
        val color6 = ContextCompat.getColor(this, R.color.status_blacklist)

        // Create a list of custom colors
        val customColors = listOf(color1, color2, color3, color4, color5, color6)

        val dataSet = BarDataSet(entries, "")
        dataSet.colors = customColors

        val textColorResId = if (customUtility.isDarkMode()) R.color.white else R.color.black_200
        val textColor = ContextCompat.getColor(this, textColorResId)

        // Customize the x-axis labels (optional)
        val xAxis = barChart.xAxis
        val yAxisLeft = barChart.axisLeft
        val yAxisRight = barChart.axisRight
        val labels = arrayOf("Active", "Passive", "Data", "Bid", "Not Set", "Blacklist")

        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.granularity = 1f

        xAxis.textColor = textColor
        yAxisLeft.textColor = textColor
        yAxisRight.textColor = textColor
        dataSet.valueTextColor = textColor

        val data = BarData(dataSet)
        barChart.data = data

        // Customize the chart (optional)
        barChart.setFitBars(true)
        barChart.description.isEnabled = false
        barChart.animateY(1000)

        toggleBarChart()
    }

    private fun toggleBarChart() {
        val barChart = binding.storeBarChart
        val title = binding.tvToggleBarChart
        val icon = binding.iconToggleBarChart

        if (barChart.isVisible) {
            barChart.visibility = View.GONE
            title.text = "Statistik Toko di Kota Malang (tampilkan)"
            icon.setImageDrawable(getDrawable(R.drawable.chevron_down_solid))
        } else {
            barChart.visibility = View.VISIBLE
            title.text = "Statistik Toko di Kota Malang (sembunyikan)"
            icon.setImageDrawable(getDrawable(R.drawable.chevron_up_solid))
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe
    fun onEventBus(event: EventBusUtils.MessageEvent) {
        // Handle the event here
        val message = event.message
        Toast.makeText(this, message, TOAST_SHORT).show()
        // Update UI or perform other actions
    }

}