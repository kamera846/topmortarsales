package com.topmortar.topmortarsales.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_ADMIN
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_BA
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_COURIER
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_SALES
import com.topmortar.topmortarsales.commons.LOGGED_IN
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_BA
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.utils.KeyboardHandler
import com.topmortar.topmortarsales.commons.utils.KeyboardHandler.showKeyboard
import com.topmortar.topmortarsales.commons.utils.PhoneHandler
import com.topmortar.topmortarsales.commons.utils.PhoneHandler.formatPhoneNumber
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.view.tukang.ListTukangActivity
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var rlModal: LinearLayout
    private lateinit var cardAlert: LinearLayout
    private lateinit var inputAuth: LinearLayout
    private lateinit var inputOtp: LinearLayout
    private lateinit var inputNewPassword: RelativeLayout
    private lateinit var icEyeContainer: RelativeLayout
    private lateinit var icEyeNewPasswordContainer: RelativeLayout
    private lateinit var btnLogin: Button
    private lateinit var icBack: ImageView
    private lateinit var ivLogo: ImageView
    private lateinit var icEyeShow: ImageView
    private lateinit var icEyeClose: ImageView
    private lateinit var icEyeNewPasswordShow: ImageView
    private lateinit var icEyeNewPasswordClose: ImageView
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etUsernameForgot: EditText
    private lateinit var tvUsernameForgot: TextView
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
    private lateinit var listOtpInput: List<EditText>

    private lateinit var sessionManager: SessionManager

    private val splashScreenDuration = 2000L
    private var isPasswordShow = false
    private var currentSubmitStep = 0
    private var idUserResetPassword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)

        setContentView(R.layout.activity_splash_screen)

        initVariable()
        initClickHandler()
        initOtpListener()

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
        icBack = findViewById(R.id.ic_back)

        icEyeContainer = findViewById(R.id.ic_eye_container)
        icEyeShow = findViewById(R.id.ic_eye_show)
        icEyeClose = findViewById(R.id.ic_eye_close)
        icEyeNewPasswordContainer = findViewById(R.id.ic_eye_container_new_password)
        icEyeNewPasswordShow = findViewById(R.id.ic_eye_show_new_password)
        icEyeNewPasswordClose = findViewById(R.id.ic_eye_close_new_password)

        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        etUsernameForgot = findViewById(R.id.et_username_forgot)
        tvUsernameForgot = findViewById(R.id.tv_username_forgot)
        etNewPassword = findViewById(R.id.et_new_password)

        etOtp1 = findViewById(R.id.otp_1)
        etOtp2 = findViewById(R.id.otp_2)
        etOtp3 = findViewById(R.id.otp_3)
        etOtp4 = findViewById(R.id.otp_4)
        etOtp5 = findViewById(R.id.otp_5)
        etOtp6 = findViewById(R.id.otp_6)

        tvTitleAuth = findViewById(R.id.tv_title_auth)
        tvAlert = findViewById(R.id.tv_alert)
        tvResetPassword = findViewById(R.id.tv_reset_password)

        // Set default input type ke password
        etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
        etPassword.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        etNewPassword.transformationMethod = PasswordTransformationMethod.getInstance()
        etNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD

        // Set list of otp
        listOtpInput = listOf(etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6)

        setPasswordState()

    }

    private fun initClickHandler() {

        btnLogin.setOnClickListener { submitHandler(submit = true) }
        icEyeContainer.setOnClickListener { togglePassword() }
        icEyeNewPasswordContainer.setOnClickListener { togglePassword() }
        tvResetPassword.setOnClickListener {
            currentSubmitStep = 1
            submitHandler(next = true)
        }
        icBack.setOnClickListener { if (currentSubmitStep > 0) submitHandler(previous = true) }

    }

    private fun initOtpListener() {

        for (i in listOtpInput.indices) {

            val etObject = listOtpInput[i]
            etObject.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    if (!etObject.text.isNullOrEmpty()) {
                        if (i < (listOtpInput.size - 1)) {
                            listOtpInput[i+1].requestFocus()
                            listOtpInput[i+1].setSelection(listOtpInput[i+1].text.length)
                        } else KeyboardHandler.hideKeyboard(etObject, this@SplashScreenActivity)
                    } else {
                        if ( i > 0) {
                            listOtpInput[i-1].requestFocus()
                            listOtpInput[i-1].setSelection(listOtpInput[i-1].text.length)
                        }
                    }

                }

                override fun afterTextChanged(s: Editable?) {
                }

            })

        }

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
            etNewPassword.transformationMethod = null
            etNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            icEyeNewPasswordClose.visibility = View.GONE
            icEyeNewPasswordShow.visibility = View.VISIBLE

        } else {

            etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            etPassword.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            icEyeClose.visibility = View.VISIBLE
            icEyeShow.visibility = View.GONE
            etNewPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            etNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            icEyeNewPasswordClose.visibility = View.VISIBLE
            icEyeNewPasswordShow.visibility = View.GONE

        }

        // Set kursor ke posisi terakhir
        etPassword.setSelection(etPassword.text.length)
        etNewPassword.setSelection(etNewPassword.text.length)
    }

    private fun navigateToMain() {

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()

    }
    private fun navigateToListTukang() {

        val intent = Intent(this, ListTukangActivity::class.java)
        startActivity(intent)
        finish()

    }

    private fun checkSession() {

        val isLoggedIn = sessionManager.isLoggedIn()
        val userId = sessionManager.userID()!!
        val userCity = sessionManager.userCityID()!!
        val userKind = sessionManager.userKind()!!

        if (!isLoggedIn || userId.isEmpty() || userCity.isEmpty() || userKind.isEmpty()) showCardLogin()
        else {
            when (userKind) {
                USER_KIND_BA -> navigateToListTukang()
                else -> navigateToMain()
            }
        }

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

    private fun showResetConfirmation() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Batalkan Setel Ulang Kata Sandi")
            .setMessage("Apakah Anda yakin ingin membatalkan proses \"Setel Ulang Kata Sandi\"?")
            .setNegativeButton("Tidak") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Iya") { dialog, _ ->

                currentSubmitStep += 1
                submitHandler(next = true)

            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun loginHandler() {

        val username = "${ etUsername.text }"
        val password = "${ etPassword.text }"

        if (TextUtils.isEmpty(username)) {
            etUsername.requestFocus()
            etUsername.error = "Username tidak boleh kosong!"
            return
        } else if (TextUtils.isEmpty(password)) {
            etUsername.clearFocus()
            etPassword.requestFocus()
            etPassword.error = "Kata sandi tidak boleh kosong!"
            return
        } else if (password.length < 8) {
            etUsername.clearFocus()
            etPassword.requestFocus()
            etPassword.error = "Kata sandi minimum adalah 8 karakter!"
            return
        } else {
            etUsername.clearFocus()
            etPassword.clearFocus()
        }

        loadingState(true)

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

                        when (data.level_user) {
                            AUTH_LEVEL_ADMIN -> sessionManager.setUserKind(USER_KIND_ADMIN)
                            AUTH_LEVEL_COURIER -> sessionManager.setUserKind(USER_KIND_COURIER)
                            AUTH_LEVEL_BA -> sessionManager.setUserKind(USER_KIND_BA)
                            else -> sessionManager.setUserKind(USER_KIND_SALES)
                        }

                        sessionManager.setUserID(data.id_user)
                        sessionManager.setUserName(data.username)
                        sessionManager.setFullName(data.full_name)
                        sessionManager.setUserCityID(data.id_city)

                        sessionManager.setLoggedIn(LOGGED_IN)

                        when (data.level_user) {
                            AUTH_LEVEL_BA -> navigateToListTukang()
                            else -> navigateToMain()
                        }

                        loadingState(false)

                    }
                    RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                        showAlert("${ response.message }", 5000)
                        loadingState(false)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        showAlert("Username atau kata sandi anda sepertinya salah!", 5000)
                        loadingState(false)

                    }
                    else -> {

                        handleMessage(this@SplashScreenActivity, TAG_RESPONSE_CONTACT, response.message.let { if (!it.isNullOrEmpty()) it else "Proses autentikasi gagal" })
                        loadingState(false)

                    }
                }

            } catch (e: Exception) {

                handleMessage(this@SplashScreenActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(false)

            }
        }

    }

    private fun requestOtpHandler() {

        val usernameForgot = "${ etUsernameForgot.text }".trim().replace(" ", "").toLowerCase()

        if (usernameForgot.isEmpty()) {
            etUsernameForgot.error = "Username tidak boleh kosong!"
            etUsernameForgot.requestFocus()
            return
        } else {
            etUsernameForgot.error = null
            etUsernameForgot.clearFocus()
        }

        loadingState(true)

//        Handler().postDelayed({
//            currentSubmitStep += 1
//            submitHandler(next = true)
//            handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE, "Success creating new OTP code!")
//            loadingState(false)
//        }, 1000)
//        return

        lifecycleScope.launch {
            try {

                val rbUsername = createPartFromString(usernameForgot)
                val apiService = HttpClient.create()
                val response = apiService.requestOtp(rbUsername)

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    if (responseBody.status == RESPONSE_STATUS_OK) {

                        currentSubmitStep += 1
                        submitHandler(next = true)
                        handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE, "${ responseBody.message }")
                        loadingState(false)

                    } else {

                        handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE, "Gagal mendapatkan kode OTP!: ${ responseBody.message }")
                        loadingState(false)

                    }

                } else {

                    handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE, "Gagal mendapatkan kode OTP! Error: " + response.message())
                    loadingState(false)

                }

            } catch (e: Exception) {

                handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE, "Failed run service. Exception " + e.message)
                loadingState(false)

            }
        }

    }

    private fun verifyOtpHandler() {

        var isReady = true
        var otpCode = ""

        for (i in listOtpInput.indices) {
            val otpObject = listOtpInput[i]
            val otpInput = "${ otpObject.text }"

            otpObject.error = null

            if (otpInput.isEmpty()) {
                otpObject.error = "OTP ${ i + 1 } tidak boleh kosong"
                otpObject.requestFocus()
                isReady = false
                break
            }

            otpCode += otpInput

        }

        if (otpCode.isEmpty()) {
            isReady = false
            handleMessage(this, "EMPTY OTP CODE", "Kode OTP tidak boleh kosong!")
        }

        if (!isReady) return

        loadingState(true)

//        Handler().postDelayed({
//            currentSubmitStep += 1
//            submitHandler(next = true)
//            handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE, "$otpCode")
////            handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE, "OTP code verified, please insert your new password!")
//            loadingState(false)
//        }, 1000)
//        return

        lifecycleScope.launch {
            try {

                val rbOtp = createPartFromString(otpCode)
                val apiService = HttpClient.create()
                val response = apiService.verifyOtp(rbOtp)

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    if (responseBody.status == RESPONSE_STATUS_OK) {

                        idUserResetPassword = responseBody.user_id
                        currentSubmitStep += 1
                        submitHandler(next = true)
                        handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE, "${ responseBody.message }")
                        loadingState(false)

                    } else {

                        handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE, "Gagal verifikasi OTP!: ${ responseBody.message }")
                        loadingState(false)

                    }

                } else {

                    handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE, "Gagal verifikasi OTP! Error: " + response.message())
                    loadingState(false)

                }

            } catch (e: Exception) {
                handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE, "Failed run service. Exception " + e.message)
                loadingState(false)
            }
        }

    }
    private fun resetPasswordHandler() {
        val userID = "$idUserResetPassword"
        val password = "${ etNewPassword.text }"

        if (userID.isNullOrEmpty()) {
            handleMessage(this, "USER RESET PASSWORD", "Tidak dapat menemukan pengguna untuk setel ulang kata sandi, silakan coba masukkan kata sandi Anda!")
        } else if (password.isEmpty()) {
            etNewPassword.error = "Kata sandi baru Anda tidak boleh kosong!"
            etNewPassword.requestFocus()
            return
        } else if (password.length < 8) {
            etUsername.clearFocus()
            etPassword.requestFocus()
            etPassword.error = "Kata sandi minimum adalah 8 karakter!"
            return
        } else {
            etNewPassword.error = null
            etNewPassword.clearFocus()
        }

        loadingState(true)

//        Handler().postDelayed({
//            currentSubmitStep += 1
//            submitHandler(next = true)
////            handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE, "Success change password. Please login again!")
//            handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE, "$password : $userID")
//            loadingState(false)
//        }, 1000)
//        return

        lifecycleScope.launch {
            try {

                val rbUserID = createPartFromString(userID)
                val rbPassword = createPartFromString(password)
                val apiService = HttpClient.create()
                val response = apiService.updatePassword(rbUserID, rbPassword)

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            idUserResetPassword = null
                            currentSubmitStep += 1
                            submitHandler(next = true)
                            handleMessage(
                                this@SplashScreenActivity,
                                TAG_RESPONSE_MESSAGE,
                                "${responseBody.message}"
                            )
                            loadingState(false)
                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE, "Gagal setel ulang kata sandi! Message: ${ responseBody.message }")
                            loadingState(false)

                        }
                        else -> {

                            handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE, "Gagal setel ulang kata sandi!: ${ responseBody.message }")
                            loadingState(false)

                        }
                    }

                } else {

                    handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE, "Gagal setel ulang kata sandi! Error: " + response.message())
                    loadingState(false)

                }

            } catch (e: Exception) {
                handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE, "Failed run service. Exception " + e.message)
                loadingState(false)
            }
        }

    }

    private fun submitHandler(next: Boolean? = null, previous: Boolean? = null, submit: Boolean? = null) {

        if (isPasswordShow) togglePassword()

        if (previous != null && previous == true) {
            currentSubmitStep -= 1
            if (currentSubmitStep == 0) currentSubmitStep = -1
        }

        // Hide alert
        cardAlert.visibility = View.GONE

        when (currentSubmitStep) {
            0 -> if (submit != null && submit == true) loginHandler()
            1 -> {

                if (submit != null && submit == true) requestOtpHandler()
                else {
                    clearInput()

                    inputAuth.visibility = View.GONE
                    inputOtp.visibility = View.GONE
                    inputNewPassword.visibility = View.GONE
                    icBack.visibility = View.VISIBLE
                    etUsernameForgot.visibility = View.VISIBLE
                    tvUsernameForgot.visibility = View.VISIBLE
                    tvTitleAuth.text = "Setel Ulang Kata Sandi"
                    btnLogin.text = "Dapatkan Kode OTP"

                    etUsernameForgot.requestFocus()
                }

            }
            2 -> {

                if (submit != null && submit == true) verifyOtpHandler()
                else {
                    clearInput()

                    inputAuth.visibility = View.GONE
                    etUsernameForgot.visibility = View.GONE
                    tvUsernameForgot.visibility = View.GONE
                    inputNewPassword.visibility = View.GONE
                    icBack.visibility = View.VISIBLE
                    inputOtp.visibility = View.VISIBLE
                    tvTitleAuth.text = "Masukkan Kode OTP"
                    btnLogin.text = "Verifikasi Kode OTP"

                    etOtp1.requestFocus()
                    showKeyboard(etOtp1, this)
                }

            }
            3 -> {

                if (submit != null && submit == true) resetPasswordHandler()
                else {
                    clearInput()

                    inputAuth.visibility = View.GONE
                    etUsernameForgot.visibility = View.GONE
                    tvUsernameForgot.visibility = View.GONE
                    inputOtp.visibility = View.GONE
                    icBack.visibility = View.GONE
                    inputNewPassword.visibility = View.VISIBLE
                    tvTitleAuth.text = "Masukkan Password Baru"
                    btnLogin.text = "Setel Ulang Password Sekarang"
                    etNewPassword.requestFocus()
                }

            }
            else -> {
                clearInput()

                inputNewPassword.visibility = View.GONE
                inputOtp.visibility = View.GONE
                etUsernameForgot.visibility = View.GONE
                tvUsernameForgot.visibility = View.GONE
                icBack.visibility = View.GONE
                inputAuth.visibility = View.VISIBLE
                tvTitleAuth.text = "Hey, \nLogin Now"
                btnLogin.text = "Login"
                currentSubmitStep = 0
                idUserResetPassword = null

                etUsername.requestFocus()
            }
        }

    }

    private fun clearInput() {

        etUsername.error = null
        etPassword.error = null
        etUsernameForgot.error = null
        etNewPassword.error = null
        etOtp1.error = null
        etOtp2.error = null
        etOtp3.error = null
        etOtp4.error = null
        etOtp5.error = null
        etOtp6.error = null
        etUsername.setText("")
        etPassword.setText("")
        etUsernameForgot.setText("")
        etNewPassword.setText("")
        etOtp1.setText("")
        etOtp2.setText("")
        etOtp3.setText("")
        etOtp4.setText("")
        etOtp5.setText("")
        etOtp6.setText("")

    }

    private fun loadingState(state: Boolean) {

        btnLogin.setTextColor(ContextCompat.getColor(this, R.color.white))
        btnLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_200))

        if (state) {

            btnLogin.isEnabled = false
            btnLogin.text = getString(R.string.txt_loading)

        } else {

            btnLogin.isEnabled = true
            when (currentSubmitStep) {
                0 -> btnLogin.text = "Login"
                1 -> btnLogin.text = "Dapatkan Kode OTP"
                2 -> btnLogin.text = "Verifikasi Kode OTP"
                3 -> btnLogin.text = "Setel Ulang Password Sekarang"
                else -> btnLogin.text = "Login"
            }
            btnLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))

        }

    }

    override fun onBackPressed() {
        if (currentSubmitStep > 0) {
            if (currentSubmitStep > 2) showResetConfirmation()
            else submitHandler(previous = true)
        } else {
            super.onBackPressed()
        }
    }

}