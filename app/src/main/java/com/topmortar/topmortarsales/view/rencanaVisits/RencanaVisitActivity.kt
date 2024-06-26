package com.topmortar.topmortarsales.view.rencanaVisits

import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.itextpdf.kernel.pdf.canvas.parser.EventType
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.viewpager.RencanaVisitVPA
import com.topmortar.topmortarsales.commons.CONFIRM_SELECTED_CODE
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_CITY_ID
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_NAME
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_STATUS
import com.topmortar.topmortarsales.commons.CONST_NEAREST_STORE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_PENAGIHAN
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.EventBusUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityRencanaVisitBinding
import com.topmortar.topmortarsales.model.RencanaVisitModel
import com.topmortar.topmortarsales.view.MapsActivity
import com.topmortar.topmortarsales.view.skill.ManageSkillActivity
import com.topmortar.topmortarsales.view.user.UserProfileActivity
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class RencanaVisitActivity : AppCompatActivity() {

    private var _binding: ActivityRencanaVisitBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager
    private val userId get() = sessionManager.userID()
    private val userCityID get() = sessionManager.userCityID()
    private val userKind get() = sessionManager.userKind()
    private val userDistributorId get() = sessionManager.userDistributor()

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var pagerAdapter: RencanaVisitVPA
    private lateinit var progressDialog: ProgressDialog
    private var activeTab = 0
    private var isSelectBarActive = false

    private val tabTitles = listOf("Jatuh Tempo", "Voucher", "Pasif", "Mingguan")
    private val tabTitleViews = mutableListOf<TextView>()

    private lateinit var listCoordinate: ArrayList<String>
    private lateinit var listCoordinateName: ArrayList<String>
    private lateinit var listCoordinateStatus: ArrayList<String>
    private lateinit var listCoordinateCityID: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        _binding = ActivityRencanaVisitBinding.inflate(layoutInflater)
        sessionManager = SessionManager(this@RencanaVisitActivity)

        setContentView(binding.root)

        apiService = HttpClient.create()
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage(getString(R.string.txt_loading))
        binding.selectTitleBarDark.componentSelectTitleBarDark.visibility = View.GONE

        binding.titleBarDark.icBack.visibility = View.VISIBLE
        binding.titleBarDark.vBorder.visibility = View.GONE
        binding.titleBarDark.tvTitleBar.text = "Rencana Visit Sales"
        binding.titleBarDark.icBack.setOnClickListener {
            if (activeTab != 0) tabLayout.getTabAt(0)?.select()
            else finish()
        }
        if (sessionManager.userKind() == USER_KIND_SALES || sessionManager.userKind() == USER_KIND_PENAGIHAN) {
            CustomUtility(this).setUserStatusOnline(
                true,
                userDistributorId ?: "-custom-011",
                userId ?: ""
            )
        }

        initLayout()

    }

    override fun onBackPressed() {
        if (isSelectBarActive) toggleSelectBar()
        else {
            if (activeTab != 0) tabLayout.getTabAt(0)?.select()
            else super.onBackPressed()
        }
    }

    private fun initLayout() {
        binding.tabContainer.visibility = View.VISIBLE

        tabLayout = binding.tabLayout
        viewPager = binding.viewPager

        pagerAdapter = RencanaVisitVPA(supportFragmentManager, tabTitles.size)
        viewPager.adapter = pagerAdapter

        // Connect TabLayout and ViewPager
        tabLayout.setupWithViewPager(viewPager)
        for ((idx, item) in tabTitles.listIterator().withIndex()) {
            val textView = LayoutInflater.from(this).inflate(R.layout.tab_renvi_title, null) as TextView
            textView.text = item
            tabTitleViews.add(textView)
            tabLayout.getTabAt(idx)?.customView = textView
        }
        tabTitleViews[0].setTypeface(null, android.graphics.Typeface.BOLD)
        pagerAdapter.setCounterPageItem(object : RencanaVisitVPA.CounterPageItem{
            override fun counterItem(count: Int, tabIndex: Int) {
                tabTitleViews[tabIndex].text = "${if (count != 0) "($count) " else ""}" +  tabTitles[tabIndex]
            }

        })
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                activeTab = tab?.position!!
                tabTitleViews[activeTab].setTypeface(null, android.graphics.Typeface.BOLD)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tabTitleViews[activeTab].setTypeface(null, android.graphics.Typeface.NORMAL)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })

        if (CustomUtility(this).isDarkMode()) {
            tabLayout.setBackgroundColor(getColor(R.color.black_300))
            tabLayout.setTabTextColors(getColor(R.color.black_600), getColor(R.color.primary))
            tabLayout.setSelectedTabIndicatorColor(getColor(R.color.primary))
        }
        else {
            tabLayout.setBackgroundColor(getColor(R.color.primary))
            tabLayout.setTabTextColors(getColor(R.color.primary_600), getColor(R.color.white))
            tabLayout.setSelectedTabIndicatorColor(getColor(R.color.white))
        }

        val tabIndexFromIntent = intent.getIntExtra("tabIndex", 0)
        activeTab = tabIndexFromIntent
        tabLayout.getTabAt(activeTab)?.select()

