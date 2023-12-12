package com.topmortar.topmortarsales.adapter.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.topmortar.topmortarsales.view.courier.BasecampFragment
import com.topmortar.topmortarsales.view.tukang.TukangFragment

class BAViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private var listener: CounterPageItem? = null
    interface CounterPageItem {
        fun counterItem(count: Int, tabIndex: Int)
    }
    fun setCounterPageItem(listener: CounterPageItem) {
        this.listener = listener
    }
    private lateinit var fragmentClosing: TukangFragment
    private lateinit var fragmentBasecamp: BasecampFragment
    fun setSyncAction(index: Int) {
        if (index == 0) fragmentClosing.syncNow()
        else if (index == 1) fragmentBasecamp.syncNow()
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {

                fragmentClosing = TukangFragment()
                fragmentClosing.setCounterItem(object : TukangFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, 0)
                    }

                })
                return fragmentClosing

            } else -> {

                fragmentBasecamp = BasecampFragment()
                fragmentBasecamp.setCounterItem(object : BasecampFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, 1)
                    }

                })
                return fragmentBasecamp

            }
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Tukang"
            else -> "Basecamp"
        }
    }
}