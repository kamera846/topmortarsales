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

    override fun getItem(position: Int): Fragment {
        // Return a Fragment for each tab position
        return when (position) {
            0 -> {

                val fragment = UserOnGoingStoreFragment()
                fragment.setUserCityParam(userCityParam)
                return fragment

            } else -> {

                val fragment = UserVisitedStoreFragment()
                fragment.setUserCityParam(userCityParam)
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
            0 -> "On Going"
            else -> "Visited (85)"
        }
    }
}