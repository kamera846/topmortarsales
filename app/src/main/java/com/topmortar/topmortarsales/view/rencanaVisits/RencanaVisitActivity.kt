package com.topmortar.topmortarsales.view.rencanaVisits

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.viewpager.RencanaVisitVPA
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.databinding.ActivityRencanaVisitBinding

class RencanaVisitActivity : AppCompatActivity() {

    private var _binding: ActivityRencanaVisitBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var pagerAdapter: RencanaVisitVPA
    private var activeTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        _binding = ActivityRencanaVisitBinding.inflate(layoutInflater)
        sessionManager = SessionManager(this@RencanaVisitActivity)

        setContentView(binding.root)

        binding.titleBarDark.icBack.visibility = View.VISIBLE
        binding.titleBarDark.vBorder.visibility = View.GONE
        binding.titleBarDark.tvTitleBar.text = "Rencana Visit"
        binding.titleBarDark.icBack.setOnClickListener {
            if (activeTab != 0) tabLayout.getTabAt(0)?.select()
            else finish()
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

        pagerAdapter = RencanaVisitVPA(supportFragmentManager)
        viewPager.adapter = pagerAdapter

        // Connect TabLayout and ViewPager
        tabLayout.setupWithViewPager(viewPager)
        pagerAdapter.setCounterPageItem(object : RencanaVisitVPA.CounterPageItem{
            override fun counterItem(count: Int, tabIndex: Int) {
                when (tabIndex) {
                    0 -> tabLayout.getTabAt(tabIndex)?.text = "${if (count != 0) "($count)\n" else ""}Jatuh Tempo"
                    1 -> tabLayout.getTabAt(tabIndex)?.text = "${if (count != 0) "($count)\n" else ""}Voucher"
                    else -> tabLayout.getTabAt(tabIndex)?.text = "${if (count != 0) "($count)\n" else ""}Pasif"
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

}