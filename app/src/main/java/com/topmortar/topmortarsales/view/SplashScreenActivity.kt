@file:Suppress("DEPRECATION")

package com.topmortar.topmortarsales.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_ADMIN
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_ADMIN_CITY
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_BA
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_COURIER
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_MARKETING
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_PENAGIHAN
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_SALES
import com.topmortar.topmortarsales.commons.CONST_FULL_NAME
import com.topmortar.topmortarsales.commons.CONST_USER_ID
import com.topmortar.topmortarsales.commons.CONST_USER_LEVEL
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_ABSENT
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_AUTH
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_IS_ALLOWED_LOGOUT
import com.topmortar.topmortarsales.commons.LOGGED_IN
import com.topmortar.topmortarsales.commons.LOGGED_OUT
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN_CITY
import com.topmortar.topmortarsales.commons.USER_KIND_BA
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.USER_KIND_MARKETING
import com.topmortar.topmortarsales.commons.USER_KIND_PENAGIHAN
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.services.TrackingService
import com.topmortar.topmortarsales.commons.utils.AppUpdateHelper
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.KeyboardHandler
import com.topmortar.topmortarsales.commons.utils.KeyboardHandler.showKeyboard
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.model.DeviceModel
import com.topmortar.topmortarsales.view.courier.HomeCourierActivity
import com.topmortar.topmortarsales.view.rencanaVisits.HomeSalesActivity
import com.topmortar.topmortarsales.view.reports.ReportsActivity
import com.topmortar.topmortarsales.view.tukang.BrandAmbassadorActivity
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

@SuppressLint("CustomSplashScreen", "SetTextI18n")
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
    private lateinit var firebaseReference: DatabaseReference

    private val splashScreenDuration = 1000L
    private var isPasswordShow = false
    private var currentSubmitStep = 0
    private var idUserResetPassword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            enableEdgeToEdge()
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
                val systemBars = insets.getInsets(Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        } else {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                enableEdgeToEdge(SystemBarStyle.dark(getColor(R.color.primary)))
                ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
                    val systemBars = insets.getInsets(Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                    insets
                }
            } else {
                window.statusBarColor = getColor(R.color.primary)
            }
        }



        sessionManager = SessionManager(this)
        setContentView(R.layout.activity_splash_screen)

