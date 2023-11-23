package com.topmortar.topmortarsales.view.courier

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.viewpager.CourierViewPagerAdapter
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_CITY_ID
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_NAME
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_STATUS
import com.topmortar.topmortarsales.commons.CONST_NEAREST_STORE
import com.topmortar.topmortarsales.commons.LOGGED_OUT
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityCourierBinding
import com.topmortar.topmortarsales.view.MapsActivity
import com.topmortar.topmortarsales.view.SplashScreenActivity
import com.topmortar.topmortarsales.view.user.UserProfileActivity
import kotlinx.coroutines.launch

class CourierActivity : AppCompatActivity() {

    private var _binding: ActivityCourierBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val userId get() = sessionManager.userID()!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        _binding = ActivityCourierBinding.inflate(layoutInflater)
        sessionManager = SessionManager(this@CourierActivity)

        setContentView(binding.root)

        binding.titleBarDark.tvTitleBarDescription.text = sessionManager.userName().let { if (!it.isNullOrEmpty()) "Halo, $it" else ""}
        binding.titleBarDark.tvTitleBarDescription.visibility = binding.titleBarDark.tvTitleBarDescription.text.let { if (it.isNotEmpty()) View.VISIBLE else View.GONE }
        binding.titleBarDark.tvTitleBar.setPadding(convertDpToPx(16, this), 0, 0, 0)
        binding.titleBarDark.tvTitleBarDescription.setPadding(convertDpToPx(16, this), 0, 0, 0)
        binding.titleBarDark.icBack.visibility = View.GONE
        binding.titleBarDark.icMore.visibility = View.VISIBLE
        binding.titleBarDark.icMore.setOnClickListener { showPopupMenu(it) }

        val tabLayout: TabLayout = binding.tabLayout
        val viewPager: ViewPager = binding.viewPager

        val pagerAdapter = CourierViewPagerAdapter(supportFragmentManager)
        viewPager.adapter = pagerAdapter

        // Connect TabLayout and ViewPager
        tabLayout.setupWithViewPager(viewPager)
        pagerAdapter.setCounterPageItem(object : CourierViewPagerAdapter.CounterPageItem{
            override fun counterItem(count: Int, tabIndex: Int) {
                if (tabIndex == 0) tabLayout.getTabAt(tabIndex)?.text = "Toko ($count)"
                else tabLayout.getTabAt(tabIndex)?.text = "Gudang ($count)"
            }

        })

    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this@CourierActivity, view)
        popupMenu.inflate(R.menu.option_main_menu)

        val optionSyncNow = popupMenu.menu.findItem(R.id.option_sync_now)
        val optionMyProfile = popupMenu.menu.findItem(R.id.option_my_profile)
        val optionNearestStore = popupMenu.menu.findItem(R.id.nearest_store)
        val optionSearch = popupMenu.menu.findItem(R.id.option_search)
        val optionUser = popupMenu.menu.findItem(R.id.option_user)
        val optionCity = popupMenu.menu.findItem(R.id.option_city)
        val optionSkill = popupMenu.menu.findItem(R.id.option_skill)
        val optionlogout = popupMenu.menu.findItem(R.id.option_logout)

        optionSyncNow.isVisible = false
        optionMyProfile.isVisible = true
        optionNearestStore.isVisible = true
        optionSearch.isVisible = false
        optionUser.isVisible = false
        optionCity.isVisible = false
        optionSkill.isVisible = false
        optionlogout.isVisible = true

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nearest_store -> {
                    navigateChecklocation()
                    true
                }
                R.id.option_my_profile -> {
                    val intent = Intent(this@CourierActivity, UserProfileActivity::class.java)
                    startActivity(intent)
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

    private fun navigateChecklocation() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Memuat data toko…")
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

                            val intent = Intent(this@CourierActivity, MapsActivity::class.java)

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

                            val intent = Intent(this@CourierActivity, MapsActivity::class.java)

                            intent.putExtra(CONST_NEAREST_STORE, true)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)

                            progressDialog.dismiss()
                            startActivity(intent)

                        }
                        else -> {

                            handleMessage(this@CourierActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                            progressDialog.dismiss()

                        }
                    }


                } catch (e: Exception) {

                    handleMessage(this@CourierActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
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
        sessionManager.setUserKind("")
        sessionManager.setUserID("")
        sessionManager.setUserName("")
        sessionManager.setFullName("")
        sessionManager.setUserCityID("")

        val intent = Intent(this@CourierActivity, SplashScreenActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}