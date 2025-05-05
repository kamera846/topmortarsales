@file:Suppress("DEPRECATION")

package com.topmortar.topmortarsales.view.courier

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.viewpager.CourierViewPagerAdapter
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_COURIER
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_ABSENT
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_AUTH
import com.topmortar.topmortarsales.commons.LOCATION_PERMISSION_REQUEST_CODE
import com.topmortar.topmortarsales.commons.LOGGED_OUT
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.services.TrackingService
import com.topmortar.topmortarsales.commons.utils.AppUpdateHelper
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityCourierBinding
import com.topmortar.topmortarsales.view.SplashScreenActivity
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@SuppressLint("SetTextI18n")
class CourierActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourierBinding

    private lateinit var sessionManager: SessionManager
    private val userKind get() = sessionManager.userKind()!!
    private val userId get() = sessionManager.userID()!!
    private val username get() = sessionManager.userName()!!
    private val fullname get() = sessionManager.fullName()!!
    private val userCity get() = sessionManager.userCityID()!!
    private val userDistributorId get() = sessionManager.userDistributor()!!
    private val userDistributorIds get() = sessionManager.userDistributor()
    private val userAbsentDateTime get() = sessionManager.absentDateTime()!!

    private lateinit var firebaseReference : DatabaseReference

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var pagerAdapter: CourierViewPagerAdapter
    private var activeTab = 0

    private lateinit var absentProgressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge()

        binding = ActivityCourierBinding.inflate(layoutInflater)
        sessionManager = SessionManager(this@CourierActivity)
        val isLoggedIn = sessionManager.isLoggedIn()

        if (!isLoggedIn || userId.isEmpty() || userCity.isEmpty() || userKind.isEmpty()|| userDistributorId.isEmpty()) return missingDataHandler()

        setContentView(binding.root)

        AppUpdateHelper.initialize()
        AppUpdateHelper.checkForUpdate(this)

        val userDistributorIds = sessionManager.userDistributor()
        firebaseReference = FirebaseUtils.getReference(distributorId = userDistributorIds ?: "-firebase-010")

        binding.titleBarDark.tvTitleBarDescription.text = sessionManager.userName().let { if (!it.isNullOrEmpty()) "Halo, $it" else ""}
        binding.titleBarDark.tvTitleBarDescription.visibility = binding.titleBarDark.tvTitleBarDescription.text.let { if (it.isNotEmpty()) View.VISIBLE else View.GONE }
        binding.titleBarDark.icBack.visibility = View.VISIBLE
        binding.titleBarDark.icBack.setOnClickListener {
            if (activeTab != 0) tabLayout.getTabAt(0)?.select()
            else finish()
        }
        binding.titleBarDark.vBorder.visibility = View.GONE
        binding.titleBarDark.tvTitleBarDescription.isSelected = true

        absentProgressDialog = ProgressDialog(this)
        absentProgressDialog.setCancelable(false)
        absentProgressDialog.setMessage(getString(R.string.txt_loading))

        CustomUtility(this).setUserStatusOnline(true, userDistributorIds ?: "-custom-007", userId)
        checkSwipeRefreshLayoutHint()
        initLayout()

    }

    @SuppressLint("HardwareIds")
    private fun logoutHandler() {

        // Firebase Auth Session
        lifecycleScope.launch {
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
                if (e is CancellationException) {
                    return@launch
                }
                FirebaseUtils.logErr(this@CourierActivity, "Failed CourierActivity on logoutHandler(). Catch: ${e.message}")
                Log.d("Firebase Auth", "$e")
            }
        }

        sessionManager.setLoggedIn(LOGGED_OUT)
        sessionManager.setUserLoggedIn(null)

        val intent = Intent(this@CourierActivity, SplashScreenActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getUserLoggedIn() {

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.detailUser(userId = userId)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        val data = response.results[0]
                        sessionManager.setUserLoggedIn(data)

//                        tvTitleBarDescription.text = sessionManager.fullName().let { if (!it.isNullOrEmpty()) "Halo, $it" else "Halo, ${ sessionManager.userName() }"}
                        binding.titleBarDark.tvTitleBarDescription.text = sessionManager.userName().let { if (!it.isNullOrEmpty()) "Halo, $it" else ""}
                        binding.titleBarDark.tvTitleBarDescription.visibility = binding.titleBarDark.tvTitleBarDescription.text.let { if (it.isNotEmpty()) View.VISIBLE else View.GONE }
                        setTitleBarAbsent(userAbsentDateTime)

                    }
                    RESPONSE_STATUS_EMPTY -> missingDataHandler()

                }


            } catch (e: Exception) {
                if (e is CancellationException) {
                    return@launch
                }
                FirebaseUtils.logErr(this@CourierActivity, "Failed CourierActivity on getUserLoggedIn(). Catch: ${e.message}")
                Log.d("TAG USER LOGGED IN", "Failed run service. Exception " + e.message)
            }

        }

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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (activeTab != 0) tabLayout.getTabAt(0)?.select()
        else super.onBackPressed()
    }

    private fun checkUserAbsent() {
        absentProgressDialog.show()

        val absentChild = firebaseReference.child(FIREBASE_CHILD_ABSENT)
        val userChild = absentChild.child(userId)

        userChild.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Do something
                    val absentDateTime = snapshot.child("morningDateTime").getValue(String::class.java).toString()

                    if (absentDateTime.isNotEmpty()) {
                        sessionManager.absentDateTime(absentDateTime)

                        val absentDate = DateFormat.format(absentDateTime, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd")

                        if (DateFormat.dateAfterNow(absentDate)) {
                            showDialogAbsent(userChild)
                            setTitleBarAbsent(absentDateTime)
                        } else {
                            checkPermission {
                                absentProgressDialog.dismiss()
                                setTitleBarAbsent(absentDateTime)
                                initLayout()
                            }
                        }

                    } else {
                        showDialogAbsent(userChild)
                    }
                } else {
                    showDialogAbsent(userChild)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Do something
                showDialogAbsent(userChild)
            }

        })

    }

    private fun showDialogAbsent(userChild: DatabaseReference) {
        absentProgressDialog.dismiss()

        AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle("Yuk, Catat Kehadiranmu Hari ini!")
            .setMessage("Absenmu penting! Jangan lupa untuk mencatat kehadiranmu sekarang dan ciptakan jejak kerja yang positif.")
            .setPositiveButton("Absen Sekarang") { dialog, _ ->

                checkPermission(userChild, dialog)

            }
            .setNegativeButton("Tutup Aplikasi") { _, _ -> finish() }
            .show()
    }

    private fun initFirebase(userChild: DatabaseReference) {
        absentProgressDialog.show()

        userChild.child("id").setValue(userId)
        userChild.child("username").setValue(username)
        userChild.child("fullname").setValue(fullname)
        userChild.child("lat").setValue("")
        userChild.child("lng").setValue("")
        userChild.child("eveningDateTime").setValue("")
        userChild.child("isOnline").setValue(true)

        val absentDateTime = DateFormat.now()
        userChild.child("morningDateTime").setValue(absentDateTime)
        userChild.child("lastSeen").setValue(absentDateTime)

        sessionManager.absentDateTime(absentDateTime)
        setTitleBarAbsent(absentDateTime)

        val serviceIntent = Intent(this, TrackingService::class.java)
        serviceIntent.putExtra("userId", userId)
        serviceIntent.putExtra("userDistributorId", userDistributorIds ?: "-start-003-$username")
        serviceIntent.putExtra("deliveryId", AUTH_LEVEL_COURIER + userId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        initLayout()
    }

    private fun checkSwipeRefreshLayoutHint() {
        val swipeRefreshHint = sessionManager.swipeRefreshHint()

        if (!swipeRefreshHint) {
            binding.includeSwipeRefreshHint.swipeRefreshHint.visibility = View.VISIBLE
            binding.includeSwipeRefreshHint.btnTrySwipeRefresh.setOnClickListener {
                binding.includeSwipeRefreshHint.swipeRefreshHint.visibility = View.GONE
                sessionManager.swipeRefreshHint(true)
            }
        } else binding.includeSwipeRefreshHint.swipeRefreshHint.visibility = View.GONE
    }

    private fun initLayout() {
        binding.tabContainer.visibility = View.VISIBLE

        tabLayout = binding.tabLayout
        viewPager = binding.viewPager

        pagerAdapter = CourierViewPagerAdapter(supportFragmentManager)
        viewPager.adapter = pagerAdapter

        // Connect TabLayout and ViewPager
        tabLayout.setupWithViewPager(viewPager)
        pagerAdapter.setCounterPageItem(object : CourierViewPagerAdapter.CounterPageItem{
            override fun counterItem(count: Int, tabIndex: Int) {
                if (tabIndex == 0) tabLayout.getTabAt(tabIndex)?.text = "Toko${if (count != 0) " ($count)" else ""}"
                else tabLayout.getTabAt(tabIndex)?.text = "Basecamp${if (count != 0) " ($count)" else ""}"
            }

        })
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                activeTab = tab?.position!!
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })

        if (CustomUtility(this).isDarkMode()) {
            tabLayout.setBackgroundColor(getColor(R.color.black_300))
            tabLayout.setTabTextColors(getColor(R.color.black_600), getColor(R.color.primary))
            tabLayout.setSelectedTabIndicatorColor(getColor(R.color.primary))
        }
        else {
            tabLayout.setBackgroundColor(getColor(R.color.primary))
            tabLayout.setTabTextColors(getColor(R.color.primary_600), getColor(R.color.white))
            tabLayout.setSelectedTabIndicatorColor(getColor(R.color.white))
        }

        val tabIndexFromIntent = intent.getIntExtra("tabIndex", 0)
        activeTab = tabIndexFromIntent
        tabLayout.getTabAt(activeTab)?.select()

        binding.titleBarDark.icSyncNow.visibility = View.VISIBLE
        binding.titleBarDark.icSyncNow.setOnClickListener { pagerAdapter.setSyncAction(activeTab) }

        absentProgressDialog.dismiss()
    }

    private fun setTitleBarAbsent(dateString: String) {

        val dateDesc = DateFormat.differenceDateNowDesc(dateString)
        val date = DateFormat.format(dateString, "yyyy-MM-dd HH:mm:ss", "HH.mm")
        binding.titleBarDark.tvTitleBarDescription.text = "Halo $username, absenmu tercatat pukul $date $dateDesc"

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val absentChild = firebaseReference.child(FIREBASE_CHILD_ABSENT)
                val userChild = absentChild.child(userId)
                checkPermission(userChild = userChild)
            } else {
                val message = getString(R.string.bg_service_location_permission_message)
                val title = getString(R.string.bg_service_location_permission_title)
                AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(getString(R.string.open_settings)) { localDialog, _ ->
                        localDialog.dismiss()
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", packageName, null)
                        startActivityForResult(intent, LOCATION_PERMISSION_REQUEST_CODE)
                    }
                    .show()
            }
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            checkUserAbsent()
        }
    }

    private fun checkPermission(userChild: DatabaseReference? = null, dialog: DialogInterface? = null, validAction: (() -> Unit)? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {

                    if (validAction != null) validAction()
                    else {
                        if (userChild != null) initFirebase(userChild)
                        dialog?.dismiss()
                    }

                } else {
                    dialog?.dismiss()
                    val message = getString(R.string.bg_service_location_permission_message)
                    val title = getString(R.string.bg_service_location_permission_title)
                    AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(getString(R.string.open_settings)) { localDialog, _ ->
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                                LOCATION_PERMISSION_REQUEST_CODE
                            )
                            localDialog.dismiss()
                        }
                        .show()
                }
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
                dialog?.dismiss()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (validAction != null) validAction()
                else {
                    if (userChild != null) initFirebase(userChild)
                    dialog?.dismiss()
                }
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
                dialog?.dismiss()
            }
        }
    }

    override fun onResume() {

        super.onResume()
        getUserLoggedIn()

    }

    override fun onStart() {
        super.onStart()
        Handler(Looper.getMainLooper()).postDelayed({
            CustomUtility(this).setUserStatusOnline(true, userDistributorIds ?: "-custom-007", userId)
        }, 1000)
    }

    override fun onStop() {
        super.onStop()
        CustomUtility(this).setUserStatusOnline(false, userDistributorIds ?: "-custom-007", userId)
    }

    override fun onDestroy() {
        super.onDestroy()
        CustomUtility(this).setUserStatusOnline(false, userDistributorIds ?: "-custom-007", userId)
    }

}