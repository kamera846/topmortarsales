package com.topmortar.topmortarsales.view.tukang

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.viewpager.BAViewPagerAdapter
import com.topmortar.topmortarsales.commons.CONST_IS_BASE_CAMP
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_CITY_ID
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_NAME
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_STATUS
import com.topmortar.topmortarsales.commons.CONST_NEAREST_STORE
import com.topmortar.topmortarsales.commons.LOGGED_OUT
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_ACTION_MAIN_ACTIVITY
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.utils.AppUpdateHelper
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityBrandAmbassadorBinding
import com.topmortar.topmortarsales.view.MapsActivity
import com.topmortar.topmortarsales.view.SplashScreenActivity
import com.topmortar.topmortarsales.view.skill.ManageSkillActivity
import com.topmortar.topmortarsales.view.user.UserProfileActivity
import kotlinx.coroutines.launch

class BrandAmbassadorActivity : AppCompatActivity() {

    private var _binding: ActivityBrandAmbassadorBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val userKind get() = sessionManager.userKind()!!
    private val userId get() = sessionManager.userID()!!
    private val userCity get() = sessionManager.userCityID()!!
    private val userDistributorId get() = sessionManager.userDistributor()!!
    private var doubleBackToExitPressedOnce = false
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var pagerAdapter: BAViewPagerAdapter
    private var activeTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        _binding = ActivityBrandAmbassadorBinding.inflate(layoutInflater)
        sessionManager = SessionManager(this@BrandAmbassadorActivity)
        val isLoggedIn = sessionManager.isLoggedIn()

        if (!isLoggedIn || userId.isEmpty() || userCity.isEmpty() || userKind.isEmpty()|| userDistributorId.isEmpty()) return missingDataHandler()

        setContentView(binding.root)

        binding.titleBarDark.tvTitleBarDescription.text = sessionManager.userName().let { if (!it.isNullOrEmpty()) "Halo, $it" else ""}
        binding.titleBarDark.tvTitleBarDescription.visibility = binding.titleBarDark.tvTitleBarDescription.text.let { if (it.isNotEmpty()) View.VISIBLE else View.GONE }
        binding.titleBarDark.tvTitleBar.setPadding(convertDpToPx(16, this), 0, 0, 0)
        binding.titleBarDark.tvTitleBarDescription.setPadding(convertDpToPx(16, this), 0, 0, 0)
        binding.titleBarDark.icBack.visibility = View.GONE
        binding.titleBarDark.icMore.visibility = View.VISIBLE
        binding.titleBarDark.icMore.setOnClickListener { showPopupMenu(it) }
        binding.titleBarDark.vBorder.visibility = View.GONE

        tabLayout = binding.tabLayout
        viewPager = binding.viewPager

        pagerAdapter = BAViewPagerAdapter(supportFragmentManager)
        viewPager.adapter = pagerAdapter

        // Connect TabLayout and ViewPager
        tabLayout.setupWithViewPager(viewPager)
        pagerAdapter.setCounterPageItem(object : BAViewPagerAdapter.CounterPageItem{
            override fun counterItem(count: Int, tabIndex: Int) {
                if (tabIndex == 0) tabLayout.getTabAt(tabIndex)?.text = "Tukang${if (count != 0) " ($count)" else ""}"
                else tabLayout.getTabAt(tabIndex)?.text = "Basecamp${if (count != 0) " ($count)" else ""}"
            }

        })
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                activeTab = tab?.position!!
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

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

    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this@BrandAmbassadorActivity, view)
        popupMenu.inflate(R.menu.option_main_menu)

        val optionSyncNow = popupMenu.menu.findItem(R.id.option_sync_now)
        val optionMyProfile = popupMenu.menu.findItem(R.id.option_my_profile)
        val optionNearestStore = popupMenu.menu.findItem(R.id.nearest_store)
        val optionSearch = popupMenu.menu.findItem(R.id.option_search)
        val optionUser = popupMenu.menu.findItem(R.id.option_user)
        val optionCity = popupMenu.menu.findItem(R.id.option_city)
        val optionSkill = popupMenu.menu.findItem(R.id.option_skill)
        val optionBasecamp = popupMenu.menu.findItem(R.id.option_basecamp)
        val optionlogout = popupMenu.menu.findItem(R.id.option_logout)

