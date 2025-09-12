package com.topmortar.topmortarsales.view

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.databinding.ActivityWebviewBinding

class WebviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebviewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        applyMyEdgeToEdge()

        binding = ActivityWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBar.tvTitleBar.text = getString(R.string.app_name) + " Webview"
        binding.titleBar.icBack.setOnClickListener { myOnBackPressed() }

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.webChromeClient = WebChromeClient()

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.progressBar.visibility = View.VISIBLE
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.progressBar.visibility = View.GONE
                super.onPageFinished(view, url)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                view?.loadData(
                    "<html><body><h2>Terjadi kesalahan saat memuat halaman</h2></body></html>",
                    "text/html",
                    "UTF-8"
                )
            }
        }

        val urlToLoad = intent.getStringExtra("URL") ?: "https://unkwon-url"
        binding.webView.loadUrl(urlToLoad)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                myOnBackPressed()
            }

        })
    }

    private fun myOnBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            this@WebviewActivity.finish()
        }
    }
}