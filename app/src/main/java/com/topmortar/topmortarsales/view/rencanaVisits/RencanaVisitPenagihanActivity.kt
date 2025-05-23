package com.topmortar.topmortarsales.view.rencanaVisits

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.viewpager.RencanaVisitPenagihanVPA
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_CITY_ID
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_NAME
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_STATUS
import com.topmortar.topmortarsales.commons.CONST_NEAREST_STORE
import com.topmortar.topmortarsales.commons.CONST_NEAREST_STORE_HIDE_FILTER
import com.topmortar.topmortarsales.commons.CONST_NEAREST_STORE_WITH_DEFAULT_RANGE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.utils.CustomProgressBar
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.EventBusUtils
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityRencanaVisitPenagihanBinding
import com.topmortar.topmortarsales.model.RencanaVisitModel
import com.topmortar.topmortarsales.view.MapsActivity
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import kotlin.coroutines.cancellation.CancellationException

class RencanaVisitPenagihanActivity : AppCompatActivity(), TagihMingguanFragment.OnSelectedItemListener {

    private var _binding: ActivityRencanaVisitPenagihanBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager
    private val userId get() = sessionManager.userID()
    private val userCityID get() = sessionManager.userCityID()
    private val userKind get() = sessionManager.userKind()
    private val userDistributorId get() = sessionManager.userDistributor()

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var pagerAdapter: RencanaVisitPenagihanVPA
    private lateinit var progressBar: CustomProgressBar
    private var pagerAdapterItemCount = mutableListOf(0,0,0,0)
    private var activeTab = 0
    private var selectedItemCount = 0
    private var isSelectBarActive = false
    private var totalProcess = 0
    private var processed = 0
    private var percentage = 0

    private val tabTitles = mutableListOf("Jatem 0-7", "Jatem 8-15", "Jatem 16+", "Mingguan")
    private val tabTitleViews = mutableListOf<TextView>()

    private lateinit var listCoordinate: ArrayList<String>
    private lateinit var listCoordinateName: ArrayList<String>
    private lateinit var listCoordinateStatus: ArrayList<String>
    private lateinit var listCoordinateCityID: ArrayList<String>
    private lateinit var listAllRenvi: ArrayList<RencanaVisitModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge()

        _binding = ActivityRencanaVisitPenagihanBinding.inflate(layoutInflater)
        sessionManager = SessionManager(this@RencanaVisitPenagihanActivity)

        setContentView(binding.root)

        apiService = HttpClient.create()
        progressBar = CustomProgressBar(this)
        progressBar.setMessage(getString(R.string.txt_loading))
        binding.selectTitleBarDark.componentSelectTitleBarDark.visibility = View.GONE

