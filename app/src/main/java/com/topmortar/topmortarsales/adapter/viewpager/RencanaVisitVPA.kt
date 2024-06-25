package com.topmortar.topmortarsales.adapter.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.topmortar.topmortarsales.commons.SALES_REPORT_RENVI
import com.topmortar.topmortarsales.view.rencanaVisits.JatemFragment
import com.topmortar.topmortarsales.view.rencanaVisits.PasifRenViFragment
import com.topmortar.topmortarsales.view.rencanaVisits.TagihMingguanFragment
import com.topmortar.topmortarsales.view.rencanaVisits.VoucherRenViFragment

class RencanaVisitVPA(fm: FragmentManager, private var tabSize: Int) : FragmentPagerAdapter(fm) {

    private var listener: CounterPageItem? = null
    interface CounterPageItem {
        fun counterItem(count: Int, tabIndex: Int)
    }
    fun setCounterPageItem(listener: CounterPageItem) {
        this.listener = listener
    }
    private lateinit var frgamentJatem: JatemFragment
    private lateinit var fragmentVoucherRenVi: VoucherRenViFragment
    private lateinit var fragmentPasifRenViFragment: PasifRenViFragment
    private lateinit var fragmentWeekly: TagihMingguanFragment
    fun setSyncAction(index: Int) {
        when (index) {
            0 -> frgamentJatem.syncNow()
            1 -> fragmentVoucherRenVi.syncNow()
            2 -> fragmentPasifRenViFragment.syncNow()
            3 -> fragmentWeekly.syncNow()
        }
    }
    fun setSelectBarActive(index: Int, state: Boolean) {
        when (index) {
            0 -> frgamentJatem.isSelectBarActive(state)
            1 -> fragmentVoucherRenVi.isSelectBarActive(state)
            2 -> fragmentPasifRenViFragment.isSelectBarActive(state)
            3 -> fragmentWeekly.isSelectBarActive(state)
        }
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {

                frgamentJatem = JatemFragment()
                frgamentJatem.setCounterItem(object : JatemFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, position)
                    }

                })
                return frgamentJatem

            }
            1 -> {

                fragmentVoucherRenVi = VoucherRenViFragment()
                fragmentVoucherRenVi.setCounterItem(object : VoucherRenViFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, position)
                    }

                })
                return fragmentVoucherRenVi

            }
            2 -> {

                fragmentPasifRenViFragment = PasifRenViFragment()
                fragmentPasifRenViFragment.setCounterItem(object : PasifRenViFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, position)
                    }

                })
                return fragmentPasifRenViFragment

            } else -> {

                fragmentWeekly = TagihMingguanFragment()
                fragmentWeekly.setReportSource(SALES_REPORT_RENVI)
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