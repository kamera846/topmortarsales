package com.topmortar.topmortarsales.view

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.zxing.Result
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.utils.CustomProgressBar
import com.topmortar.topmortarsales.commons.utils.ResponseMessage.generateFailedRunServiceMessage
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityScannerBinding
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class ScannerActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var binding: ActivityScannerBinding
    private lateinit var codeScanner: CodeScanner
    private lateinit var progressBar: CustomProgressBar

    private val userId get() = sessionManager.userID()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBar = CustomProgressBar(this)
        progressBar.setMessage(getString(R.string.txt_loading))
        progressBar.setCancelable(false)

        if (Build.VERSION.SDK_INT >= 30) {
            binding.scannerView.windowInsetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        }

        binding.icBack.setOnClickListener { finish() }

        val scannerView = binding.scannerView

        codeScanner = CodeScanner(this, scannerView)

        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.isAutoFocusEnabled = true
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isFlashEnabled = false

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                executeAssignTukang(it)
            }
        }
        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread {
                Toast.makeText(this, "Terjadi kesalahan: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun executeAssignTukang(scannResult: Result) {
        codeScanner.releaseResources()
        progressBar.show()

        var responseMessage = ""
        var isSuccess = false

        lifecycleScope.launch {
            try {
                val apiService = HttpClient.create()
                val response = apiService.assignTukang(
                    idUser = createPartFromString(userId ?: "-1"),
                    idMd5 = createPartFromString(scannResult.text)
                )
                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            isSuccess = true
                            responseMessage = responseBody.message

                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            responseMessage = responseBody.message

                        }
                        else -> {

                            responseMessage = "Gagal memindai qr tukang!"

                        }
                    }

                } else {

                    responseMessage = "Gagal memindai! " + response.message()

                }


            } catch (e: Exception) {

                if (e is CancellationException) {
                    return@launch
                }
                responseMessage = generateFailedRunServiceMessage(e.message.toString())

            } finally {

                progressBar.dismiss()
                val alertDialog = AlertDialog.Builder(this@ScannerActivity)
                alertDialog.setCancelable(false)
                alertDialog.setTitle("Pemindaian Selesai")
                alertDialog.setMessage(responseMessage)
                if (isSuccess) {
                    alertDialog.setPositiveButton("Oke") {
                            dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                } else {
                    alertDialog.setNegativeButton("Ulangi") {
                        dialog, _ ->
                        dialog.dismiss()
                        codeScanner.startPreview()
                    }
                    alertDialog.setPositiveButton("Keluar") {
                        dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                }

                alertDialog.show()

            }
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::progressBar.isInitialized && progressBar.isShowing()) {
            progressBar.dismiss()
        }
    }
}