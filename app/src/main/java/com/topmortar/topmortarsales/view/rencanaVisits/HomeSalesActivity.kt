@file:Suppress("DEPRECATION")

package com.topmortar.topmortarsales.view.rencanaVisits

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.DatabaseReference
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_SALES
import com.topmortar.topmortarsales.commons.CONST_FULL_NAME
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_CITY_ID
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_NAME
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_STATUS
import com.topmortar.topmortarsales.commons.CONST_NEAREST_STORE
import com.topmortar.topmortarsales.commons.CONST_USER_ID
import com.topmortar.topmortarsales.commons.CONST_USER_LEVEL
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_AUTH
import com.topmortar.topmortarsales.commons.LOGGED_OUT
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_ACTION_MAIN_ACTIVITY
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityHomeSalesBinding
import com.topmortar.topmortarsales.view.MainActivity
import com.topmortar.topmortarsales.view.MapsActivity
import com.topmortar.topmortarsales.view.SplashScreenActivity
import com.topmortar.topmortarsales.view.contact.NewRoomChatFormActivity
import com.topmortar.topmortarsales.view.reports.ReportsActivity
import com.topmortar.topmortarsales.view.user.UserProfileActivity
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
class HomeSalesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeSalesBinding
    private lateinit var apiService: ApiService

    private lateinit var sessionManager: SessionManager
    private val userId get() = sessionManager.userID()
    private val userCity get() = sessionManager.userCityID()
    private val userKind get() = sessionManager.userKind()
    private val userFullName get() = sessionManager.fullName()
    private val userDistributorId get() = sessionManager.userDistributor()

    private var doubleBackToExitPressedOnce = false
    private lateinit var firebaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)
        firebaseReference = FirebaseUtils().getReference(distributorId = userDistributorId!!)

        val isLoggedIn = sessionManager.isLoggedIn()
        if (!isLoggedIn || userId!!.isEmpty() || userCity!!.isEmpty() || userKind!!.isEmpty()|| userDistributorId!!.isEmpty()) return missingDataHandler()

        binding = ActivityHomeSalesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = HttpClient.create()

        initView()

    }

    private fun initView() {

        binding.fullName.text = userFullName

        binding.rencanaVisit.setOnClickListener { navigateToTargetVisit() }
        binding.allStore.setOnClickListener { navigateToAllStore()}
        binding.nearestStoreItem.setOnClickListener { navigateToNearestStore()}
        binding.registerNewStore.setOnClickListener { navigateToRegisterStore()}
        binding.myProfileItem.setOnClickListener { navigateToMyProfile() }
        binding.btnLogout.setOnClickListener { logoutConfirmation() }
        binding.reportDetail.setOnClickListener { navigateToListReport() }

    }

    private fun navigateToTargetVisit() {
        val intent = Intent(this, RencanaVisitActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToAllStore() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToRegisterStore() {
        val intent = Intent(this, NewRoomChatFormActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToMyProfile() {
        val intent = Intent(this, UserProfileActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToListReport() {
        val intent = Intent(this, ReportsActivity::class.java)
        intent.putExtra(CONST_USER_ID, userId)
        intent.putExtra(CONST_FULL_NAME, userFullName)
        intent.putExtra(CONST_USER_LEVEL, AUTH_LEVEL_SALES)
        startActivity(intent)
    }

    private fun navigateToNearestStore() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Memuat data toko…")
        progressDialog.show()

        Handler().postDelayed({

            lifecycleScope.launch {
                try {

                    val response = apiService.getContacts(cityId = userCity!!, distributorID = userDistributorId!!)

                    when (response.status) {
                        RESPONSE_STATUS_OK -> {

                            val listCoordinate = arrayListOf<String>()
                            val listCoordinateName = arrayListOf<String>()
                            val listCoordinateStatus = arrayListOf<String>()
                            val listCoordinateCityID = arrayListOf<String>()

                            for (item in response.results.listIterator()) {
                                listCoordinate.add(item.maps_url)
                                listCoordinateName.add(item.nama)
                                listCoordinateStatus.add(item.store_status)
                                listCoordinateCityID.add(item.id_city)
                            }

                            val intent = Intent(this@HomeSalesActivity, MapsActivity::class.java)

                            intent.putExtra(CONST_NEAREST_STORE, true)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_STATUS, listCoordinateStatus)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_CITY_ID, listCoordinateCityID)

                            progressDialog.dismiss()
                            startActivity(intent)

                        }
                        RESPONSE_STATUS_EMPTY -> {

                            val listCoordinate = arrayListOf<String>()
                            val listCoordinateName = arrayListOf<String>()

                            val intent = Intent(this@HomeSalesActivity, MapsActivity::class.java)

                            intent.putExtra(CONST_NEAREST_STORE, true)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)

                            progressDialog.dismiss()
                            startActivity(intent)

                        }
                        else -> {

                            handleMessage(this@HomeSalesActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                            progressDialog.dismiss()

                        }
                    }


                } catch (e: Exception) {

                    handleMessage(this@HomeSalesActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                    progressDialog.dismiss()

                }

            }

        }, 1000)
    }

    private fun logoutConfirmation() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Logout")
            .setMessage("Apakah anda yakin ingin keluar?")
            .setNegativeButton("Tidak") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Iya") { dialog, _ ->

                dialog.dismiss()
                logoutHandler()

            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun missingDataHandler() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Data Tidak Lengkap Terdeteksi")
            .setCancelable(false)
            .setMessage("Data login yang tidak lengkap telah terdeteksi, silakan coba login kembali!")
            .setPositiveButton("Oke") { dialog, _ ->

                dialog.dismiss()
                logoutHandler()

            }
        val dialog = builder.create()
        dialog.show()
    }

    @SuppressLint("HardwareIds")
    private fun logoutHandler() {

        // Firebase Auth Session
        try {
            val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            val model = Build.MODEL
            val manufacturer = Build.MANUFACTURER

            val authChild = firebaseReference.child(FIREBASE_CHILD_AUTH)
            val userChild = authChild.child(sessionManager.userName() + sessionManager.userID())
            val userDevices = userChild.child("devices")
            var userDeviceText = "$manufacturer$model$androidId"
            userDeviceText = userDeviceText.replace(".", "_").replace(",", "_").replace(" ", "")
            val userDevice = userDevices.child(userDeviceText)

            userDevice.child("logout_at").setValue(DateFormat.now())
            userDevice.child("login_at").setValue("")
        } catch (e: Exception) {
            Log.d("Firebase Auth", "$e")
        }

        sessionManager.setLoggedIn(LOGGED_OUT)
        sessionManager.setUserLoggedIn(null)

        val intent = Intent(this@HomeSalesActivity, SplashScreenActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getUserLoggedIn() {

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.detailUser(userId = userId!!)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        val data = response.results[0]
                        sessionManager.setUserLoggedIn(data)
                        binding.fullName.text = sessionManager.fullName().let { if (!it.isNullOrEmpty()) it else "Selamat Datang"}

                    } RESPONSE_STATUS_EMPTY -> missingDataHandler()
                    else -> Log.d("TAG USER LOGGED IN", "Failed get data!")
                }


            } catch (e: Exception) {
                Log.d("TAG USER LOGGED IN", "Failed run service. Exception " + e.message)
            }

        }

    }

// Override Class

    override fun onResume() {
        super.onResume()
        getUserLoggedIn()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        doubleBackToExitPressedOnce = true
        handleMessage(this, TAG_ACTION_MAIN_ACTIVITY, "Tekan sekali lagi untuk keluar!", TOAST_SHORT)

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }
}