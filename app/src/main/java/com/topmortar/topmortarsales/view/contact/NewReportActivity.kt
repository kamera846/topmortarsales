package com.topmortar.topmortarsales.view.contact

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.databinding.ActivityNewReportBinding

class NewReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initContent()
        initClickHandler()
    }

    private fun initContent() {
        loadingContent(true)

        Handler().postDelayed({
            loadingContent(false)
        }, 2000)
    }

    private fun initClickHandler() {
        binding.btnReport.setOnClickListener { submitValidation() }
    }

    private fun submitValidation() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Submit Confirmation")
            .setMessage("Are you sure to submit your report now?")
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Yes") { _, _ -> submitReport() }
            .create()

        dialog.show()
    }

    private fun submitReport() {
        loadingSubmit(true)

        Handler().postDelayed({
            loadingSubmit(false)
        }, 2000)
    }

    private fun loadingSubmit(state: Boolean) {
        if (state) {
            binding.btnReport.text = getString(R.string.txt_loading)
            binding.btnReport.isEnabled = false
        } else {
            binding.btnReport.text = "SUBMIT REPORT"
            binding.btnReport.isEnabled = true
        }
    }

    private fun loadingContent(state: Boolean) {
        if (state) {
            binding.tvLoading.visibility = View.VISIBLE
            binding.container.visibility = View.GONE
        } else {
            binding.tvLoading.visibility = View.GONE
            binding.container.visibility = View.VISIBLE
        }
    }
}