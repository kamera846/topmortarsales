package com.topmortar.topmortarsales.view.delivery

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.viewpager.DeliveryViewPagerAdapter
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.databinding.ActivityDeliveryBinding


class DeliveryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeliveryBinding

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var pagerAdapter: DeliveryViewPagerAdapter
    private var activeTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        binding = ActivityDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBarDark.tvTitleBar.text = "Pengiriman"
        binding.titleBarDark.vBorder.visibility = View.GONE
        binding.titleBarDark.icSyncNow.visibility = View.VISIBLE
        binding.titleBarDark.icBack.setOnClickListener { finish() }
        binding.titleBarDark.icSyncNow.setOnClickListener { pagerAdapter.setSyncAction(activeTab) }

        tabLayout = binding.tabLayout
        viewPager = binding.viewPager

        pagerAdapter = DeliveryViewPagerAdapter(supportFragmentManager)
        viewPager.adapter = pagerAdapter

        // Connect TabLayout and ViewPager
        tabLayout.setupWithViewPager(viewPager)
        pagerAdapter.setCounterPageItem(object : DeliveryViewPagerAdapter.CounterPageItem{
            override fun counterItem(count: Int, tabIndex: Int) {
                if (tabIndex == 0) tabLayout.getTabAt(tabIndex)?.text = "Berlangsung${if (count != 0) " ($count)" else ""}"
                else tabLayout.getTabAt(tabIndex)?.text = "Selesai${if (count != 0) " ($count)" else ""}"
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
}