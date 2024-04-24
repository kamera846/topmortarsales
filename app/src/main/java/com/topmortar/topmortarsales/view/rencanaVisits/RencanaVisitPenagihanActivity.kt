package com.topmortar.topmortarsales.view.rencanaVisits

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.viewpager.RencanaVisitPenagihanVPA
import com.topmortar.topmortarsales.commons.USER_KIND_PENAGIHAN
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.databinding.ActivityRencanaVisitPenagihanBinding

class RencanaVisitPenagihanActivity : AppCompatActivity() {

    private var _binding: ActivityRencanaVisitPenagihanBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val userId get() = sessionManager.userID()
    private val userDistributorId get() = sessionManager.userDistributor()

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var pagerAdapter: RencanaVisitPenagihanVPA
    private var activeTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        _binding = ActivityRencanaVisitPenagihanBinding.inflate(layoutInflater)
        sessionManager = SessionManager(this@RencanaVisitPenagihanActivity)

        setContentView(binding.root)

        binding.titleBarDark.icBack.visibility = View.VISIBLE
        binding.titleBarDark.vBorder.visibility = View.GONE
        binding.titleBarDark.tvTitleBar.text = "Rencana Visit Penagihan"
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
        if (activeTab != 0) tabLayout.getTabAt(0)?.select()
        else super.onBackPressed()
    }

    private fun initLayout() {
        binding.tabContainer.visibility = View.VISIBLE

        tabLayout = binding.tabLayout
        viewPager = binding.viewPager

        pagerAdapter = RencanaVisitPenagihanVPA(supportFragmentManager)
        viewPager.adapter = pagerAdapter

        // Connect TabLayout and ViewPager
        tabLayout.setupWithViewPager(viewPager)
        pagerAdapter.setCounterPageItem(object : RencanaVisitPenagihanVPA.CounterPageItem{
            override fun counterItem(count: Int, tabIndex: Int) {
                when (tabIndex) {
                    0 -> tabLayout.getTabAt(tabIndex)?.text = "${if (count != 0) "($count)\n" else ""}Jatem 0-7"
                    1 -> tabLayout.getTabAt(tabIndex)?.text = "${if (count != 0) "($count)\n" else ""}Jatem 7-15"
                    else -> tabLayout.getTabAt(tabIndex)?.text = "${if (count != 0) "($count)\n" else ""}Jatem 15+"
                }
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

        val tabIndexFromIntent = intent.getIntExtra("tabIndex", 0)
        activeTab = tabIndexFromIntent
        tabLayout.getTabAt(activeTab)?.select()

        binding.titleBarDark.icSyncNow.visibility = View.VISIBLE
        binding.titleBarDark.icSyncNow.setOnClickListener { pagerAdapter.setSyncAction(activeTab) }

    }

    override fun onStart() {
        super.onStart()
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
        super.onStop()

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

}