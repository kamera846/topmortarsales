package com.topmortar.topmortarsales.view.rencanaVisits

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.databinding.ActivityScheduledRenviBinding
import com.topmortar.topmortarsales.view.MapsActivity
import com.topmortar.topmortarsales.view.SendMessageActivity

class ScheduledRenviActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduledRenviBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        applyMyEdgeToEdge()
        binding = ActivityScheduledRenviBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        setupTitleBar()
        setupListener()
        setupLauncher()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun setupTitleBar() {
        binding.titleBar.tvTitleBar.text = "Rencana Visit Terjadwal"
    }

    private fun setupListener() {
        binding.titleBar.icBack.setOnClickListener { finish() }
        binding.buttonDetailToko.setOnClickListener { navigateToStoreDetail() }
        binding.buttonChat.setOnClickListener { navigateToOpenChat() }
        binding.buttonRute.setOnClickListener { navigateToMaps() }
        binding.buttonBuatLaporan.setOnClickListener { navigateToCreateReport() }
        binding.buttonPilihTargetSekarang.setOnClickListener { navigateToSelectTarget() }
        binding.buttonPilihTargetLain.setOnClickListener { navigateToSelectTarget() }
    }

    private fun setupLauncher() {
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                // Handle the result data here
            }
        }
    }

    private fun navigateToStoreDetail() {

    }

    private fun navigateToOpenChat() {
        val intent = Intent(this, SendMessageActivity::class.java)
        resultLauncher.launch(intent)
    }

    private fun navigateToMaps() {
        val intent = Intent(this, MapsActivity::class.java)
        resultLauncher.launch(intent)
    }

    private fun navigateToCreateReport() {

    }

    private fun navigateToSelectTarget() {

    }
}