package com.topmortar.topmortarsales.view.tukang

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.databinding.ActivityListTukangBinding

class ListTukangActivity : AppCompatActivity() {

    private var _binding: ActivityListTukangBinding? = null
    private lateinit var sessionManager: SessionManager
    private val userKind get() = sessionManager.userKind()
    private val userID get() = sessionManager.userID()
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)
        _binding = ActivityListTukangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBarDark.tvTitleBar.text = "Daftar Tukang"
        binding.titleBarDark.icBack.setOnClickListener { finish() }

        /*
        Call Fragment
         */
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val myFragment = TukangFragment()
//        if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_ADMIN_CITY) myFragment.setUserID(intent.getStringExtra(CONST_USER_ID)) else myFragment.setUserID(userID)
        if (userKind != USER_KIND_ADMIN) {
            binding.titleBarDark.tvTitleBarDescription.visibility = View.VISIBLE
            myFragment.setCounterItem(object : TukangFragment.CounterItem {
                override fun counterItem(count: Int) {
                    binding.titleBarDark.tvTitleBarDescription.text = "Total $count data"
                }

            })
        }
        fragmentTransaction.replace(R.id.listTukangFragmentContainer, myFragment)
        fragmentTransaction.addToBackStack(null)

        binding.titleBarDark.icSyncNow.setOnClickListener { myFragment.syncNow() }

        fragmentTransaction.commit()
        /*
        End Call Fragment
         */
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        finish()
    }

}