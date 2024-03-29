package com.topmortar.topmortarsales.adapter.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.topmortar.topmortarsales.view.rencanaVisits.JatemFragment
import com.topmortar.topmortarsales.view.rencanaVisits.PasifRenViFragment
import com.topmortar.topmortarsales.view.rencanaVisits.VoucherRenViFragment

class RencanaVisitVPA(fm: FragmentManager) : FragmentPagerAdapter(fm) {

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
    fun setSyncAction(index: Int) {
        when (index) {
            0 -> frgamentJatem.syncNow()
            1 -> fragmentVoucherRenVi.syncNow()
            2 -> fragmentPasifRenViFragment.syncNow()
        }
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {

                frgamentJatem = JatemFragment()
                frgamentJatem.setCounterItem(object : JatemFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, 0)
                    }

                })
                return frgamentJatem

            }
            1 -> {

                fragmentVoucherRenVi = VoucherRenViFragment()
                fragmentVoucherRenVi.setCounterItem(object : VoucherRenViFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, 1)
                    }

                })
                return fragmentVoucherRenVi

            } else -> {

                fragmentPasifRenViFragment = PasifRenViFragment()
                fragmentPasifRenViFragment.setCounterItem(object : PasifRenViFragment.CounterItem{
                    override fun counterItem(count: Int) {
                        listener?.counterItem(count, 2)
                    }

                })
                return fragmentPasifRenViFragment

            }
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Jatuh Tempo"
            1 -> "Voucher"
            else -> "Pasif"
        }
    }
}