//        AppUpdateHelper.initialize()
//        AppUpdateHelper.checkForUpdate(this) {
//            initView()
//        }
    }

    private fun initView() {
        initVariable()
        initClickHandler()
        initOtpListener()

        Handler(Looper.getMainLooper()).postDelayed({

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
            submitHandler()
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

        val intent = Intent(this, BrandAmbassadorActivity::class.java)
        startActivity(intent)
        finish()

    }
    private fun navigateToCourier() {

        val intent = Intent(this, HomeCourierActivity::class.java)
        startActivity(intent)
        finish()

    }
    private fun navigateToSales() {

        val intent = Intent(this, HomeSalesActivity::class.java)
        startActivity(intent)
        finish()

    }

    private fun checkSession() {

        val isLoggedIn = sessionManager.isLoggedIn()
        val userId = sessionManager.userID()!!
        val userCity = sessionManager.userCityID()!!
        val userKind = sessionManager.userKind()!!
        val userDistributor = sessionManager.userDistributor()!!
        val availableLevel = checkAvailableAccount(sessionManager.userAuthLevel() ?: "0")

        if (!isLoggedIn || !availableLevel || userId.isEmpty() || userCity.isEmpty() || userKind.isEmpty() || userDistributor.isEmpty()) showCardLogin()
        else getUserLoggedIn()

    }

    private fun showCardLogin() {
        sessionManager.setLoggedIn(LOGGED_OUT)
        sessionManager.setUserLoggedIn(null)

        val serviceIntent = Intent(this, TrackingService::class.java)
        this.stopService(serviceIntent)

        rlModal.visibility = View.VISIBLE
    }

    private fun showAlert(message: String) {

        cardAlert.visibility = View.VISIBLE
        tvAlert.text = message

        Handler(Looper.getMainLooper()).postDelayed({

            cardAlert.visibility = View.GONE
            tvAlert.text = ""

        }, 5000)

    }

    private fun showResetConfirmation() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Batalkan Setel Ulang Kata Sandi")
            .setMessage("Apakah Anda yakin ingin membatalkan proses \"Setel Ulang Kata Sandi\"?")
            .setNegativeButton("Tidak") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Iya") { _, _ ->

                currentSubmitStep += 1
                submitHandler()

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
                        val availableAccount = checkAvailableAccount(data.level_user)

                        if(!availableAccount) {
                            showAlert("Akun tidak ditemukan!")
                            loadingState(false)
                            return@launch
                        } else if (data.phone_user == "0") {
                            showAlert("Akun anda telah ditangguhkan!")
                            loadingState(false)
                            return@launch
                        }

                        sessionManager.setUserLoggedIn(data)
                        sessionManager.setLoggedIn(LOGGED_IN)

                        // Firebase Auth Session
                        try {
                            firebaseReference = FirebaseUtils.getReference(distributorId = data.id_distributor.ifEmpty { "-firebase-005" })
                            val authChild = firebaseReference.child(FIREBASE_CHILD_AUTH)
                            val userChild = authChild.child(data.username + data.id_user)
                            val userDevicesChild = userChild.child("devices")

                            userChild.child("id_user").setValue(data.id_user)
                            userChild.child("phone_user").setValue(data.phone_user)
                            userChild.child("username").setValue(data.username)
                            userChild.child("password").setValue(data.password)
                            userChild.child("level_user").setValue(data.level_user)
                            userChild.child("id_city").setValue(data.id_city)
                            userChild.child("nama_city").setValue(data.nama_city)
                            userChild.child("kode_city").setValue(data.kode_city)
                            userChild.child("bid_limit").setValue(data.bid_limit)
                            userChild.child("full_name").setValue(data.full_name)
                            userChild.child("id_distributor").setValue(data.id_distributor)
                            userChild.child("nama_distributor").setValue(data.nama_distributor)
                            userChild.child("nomorhp_distributor").setValue(data.nomorhp_distributor)
                            userChild.child("alamat_distributor").setValue(data.alamat_distributor)
                            userChild.child("jenis_distributor").setValue(data.jenis_distributor)

                            userDevicesChild.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        for (item in snapshot.children) {

                                            if (!item.hasChildren()) {
                                                item.ref.removeValue()
                                                return
                                            }

                                            val device = item.getValue(DeviceModel::class.java) ?: return

                                            var userDeviceText = "${device.manufacture}${device.model}${device.id}"

                                            if (userDeviceText.isEmpty()) return

                                            userDeviceText = userDeviceText.replace(".", "_").replace(",", "_").replace(" ", "")
                                            val userDevice = userDevicesChild.child(userDeviceText)
                                            userDevice.child("id").setValue(device.id)
                                            userDevice.child("model").setValue(device.model)
                                            userDevice.child("manufacture").setValue(device.manufacture)
                                            userDevice.child("device").setValue(device.device)
                                            userDevice.child("product").setValue(device.product)
                                            userDevice.child("sdk_version").setValue(device.sdk_version)
                                            userDevice.child("version_release").setValue(device.version_release)
                                            userDevice.child("screen_width").setValue(device.screen_width)
                                            userDevice.child("screen_height").setValue(device.screen_height)
                                            userDevice.child("density").setValue(device.density)
                                            userDevice.child("login_at").setValue(device.login_at)
                                            userDevice.child("logout_at").setValue(device.logout_at)
                                        }

                                        userDevice(userChild)
                                    } else {
                                        userDevice(userChild)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    userDevice(userChild)
                                }

                            })

                            // Reset isAllowedLogout value
                            val absentChild = firebaseReference.child(FIREBASE_CHILD_ABSENT)
                            absentChild.child(data.id_user).child(FIREBASE_CHILD_IS_ALLOWED_LOGOUT).setValue(false)

                        } catch (e: Exception) {
                            if (e is CancellationException) {
                                return@launch
                            }
                            FirebaseUtils.logErr(this@SplashScreenActivity, "Failed SplashScreenActivity on loginHandler(). Catch: ${e.message}")
                            Log.e("Firebase Auth", "$e")
                        }

                        when (data.level_user) {
                            AUTH_LEVEL_BA -> navigateToListTukang()
                            AUTH_LEVEL_COURIER -> navigateToCourier()
                            AUTH_LEVEL_SALES, AUTH_LEVEL_PENAGIHAN, AUTH_LEVEL_MARKETING -> navigateToSales()
                            else -> navigateToMain()
                        }

                        loadingState(false)

                    }
                    RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                        showAlert(response.message)
                        loadingState(false)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        showAlert("Username atau kata sandi anda sepertinya salah!")
                        loadingState(false)

                    }
                    else -> {

                        handleMessage(this@SplashScreenActivity, TAG_RESPONSE_CONTACT, response.message.let { it.ifEmpty { "Proses autentikasi gagal" } })
                        loadingState(false)

                    }
                }

            } catch (e: Exception) {

                FirebaseUtils.logErr(this@SplashScreenActivity, "Failed SplashScreenActivity on loginHandler(). Catch: ${e.message}")
                handleMessage(this@SplashScreenActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(false)

            }
        }

    }

    private fun checkAvailableAccount(dataLevel: String): Boolean {
        return dataLevel == AUTH_LEVEL_ADMIN ||
                dataLevel == AUTH_LEVEL_ADMIN_CITY ||
                dataLevel == AUTH_LEVEL_SALES ||
                dataLevel == AUTH_LEVEL_COURIER ||
                dataLevel == AUTH_LEVEL_BA ||
                dataLevel == AUTH_LEVEL_MARKETING ||
                dataLevel == AUTH_LEVEL_PENAGIHAN
    }

    private fun requestOtpHandler() {

        val usernameForgot = "${ etUsernameForgot.text }".trim().replace(" ", "")
            .lowercase(Locale.getDefault())

        if (usernameForgot.isEmpty()) {
            etUsernameForgot.error = "Username tidak boleh kosong!"
            etUsernameForgot.requestFocus()
            return
        } else {
            etUsernameForgot.error = null
            etUsernameForgot.clearFocus()
        }

        loadingState(true)

        lifecycleScope.launch {
            try {

                val rbUsername = createPartFromString(usernameForgot)
                val apiService = HttpClient.create()
                val response = apiService.requestOtp(rbUsername)

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    if (responseBody.status == RESPONSE_STATUS_OK) {

                        currentSubmitStep += 1
                        submitHandler()
                        handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE,
                            responseBody.message
                        )
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

                if (e is CancellationException) {
                    return@launch
                }
                FirebaseUtils.logErr(this@SplashScreenActivity, "Failed SplashScreenActivity on requestOtpHandler(). Catch: ${e.message}")
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
                        submitHandler()
                        handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE,
                            responseBody.message
                        )
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
                if (e is CancellationException) {
                    return@launch
                }
                FirebaseUtils.logErr(this@SplashScreenActivity, "Failed SplashScreenActivity on verifyOtpHandler(). Catch: ${e.message}")
                handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE, "Failed run service. Exception " + e.message)
                loadingState(false)
            }
        }

    }
    private fun resetPasswordHandler() {
        val userID = "$idUserResetPassword"
        val password = "${ etNewPassword.text }"

        if (userID.isEmpty()) {
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
                            submitHandler()
                            handleMessage(
                                this@SplashScreenActivity,
                                TAG_RESPONSE_MESSAGE,
                                responseBody.message
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
                if (e is CancellationException) {
                    return@launch
                }
                FirebaseUtils.logErr(this@SplashScreenActivity, "Failed SplashScreenActivity on resetPasswordHandler(). Catch: ${e.message}")
                handleMessage(this@SplashScreenActivity, TAG_RESPONSE_MESSAGE, "Failed run service. Exception " + e.message)
                loadingState(false)
            }
        }

    }

    private fun submitHandler(previous: Boolean? = null, submit: Boolean? = null) {

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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (currentSubmitStep > 0) {
            if (currentSubmitStep > 2) showResetConfirmation()
            else submitHandler(previous = true)
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("HardwareIds")
    private fun userDevice(userChild: DatabaseReference) {
        // Detail Device
        val model = Build.MODEL
        val manufacturer = Build.MANUFACTURER
        val device = Build.DEVICE
        val product = Build.PRODUCT
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val deviceUUID = sessionManager.deviceUUID() ?: UUID.randomUUID().toString()
        sessionManager.deviceUUID(deviceUUID)

        val version = Build.VERSION.SDK_INT
        val versionRelease = Build.VERSION.RELEASE

        val displayMetrics = DisplayMetrics()
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val density = displayMetrics.density

        // User Device Detail
        val userDevices = userChild.child("devices")
        var userDeviceText = "$manufacturer$model$androidId"

        if (userDeviceText.isEmpty()) return

        userDeviceText = userDeviceText.replace(".", "_").replace(",", "_").replace(" ", "")
        val userDevice = userDevices.child(userDeviceText)
        userDevice.child("id").setValue(androidId)
        userDevice.child("model").setValue(model)
        userDevice.child("manufacture").setValue(manufacturer)
        userDevice.child("device").setValue(device)
        userDevice.child("product").setValue(product)
        userDevice.child("sdk_version").setValue("$version")
        userDevice.child("version_release").setValue(versionRelease)
        userDevice.child("screen_width").setValue("$screenWidth px")
        userDevice.child("screen_height").setValue("$screenHeight px")
        userDevice.child("density").setValue("$density")

        // User Loged In Datetime
        userDevice.child("login_at").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val loginTime = snapshot.getValue(String::class.java)
                if (loginTime.isNullOrEmpty()) userDevice.child("login_at").setValue(DateFormat.now())
            }

            override fun onCancelled(error: DatabaseError) {
                // Do something
            }

        })
        userDevice.child("logout_at").setValue("")

    }

    private fun getUserLoggedIn() {

        val userId = sessionManager.userID()!!
        val username = sessionManager.userName()!!
        val userFullName = sessionManager.fullName()!!
        val userCity = sessionManager.userCityID()!!
        val userKind = sessionManager.userKind()!!
        val userDistributor = sessionManager.userDistributor()!!
        val userDistributorNumber = sessionManager.userDistributorNumber()!!
        val userBidLimit = sessionManager.userBidLimit()!!

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.detailUser(userId = userId)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        val data = response.results[0]
                        if (data.phone_user == "0") {

                            // Firebase Auth Session
                            try {
                                val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                                val model = Build.MODEL
                                val manufacturer = Build.MANUFACTURER

                                val authChild = firebaseReference.child(FIREBASE_CHILD_AUTH)
                                val userChild = authChild.child(sessionManager.userName() + sessionManager.userID())
                                val userDevices = userChild.child("devices")
                                var userDeviceText = "$manufacturer$model$androidId"

                                if (userDeviceText.isEmpty()) return@launch

                                userDeviceText = userDeviceText.replace(".", "_").replace(",", "_").replace(" ", "")
                                val userDevice = userDevices.child(userDeviceText)

                                userDevice.child("logout_at").setValue(DateFormat.now())
                                userDevice.child("login_at").setValue("")
                            } catch (e: Exception) {
                                if (e is CancellationException) {
                                    return@launch
                                }
                                FirebaseUtils.logErr(this@SplashScreenActivity, "Failed SplashScreenActivity on getUserLoggedIn(). Catch: ${e.message}")
                                Log.d("Firebase Auth", "$e")
                            }

                            showCardLogin()
                        } else {
                            val userLevel = when (userKind) {
                                USER_KIND_ADMIN -> AUTH_LEVEL_ADMIN
                                USER_KIND_ADMIN_CITY -> AUTH_LEVEL_ADMIN_CITY
                                USER_KIND_COURIER -> AUTH_LEVEL_COURIER
                                USER_KIND_BA -> AUTH_LEVEL_BA
                                USER_KIND_MARKETING -> AUTH_LEVEL_MARKETING
                                else -> AUTH_LEVEL_SALES
                            }

                            // Firebase Auth Session
                            try {
                                val userDistributorIds = sessionManager.userDistributor()
                                firebaseReference = FirebaseUtils.getReference(distributorId = userDistributorIds ?: "-firebase-006")
                                val authChild = firebaseReference.child(FIREBASE_CHILD_AUTH)
                                val userChild = authChild.child(username + userId)
                                val userDevicesChild = userChild.child("devices")

                                userChild.child("id_user").setValue(userId)
                                userChild.child("username").setValue(username)
                                userChild.child("id_city").setValue(userCity)
                                userChild.child("level_user").setValue(userLevel)
                                userChild.child("id_distributor").setValue(userDistributor)
                                userChild.child("full_name").setValue(userFullName)
                                userChild.child("nomorhp_distributor").setValue(userDistributorNumber)
                                userChild.child("bid_limit").setValue(userBidLimit)

                                userDevicesChild.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            for (item in snapshot.children) {

                                                if (!item.hasChildren()) {
                                                    item.ref.removeValue()
                                                    return
                                                }

                                                val device = item.getValue(DeviceModel::class.java) ?: return

                                                var userDeviceText = "${device.manufacture}${device.model}${device.id}"

                                                if (userDeviceText.isEmpty()) return

                                                userDeviceText = userDeviceText.replace(".", "_").replace(",", "_").replace(" ", "")
                                                val userDevice = userDevicesChild.child(userDeviceText)
                                                userDevice.child("id").setValue(device.id)
                                                userDevice.child("model").setValue(device.model)
                                                userDevice.child("manufacture").setValue(device.manufacture)
                                                userDevice.child("device").setValue(device.device)
                                                userDevice.child("product").setValue(device.product)
                                                userDevice.child("sdk_version").setValue(device.sdk_version)
                                                userDevice.child("version_release").setValue(device.version_release)
                                                userDevice.child("screen_width").setValue(device.screen_width)
                                                userDevice.child("screen_height").setValue(device.screen_height)
                                                userDevice.child("density").setValue(device.density)
                                                userDevice.child("login_at").setValue(device.login_at)
                                                userDevice.child("logout_at").setValue(device.logout_at)
                                            }

                                            userDevice(userChild)
                                        } else {
                                            userDevice(userChild)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        userDevice(userChild)
                                    }

                                })
                            } catch (e: Exception) {
                                if (e is CancellationException) {
                                    return@launch
                                }
                                FirebaseUtils.logErr(this@SplashScreenActivity, "Failed SplashScreenActivity on getUserLoggedIn(). Catch: ${e.message}")
                                Log.e("Firebase Auth", "$e")
                            }

                            val nUserId =  intent.getStringExtra(CONST_USER_ID)
                            val nFullName =  intent.getStringExtra(CONST_FULL_NAME)
                            val nUserLevel =  intent.getStringExtra(CONST_USER_LEVEL)
                            val nIntent =  intent.getStringExtra("notification_intent")
                            val nVisitId =  intent.getStringExtra("nVisitId")

                            if (!nIntent.isNullOrEmpty()) {
                                val intent = Intent(this@SplashScreenActivity, ReportsActivity::class.java).apply {
                                    putExtra(CONST_USER_ID, nUserId)
                                    putExtra(CONST_FULL_NAME, nFullName)
                                    putExtra(CONST_USER_LEVEL, nUserLevel)
                                    putExtra("notification_intent", nIntent)
                                    putExtra("nVisitId", nVisitId)
                                }
                                startActivity(intent)
                                finish()
                            } else {
                                when (userKind) {
                                    USER_KIND_BA -> navigateToListTukang()
                                    USER_KIND_COURIER -> navigateToCourier()
                                    USER_KIND_SALES, USER_KIND_PENAGIHAN, USER_KIND_MARKETING -> navigateToSales()
                                    else -> navigateToMain()
                                }
                            }
                        }

                    } RESPONSE_STATUS_EMPTY -> {
                        showCardLogin()
                }
                    else -> {
                        showCardLogin()
                    }
                }

            } catch (e: Exception) {
                if (e is CancellationException) {
                    return@launch
                }
                FirebaseUtils.logErr(this@SplashScreenActivity, "Failed SplashScreenActivity on getUserLoggedIn(). Catch: ${e.message}")
                showCardLogin()
            }

        }

    }

    override fun onResume() {
        super.onResume()

        AppUpdateHelper.initialize()
        AppUpdateHelper.checkForUpdate(this) {
            initView()
        }
    }

}