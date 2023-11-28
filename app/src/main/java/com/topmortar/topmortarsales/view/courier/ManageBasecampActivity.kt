package com.topmortar.topmortarsales.view.courier

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.databinding.ActivityManageBasecampBinding
import com.topmortar.topmortarsales.databinding.FragmentBasecampBinding

class ManageBasecampActivity : AppCompatActivity() {

    private var _binding: ActivityManageBasecampBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        _binding = ActivityManageBasecampBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBarDark.tvTitleBar.text = "Kelola Basecamp"
        binding.titleBarDark.icBack.setOnClickListener { finish() }

        /*
        Call Fragment
         */
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val myFragment = BasecampFragment()
        fragmentTransaction.replace(R.id.basecampFragmentContainer, myFragment)
        fragmentTransaction.addToBackStack(null)

        fragmentTransaction.commit()
        /*
        End Call Fragment
         */
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}