package com.topmortar.topmortarsales.view.courier

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.databinding.ActivityManageBasecampBinding

class ManageBasecampActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageBasecampBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge()

        binding = ActivityManageBasecampBinding.inflate(layoutInflater)
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

}