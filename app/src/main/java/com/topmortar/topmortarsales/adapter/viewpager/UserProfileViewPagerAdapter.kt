package com.topmortar.topmortarsales.adapter.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.topmortar.topmortarsales.view.user.UserOnGoingStoreFragment
import com.topmortar.topmortarsales.view.user.UserVisitedStoreFragment

class UserProfileViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // Return a Fragment for each tab position
        return when (position) {
            0 -> UserOnGoingStoreFragment()
            else -> UserVisitedStoreFragment()
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