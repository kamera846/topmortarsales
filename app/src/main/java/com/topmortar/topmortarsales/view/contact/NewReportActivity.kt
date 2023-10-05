package com.topmortar.topmortarsales.view.contact

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.utils.CustomEtHandler
import com.topmortar.topmortarsales.commons.utils.CustomEtHandler.setMaxLength
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.databinding.ActivityNewReportBinding

class NewReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewReportBinding

    private val msgMaxLines = 5
    private val msgMaxLength = 300

    private var id: String = ""
    private var name: String = ""
    private var coordinate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        binding = ActivityNewReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initContent()
        initClickHandler()
    }

    private fun initContent() {
        loadingContent(true)

        etMessageListener()

        id = intent.getStringExtra(CONST_CONTACT_ID).toString()
        name = intent.getStringExtra(CONST_NAME).toString()
        coordinate = intent.getStringExtra(CONST_MAPS).toString()

        Handler().postDelayed({
            binding.titleBarLight.tvTitleBar.text = "Report Form"
            binding.titleBarLight.tvTitleBar.setPadding(0, 0, convertDpToPx(16, this), 0)

            binding.etName.text = name
            binding.etDistance.text = coordinate

            loadingContent(false)
        }, 2000)
    }

    private fun initClickHandler() {
        binding.titleBarLight.icBack.setOnClickListener { finish() }
        binding.btnReport.setOnClickListener { submitValidation() }
    }

    fun calculateDistance(view: View) {
        val etDistance = binding.etDistance
        val icRefreshDistance = binding.icRefreshDistance

        val progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Getting your distance...")

        progressDialog.show()

        Handler().postDelayed({
            progressDialog.dismiss()
            etDistance.text = "0.01"
            icRefreshDistance.visibility = View.VISIBLE
        }, 2000)
    }

    private fun etMessageListener() {
        val etMessage = binding.etMessage
        val tvMaxMessage = binding.tvMaxMessage

        etMessage.maxLines = msgMaxLines
        etMessage.setMaxLength(msgMaxLength)

        etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                CustomEtHandler.updateTxtMaxLength(
                    tvMaxMessage,
                    msgMaxLength,
                    etMessage.text.length
                )
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

    }

    private fun submitValidation() {

        if (!formValidation()) return

        val dialog = AlertDialog.Builder(this)
            .setTitle("Submit Confirmation")
            .setMessage("Are you sure to submit your report now?")
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Yes") { _, _ -> submitReport() }
            .create()

        dialog.show()
    }

    private fun formValidation(): Boolean {
        val message = binding.etMessage
        if (message.text.isNullOrEmpty()) {
            message.error = "The report message cannot be empty"
            message.requestFocus()
            return false
        }
        message.error = null
        message.clearFocus()
        return true
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