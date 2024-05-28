package com.topmortar.topmortarsales.view.delivery

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.CONST_FULL_NAME
import com.topmortar.topmortarsales.commons.CONST_USER_ID
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN_CITY
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.databinding.ActivityHistoryDeliveryBinding

class HistoryDeliveryActivity : AppCompatActivity() {

    private var _binding: ActivityHistoryDeliveryBinding? = null
    private lateinit var sessionManager: SessionManager
    private val userKind get() = sessionManager.userKind()
    private val userID get() = sessionManager.userID()
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)
        _binding = ActivityHistoryDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBarDark.tvTitleBar.text = "Riwayat Pengiriman"
        binding.titleBarDark.tvTitleBarDescription.text = intent.getStringExtra(CONST_FULL_NAME)
        binding.titleBarDark.tvTitleBarDescription.visibility = View.VISIBLE
        binding.titleBarDark.icSyncNow.visibility = View.VISIBLE
        binding.titleBarDark.icBack.setOnClickListener { finish() }

        /*
        Call Fragment
         */
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val myFragment = HistoryDeliveryFragment()
        if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_ADMIN_CITY) myFragment.setUserID(intent.getStringExtra(CONST_USER_ID)) else myFragment.setUserID(userID)
        fragmentTransaction.replace(R.id.historyDeliveryFragmentContainer, myFragment)
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

    override fun onBackPressed() {
//        super.onBackPressed()
        finish()
    }
}