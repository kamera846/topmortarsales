@file:Suppress("DEPRECATION")

package com.topmortar.topmortarsales.adapter.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.topmortar.topmortarsales.view.delivery.DeliveryEndedFragment
import com.topmortar.topmortarsales.view.delivery.DeliveryProgressFragment
import com.topmortar.topmortarsales.view.delivery.DeliveryTargetFragment

class DeliveryViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private var listener: CounterPageItem? = null
    interface CounterPageItem {
        fun counterItem(count: Int, tabIndex: Int)
    }
    fun setCounterPageItem(listener: CounterPageItem) {
        this.listener = listener
    }
    private lateinit var targetFragment: DeliveryTargetFragment
    private lateinit var progressFragment: DeliveryProgressFragment
    private lateinit var endedFragment: DeliveryEndedFragment
    fun setSyncAction(index: Int) {
        when (index) {
            0 -> targetFragment.syncNow()
//            1 -> progressFragment.syncNow()
//            2 -> endedFragment.syncNow()
            1 -> endedFragment.syncNow()
            else -> targetFragment.syncNow()
        }
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {

                targetFragment = DeliveryTargetFragment()
                targetFragment.setCounterItem(object : DeliveryTargetFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, 0)
                    }

                })
                targetFragment

            } 1 -> {

                endedFragment = DeliveryEndedFragment()
                endedFragment.setCounterItem(object : DeliveryEndedFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, 1)
                    }

                })
                endedFragment

            } else -> {

                targetFragment = DeliveryTargetFragment()
                targetFragment.setCounterItem(object : DeliveryTargetFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, 0)
                    }

                })
                targetFragment

            }
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Target"
            1 -> "Riwayat"
            else -> "Target"
        }
    }
}