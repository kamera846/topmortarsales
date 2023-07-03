package com.topmortar.topmortarsales

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var ivLogo: ImageView
    private lateinit var rlModal: LinearLayout
    private lateinit var btnLogin: Button
    private lateinit var icEyeContainer: RelativeLayout
    private lateinit var icEyeShow: ImageView
    private lateinit var icEyeClose: ImageView
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText

    private val splashScreenDuration = 2000L
    private var isPasswordShow = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        setContentView(R.layout.activity_splash_screen)

        initVariable()
        initClickHandler()

        Handler().postDelayed({

            val logoParams = ivLogo.layoutParams as RelativeLayout.LayoutParams
            logoParams.addRule(RelativeLayout.CENTER_VERTICAL, 0)
            logoParams.height = 500
            ivLogo.layoutParams = logoParams
            rlModal.visibility = View.VISIBLE

        }, splashScreenDuration)

    }

    private fun initVariable() {

        ivLogo = findViewById(R.id.logo)
        rlModal = findViewById(R.id.card_auth)
        btnLogin = findViewById(R.id.btn_login)
        icEyeContainer = findViewById(R.id.ic_eye_container)
        icEyeShow = findViewById(R.id.ic_eye_show)
        icEyeClose = findViewById(R.id.ic_eye_close)
        etEmail = findViewById(R.id.et_email_address)
        etPassword = findViewById(R.id.et_password)

        setPasswordState()

    }

    private fun initClickHandler() {

        btnLogin.setOnClickListener { navigateToMain() }
        icEyeContainer.setOnClickListener { togglePassword() }

    }

    private fun togglePassword() {

        isPasswordShow = !isPasswordShow
        setPasswordState()

    }

    private fun setPasswordState() {

        if (isPasswordShow) {

            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            icEyeClose.visibility = View.GONE
            icEyeShow.visibility = View.VISIBLE

        } else {

            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            icEyeClose.visibility = View.VISIBLE
            icEyeShow.visibility = View.GONE

        }

        // Set kursor ke posisi terakhir
        etPassword.setSelection(etPassword.text.length)

    }

    private fun navigateToMain() {

        val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
        startActivity(intent)
        finish()

    }

}