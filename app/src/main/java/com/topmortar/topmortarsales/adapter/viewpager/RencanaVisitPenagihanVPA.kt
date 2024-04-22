package com.topmortar.topmortarsales.adapter.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.topmortar.topmortarsales.view.rencanaVisits.JatemFragment
import com.topmortar.topmortarsales.view.rencanaVisits.PasifRenViFragment
import com.topmortar.topmortarsales.view.rencanaVisits.VoucherRenViFragment

class RencanaVisitPenagihanVPA(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private var listener: CounterPageItem? = null
    interface CounterPageItem {
        fun counterItem(count: Int, tabIndex: Int)
    }
    fun setCounterPageItem(listener: CounterPageItem) {
        this.listener = listener
    }
    private lateinit var fragmentJatem: JatemFragment
    private lateinit var fragmentVoucherRenVi: VoucherRenViFragment
    private lateinit var fragmentPasifRenVi: PasifRenViFragment
    fun setSyncAction(index: Int) {
        when (index) {
            0 -> fragmentJatem.syncNow()
            1 -> fragmentJatem.syncNow()
            2 -> fragmentJatem.syncNow()
            3 -> fragmentVoucherRenVi.syncNow()
            4 -> fragmentPasifRenVi.syncNow()
        }
    }

    fun clearData() {
        fragmentJatem = JatemFragment()
        fragmentVoucherRenVi = VoucherRenViFragment()
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {

                fragmentJatem = JatemFragment()
                fragmentJatem.setCounterItem(object : JatemFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, position)
                    }

                })
                return fragmentJatem

            }
            1 -> {

                fragmentJatem = JatemFragment()
                fragmentJatem.setCounterItem(object : JatemFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, position)
                    }

                })
                return fragmentJatem

            }
            2 -> {

                fragmentJatem = JatemFragment()
                fragmentJatem.setCounterItem(object : JatemFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, position)
                    }

                })
                return fragmentJatem

            } 3 -> {

                fragmentVoucherRenVi = VoucherRenViFragment()
                fragmentVoucherRenVi.setCounterItem(object : VoucherRenViFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, position)
                    }

                })
                return fragmentVoucherRenVi

            } else -> {

                fragmentPasifRenVi = PasifRenViFragment()
                fragmentPasifRenVi.setCounterItem(object : PasifRenViFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, position)
                    }

                })
                return fragmentPasifRenVi

            }
        }
    }

    override fun getCount(): Int {
        return 5
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Jatem 7"
            1 -> "Jatem 15"
            2 -> "Jatem 15+"
            3 -> "Voucher"
            else -> "Pasif"
        }
    }
}