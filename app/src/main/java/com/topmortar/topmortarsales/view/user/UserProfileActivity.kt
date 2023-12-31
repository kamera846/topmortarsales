package com.topmortar.topmortarsales.view.user

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.google.android.material.tabs.TabLayout
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.viewpager.UserProfileViewPagerAdapter
import com.topmortar.topmortarsales.commons.ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_SALES
import com.topmortar.topmortarsales.commons.CONST_ADDRESS
import com.topmortar.topmortarsales.commons.CONST_BIRTHDAY
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_DATE
import com.topmortar.topmortarsales.commons.CONST_FULL_NAME
import com.topmortar.topmortarsales.commons.CONST_KTP
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_OWNER
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.CONST_PROMO
import com.topmortar.topmortarsales.commons.CONST_REPUTATION
import com.topmortar.topmortarsales.commons.CONST_STATUS
import com.topmortar.topmortarsales.commons.CONST_TERMIN
import com.topmortar.topmortarsales.commons.CONST_USER_ID
import com.topmortar.topmortarsales.commons.CONST_USER_LEVEL
import com.topmortar.topmortarsales.commons.EMPTY_FIELD_VALUE
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.MANAGE_USER_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.EventBusUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityUserProfileBinding
import com.topmortar.topmortarsales.modal.ChartSalesPricingModal
import com.topmortar.topmortarsales.model.ContactModel
import com.topmortar.topmortarsales.view.contact.DetailContactActivity
import com.topmortar.topmortarsales.view.reports.ReportsActivity
import com.topmortar.topmortarsales.view.reports.UsersReportActivity
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

    private val bidLimit get() = sessionManager.userBidLimit().toString()
    private var isRequestSync = false

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
                            sessionManager.userBidLimit(data.bid_limit)

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

    private fun navigateDetailContact(data: ContactModel? = null) {

        val intent = Intent(this@UserProfileActivity, DetailContactActivity::class.java)

        if (data != null) {
            intent.putExtra(ACTIVITY_REQUEST_CODE, MAIN_ACTIVITY_REQUEST_CODE)
            intent.putExtra(CONST_CONTACT_ID, data.id_contact)
            intent.putExtra(CONST_NAME, data.nama)
            intent.putExtra(CONST_PHONE, data.nomorhp)
            intent.putExtra(CONST_BIRTHDAY, data.tgl_lahir)
            intent.putExtra(CONST_OWNER, data.store_owner)
            intent.putExtra(CONST_LOCATION, data.id_city)
            intent.putExtra(CONST_MAPS, data.maps_url)
            intent.putExtra(CONST_ADDRESS, data.address)
            intent.putExtra(CONST_STATUS, data.store_status)
            intent.putExtra(CONST_KTP, data.ktp_owner)
            intent.putExtra(CONST_TERMIN, data.termin_payment)
            intent.putExtra(CONST_PROMO, data.id_promo)
            intent.putExtra(CONST_REPUTATION, data.reputation)
            intent.putExtra(CONST_DATE, data.created_at)
        }

        startActivity(intent)

    }

    private fun navigateSalesReport() {

        val intent = Intent(this@UserProfileActivity, ReportsActivity::class.java)
        intent.putExtra(CONST_USER_ID, iUserID)
        intent.putExtra(CONST_FULL_NAME, iFullName)
        intent.putExtra(CONST_USER_LEVEL, iUserLevel)
        startActivity(intent)

    }

    private fun initClickHandler() {

        if (sessionManager.userKind() == USER_KIND_ADMIN) {
            binding.titleBarLight.icEdit.visibility = View.VISIBLE
            binding.titleBarLight.icEdit.setOnClickListener { navigateEditUser() }
        }
        binding.toggleBarChart.setOnClickListener { toggleBarChart() }
        binding.priceContainer.setOnClickListener { modalPricingDetails.show() }
        binding.titleBarLight.icBack.setOnClickListener { backHandler() }
        binding.salesReportContainer.setOnClickListener { navigateSalesReport() }

    }

    private fun dataActivityValidation() {

        if (iUserID.isNullOrEmpty()) {
            return getUserDetail()
        }

        binding.salesReportContainer.visibility = View.VISIBLE

        if (iUserName!!.isNotEmpty()) {
            binding.titleBarLight.tvTitleBar.text = iUserName
            binding.titleBarLight.tvTitleBarDescription.visibility = View.VISIBLE
            binding.titleBarLight.tvTitleBarDescription.text = iUserLevel
        }
        if (iFullName!!.isNotEmpty()) binding.tvFullName.text = iFullName
        if (iUserLevel!!.isNotEmpty()) binding.tvLevel.text = iUserLevel

        if (iUserLevel == AUTH_LEVEL_SALES) {

            if (sessionManager.userKind() == USER_KIND_ADMIN) binding.titleBarLight.icEdit.visibility = View.VISIBLE
            binding.priceContainer.visibility = View.GONE
            binding.tabContainer.visibility = View.VISIBLE
//            binding.counterContainer.visibility = View.VISIBLE

            val tabLayout: TabLayout = binding.tabLayout
            val viewPager: ViewPager = binding.viewPager // If using ViewPager

            val pagerAdapter = UserProfileViewPagerAdapter(supportFragmentManager) // Create your PagerAdapter

            pagerAdapter.setUserCityParam(iLocation)
            pagerAdapter.setUserIdParam(iUserID)
            viewPager.adapter = pagerAdapter

            // Connect TabLayout and ViewPager
            tabLayout.setupWithViewPager(viewPager)
            pagerAdapter.setCounterPageItem(object : UserProfileViewPagerAdapter.CounterPageItem{
                override fun counterItem(count: Int, tabIndex: Int) {
//                    if (tabIndex == 0) tabLayout.getTabAt(tabIndex)?.text = "On Bid ($count/$bidLimit)"
                    if (tabIndex == 0) tabLayout.getTabAt(tabIndex)?.text = "Visited ($count)"
                    else tabLayout.getTabAt(tabIndex)?.text = "Visited ($count)"
                }

            })

        }

        setupBarChart()

    }

    private fun setupBarChart() {
        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = when (sessionManager.userKind()) {
                    USER_KIND_ADMIN -> apiService.getStoreCount()
                    else -> apiService.getStoreCount(sessionManager.userCityID().toString())
                }

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        val dataList = response.results

                        binding.barChartContainer.visibility = View.GONE
                        val barChart = binding.storeBarChart

                        // Create some sample data
                        val entries = ArrayList<BarEntry>()
                        val labels = arrayOf<String>()

                        for ((i, item) in dataList.listIterator().withIndex()) {
                            entries.add(BarEntry(i.toFloat(), item.jml_store.toFloat()))
                            labels.plus(item.store_status)
                        }

//                        entries.add(BarEntry(0f, 100f))
//                        entries.add(BarEntry(1f, 150f))
//                        entries.add(BarEntry(2f, 80f))
//                        entries.add(BarEntry(3f, 120f))
//                        entries.add(BarEntry(4f, 60f))
//                        entries.add(BarEntry(5f, 90f))

                        // Access the custom colors from resources
                        val colorActive = ContextCompat.getColor(this@UserProfileActivity, R.color.status_active)
                        val colorPassive = ContextCompat.getColor(this@UserProfileActivity, R.color.status_passive)
                        val colorData = ContextCompat.getColor(this@UserProfileActivity, R.color.status_data)
                        val colorBid = ContextCompat.getColor(this@UserProfileActivity, R.color.status_bid)
                        val colorBlack = ContextCompat.getColor(this@UserProfileActivity, R.color.black_200)
                        val colorBlackList = ContextCompat.getColor(this@UserProfileActivity, R.color.status_blacklist)

                        // Create a list of custom colors
                        val customColors = listOf(
                            colorActive,
                            colorBid,
                            colorBlackList,
                            colorPassive,
//                            colorData,
//                            colorBlack
                        )

                        val dataSet = BarDataSet(entries, "")
                        dataSet.colors = customColors

                        val textColorResId = if (customUtility.isDarkMode()) R.color.white else R.color.black_200
                        val textColor = ContextCompat.getColor(this@UserProfileActivity, textColorResId)

                        // Customize the x-axis labels (optional)
                        val xAxis = barChart.xAxis
                        val yAxisLeft = barChart.axisLeft
                        val yAxisRight = barChart.axisRight
//                        val labels = arrayOf("Active", "Passive", "Data", "Bid", "Not Set", "Blacklist")

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
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(this@UserProfileActivity, TAG_RESPONSE_CONTACT, "Belum ada statistik toko.")

                    }
                    else -> {

                        handleMessage(this@UserProfileActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))

                    }
                }


            } catch (e: Exception) {

                handleMessage(this@UserProfileActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)

            }

        }
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
    fun onEventBus(event: EventBusUtils.ContactModelEvent) {
        navigateDetailContact(event.data)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MANAGE_USER_ACTIVITY_REQUEST_CODE) {

            val resultData = data?.getStringExtra("$MANAGE_USER_ACTIVITY_REQUEST_CODE")

            if (resultData == SYNC_NOW) {

                isRequestSync = true

            }

        }

    }

    private fun backHandler() {
        if (isRequestSync) {
            val resultIntent = Intent()
            resultIntent.putExtra("$MANAGE_USER_ACTIVITY_REQUEST_CODE", SYNC_NOW)
            setResult(RESULT_OK, resultIntent)
            finish()
        } else finish()
    }

    override fun onBackPressed() {
        backHandler()
    }

}