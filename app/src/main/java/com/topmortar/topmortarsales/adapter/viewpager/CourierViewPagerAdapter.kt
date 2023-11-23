package com.topmortar.topmortarsales.adapter.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.topmortar.topmortarsales.view.courier.ClosingStoreFragment
import com.topmortar.topmortarsales.view.courier.GudangFragment

class CourierViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private var listener: CounterPageItem? = null
    interface CounterPageItem {
        fun counterItem(count: Int, tabIndex: Int)
    }
    fun setCounterPageItem(listener: CounterPageItem) {
        this.listener = listener
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {

                val fragment = ClosingStoreFragment()
                fragment.setCounterItem(object : ClosingStoreFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, 0)
                    }

                })
                return fragment

            } else -> {

                val fragment = GudangFragment()
                fragment.setCounterItem(object : GudangFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, 1)
                    }

                })
                return fragment

            }
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Toko"
            else -> "Gudang"
        }
    }
}