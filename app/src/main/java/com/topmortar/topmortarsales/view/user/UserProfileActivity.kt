@file:Suppress("DEPRECATION")

package com.topmortar.topmortarsales.view.user

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_COURIER
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_SALES
import com.topmortar.topmortarsales.commons.CONST_COURIER_ID
import com.topmortar.topmortarsales.commons.CONST_FULL_NAME
import com.topmortar.topmortarsales.commons.CONST_IS_NOTIFY
import com.topmortar.topmortarsales.commons.CONST_IS_TRACKING_COURIER
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.CONST_USER_CITY
import com.topmortar.topmortarsales.commons.CONST_USER_ID
import com.topmortar.topmortarsales.commons.CONST_USER_LEVEL
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_ABSENT
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_AUTH
import com.topmortar.topmortarsales.commons.LOGGED_OUT
import com.topmortar.topmortarsales.commons.MANAGE_USER_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN_CITY
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.services.TrackingService
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.EventBusUtils
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityUserProfileBinding
import com.topmortar.topmortarsales.modal.ChartSalesPricingModal
import com.topmortar.topmortarsales.view.MapsActivity
import com.topmortar.topmortarsales.view.SplashScreenActivity
import com.topmortar.topmortarsales.view.reports.ReportsActivity
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import java.util.Calendar

@SuppressLint("SetTextI18n")
class UserProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private val userDistributorId get() = sessionManager.userDistributor()
    private val userKind get() = sessionManager.userKind()
    private val userId get() = sessionManager.userID()
    private val userCity get() = sessionManager.userCityID()

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var customUtility: CustomUtility
    private lateinit var modalPricingDetails: ChartSalesPricingModal

    private var iUserName: String? = null; private var iFullName: String? = null; private var iUserLevel: String? = null
    private var iUserID: String? = null; private var iPhone: String? = null; private var iLocation: String? = null
    private var iIsNotify: String? = null; private var iUserCity: String? = null

    private var isRequestSync = false

    private lateinit var firebaseReference: DatabaseReference
    private var childAbsent: DatabaseReference? = null
    private var childCourier: DatabaseReference? = null
    private var courierTrackingListener: ValueEventListener? = null

    private var isAbsentMorningNow = false
    private var isAbsentEveningNow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseReference = FirebaseUtils().getReference(distributorId = userDistributorId.toString())

        iUserID = intent.getStringExtra(CONST_USER_ID)
        iPhone = intent.getStringExtra(CONST_PHONE)
        iUserName = intent.getStringExtra(CONST_NAME)
        iFullName = intent.getStringExtra(CONST_FULL_NAME)
        iUserLevel = intent.getStringExtra(CONST_USER_LEVEL)
        iLocation = intent.getStringExtra(CONST_LOCATION)
        iIsNotify = intent.getStringExtra(CONST_IS_NOTIFY)
        iUserCity = intent.getStringExtra(CONST_USER_CITY)

        customUtility = CustomUtility(this)
        modalPricingDetails = ChartSalesPricingModal(this)

        binding.salesReportContainer.visibility = View.VISIBLE

