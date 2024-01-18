package com.topmortar.topmortarsales.view.suratJalan

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
import com.topmortar.topmortarsales.commons.CONST_DISTANCE
import com.topmortar.topmortarsales.commons.CONST_INVOICE_ID
import com.topmortar.topmortarsales.commons.CONST_INVOICE_IS_COD
import com.topmortar.topmortarsales.commons.CONST_URI
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_SUCCESS
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.services.TrackingService
import com.topmortar.topmortarsales.commons.utils.CompressImageUtil.compressImage
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.view.courier.CourierActivity
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
    private var imgUri: Uri? = null

    private var isLoading: Boolean = false
    private var isCod: Boolean = false
    private var invoiceId: String? = null
    private var imagePart: MultipartBody.Part? = null
    private var iDistance: Double = -1.0

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
                val response = apiService.closingInvoice(
                    invoiceId = createPartFromString(invoiceId!!),
                    distance = createPartFromString(iDistance.let { if (it >= 0) it.toString() else "" }),
                    image = imagePart!!
                )

                when (response.status) {
                    RESPONSE_STATUS_OK, RESPONSE_STATUS_SUCCESS -> {

                        if (!isCod) createInvoice()
                        else onClosingFinished(response.message)

                    }

                    else -> {

                        handleMessage(
                            this@PreviewClosingActivity,
                            TAG_RESPONSE_CONTACT,
                            "Gagal closing. Error: ${response.message}"
                        )
                        loadingState(false)

                    }
                }

            } catch (e: Exception) {

                handleMessage(
                    this@PreviewClosingActivity,
                    TAG_RESPONSE_CONTACT,
                    "Failed run service. Exception " + e.message
                )
                loadingState(false)

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

                        onClosingFinished(response.message)

                    }

                    else -> {

                        handleMessage(
                            this@PreviewClosingActivity,
                            TAG_RESPONSE_CONTACT,
                            "Gagal closing. Error: ${response.message}"
                        )
                        loadingState(false)

                    }
                }

            } catch (e: Exception) {

                handleMessage(
                    this@PreviewClosingActivity,
                    TAG_RESPONSE_CONTACT,
                    "Failed run service. Exception " + e.message
                )
                loadingState(false)

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
        if (sessionManager.userKind() == USER_KIND_COURIER) bottomAction.visibility = View.VISIBLE

    }

    private fun initClickHandler() {
        icBack.setOnClickListener { onBackHandler() }
//        btnUpload.setOnClickListener { onClosingFinished("Success to closing delivery!") }
        btnUpload.setOnClickListener { executeClosing() }

    }

    private fun dataActivityValidation() {
        val invoiceId = intent.getStringExtra(CONST_INVOICE_ID)
        isCod = intent.getBooleanExtra(CONST_INVOICE_IS_COD, false)
        iDistance = intent.getDoubleExtra(CONST_DISTANCE, -1.0)

        if (!invoiceId.isNullOrEmpty()) {
            this.invoiceId = invoiceId
        }

        val imgUris = intent.getParcelableArrayListExtra<Uri>(CONST_URI)

        if (!imgUris.isNullOrEmpty()) {

            imgPreview.setImageURI(imgUris[0])
            imgUri = compressImage(this@PreviewClosingActivity, imgUris[0], 50)

            val contentResolver = contentResolver
            val inputStream = contentResolver.openInputStream(imgUri!!)
            val byteArray = inputStream?.readBytes()

            if (byteArray != null) {
                val requestFile: RequestBody =
                    RequestBody.create("image/*".toMediaTypeOrNull(), byteArray)
                imagePart = MultipartBody.Part.createFormData("pic", "image.jpg", requestFile)
            } else handleMessage(this, TAG_RESPONSE_CONTACT, "Gambar tidak ditemukan")

        }

    }

    private fun loadingState(state: Boolean) {
        if (state) {
            isLoading = true
            btnUpload.text = getString(R.string.txt_loading)

        } else {
            isLoading = false
            btnUpload.text = "Closing Sekarang"

        }

    }

    private fun onBackHandler() {
        if (!isLoading) {
            finish()

        } else {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Tunggu sebentar. Proses closing masih berjalan.")
                .setPositiveButton("Oke") { dialog, _ -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()

        }

    }

    override fun onBackPressed() {
        onBackHandler()

    }

    private fun onClosingFinished(message: String) {

        val serviceIntent = Intent(this, TrackingService::class.java)
        this.stopService(serviceIntent)
        val intent = Intent(this, CourierActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

        handleMessage(
            this@PreviewClosingActivity,
            TAG_RESPONSE_CONTACT,
            message
        )

        Handler().postDelayed({
            loadingState(false)
            startActivity(intent)
            finish()
        }, 1000)

    }
}