package com.topmortar.topmortarsales.view.rencanaVisits

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.viewpager.RencanaVisitVPA
import com.topmortar.topmortarsales.commons.USER_KIND_PENAGIHAN
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.databinding.ActivityRencanaVisitBinding

class RencanaVisitActivity : AppCompatActivity() {

    private var _binding: ActivityRencanaVisitBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val userId get() = sessionManager.userID()
    private val userDistributorId get() = sessionManager.userDistributor()

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var pagerAdapter: RencanaVisitVPA
    private var activeTab = 0

    private val tabTitles = listOf("Jatuh Tempo", "Voucher", "Pasif", "Mingguan")
    private val tabTitleViews = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        _binding = ActivityRencanaVisitBinding.inflate(layoutInflater)
        sessionManager = SessionManager(this@RencanaVisitActivity)

        setContentView(binding.root)

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
        if (activeTab != 0) tabLayout.getTabAt(0)?.select()
        else super.onBackPressed()
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