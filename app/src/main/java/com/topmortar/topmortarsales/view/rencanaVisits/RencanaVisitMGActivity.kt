package com.topmortar.topmortarsales.view.rencanaVisits

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.databinding.ActivityRencanaVisitMgBinding

class RencanaVisitMGActivity : AppCompatActivity() {

    private var _binding: ActivityRencanaVisitMgBinding? = null
    private lateinit var sessionManager: SessionManager
    private val userId get() = sessionManager.userID()
    private val userDistributorId get() = sessionManager.userDistributor()
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)
        _binding = ActivityRencanaVisitMgBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBarDark.tvTitleBar.text = "Rencana Visit MG"
        binding.titleBarDark.icSyncNow.visibility = View.VISIBLE
        binding.titleBarDark.icBack.setOnClickListener { finish() }

        if (CustomUtility(this).isUserWithOnlineStatus()) {
            CustomUtility(this).setUserStatusOnline(
                true,
                userDistributorId ?: "-custom-011",
                userId ?: ""
            )
        }

        /*
        Call Fragment
         */
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val myFragment = MGFragment()
        fragmentTransaction.replace(R.id.renviMGFragmentContainer, myFragment)
        fragmentTransaction.addToBackStack(null)

        binding.titleBarDark.icSyncNow.setOnClickListener { myFragment.syncNow() }

        fragmentTransaction.commit()
        /*
        End Call Fragment
         */
    }

    override fun onStart() {
        super.onStart()
        Handler(Looper.getMainLooper()).postDelayed({
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    true,
                    userDistributorId ?: "-custom-011",
                    userId ?: ""
                )
            }
        }, 1000)
    }

    override fun onStop() {
        if (sessionManager.isLoggedIn()) {
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    userDistributorId ?: "-custom-011",
                    userId ?: ""
                )
            }
        }
        super.onStop()
    }

    override fun onDestroy() {
        if (sessionManager.isLoggedIn()) {
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    userDistributorId ?: "-custom-011",
                    userId ?: ""
                )
            }
        }
        _binding = null
        super.onDestroy()
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        finish()
    }

}