//        if (sessionManager.userKind() == USER_KIND_COURIER || sessionManager.userKind() == USER_KIND_SALES) {
//            CustomUtility(this).setUserStatusOnline(true, sessionManager.userDistributor().toString(), sessionManager.userID().toString())
//            checkAbsent()
//        }
//        if (iUserLevel == AUTH_LEVEL_COURIER || (iUserLevel == AUTH_LEVEL_SALES || userKind == USER_KIND_SALES)) {
//            binding.salesReportContainer.visibility = View.GONE
//            binding.deliveryContainer.visibility = View.VISIBLE
//
//            if (iUserLevel == AUTH_LEVEL_COURIER) {
//                binding.btnHistoryVisit.visibility = View.GONE
//                binding.btnCourierHistoryDelivery.visibility = View.VISIBLE
//            } else if (iUserLevel == AUTH_LEVEL_SALES || userKind == USER_KIND_SALES) {
//                binding.btnHistoryVisit.visibility = View.VISIBLE
//                binding.btnCourierHistoryDelivery.visibility = View.GONE
//                if (userKind == USER_KIND_SALES) binding.btnCourierTracking.visibility = View.GONE
//            }
//
//            setupCourierMenu()
//        }
//
//        initClickHandler()
        dataActivityValidation()

    }

    private fun getUserDetail(isRequestSync: Boolean = false) {
        binding.titleBarLight.tvTitleBar.text = getString(R.string.txt_loading)
        binding.tvFullName.text = getString(R.string.txt_loading)
        binding.container.visibility = View.GONE

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val userId = when (isRequestSync) {
                    true -> iUserID.toString()
                    else -> sessionManager.userID().toString()
                }
                val response = apiService.detailUser(userId = userId)
                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        val data = response.results[0]
                        if (!isRequestSync) sessionManager.setUserLoggedIn(data)

                        iUserID = data.id_user
                        iPhone = data.phone_user
                        iUserName = data.username
                        iFullName = data.full_name
                        iUserLevel = data.level_user
                        iLocation = data.id_city
                        iIsNotify = data.is_notify
                        binding.container.visibility = View.VISIBLE

                        initClickHandler()
                        dataActivityValidation()

                    } RESPONSE_STATUS_EMPTY -> handleMessage(this@UserProfileActivity, "GET USER DETAIL", "Gagal memuat detail pengguna: ${response.message}")
                    else -> handleMessage(this@UserProfileActivity, "GET USER DETAIL", "Gagal memuat data. Error: ${response.message}")
                }

            } catch (e: Exception) {
                handleMessage(this@UserProfileActivity, "GET USER DETAIL", "Failed run service. Error: ${e.message}")
            }

        }
    }

    private fun navigateEditUser() {

        val intent = Intent(this, AddUserActivity::class.java)

        intent.putExtra(CONST_USER_ID, iUserID)
        intent.putExtra(CONST_PHONE, iPhone)
        intent.putExtra(CONST_NAME, iUserName)
        intent.putExtra(CONST_USER_LEVEL, iUserLevel)
        intent.putExtra(CONST_LOCATION, iLocation)
        intent.putExtra(CONST_FULL_NAME, iFullName)
        intent.putExtra(CONST_IS_NOTIFY, iIsNotify)

        startActivityForResult(intent, MANAGE_USER_ACTIVITY_REQUEST_CODE)

    }

    private fun navigateHistoryVisit() {

        val intent = Intent(this@UserProfileActivity, HistoryVisitedActivity::class.java)
        intent.putExtra(CONST_USER_ID, iUserID ?: userId)
        intent.putExtra(CONST_USER_CITY, iUserCity ?: userCity)
        startActivity(intent)

    }

    private fun navigateSalesReport() {

        val intent = Intent(this@UserProfileActivity, ReportsActivity::class.java)
        intent.putExtra(CONST_USER_ID, iUserID)
        intent.putExtra(CONST_FULL_NAME, iFullName)
        intent.putExtra(CONST_USER_LEVEL, iUserLevel)
        startActivity(intent)

    }

    private fun navigateTrackingCourier() {

        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra(CONST_IS_TRACKING_COURIER, true)
        intent.putExtra(CONST_COURIER_ID, iUserID)

        startActivityForResult(intent, MANAGE_USER_ACTIVITY_REQUEST_CODE)

    }

    private fun initClickHandler() {

        if (sessionManager.userKind() == USER_KIND_ADMIN || sessionManager.userKind() == USER_KIND_ADMIN_CITY) {
            binding.titleBarLight.icEdit.visibility = View.VISIBLE
            binding.titleBarLight.icEdit.setOnClickListener { navigateEditUser() }
        }

        if (sessionManager.userKind() == USER_KIND_COURIER || sessionManager.userKind() == USER_KIND_SALES) {
            binding.btnLogout.setOnClickListener { logoutConfirmation() }
        }

        binding.toggleBarChart.setOnClickListener { toggleBarChart() }
        binding.priceContainer.setOnClickListener { modalPricingDetails.show() }
        binding.titleBarLight.icBack.setOnClickListener { backHandler() }
        binding.salesReportContainer.setOnClickListener { navigateSalesReport() }

    }

    private fun dataActivityValidation() {

        if (sessionManager.userKind() == USER_KIND_COURIER || sessionManager.userKind() == USER_KIND_SALES) {
            CustomUtility(this).setUserStatusOnline(true, sessionManager.userDistributor().toString(), sessionManager.userID().toString())
            checkAbsent()
        }
        if (iUserLevel == AUTH_LEVEL_COURIER || (iUserLevel == AUTH_LEVEL_SALES || userKind == USER_KIND_SALES)) {
            binding.salesReportContainer.visibility = View.GONE
            binding.deliveryContainer.visibility = View.VISIBLE

            if (iUserLevel == AUTH_LEVEL_COURIER) {
                binding.btnHistoryVisit.visibility = View.GONE
                binding.btnCourierHistoryDelivery.visibility = View.VISIBLE
            } else if (iUserLevel == AUTH_LEVEL_SALES || userKind == USER_KIND_SALES) {
                binding.btnHistoryVisit.visibility = View.VISIBLE
                binding.btnCourierHistoryDelivery.visibility = View.GONE
                if (userKind == USER_KIND_SALES) binding.btnCourierTracking.visibility = View.GONE
            }

            setupCourierMenu()
        }

        initClickHandler()

        if (iUserID.isNullOrEmpty()) {
            return getUserDetail()
        }

        if (iUserName!!.isNotEmpty()) {
            binding.titleBarLight.tvTitleBar.text = iUserName
            binding.titleBarLight.tvTitleBarDescription.visibility = View.VISIBLE
            binding.titleBarLight.tvTitleBarDescription.text = iUserLevel
        }
        if (iFullName!!.isNotEmpty()) binding.tvFullName.text = iFullName
        if (iUserLevel!!.isNotEmpty()) binding.tvLevel.text = iUserLevel

//        if (iUserLevel == AUTH_LEVEL_SALES) {
//
//            if (sessionManager.userKind() == USER_KIND_ADMIN || sessionManager.userKind() == USER_KIND_ADMIN_CITY) binding.titleBarLight.icEdit.visibility = View.VISIBLE
//            binding.priceContainer.visibility = View.GONE
//            binding.tabContainer.visibility = View.VISIBLE
////            binding.counterContainer.visibility = View.VISIBLE
//
//            val tabLayout: TabLayout = binding.tabLayout
//            val viewPager: ViewPager = binding.viewPager // If using ViewPager
//
//            val pagerAdapter = UserProfileViewPagerAdapter(supportFragmentManager) // Create your PagerAdapter
//
//            pagerAdapter.setUserCityParam(iLocation)
//            pagerAdapter.setUserIdParam(iUserID)
//            viewPager.adapter = pagerAdapter
//
//            // Connect TabLayout and ViewPager
//            tabLayout.setupWithViewPager(viewPager)
//            pagerAdapter.setCounterPageItem(object : UserProfileViewPagerAdapter.CounterPageItem{
//                override fun counterItem(count: Int, tabIndex: Int) {
////                    if (tabIndex == 0) tabLayout.getTabAt(tabIndex)?.text = "On Bid ($count/$bidLimit)"
//                    if (tabIndex == 0) tabLayout.getTabAt(tabIndex)?.text = "Visited ($count)"
//                    else tabLayout.getTabAt(tabIndex)?.text = "Visited ($count)"
//                }
//
//            })
//
//        } else binding.tabContainer.visibility = View.GONE

//        setupBarChart()

    }

    private fun toggleBarChart() {
        val barChart = binding.storeBarChart
        val title = binding.tvToggleBarChart
        val icon = binding.iconToggleBarChart

        if (barChart.isVisible) {
            barChart.visibility = View.GONE
            title.text = "Statistik Toko di Kota Malang (tampilkan)"
            icon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.chevron_down_solid))
        } else {
            barChart.visibility = View.VISIBLE
            title.text = "Statistik Toko di Kota Malang (sembunyikan)"
            icon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.chevron_up_solid))
        }
    }

    @Subscribe
    fun onEventBus(event: EventBusUtils.ContactModelEvent) {
//        navigateDetailContact(event.data)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MANAGE_USER_ACTIVITY_REQUEST_CODE) {

            val resultData = data?.getStringExtra("$MANAGE_USER_ACTIVITY_REQUEST_CODE")

            if (resultData == SYNC_NOW) {

                isRequestSync = true
                getUserDetail(true)

            }

        }

    }

    private fun backHandler() {
        if (isRequestSync) {
            val resultIntent = Intent()
            resultIntent.putExtra("$MANAGE_USER_ACTIVITY_REQUEST_CODE", SYNC_NOW)
            setResult(RESULT_OK, resultIntent)
            finish()
        } else finish()
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

    @SuppressLint("HardwareIds")
    private fun logoutHandler() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage(getString(R.string.txt_loading))
        progressDialog.show()

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

            if (sessionManager.userKind() == USER_KIND_COURIER || sessionManager.userKind() == USER_KIND_SALES) {
//                Log.d("Kurir Logout", "${sessionManager.userDistributor()} : ${sessionManager.userID()}")
                CustomUtility(this).setUserStatusOnline(false, sessionManager.userDistributor().toString(), sessionManager.userID().toString())
            }
        } catch (e: Exception) {
            Log.d("Firebase Auth", "$e")
        }

        val isTracking = CustomUtility(this).isServiceRunning(TrackingService::class.java)
        if (isTracking) {
            val serviceIntent = Intent(this, TrackingService::class.java)
            this.stopService(serviceIntent)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            sessionManager.setLoggedIn(LOGGED_OUT)
            sessionManager.setUserLoggedIn(null)

            progressDialog.dismiss()
            val intent = Intent(this, SplashScreenActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }, 1000)
    }

    private fun  setupCourierMenu() {

        val personCall = if (userKind == USER_KIND_SALES) "Anda" else "Pengguna"
        val personcall = if (userKind == USER_KIND_SALES) "anda" else "pengguna"

        childAbsent = firebaseReference.child(FIREBASE_CHILD_ABSENT)
        childCourier = childAbsent?.child(iUserID ?: userId ?: "0")

        if (userKind != USER_KIND_SALES) {
            courierTrackingListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Do something here
                    if (snapshot.child("isOnline").exists()) {

                        val padding8 = convertDpToPx(8, this@UserProfileActivity)
                        val padding2 = convertDpToPx(2, this@UserProfileActivity)
                        binding.userStatus.visibility = View.VISIBLE

                        val isOnline = snapshot.child("isOnline").getValue(Boolean::class.java) ?: false
                        if (isOnline) {
                            binding.userStatus.text = "Online"
                            binding.userStatus.setTextColor(getColor(R.color.white))
                            binding.userStatus.setBackgroundResource(R.drawable.bg_green_reseda_round_8)
                            binding.userStatus.setPadding(padding8, padding2, padding8, padding2)
                        } else {
                            if (snapshot.child("lastSeen").exists()) {
                                val lastSeen = snapshot.child("lastSeen").getValue(String::class.java).toString()
                                val dateDescEvening = DateFormat.differenceDateNowDesc(lastSeen)
                                val dateEvening =
                                    DateFormat.format(lastSeen, "yyyy-MM-dd HH:mm:ss", "HH.mm")
                                binding.userStatus.text = "Terakhir terlihat $dateEvening $dateDescEvening"
                                binding.userStatus.setTextColor(getColor(R.color.black_200))
                                binding.userStatus.setBackgroundResource(android.R.color.transparent)
                                binding.userStatus.setPadding(0, padding2, 0, padding2)
                            } else {
                                binding.userStatus.text = "Offline"
                                binding.userStatus.setTextColor(getColor(R.color.black_200))
                                binding.userStatus.setBackgroundResource(R.drawable.bg_light_dark_round)
                                binding.userStatus.setPadding(padding8, padding2, padding8, padding2)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Do something here
                }

            }
            childCourier?.addValueEventListener(courierTrackingListener!!)
        }

        childCourier?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Do something
                    if (snapshot.child("morningDateTime").exists()) {
                        val morningDateTime = snapshot.child("morningDateTime").getValue(String::class.java).toString()
                        if (morningDateTime.isNotEmpty()) {
                            sessionManager.absentDateTime(morningDateTime)

                            val absentMorningDate = DateFormat.format(morningDateTime, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd")

                            if (DateFormat.dateAfterNow(absentMorningDate)) {
                                // Absent morning false
                                binding.absentTitle.text = "$personCall Belum Absen"
                                binding.absentDescription.text = "Absen $personcall hari ini belum tercatat"
                            } else {

                                // Absent morning true
                                val dateDescMorning = DateFormat.differenceDateNowDesc(morningDateTime)
                                val dateMorning = DateFormat.format(morningDateTime, "yyyy-MM-dd HH:mm:ss", "HH.mm")
                                binding.absentTitle.text = "$personCall Telah Absen"
                                binding.absentDescription.text = "Absen $personcall telah tercatat pada pukul $dateMorning $dateDescMorning"

                                if (snapshot.child("eveningDateTime").exists()) {
                                    val eveningDateTime = snapshot.child("eveningDateTime").getValue(String::class.java).toString()

                                    if (eveningDateTime.isNotEmpty()) {
                                        val absentEveningDate = DateFormat.format(eveningDateTime, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd")

                                        if (DateFormat.dateAfterNow(absentEveningDate)) {
                                            // Absent evening false
                                        } else {
                                            // Absent evening true
                                            val dateDescEvening = DateFormat.differenceDateNowDesc(eveningDateTime)
                                            val dateEvening = DateFormat.format(eveningDateTime, "yyyy-MM-dd HH:mm:ss", "HH.mm")
                                            binding.absentTitle.text = "$personCall Telah Pulang"
                                            binding.absentDescription.text = "Absen $personcall telah tercatat pada pukul $dateMorning $dateDescMorning dan pulang pada pukul $dateEvening $dateDescEvening"
                                        }
                                    } else {
                                        // Absent evening false
                                    }
                                } else {
                                    // Absent evening false
                                }

                            }

                        } else {
                            // Absent morning false
                            binding.absentTitle.text = "$personCall Belum Absen"
                            binding.absentDescription.text = "Absen $personcall hari ini belum tercatat"
                        }
                    } else {
                        // Absent morning false
                        binding.absentTitle.text = "$personCall Belum Absen"
                        binding.absentDescription.text = "Absen $personcall hari ini belum tercatat"
                    }
                } else {
                    // Absent morning false
                    binding.absentTitle.text = "$personCall Belum Absen"
                    binding.absentDescription.text = "Absen $personcall hari ini belum tercatat"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Do something here
            }

        })

        binding.btnHistoryVisit.setOnClickListener { navigateHistoryVisit() }
        binding.btnCourierReport.setOnClickListener { navigateSalesReport() }
        binding.btnCourierTracking.setOnClickListener { navigateTrackingCourier() }
    }

    private fun lockBtnLogout() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY) // Mengambil jam saat ini dalam format 24 jam

