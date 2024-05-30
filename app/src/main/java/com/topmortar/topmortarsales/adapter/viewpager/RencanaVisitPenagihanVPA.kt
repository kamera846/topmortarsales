package com.topmortar.topmortarsales.adapter.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.topmortar.topmortarsales.commons.PENAGIHAN_REPORT_RENVI
import com.topmortar.topmortarsales.view.rencanaVisits.JatemPenagihan1Fragment
import com.topmortar.topmortarsales.view.rencanaVisits.JatemPenagihan2Fragment
import com.topmortar.topmortarsales.view.rencanaVisits.JatemPenagihan3Fragment
import com.topmortar.topmortarsales.view.rencanaVisits.TagihMingguanFragment

class RencanaVisitPenagihanVPA(fm: FragmentManager, private var tabSize: Int) : FragmentPagerAdapter(fm) {

    private var listener: CounterPageItem? = null
    interface CounterPageItem {
        fun counterItem(count: Int, tabIndex: Int)
    }
    fun setCounterPageItem(listener: CounterPageItem) {
        this.listener = listener
    }
    private lateinit var fragmentJatem1: JatemPenagihan1Fragment
    private lateinit var fragmentJatem2: JatemPenagihan2Fragment
    private lateinit var fragmentJatem3: JatemPenagihan3Fragment
    private lateinit var fragmentWeekly: TagihMingguanFragment
    fun setSyncAction(index: Int) {
        when (index) {
            0 -> fragmentJatem1.syncNow()
            1 -> fragmentJatem2.syncNow()
            2 -> fragmentJatem3.syncNow()
            3 -> fragmentWeekly.syncNow()
        }
    }

    fun clearData() {
        fragmentJatem1 = JatemPenagihan1Fragment()
        fragmentJatem2 = JatemPenagihan2Fragment()
        fragmentJatem3 = JatemPenagihan3Fragment()
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {

                fragmentJatem1 = JatemPenagihan1Fragment()
                fragmentJatem1.setCounterItem(object : JatemPenagihan1Fragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, position)
                    }

                })
                return fragmentJatem1

            }
            1 -> {

                fragmentJatem2 = JatemPenagihan2Fragment()
                fragmentJatem2.setCounterItem(object : JatemPenagihan2Fragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, position)
                    }

                })
                return fragmentJatem2

            }
            2 -> {

                fragmentJatem3 = JatemPenagihan3Fragment()
                fragmentJatem3.setCounterItem(object : JatemPenagihan3Fragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, position)
                    }

                })
                return fragmentJatem3

            }
            else -> {

                fragmentWeekly = TagihMingguanFragment()
                fragmentWeekly.setReportSource(PENAGIHAN_REPORT_RENVI)
                fragmentWeekly.setCounterItem(object : TagihMingguanFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, position)
                    }

                })
                return fragmentWeekly

            }
        }
    }

    override fun getCount(): Int {
        return tabSize
    }
}