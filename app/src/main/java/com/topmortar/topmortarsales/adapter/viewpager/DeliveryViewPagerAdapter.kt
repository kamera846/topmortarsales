package com.topmortar.topmortarsales.adapter.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.topmortar.topmortarsales.view.delivery.DeliveryEndedFragment
import com.topmortar.topmortarsales.view.delivery.DeliveryProgressFragment

class DeliveryViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private var listener: CounterPageItem? = null
    interface CounterPageItem {
        fun counterItem(count: Int, tabIndex: Int)
    }
    fun setCounterPageItem(listener: CounterPageItem) {
        this.listener = listener
    }
    private lateinit var progressFragment: DeliveryProgressFragment
    private lateinit var endedFragment: DeliveryEndedFragment
    fun setSyncAction(index: Int) {
        if (index == 0) progressFragment.syncNow()
        else if (index == 1) endedFragment.syncNow()
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {

                progressFragment = DeliveryProgressFragment()
                progressFragment.setCounterItem(object : DeliveryProgressFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, 0)
                    }

                })
                return progressFragment

            } else -> {

                endedFragment = DeliveryEndedFragment()
                endedFragment.setCounterItem(object : DeliveryEndedFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, 1)
                    }

                })
                return endedFragment

            }
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Berlangsung"
            else -> "Selesai"
        }
    }
}