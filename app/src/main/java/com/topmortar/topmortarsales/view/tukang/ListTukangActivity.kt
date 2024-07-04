package com.topmortar.topmortarsales.view.tukang

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.topmortar.topmortarsales.R
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
//        binding.titleBarDark.tvTitleBarDescription.text = intent.getStringExtra(CONST_FULL_NAME)
//        binding.titleBarDark.tvTitleBarDescription.visibility = View.VISIBLE
//        binding.titleBarDark.icSyncNow.visibility = View.VISIBLE
        binding.titleBarDark.icBack.setOnClickListener { finish() }

        /*
        Call Fragment
         */
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val myFragment = TukangFragment()
//        if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_ADMIN_CITY) myFragment.setUserID(intent.getStringExtra(CONST_USER_ID)) else myFragment.setUserID(userID)
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

    override fun onBackPressed() {
//        super.onBackPressed()
        finish()
    }
}