//        optionSyncNow.isVisible = false
        optionMyProfile.isVisible = true
//        optionNearestStore.isVisible = true
//        optionNearestStore.isVisible = activeTab == 0
        if (activeTab == 0) {
            optionNearestStore.isVisible = false
            optionNearestStore.title = "Cari Tukang Terdekat"
        } else {
            optionNearestStore.isVisible = true
            optionNearestStore.title = "Cari Basecamp Terdekat"
        }
        optionSearch.isVisible = false
        optionUser.isVisible = false
        optionCity.isVisible = false
        optionSkill.isVisible = true
        optionBasecamp.isVisible = false
        optionlogout.isVisible = true

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.option_sync_now -> {
                    pagerAdapter.setSyncAction(activeTab)
                    true
                }
                R.id.nearest_store -> {
                    if (activeTab == 0) navigateChecklocationStore()
                    else navigateChecklocationBasecamp()
                    true
                }
                R.id.option_my_profile -> {
                    val intent = Intent(this@BrandAmbassadorActivity, UserProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.option_skill -> {
                    startActivity(Intent(this@BrandAmbassadorActivity, ManageSkillActivity::class.java))
                    true
                }
                R.id.option_logout -> {
                    logoutConfirmation()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun navigateChecklocationStore() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Memuat data tukang…")
        progressDialog.show()

        Handler().postDelayed({

            lifecycleScope.launch {
                try {

                    val apiService: ApiService = HttpClient.create()
                    val response = apiService.getCourierStore(processNumber = "1", courierId = userId)

                    when (response.status) {
                        RESPONSE_STATUS_OK -> {

                            val listCoordinate = arrayListOf<String>()
                            val listCoordinateName = arrayListOf<String>()
                            val listCoordinateStatus = arrayListOf<String>()
                            val listCoordinateCityID = arrayListOf<String>()

                            for (item in response.results.listIterator()) {
                                listCoordinate.add(item.maps_url)
                                listCoordinateName.add(item.nama)
                                listCoordinateStatus.add(item.store_status)
                                listCoordinateCityID.add(item.id_city)
                            }

                            val intent = Intent(this@BrandAmbassadorActivity, MapsActivity::class.java)

                            intent.putExtra(CONST_NEAREST_STORE, true)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_STATUS, listCoordinateStatus)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_CITY_ID, listCoordinateCityID)

                            progressDialog.dismiss()
                            startActivity(intent)

                        }
                        RESPONSE_STATUS_EMPTY -> {

                            val listCoordinate = arrayListOf<String>()
                            val listCoordinateName = arrayListOf<String>()

                            val intent = Intent(this@BrandAmbassadorActivity, MapsActivity::class.java)

                            intent.putExtra(CONST_NEAREST_STORE, true)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)

                            progressDialog.dismiss()
                            startActivity(intent)

                        }
                        else -> {

                            handleMessage(this@BrandAmbassadorActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                            progressDialog.dismiss()

                        }
                    }


                } catch (e: Exception) {

                    handleMessage(this@BrandAmbassadorActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                    progressDialog.dismiss()

                }

            }

        }, 1000)
    }

    private fun navigateChecklocationBasecamp() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Memuat data basecamp…")
        progressDialog.show()

        Handler().postDelayed({

            lifecycleScope.launch {
                try {

                    val apiService: ApiService = HttpClient.create()
                    val response = apiService.getListBaseCamp(cityId = userCity, distributorID = userDistributorId)

                    when (response.status) {
                        RESPONSE_STATUS_OK -> {

                            val listCoordinate = arrayListOf<String>()
                            val listCoordinateName = arrayListOf<String>()
                            val listCoordinateStatus = arrayListOf<String>()
                            val listCoordinateCityID = arrayListOf<String>()

                            for (item in response.results.listIterator()) {
                                listCoordinate.add(item.location_gudang)
                                listCoordinateName.add(item.nama_gudang)
                                listCoordinateStatus.add("blacklist")
                                listCoordinateCityID.add(item.id_city)
                            }

                            val intent = Intent(this@BrandAmbassadorActivity, MapsActivity::class.java)

                            intent.putExtra(CONST_NEAREST_STORE, true)
                            intent.putExtra(CONST_IS_BASE_CAMP, true)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_STATUS, listCoordinateStatus)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_CITY_ID, listCoordinateCityID)

                            progressDialog.dismiss()
                            startActivity(intent)

                        }
                        RESPONSE_STATUS_EMPTY -> {

                            val listCoordinate = arrayListOf<String>()
                            val listCoordinateName = arrayListOf<String>()

                            val intent = Intent(this@BrandAmbassadorActivity, MapsActivity::class.java)

                            intent.putExtra(CONST_NEAREST_STORE, true)
                            intent.putExtra(CONST_IS_BASE_CAMP, true)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)

                            progressDialog.dismiss()
                            startActivity(intent)

                        }
                        else -> {

                            handleMessage(this@BrandAmbassadorActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                            progressDialog.dismiss()

                        }
                    }


                } catch (e: Exception) {

                    handleMessage(this@BrandAmbassadorActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                    progressDialog.dismiss()

                }

            }

        }, 1000)
    }

    private fun logoutConfirmation() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Logout")
            .setMessage("Apakah anda yakin ingin keluar?")
            .setNegativeButton("Tidak") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Iya") { dialog, _ ->

                dialog.dismiss()
                logoutHandler()

            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun logoutHandler() {
        sessionManager.setLoggedIn(LOGGED_OUT)
        sessionManager.setUserLoggedIn(null)

        val intent = Intent(this@BrandAmbassadorActivity, SplashScreenActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getUserLoggedIn() {

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.detailUser(userId = userId)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        val data = response.results[0]
                        sessionManager.setUserLoggedIn(data)

//                        tvTitleBarDescription.text = sessionManager.fullName().let { if (!it.isNullOrEmpty()) "Halo, $it" else "Halo, ${ sessionManager.userName() }"}
                        binding.titleBarDark.tvTitleBarDescription.text = sessionManager.userName().let { if (!it.isNullOrEmpty()) "Halo, $it" else ""}
                        binding.titleBarDark.tvTitleBarDescription.visibility = binding.titleBarDark.tvTitleBarDescription.text.let { if (it.isNotEmpty()) View.VISIBLE else View.GONE }

                    }
                    RESPONSE_STATUS_EMPTY -> missingDataHandler()
                    else -> Log.d("TAG USER LOGGED IN", "Failed get data!")
                }


            } catch (e: Exception) {
                Log.d("TAG USER LOGGED IN", "Failed run service. Exception " + e.message)
            }

        }

    }

    private fun missingDataHandler() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Data Tidak Lengkap Terdeteksi")
            .setCancelable(false)
            .setMessage("Data login yang tidak lengkap telah terdeteksi, silakan coba login kembali!")
            .setPositiveButton("Oke") { dialog, _ ->

                dialog.dismiss()
                logoutHandler()

            }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onBackPressed() {
        if (activeTab != 0) tabLayout.getTabAt(0)?.select()
        else {

            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }

            this@BrandAmbassadorActivity.doubleBackToExitPressedOnce = true
            handleMessage(this@BrandAmbassadorActivity, TAG_ACTION_MAIN_ACTIVITY, "Tekan sekali lagi untuk keluar!", TOAST_SHORT)

            Handler(Looper.getMainLooper()).postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000)

        }
    }

    override fun onResume() {

        super.onResume()
        // Check apps for update
        AppUpdateHelper.checkForUpdates(this)
        getUserLoggedIn()

    }
}