//        if (isAbsentMorningNow && !isAbsentEveningNow && currentHour < 16) {
//            binding.btnLogout.visibility = View.GONE
//            binding.lockedInfoText.visibility = View.VISIBLE
//        } else {
            binding.btnLogout.visibility = View.VISIBLE
            binding.lockedInfoText.visibility = View.GONE
//        }
    }

    private fun checkAbsent() {

        val userId = sessionManager.userID().toString()
        val absentChild = firebaseReference.child(FIREBASE_CHILD_ABSENT)
        val userChild = absentChild.child(userId)

        userChild.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Do something
                    if (snapshot.child("morningDateTime").exists()) {
                        val morningDateTime = snapshot.child("morningDateTime").getValue(String::class.java).toString()
                        if (morningDateTime.isNotEmpty()) {
                            sessionManager.absentDateTime(morningDateTime)

                            val absentMorningDate = DateFormat.format(morningDateTime, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd")

                            if (DateFormat.dateAfterNow(absentMorningDate)) {
                                isAbsentMorningNow = false
                                lockBtnLogout()
                            } else {

                                isAbsentMorningNow = true
                                if (snapshot.child("eveningDateTime").exists()) {
                                    val eveningDateTime = snapshot.child("eveningDateTime").getValue(String::class.java).toString()

                                    if (eveningDateTime.isNotEmpty()) {
                                        val absentEveningDate = DateFormat.format(eveningDateTime, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd")

                                        if (DateFormat.dateAfterNow(absentEveningDate)) {
                                            isAbsentEveningNow = false
                                            lockBtnLogout()
                                        } else {
                                            isAbsentEveningNow = true
                                            lockBtnLogout()
                                        }

                                    } else {
                                        isAbsentEveningNow = false
                                        lockBtnLogout()
                                    }
                                } else {
                                    isAbsentEveningNow = false
                                    lockBtnLogout()
                                }

                            }

                        } else {
                            isAbsentMorningNow = false
                            lockBtnLogout()
                        }
                    } else {
                        isAbsentMorningNow = false
                        lockBtnLogout()
                    }
                } else {
                    isAbsentMorningNow = false
                    lockBtnLogout()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Do something
                isAbsentMorningNow = false
                isAbsentEveningNow = false
                lockBtnLogout()
            }

        })
    }

// Override Class

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        backHandler()
    }

    override fun onStart() {
        super.onStart()
//        EventBus.getDefault().register(this)
        Handler(Looper.getMainLooper()).postDelayed({
            if (sessionManager.isLoggedIn()) {
                if (sessionManager.userKind() == USER_KIND_COURIER || sessionManager.userKind() == USER_KIND_SALES) {
                    CustomUtility(this).setUserStatusOnline(
                        true,
                        sessionManager.userDistributor().toString(),
                        sessionManager.userID().toString()
                    )
                }
            }
        }, 1000)
    }

    override fun onStop() {
        super.onStop()
//        EventBus.getDefault().unregister(this)
        if (sessionManager.isLoggedIn()) {
            if (sessionManager.userKind() == USER_KIND_COURIER || sessionManager.userKind() == USER_KIND_SALES) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    sessionManager.userDistributor().toString(),
                    sessionManager.userID().toString()
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (sessionManager.isLoggedIn()) {
            if (sessionManager.userKind() == USER_KIND_COURIER || sessionManager.userKind() == USER_KIND_SALES) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    sessionManager.userDistributor().toString(),
                    sessionManager.userID().toString()
                )
            }
        }
    }

}