//        binding.titleBarDark.icSyncNow.visibility = View.VISIBLE
//        binding.titleBarDark.icSyncNow.setOnClickListener { pagerAdapter.setSyncAction(activeTab) }
        binding.titleBarDark.icRoadMap.visibility = View.VISIBLE
        binding.titleBarDark.icRoadMap.setOnClickListener { showMapsOption() }
        binding.selectTitleBarDark.icCloseSelect.setOnClickListener { toggleSelectBar() }
        binding.selectTitleBarDark.icConfirmSelect.setOnClickListener {
            pagerAdapter.onConfirmSelected(activeTab)
        }

    }

    private fun showMapsOption() {
        val popupMenu = PopupMenu(this, binding.titleBarDark.icRoadMap)
        popupMenu.inflate(R.menu.option_maps_menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.option_all -> {
                    navigateCheckLocationStore()
                    true
                }
                R.id.option_choices -> {
                    toggleSelectBar()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun navigateCheckLocationStore() {
        progressDialog.show()

        lifecycleScope.launch {
            try {

                val response = when (userKind) {
                    USER_KIND_ADMIN -> {
                        when (activeTab) {
                            0 -> apiService.targetJatemDst(idDistributor = userDistributorId ?: "0")
                            1 -> apiService.targetVoucherDst(idDistributor = userDistributorId ?: "0")
                            2 -> apiService.targetPasifDst(idDistributor = userDistributorId ?: "0")
                            else -> apiService.targetWeeklyDst(idDistributor = userDistributorId ?: "0")
                        }
                    } else -> when (activeTab) {
                        0 -> apiService.targetJatem(idCity = userCityID ?: "0")
                        1 -> apiService.targetVoucher(idCity = userCityID ?: "0")
                        2 -> apiService.targetPasif(idCity = userCityID ?: "0")
                        else -> apiService.targetWeekly(idCity = userCityID ?: "0")
                    }
                }

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        listCoordinate = arrayListOf()
                        listCoordinateName = arrayListOf()
                        listCoordinateStatus = arrayListOf()
                        listCoordinateCityID = arrayListOf()

                        LoopingTask(response.results).execute()

//                        for (item in response.results.listIterator()) {
//                            listCoordinate.add(item.maps_url)
//                            listCoordinateName.add(item.nama)
//                            listCoordinateStatus.add(item.store_status)
//                            listCoordinateCityID.add(item.id_city)
//                        }
//
//                        val intent = Intent(this@RencanaVisitActivity, MapsActivity::class.java)
//
//                        intent.putExtra(CONST_NEAREST_STORE, true)
//                        intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
//                        intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)
//                        intent.putStringArrayListExtra(CONST_LIST_COORDINATE_STATUS, listCoordinateStatus)
//                        intent.putStringArrayListExtra(CONST_LIST_COORDINATE_CITY_ID, listCoordinateCityID)
//
//                        progressDialog.dismiss()
//                        startActivity(intent)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        listCoordinate = arrayListOf()
                        listCoordinateName = arrayListOf()
                        listCoordinateStatus = arrayListOf()
                        listCoordinateCityID = arrayListOf()

                        val intent = Intent(this@RencanaVisitActivity, MapsActivity::class.java)

                        intent.putExtra(CONST_NEAREST_STORE, true)
                        intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
                        intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)
                        intent.putStringArrayListExtra(CONST_LIST_COORDINATE_STATUS, listCoordinateStatus)
                        intent.putStringArrayListExtra(CONST_LIST_COORDINATE_CITY_ID, listCoordinateCityID)

                        progressDialog.dismiss()
                        startActivity(intent)

                    }
                    else -> {

                        handleMessage(this@RencanaVisitActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        progressDialog.dismiss()

                    }
                }

            } catch (e: Exception) {

                handleMessage(this@RencanaVisitActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                progressDialog.dismiss()

            }

        }

    }

    private fun toggleSelectBar() {
        isSelectBarActive = !isSelectBarActive
        pagerAdapter.setSelectBarActive(activeTab, isSelectBarActive)

        val titleBar = findViewById<LinearLayout>(R.id.titleBarDark)

        if (isSelectBarActive) {
            binding.selectTitleBarDark.componentSelectTitleBarDark.visibility = View.VISIBLE
            titleBar.visibility = View.GONE
            binding.tabLayout.visibility = View.GONE
        } else {
            binding.selectTitleBarDark.componentSelectTitleBarDark.visibility = View.GONE
            titleBar.visibility = View.VISIBLE
            binding.tabLayout.visibility = View.VISIBLE
        }
    }

    fun onSelectedItems(items: ArrayList<RencanaVisitModel>) {

        listCoordinate = arrayListOf()
        listCoordinateName = arrayListOf()
        listCoordinateStatus = arrayListOf()
        listCoordinateCityID = arrayListOf()

        LoopingTask(items).execute()
//
//        for (item in items.listIterator()) {
//            listCoordinate.add(item.maps_url)
//            listCoordinateName.add(item.nama)
//            listCoordinateStatus.add(item.store_status)
//            listCoordinateCityID.add(item.id_city)
//        }
//
//        val intent = Intent(this@RencanaVisitActivity, MainActivity::class.java)
//
//        intent.putExtra(CONST_NEAREST_STORE, true)
//        intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
//        intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)
//        intent.putStringArrayListExtra(CONST_LIST_COORDINATE_STATUS, listCoordinateStatus)
//        intent.putStringArrayListExtra(CONST_LIST_COORDINATE_CITY_ID, listCoordinateCityID)

//        toggleSelectBar()
//        Handler(Looper.getMainLooper()).postDelayed({
//            progressDialog.dismiss()
//            startActivity(intent)
//        }, 1000)
    }

    private inner class LoopingTask(private var items: ArrayList<RencanaVisitModel>) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            for (item in items.listIterator()) {
                listCoordinate.add(item.maps_url)
                listCoordinateName.add(item.nama)
                listCoordinateStatus.add(item.store_status)
                listCoordinateCityID.add(item.id_city)
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            val intent = Intent(this@RencanaVisitActivity, MapsActivity::class.java)
            intent.putExtra(CONST_NEAREST_STORE, true)
            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)
            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_STATUS, listCoordinateStatus)
            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_CITY_ID, listCoordinateCityID)

            progressDialog.dismiss()
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        Handler(Looper.getMainLooper()).postDelayed({
            if (sessionManager.userKind() == USER_KIND_SALES || sessionManager.userKind() == USER_KIND_PENAGIHAN) {
                CustomUtility(this).setUserStatusOnline(
                    true,
                    userDistributorId ?: "-custom-011",
                    userId ?: ""
                )
            }
        }, 1000)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        if (sessionManager.isLoggedIn()) {
            if (sessionManager.userKind() == USER_KIND_SALES || sessionManager.userKind() == USER_KIND_PENAGIHAN) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    userDistributorId ?: "-custom-011",
                    userId ?: ""
                )
            }
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (sessionManager.isLoggedIn()) {
            if (sessionManager.userKind() == USER_KIND_SALES || sessionManager.userKind() == USER_KIND_PENAGIHAN) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    userDistributorId ?: "-custom-011",
                    userId ?: ""
                )
            }
        }
    }

    @Subscribe
    fun onEventBus(event: EventBusUtils.IntEvent) {
        binding.selectTitleBarDark.selectionTitle.text = "${event.data} Item Terpilih"
    }

}