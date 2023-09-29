package com.topmortar.topmortarsales.adapter.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.topmortar.topmortarsales.view.user.UserTargetsFragment

class UserProfileViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // Return a Fragment for each tab position
        return when (position) {
            0 -> UserTargetsFragment()
            else -> UserTargetsFragment()
        }
    }

    override fun getCount(): Int {
        // Return the total number of tabs
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        // Set tab titles
        return when (position) {
            0 -> "Targets"
            else -> "Visited (85)"
        }
    }
}