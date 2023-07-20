package com.topmortar.topmortarsales

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
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_ADMIN
import com.topmortar.topmortarsales.commons.DUMMY_ADMIN_PASSWORD
import com.topmortar.topmortarsales.commons.DUMMY_ADMIN_USERNAME
import com.topmortar.topmortarsales.commons.DUMMY_SALES_PASSWORD
import com.topmortar.topmortarsales.commons.DUMMY_SALES_USERNAME
import com.topmortar.topmortarsales.commons.LOGGED_IN
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.model.ModalSearchModel
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var rlModal: LinearLayout
    private lateinit var cardAlert: LinearLayout
    private lateinit var icEyeContainer: RelativeLayout
    private lateinit var btnLogin: Button
    private lateinit var ivLogo: ImageView
    private lateinit var icEyeShow: ImageView
    private lateinit var icEyeClose: ImageView
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvAlert: TextView

    private lateinit var sessionManager: SessionManager

    private val splashScreenDuration = 2000L
    private var isPasswordShow = false

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

        ivLogo = findViewById(R.id.logo)
        rlModal = findViewById(R.id.card_auth)
        btnLogin = findViewById(R.id.btn_login)
        icEyeContainer = findViewById(R.id.ic_eye_container)
        icEyeShow = findViewById(R.id.ic_eye_show)
        icEyeClose = findViewById(R.id.ic_eye_close)
        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        cardAlert = findViewById(R.id.card_alert)
        tvAlert = findViewById(R.id.tv_alert)

        // Set default input type ke password
        etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
        etPassword.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD

        setPasswordState()

    }

    private fun initClickHandler() {

        btnLogin.setOnClickListener { loginHandler() }
        icEyeContainer.setOnClickListener { togglePassword() }

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

        if (sessionManager.isLoggedIn()) navigateToMain()
        else showCardLogin()

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

}