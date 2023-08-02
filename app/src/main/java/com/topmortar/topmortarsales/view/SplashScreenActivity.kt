package com.topmortar.topmortarsales.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_ADMIN
import com.topmortar.topmortarsales.commons.LOGGED_IN
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var rlModal: LinearLayout
    private lateinit var cardAlert: LinearLayout
    private lateinit var inputAuth: LinearLayout
    private lateinit var inputOtp: LinearLayout
    private lateinit var inputNewPassword: RelativeLayout
    private lateinit var icEyeContainer: RelativeLayout
    private lateinit var btnLogin: Button
    private lateinit var ivLogo: ImageView
    private lateinit var icEyeShow: ImageView
    private lateinit var icEyeClose: ImageView
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etPhone: EditText
    private lateinit var etOtp1: EditText
    private lateinit var etOtp2: EditText
    private lateinit var etOtp3: EditText
    private lateinit var etOtp4: EditText
    private lateinit var etOtp5: EditText
    private lateinit var etOtp6: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var tvAlert: TextView
    private lateinit var tvResetPassword: TextView
    private lateinit var tvTitleAuth: TextView

    private lateinit var sessionManager: SessionManager

    private val splashScreenDuration = 2000L
    private var isPasswordShow = false
    private var currentResetPasswordStep = 0 // Reset Password False
    private val resetPasswordStep1 = 1 // Input Phone Number
    private val resetPasswordStep2 = 2 // Input OTP Code
    private val resetPasswordStep3 = 3 // Input New Password

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this@SplashScreenActivity)

        setContentView(R.layout.activity_splash_screen)

        initVariable()
        initClickHandler()

        Handler().postDelayed({

            checkSession()

        }, splashScreenDuration)

    }

    private fun initVariable() {

        inputAuth = findViewById(R.id.input_auth)
        inputOtp = findViewById(R.id.input_otp)
        inputNewPassword = findViewById(R.id.input_new_password)

        rlModal = findViewById(R.id.card_auth)
        cardAlert = findViewById(R.id.card_alert)

        btnLogin = findViewById(R.id.btn_login)

        ivLogo = findViewById(R.id.logo)

        icEyeContainer = findViewById(R.id.ic_eye_container)
        icEyeShow = findViewById(R.id.ic_eye_show)
        icEyeClose = findViewById(R.id.ic_eye_close)

        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        etPhone = findViewById(R.id.et_phone)

        tvTitleAuth = findViewById(R.id.tv_title_auth)
        tvAlert = findViewById(R.id.tv_alert)
        tvResetPassword = findViewById(R.id.tv_reset_password)

        // Set default input type ke password
        etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
        etPassword.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD

        setPasswordState()

    }

    private fun initClickHandler() {

        btnLogin.setOnClickListener { btnLoginHandler() }
        icEyeContainer.setOnClickListener { togglePassword() }
        tvResetPassword.setOnClickListener { resetPasswordHandler() }

    }

    private fun togglePassword() {

        isPasswordShow = !isPasswordShow
        setPasswordState()

    }

    private fun setPasswordState() {

        if (isPasswordShow) {

            etPassword.transformationMethod = null
            etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            icEyeClose.visibility = View.GONE
            icEyeShow.visibility = View.VISIBLE

        } else {

            etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            etPassword.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
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

    private fun checkSession() {

        val isLoggedIn = sessionManager.isLoggedIn()
        val userId = sessionManager.userID()!!
        val userCity = sessionManager.userCityID()!!
        val userKind = sessionManager.userKind()!!

        if (!isLoggedIn || userId.isEmpty() || userCity.isEmpty() || userKind.isEmpty()) showCardLogin()
        else navigateToMain()

    }

    private fun showCardLogin() {

//        val layoutParams = ivLogo.layoutParams
//        layoutParams.height = resources.getDimensionPixelSize(R.dimen.splashscreen_logo_height)
//        ivLogo.layoutParams = layoutParams
        rlModal.visibility = View.VISIBLE

    }

    private fun showAlert(message: String, duration: Long = 2000) {

        cardAlert.visibility = View.VISIBLE
        tvAlert.text = message

        Handler().postDelayed({

            cardAlert.visibility = View.GONE
            tvAlert.text = ""

        }, duration)

    }

    private fun loginHandler() {

        val username = "${ etUsername.text }"
        val password = "${ etPassword.text }"

        if (TextUtils.isEmpty(username)) {
            etUsername.requestFocus()
            etUsername.error = "Username cannot be empty!"
            return
        } else if (TextUtils.isEmpty(password)) {
            etUsername.clearFocus()
            etPassword.requestFocus()
            etPassword.error = "Password cannot be empty!"
            return
        } else if (password.length < 8) {
            etUsername.clearFocus()
            etPassword.requestFocus()
            etPassword.error = "Minimum password is 8 characters!"
            return
        } else {
            etUsername.clearFocus()
            etPassword.clearFocus()
        }

        lifecycleScope.launch {
            try {

                val rbUsername = createPartFromString("${ etUsername.text }")
                val rbPassword = createPartFromString("${ etPassword.text }")

                val apiService: ApiService = HttpClient.create()
                val response = apiService.auth(rbUsername, rbPassword)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        val results = response.results
                        val data = results[0]

                        if (data.level_user == AUTH_LEVEL_ADMIN ) sessionManager.setUserKind(USER_KIND_ADMIN)
                        else sessionManager.setUserKind(USER_KIND_SALES)

                        sessionManager.setUserID(data.id_user)
                        sessionManager.setUserName(data.username)
                        sessionManager.setUserCityID(data.id_city)

                        sessionManager.setLoggedIn(LOGGED_IN)
                        navigateToMain()

                    }
                    RESPONSE_STATUS_FAIL -> {

                        showAlert("Your username or password seems wrong!", 5000)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        showAlert("Your username or password seems wrong!", 5000)

                    }
                    else -> {

                        handleMessage(this@SplashScreenActivity, TAG_RESPONSE_CONTACT, "Failed process auth")

                    }
                }

            } catch (e: Exception) {

                handleMessage(this@SplashScreenActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)

            }
        }

    }

    private fun btnLoginHandler() {
        when (currentResetPasswordStep) {
            0 -> {
                loginHandler()
            }
            resetPasswordStep1 -> {
//                handleMessage(this, "RESET PASSWORD", "Get OTP Code")
                resetPasswordHandler()
            }
            resetPasswordStep2 -> {
//                handleMessage(this, "RESET PASSWORD", "Verify OTP Code")
                resetPasswordHandler()
            }
            else -> {
//                handleMessage(this, "RESET PASSWORD", "Successfully Reset Password!")
                resetPasswordHandler()
            }
        }
    }

    private fun resetPasswordHandler(next: Boolean = true) {
        if (next) currentResetPasswordStep += 1 else currentResetPasswordStep -= 1
        when (currentResetPasswordStep) {
            1 -> {
                inputAuth.visibility = View.GONE
                inputOtp.visibility = View.GONE
                inputNewPassword.visibility = View.GONE
                etPhone.visibility = View.VISIBLE
                tvTitleAuth.text = "Reset Password"
                btnLogin.text = "Get OTP Code"
            }
            2 -> {
                inputAuth.visibility = View.GONE
                etPhone.visibility = View.GONE
                inputNewPassword.visibility = View.GONE
                inputOtp.visibility = View.VISIBLE
                tvTitleAuth.text = "Input OTP Code"
                btnLogin.text = "Verify OTP Code"
            }
            3 -> {
                inputAuth.visibility = View.GONE
                etPhone.visibility = View.GONE
                inputOtp.visibility = View.GONE
                inputNewPassword.visibility = View.VISIBLE
                tvTitleAuth.text = "Input New Password"
                btnLogin.text = "Reset Password Now"
            }
            else -> {
                inputNewPassword.visibility = View.GONE
                inputOtp.visibility = View.GONE
                etPhone.visibility = View.GONE
                inputAuth.visibility = View.VISIBLE
                tvTitleAuth.text = "Hey, \nLogin Now"
                btnLogin.text = "Login"
                currentResetPasswordStep = 0
            }
        }

    }

    override fun onBackPressed() {
        if (currentResetPasswordStep > 0) {
            resetPasswordHandler(next = false)
        } else {
            super.onBackPressed()
        }
    }

}