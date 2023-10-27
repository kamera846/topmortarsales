package com.topmortar.topmortarsales.adapter.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.topmortar.topmortarsales.view.user.UserOnGoingStoreFragment
import com.topmortar.topmortarsales.view.user.UserVisitedStoreFragment

class UserProfileViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private var userCityParam: String? = ""
    fun setUserCityParam(id: String?) {
        this.userCityParam = id
    }

    private var userIDParam: String? = ""
    fun setUserIdParam(id: String?) {
        this.userIDParam = id
    }

    private var listener: CounterPageItem? = null
    interface CounterPageItem {
        fun counterItem(count: Int, tabIndex: Int)
    }
    fun setCounterPageItem(listener: CounterPageItem) {
        this.listener = listener
    }

    override fun getItem(position: Int): Fragment {
        // Return a Fragment for each tab position
        return when (position) {
            0 -> {

                val fragment = UserOnGoingStoreFragment()
                fragment.setUserCityParam(userCityParam)
                fragment.setUserIdParam(userIDParam)
                fragment.setCounterItem(object : UserOnGoingStoreFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, 0)
                    }

                })
                return fragment

            } else -> {

                val fragment = UserVisitedStoreFragment()
                fragment.setUserCityParam(userCityParam)
                fragment.setUserIdParam(userIDParam)
                fragment.setCounterItem(object : UserVisitedStoreFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, 1)
                    }

                })
                return fragment

            }
        }
    }

    override fun getCount(): Int {
        // Return the total number of tabs
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        // Set tab titles
        return when (position) {
            0 -> "On Bidding"
            else -> "Visited"
        }
    }
}