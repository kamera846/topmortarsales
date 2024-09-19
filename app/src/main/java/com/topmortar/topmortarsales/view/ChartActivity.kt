package com.topmortar.topmortarsales.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.databinding.ActivityChartBinding

class ChartActivity : AppCompatActivity() {

    private var _binding: ActivityChartBinding? = null
    private lateinit var sessionManager: SessionManager
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)
        _binding = ActivityChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBarDark.tvTitleBar.text = "Data Grafik"
        binding.titleBarDark.icBack.setOnClickListener { finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}