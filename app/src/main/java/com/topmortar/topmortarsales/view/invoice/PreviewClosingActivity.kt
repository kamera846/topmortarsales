package com.topmortar.topmortarsales.view.invoice

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.CONST_INVOICE_ID
import com.topmortar.topmortarsales.commons.CONST_URI
import com.topmortar.topmortarsales.commons.IMG_PREVIEW_STATE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_SUCCESS
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

class PreviewClosingActivity : AppCompatActivity() {

    private lateinit var icBack: ImageView
    private lateinit var txtTitleBar: TextView

    private lateinit var imgPreview: ImageView
    private lateinit var bottomAction: LinearLayout
    private lateinit var btnUpload: Button

    private lateinit var sessionManager: SessionManager

    private var isLoading: Boolean = false
    private var invoiceId: String? = null
    private var imagePart: MultipartBody.Part? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)

        setContentView(R.layout.activity_preview_closing)

        initVariable()
        initClickHandler()
        dataActivityValidation()

    }

    private fun executeClosing() {
        loadingState(true)
        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.closingInvoice(invoiceId = createPartFromString(invoiceId!!), image = imagePart!!)

                when (response.status) {
                    RESPONSE_STATUS_OK, RESPONSE_STATUS_SUCCESS -> {

                        createInvoice()

                    }
                    else -> {

                        handleMessage(this@PreviewClosingActivity, TAG_RESPONSE_CONTACT, "Failed to closing. Err: ${response.message}")
                        loadingState(true)

                    }
                }

            } catch (e: Exception) {

                handleMessage(this@PreviewClosingActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true)

            }

        }

    }

    private fun createInvoice() {
        loadingState(true)
        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.addInvoice(invoiceId = createPartFromString(invoiceId!!))

                when (response.status) {
                    RESPONSE_STATUS_OK, RESPONSE_STATUS_SUCCESS -> {

                        val resultIntent = Intent()
                        resultIntent.putExtra("$IMG_PREVIEW_STATE", RESULT_OK)
                        setResult(RESULT_OK, resultIntent)

                        handleMessage(this@PreviewClosingActivity, TAG_RESPONSE_CONTACT, response.message)
                        Handler().postDelayed({
                            loadingState(false)
                            finish()
                        }, 1000)

                    }
                    else -> {

                        handleMessage(this@PreviewClosingActivity, TAG_RESPONSE_CONTACT, "Failed to closing. Err: ${response.message}")
                        loadingState(true)

                    }
                }

            } catch (e: Exception) {

                handleMessage(this@PreviewClosingActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true)

            }

        }

    }

    private fun initVariable() {
        icBack = findViewById(R.id.ic_back)
        txtTitleBar = findViewById(R.id.tv_title_bar)

        imgPreview = findViewById(R.id.img_preview)
        bottomAction = findViewById(R.id.btn_bottom_action)
        btnUpload = bottomAction.findViewById(R.id.action1)

        // Setup Title Bar
        txtTitleBar.text = "Preview Closing"

        // Set Button Action
        if (sessionManager.userKind() == USER_KIND_ADMIN) bottomAction.visibility = View.GONE

    }

    private fun initClickHandler() {
        icBack.setOnClickListener { finish() }
        btnUpload.setOnClickListener { executeClosing() }

    }

    private fun dataActivityValidation() {
        val invoiceId = intent.getStringExtra(CONST_INVOICE_ID)
        if (!invoiceId.isNullOrEmpty()) {
            this.invoiceId = invoiceId
        }
        val imgUri = intent.getParcelableArrayListExtra<Uri>(CONST_URI)
        if (!imgUri.isNullOrEmpty()) {
            val contentResolver = contentResolver
            val inputStream = contentResolver.openInputStream(imgUri[0])
            val byteArray = inputStream?.readBytes()

            if (byteArray != null) {
                val requestFile: RequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), byteArray)
                imagePart = MultipartBody.Part.createFormData("pic", "image.jpg", requestFile)
                imgPreview.setImageURI(imgUri[0])
            } else handleMessage(this, TAG_RESPONSE_CONTACT, "Image not located")
        }

    }

    private fun loadingState(state: Boolean) {

        if (state) {
            isLoading = true
            btnUpload.text = "Loading..."

        } else {
            isLoading = false
            btnUpload.text = "Closing Now"

        }

    }

    private fun onBackHandler() {
        if (!isLoading) {
            finish()
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Wait a moment. The closing progress is still ongoing.")
                .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        onBackHandler()
    }
}