        binding.titleBarDark.icBack.visibility = View.VISIBLE
        binding.titleBarDark.vBorder.visibility = View.GONE
        binding.titleBarDark.tvTitleBar.text = "Rencana Visit Penagihan"
        binding.titleBarDark.icBack.setOnClickListener {
            if (activeTab != 0) tabLayout.getTabAt(0)?.select()
            else finish()
        }
        if (CustomUtility(this).isUserWithOnlineStatus()) {
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

//        if (userKind == USER_KIND_ADMIN) {
            pagerAdapterItemCount.add(0)
            tabTitles.add("MG")
//        }

        pagerAdapter = RencanaVisitPenagihanVPA(supportFragmentManager, tabTitles.size)
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
        pagerAdapter.setCounterPageItem(object : RencanaVisitPenagihanVPA.CounterPageItem{
            override fun counterItem(count: Int, tabIndex: Int) {
                pagerAdapterItemCount[tabIndex] = count
                tabTitleViews[tabIndex].text = "${if (count != 0) "($count) " else ""}" + tabTitles[tabIndex]
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
        binding.selectTitleBarDark.icConfirmSelect.alpha =
            if (CustomUtility(this).isDarkMode()) 0.2f
            else 0.5f
        binding.selectTitleBarDark.icConfirmSelect.setOnClickListener {
            if (selectedItemCount > 0) {
                progressBar.show()
                pagerAdapter.onConfirmSelected(activeTab)
            }
        }

    }

    private fun showMapsOption() {
        val popupMenu = PopupMenu(this, binding.titleBarDark.icRoadMap)
        popupMenu.inflate(R.menu.option_maps_menu)

        val menuPerCategory = popupMenu.menu.findItem(R.id.option_per_category)

        when (activeTab) {
            0 -> {
                menuPerCategory.title = "Lihat lokasi renvi Jatem 0-7"
            } 1 -> {
                menuPerCategory.title = "Lihat lokasi renvi Jatem 8-15"
            } 2 -> {
                menuPerCategory.title = "Lihat lokasi renvi Jatem 16+"
            } 3 -> {
                menuPerCategory.title = "Lihat lokasi renvi Mingguan"
            } 4 -> {
                menuPerCategory.title = "Lihat lokasi renvi MG"
            }
        }

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.option_all -> {
                    getAllRenvi()
                    true
                }
                R.id.option_per_category -> {
                    if (pagerAdapterItemCount[activeTab] != 0) {
                        val listIem = pagerAdapter.getAllListItem(activeTab)
                        navigateCheckLocationStore(listIem)
                    }
                    true
                }
                R.id.option_choices -> {
                    if (pagerAdapterItemCount[activeTab] != 0) toggleSelectBar()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun navigateCheckLocationStore(items: ArrayList<RencanaVisitModel>) {
        progressBar.show()

        listCoordinate = arrayListOf()
        listCoordinateName = arrayListOf()
        listCoordinateStatus = arrayListOf()
        listCoordinateCityID = arrayListOf()

        totalProcess = items.size
        LoopingTask(items).execute()

    }

    private fun getAllRenvi() {
        progressBar.show()

        listCoordinate = arrayListOf()
        listCoordinateName = arrayListOf()
        listCoordinateStatus = arrayListOf()
        listCoordinateCityID = arrayListOf()
        listAllRenvi = arrayListOf()

        totalProcess = 5
        processed = 0
        percentage = 0

        getListRenviPerCategory("jatem1")
    }
    private fun getListRenviPerCategory(category: String) {
        processed ++
        progressBar.setMessage(getString(R.string.txt_loading) + "($processed/$totalProcess)")

        lifecycleScope.launch {
            try {

                val response = when (userKind) {
                    USER_KIND_ADMIN -> {
                        when (category) {
                            "jatem1" -> apiService.jatemPenagihan(dst = userDistributorId ?: "0", type = "jatem1")
                            "jatem2" -> apiService.jatemPenagihan(dst = userDistributorId ?: "0", type = "jatem2")
                            "jatem3" -> apiService.jatemPenagihan(dst = userDistributorId ?: "0", type = "jatem3")
                            "mg" -> apiService.targetMgDst(idDistributor = userDistributorId ?: "0")
                            else -> apiService.targetWeeklyDst(idDistributor = userDistributorId ?: "0")
                        }
                    } else -> {
                        when (category) {
                            "jatem1" -> apiService.jatemPenagihanFilter(dst = userDistributorId ?: "0", idCity = userCityID ?: "0", type = "jatem1")
                            "jatem2" -> apiService.jatemPenagihanFilter(dst = userDistributorId ?: "0", idCity = userCityID ?: "0", type = "jatem2")
                            "jatem3" -> apiService.jatemPenagihanFilter(dst = userDistributorId ?: "0", idCity = userCityID ?: "0", type = "jatem3")
                            "mg" -> apiService.targetMg(idCity = userCityID ?: "0")
                            else -> apiService.targetWeekly(idCity = userCityID ?: "0")
                        }
                    }
                }

                when (response.status) {
                    RESPONSE_STATUS_OK -> {
                        listAllRenvi.addAll(response.results)
                        when (category) {
                            "jatem1" -> getListRenviPerCategory("jatem2")
                            "jatem2" -> getListRenviPerCategory("jatem3")
                            "jatem3" -> getListRenviPerCategory("weekly")
                            "weekly" -> getListRenviPerCategory("mg")
                            "mg" -> {
                                totalProcess = listAllRenvi.size
                                processed = 0
                                LoopingTask(listAllRenvi).execute()
//                                progressBar.dismiss()
                            }
                        }
                    }
                    RESPONSE_STATUS_EMPTY -> {

                        when (category) {
                            "jatem1" -> getListRenviPerCategory("jatem2")
                            "jatem2" -> getListRenviPerCategory("jatem3")
                            "jatem3" -> getListRenviPerCategory("weekly")
                            "weekly" -> getListRenviPerCategory("mg")
                            "mg" -> {
                                totalProcess = listAllRenvi.size
                                processed = 0
                                LoopingTask(listAllRenvi).execute()
//                                progressBar.dismiss()
                            }
                        }

                    }
                    else -> {

                        when (category) {
                            "jatem1" -> getListRenviPerCategory("jatem2")
                            "jatem2" -> getListRenviPerCategory("jatem3")
                            "jatem3" -> getListRenviPerCategory("weekly")
                            "weekly" -> getListRenviPerCategory("mg")
                            "mg" -> {
                                totalProcess = listAllRenvi.size
                                processed = 0
                                LoopingTask(listAllRenvi).execute()
//                                progressBar.dismiss()
                            }
                        }

                    }
                }

            } catch (e: Exception) {

                if (e is CancellationException) {
                    return@launch
                }
                FirebaseUtils.logErr(this@RencanaVisitPenagihanActivity, "Failed RencanaVisitPenagihanActivity on getListRenviPerCategory(). Catch: ${e.message}")
                when (category) {
                    "jatem1" -> getListRenviPerCategory("jatem2")
                    "jatem2" -> getListRenviPerCategory("jatem3")
                    "jatem3" -> getListRenviPerCategory("weekly")
                    "weekly" -> getListRenviPerCategory("mg")
                    "mg" -> {
                        totalProcess = listAllRenvi.size
                        processed = 0
                        LoopingTask(listAllRenvi).execute()
//                        progressBar.dismiss()
                    }
                }

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

        totalProcess = items.size
        LoopingTask(items).execute()
//
//        for (item in items.listIterator()) {
//            listCoordinate.add(item.maps_url)
//            listCoordinateName.add(item.nama)
//            listCoordinateStatus.add(item.store_status)
//            listCoordinateCityID.add(item.id_city)
//        }
//
//        val intent = Intent(this@RencanaVisitPenagihanActivity, MainActivity::class.java)
//
//        intent.putExtra(CONST_NEAREST_STORE, true)
//        intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
//        intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)
//        intent.putStringArrayListExtra(CONST_LIST_COORDINATE_STATUS, listCoordinateStatus)
//        intent.putStringArrayListExtra(CONST_LIST_COORDINATE_CITY_ID, listCoordinateCityID)

//        toggleSelectBar()
//        Handler(Looper.getMainLooper()).postDelayed({
//            progressBar.dismiss()
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

            val intent = Intent(this@RencanaVisitPenagihanActivity, MapsActivity::class.java)
            intent.putExtra(CONST_NEAREST_STORE, true)
            intent.putExtra(CONST_NEAREST_STORE_HIDE_FILTER, true)
            intent.putExtra(CONST_NEAREST_STORE_WITH_DEFAULT_RANGE, -1)
            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)
            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_STATUS, listCoordinateStatus)
            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_CITY_ID, listCoordinateCityID)

            progressBar.dismiss()
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        Handler(Looper.getMainLooper()).postDelayed({
            if (CustomUtility(this).isUserWithOnlineStatus()) {
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
            if (CustomUtility(this).isUserWithOnlineStatus()) {
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
        if (::progressBar.isInitialized && progressBar.isShowing()) {
            progressBar.dismiss()
        }
        if (sessionManager.isLoggedIn()) {
            if (CustomUtility(this).isUserWithOnlineStatus()) {
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
        selectedItemCount = event.data!!
        binding.selectTitleBarDark.selectionTitle.text = "$selectedItemCount Item Terpilih"
        if (selectedItemCount > 0)
            binding.selectTitleBarDark.icConfirmSelect.alpha = 1f
        else
            binding.selectTitleBarDark.icConfirmSelect.alpha =
                if (CustomUtility(this).isDarkMode()) 0.2f
                else 0.5f
    }

    override fun selectedItems(items: ArrayList<RencanaVisitModel>) {

        listCoordinate = arrayListOf()
        listCoordinateName = arrayListOf()
        listCoordinateStatus = arrayListOf()
        listCoordinateCityID = arrayListOf()

        totalProcess = items.size
        LoopingTask(items).execute()
    }

}