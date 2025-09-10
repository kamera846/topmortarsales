package com.topmortar.topmortarsales.view.rencanaVisits

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DatabaseReference
import com.google.firebase.messaging.FirebaseMessaging
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.recyclerview.HomeSalesMenuRV
import com.topmortar.topmortarsales.commons.ABSENT_MODE_BASECAMP
import com.topmortar.topmortarsales.commons.ABSENT_MODE_STORE
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_COURIER
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_MARKETING
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_PENAGIHAN
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_SALES
import com.topmortar.topmortarsales.commons.CONST_FULL_NAME
import com.topmortar.topmortarsales.commons.CONST_IS_BASE_CAMP
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_CITY_ID
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_NAME
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_STATUS
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_MAPS_NAME
import com.topmortar.topmortarsales.commons.CONST_NEAREST_STORE
import com.topmortar.topmortarsales.commons.CONST_USER_ID
import com.topmortar.topmortarsales.commons.CONST_USER_LEVEL
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_ABSENT
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_AUTH
import com.topmortar.topmortarsales.commons.LOGGED_OUT
import com.topmortar.topmortarsales.commons.MAX_REPORT_DISTANCE
import com.topmortar.topmortarsales.commons.NOTIFICATION_LEVEL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_ACTION_MAIN_ACTIVITY
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.USER_KIND_MARKETING
import com.topmortar.topmortarsales.commons.USER_KIND_PENAGIHAN
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.services.TrackingService
import com.topmortar.topmortarsales.commons.utils.CustomProgressBar
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.PermissionsHandler
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.SntpClient
import com.topmortar.topmortarsales.commons.utils.URLUtility
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityHomeSalesBinding
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.BaseCampModel
import com.topmortar.topmortarsales.model.ContactModel
import com.topmortar.topmortarsales.model.HomeMenuSalesModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import com.topmortar.topmortarsales.view.ChartActivity
import com.topmortar.topmortarsales.view.MainActivity
import com.topmortar.topmortarsales.view.MapsActivity
import com.topmortar.topmortarsales.view.PermissionActivity
import com.topmortar.topmortarsales.view.SplashScreenActivity
import com.topmortar.topmortarsales.view.contact.NewRoomChatFormActivity
import com.topmortar.topmortarsales.view.courier.AddBaseCampActivity
import com.topmortar.topmortarsales.view.product.ProductsActivity
import com.topmortar.topmortarsales.view.reports.ReportsActivity
import com.topmortar.topmortarsales.view.tukang.ListTukangActivity
import com.topmortar.topmortarsales.view.user.UserProfileActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.cancellation.CancellationException

class HomeSalesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeSalesBinding
    private lateinit var apiService: ApiService

    private lateinit var sessionManager: SessionManager
    private val userId get() = sessionManager.userID()
    private val userCity get() = sessionManager.userCityID()
    private val userName get() = sessionManager.userName()
    private val userFullName get() = sessionManager.fullName()
    private val userKind get() = sessionManager.userKind()
    private val userDistributorId get() = sessionManager.userDistributor()
    private val userDistributorNumber get() = sessionManager.userDistributorNumber()
    private val selectedStoreDefaultID get() = sessionManager.selectedStoreAbsentID()
    private val selectedStoreDefaultTitle get() = sessionManager.selectedStoreAbsentTitle()
    private val selectedStoreDefaultCoordinate get() = sessionManager.selectedStoreAbsentCoordinate()
    private val selectedAbsentMode get() = sessionManager.selectedAbsentMode()
    private val userAuthLevel get() = sessionManager.userAuthLevel()

    private var doubleBackToExitPressedOnce = false
    private var isAbsentMorningNow = false
    private var isAbsentEveningNow = false
    private var isLocked = false

    private lateinit var firebaseReference: DatabaseReference
    private var absentProgressBar: CustomProgressBar? = null
    private var progressBar: CustomProgressBar? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var customUtility: CustomUtility

    private var locationCallback: LocationCallback? = null

    private lateinit var searchStoreAbsentModal: SearchModal
    private var selectedStore: ModalSearchModel? = null
    private var listStore: ArrayList<ContactModel> = arrayListOf()
    private var listBaseCamp: ArrayList<BaseCampModel> = arrayListOf()
    private var isSelectStoreOnly = false

    private var absentMode: String? = null

    private val locationResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            checkLocationPermission()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge()

        binding = ActivityHomeSalesBinding.inflate(layoutInflater)
        sessionManager = SessionManager(this)

        setContentView(binding.root)

        CustomUtility(this).setUserStatusOnline(
            true,
            sessionManager.userDistributor() ?: "-custom-010",
            sessionManager.userID() ?: ""
        )

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                myOnBackPressed()
            }

        })

    }

    private fun checkPermissionsRequirement(): Boolean {
        if (!PermissionsHandler.isAllLocationRequirementMet(this)) {
            dismissProgressDialog()
            val intent = Intent(this, PermissionActivity::class.java)
            locationResultLauncher.launch(intent)
            return false
        } else {
            return true
        }
    }

    private fun checkLocationPermission() {
        try {
            if (absentProgressBar == null) {
                absentProgressBar = CustomProgressBar(this)
                absentProgressBar!!.setCancelable(false)
            }
            absentProgressBar!!.setMessage(getString(R.string.txt_loading) + " 1 / 5")
            if (!absentProgressBar!!.isShowing()) absentProgressBar?.show()

            if (checkPermissionsRequirement()) {
                checkGpsStatus()
            }
        } catch (e: Exception) {
            if (e is CancellationException) {
                return
            }
            FirebaseUtils.logErr(this, "Failed HomeSalesActivity on checkLocationPermission(). Catch: ${e.message}")
            handleMessage(
                this,
                "Home Sales Failed",
                "Failed HomeSalesActivity on checkLocationPermission(). Error: ${e.message}"
            )
        }
    }

    private fun checkGpsStatus() {
        if (absentProgressBar != null) {
            absentProgressBar!!.setMessage(getString(R.string.txt_loading) + " 2 / 5")
        }
        try {

            val locationManager =
                getSystemService(LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            if (!isGpsEnabled) showGpsDisabledDialog()
            else checkMockLocation()

        } catch (e: Exception) {
            if (e is CancellationException) {
                return
            }
            FirebaseUtils.logErr(this, "Failed HomeSalesActivity on checkGpsStatus(). Catch: ${e.message}")
            handleMessage(
                this,
                "Home Sales Failed",
                "Failed HomeSalesActivity on checkGpsStatus(). Error: ${e.message}"
            )
        }
    }

    private fun checkMockLocation() {
        if (absentProgressBar != null) {
            absentProgressBar!!.setMessage(getString(R.string.txt_loading) + " 3 / 5")
        }
        try {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
                if (status != ConnectionResult.SUCCESS) {
                    showDialogGooglePlayNotAvailable()
                    return
                }

                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->

                    if (location == null) {

                        //                    checkLocationPermission()
                        if (locationCallback != null) {

                            fusedLocationClient.removeLocationUpdates(locationCallback!!)
                        }

                        val locationRequest = LocationRequest.create().apply {
                            interval = 3000
                            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        }

                        locationCallback = object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult) {

                                fusedLocationClient.removeLocationUpdates(this)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    val intent = Intent(
                                        this@HomeSalesActivity,
                                        HomeSalesActivity::class.java
                                    )
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                }, 3000)
                            }
                        }

                        fusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            locationCallback!!,
                            Looper.getMainLooper()
                        )
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && location.isMock) {

                            showDialogIsMock()
                        } else if (location.isFromMockProvider) {

                            showDialogIsMock()
                        } else initView()
                    }
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) {
                return
            }
            FirebaseUtils.logErr(this, "Failed HomeSalesActivity on checkMockLocation(). Catch: ${e.message}")
            handleMessage(
                this,
                "Home Sales Failed",
                "Failed HomeSalesActivity on checkMockLocation(). Error: ${e.message}"
            )
//        } finally {
//            if (absentProgressBar != null) {
//                absentProgressBar!!.setMessage(getString(R.string.txt_loading))
//            }
        }
    }

    private fun showDialogIsMock() {
        try {

            dismissProgressDialog()

            val serviceIntent = Intent(this, TrackingService::class.java)
            stopService(serviceIntent)

            val dialogView = layoutInflater.inflate(R.layout.modal_mock_location, null)
            AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .setTitle("Lokasi Mock")
                .setPositiveButton("Pengaturan") { _, _ ->
                    // Buka pengaturan Developer Options
                    val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                    locationResultLauncher.launch(intent)
                }
                .show()
        } catch (e: Exception) {
            if (e is CancellationException) {
                return
            }
            FirebaseUtils.logErr(this, "Failed HomeSalesActivity on showDialogIsMock(). Catch: ${e.message}")
            handleMessage(
                this,
                "Home Sales Failed",
                "Failed HomeSalesActivity on showDialogIsMock(). Error: ${e.message}"
            )
        }
    }

    private fun showDialogGooglePlayNotAvailable() {
        try {

            absentProgressBar?.dismiss()

            val serviceIntent = Intent(this, TrackingService::class.java)
            stopService(serviceIntent)

            AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Peringatan!")
                .setMessage("Perangkat anda tidak mendukung layanan 'Google Play Service', beberapa fitur di aplikasi mungkin tidak dapat berjalan.")
                .setNegativeButton("Tutup aplikasi") { dialog, _ ->
                    dialog.dismiss()
                    finishAffinity()
                }
                .setPositiveButton("Oke") { dialog, _ ->
                    dialog.dismiss()

                    FirebaseUtils.logErr(this, "Failed HomeSalesActivity on showDialogGooglePlayNotAvailable(). Catch: Google Play Services not available")
                    handleMessage(
                        this,
                        "Home Courier Failed",
                        "Failed HomeSalesActivity on showDialogGooglePlayNotAvailable(). Error: Google Play Services not available"
                    )

                    initView()
                }
                .show()
        } catch (e: Exception) {
            if (e is CancellationException) {
                return
            }
            FirebaseUtils.logErr(this, "Failed HomeSalesActivity on showDialogGooglePlayNotAvailable(). Catch: ${e.message}")
            handleMessage(
                this,
                "Home Courier Failed",
                "Failed HomeSalesActivity on showDialogGooglePlayNotAvailable(). Error: ${e.message}"
            )
        }
    }

    private fun showGpsDisabledDialog() {
        try {

            dismissProgressDialog()

            val serviceIntent = Intent(this, TrackingService::class.java)
            stopService(serviceIntent)

            AlertDialog.Builder(this)
                .setMessage("Aplikasi memerlukan lokasi untuk berfungsi. Aktifkan lokasi sekarang?")
                .setCancelable(false)
                .setPositiveButton("Ya") { _, _ ->
                    // Buka pengaturan untuk mengaktifkan GPS
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    locationResultLauncher.launch(intent)
                }
                .show()
        } catch (e: Exception) {
            if (e is CancellationException) {
                return
            }
            FirebaseUtils.logErr(this, "Failed HomeSalesActivity on showGpsDisabledDialog(). Catch: ${e.message}")
            handleMessage(
                this,
                "Home Sales Failed",
                "Failed HomeSalesActivity on showGpsDisabledDialog(). Error: ${e.message}"
            )
        }
    }

    private fun initGlobalVariable() {

        if (absentProgressBar == null) {
            absentProgressBar = CustomProgressBar(this)
            absentProgressBar!!.setMessage(getString(R.string.txt_loading))
            absentProgressBar!!.setCancelable(false)
        }

        val userDistributorIds = sessionManager.userDistributor()
        firebaseReference =
            FirebaseUtils.getReference(distributorId = userDistributorIds ?: "-firebase-014")
        apiService = HttpClient.create()
        customUtility = CustomUtility(this)
        absentMode = selectedAbsentMode

        // Set User Absent Level (TEMP)
        val absentChild = firebaseReference.child(FIREBASE_CHILD_ABSENT)
        val userChild = absentChild.child(userId ?: "0")
        val userLevel = when (userKind) {
            USER_KIND_PENAGIHAN -> AUTH_LEVEL_PENAGIHAN
            USER_KIND_SALES -> AUTH_LEVEL_SALES
            USER_KIND_COURIER -> AUTH_LEVEL_COURIER
            USER_KIND_MARKETING -> AUTH_LEVEL_MARKETING
            else -> ""
        }
        userChild.child("userLevel").setValue(userLevel)

        binding.selectedStoreContainer.tvLabel.text = "Lokasi absen:"

        if (selectedStoreDefaultID.isNullOrEmpty()) binding.selectedStoreContainer.tvFilter.text =
            "Pilih toko"
        else {
            binding.selectedStoreContainer.tvFilter.text = selectedStoreDefaultTitle
            selectedStore = ModalSearchModel(
                id = selectedStoreDefaultID,
                title = selectedStoreDefaultTitle,
                etc = selectedStoreDefaultCoordinate
            )
        }
        absentMode = selectedAbsentMode
        setupDialogSearch()
    }

    private fun initView() {
        if (absentProgressBar != null) {
            absentProgressBar!!.setMessage(getString(R.string.txt_loading) + " 4 / 5")
        }

        try {
            initGlobalVariable()
            binding.fullName.text = userFullName

            setListMenu()

            val currentNightMode =
                resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) binding.selectedStoreContainer.componentFilter.background =
                AppCompatResources.getDrawable(this, R.color.black_400)
            else binding.selectedStoreContainer.componentFilter.background =
                AppCompatResources.getDrawable(this, R.color.light)
            binding.selectedStoreContainer.componentFilter.setOnClickListener {
                isSelectStoreOnly = true
                getListAbsent()
            }

            binding.btnAbsent.setOnClickListener {

                if (checkPermissionsRequirement()) {
                    if (!absentProgressBar!!.isShowing()) absentProgressBar?.show()
                    if (selectedStore == null) getListAbsent()
                    else absentAction()
                }

            }

            lockMenuItem(true)

            checkAbsent()
//        getFcmToken()
//        Disabled FCM
//        subscribeFcmTopic()
        } catch (e: Exception) {
            if (e is CancellationException) {
                return
            }
            FirebaseUtils.logErr(this, "Failed HomeSalesActivity on initView(). Catch: ${e.message}")
            handleMessage(
                this,
                "Home Sales Failed",
                "Failed HomeSalesActivity on initView(). Error: ${e.message}"
            )
//        } finally {
//            if (absentProgressBar != null) {
//                absentProgressBar!!.setMessage(getString(R.string.txt_loading))
//            }
        }
    }

    private fun getFcmToken() {

        try {

            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.d("FCM", "FCM Token: $token")
            }
        } catch (e: Exception) {
            if (e is CancellationException) {
                return
            }
            FirebaseUtils.logErr(this, "Failed HomeSalesActivity on getFcmToken(). Catch: ${e.message}")
            Log.e("FCM", "Error get fcm token exception: $e")
        }

    }

    private fun subscribeFcmTopic() {
        val fcmTopic = "report_feedback_${userAuthLevel}_${userId}_${NOTIFICATION_LEVEL}"
        FirebaseMessaging.getInstance().subscribeToTopic(fcmTopic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Successfully subscribed to the fcmTopic
//                    Toast.makeText(applicationContext, "Subscribed to $fcmTopic", Toast.LENGTH_SHORT).show()
                } else {
                    // Handle failure
                    Toast.makeText(
                        applicationContext,
                        "Subscribe notification failed. Error: ${task.exception?.stackTrace}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun navigateToListReport() {
        val intent = Intent(this, ReportsActivity::class.java)
        intent.putExtra(CONST_USER_ID, userId)
        intent.putExtra(CONST_FULL_NAME, userFullName)
        intent.putExtra(CONST_USER_LEVEL, AUTH_LEVEL_SALES)
        startActivity(intent)
    }

    private fun navigateToNearestStore() {
        if (progressBar == null) {
            progressBar = CustomProgressBar(this)
            progressBar!!.setMessage("Memuat data toko…")
        }

        progressBar!!.show()

        Handler().postDelayed({

            lifecycleScope.launch {
                try {

                    val response = apiService.getContacts(
                        cityId = userCity ?: "0",
                        distributorID = userDistributorId ?: "0"
                    )

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
                            intent.putStringArrayListExtra(
                                CONST_LIST_COORDINATE_NAME,
                                listCoordinateName
                            )
                            intent.putStringArrayListExtra(
                                CONST_LIST_COORDINATE_STATUS,
                                listCoordinateStatus
                            )
                            intent.putStringArrayListExtra(
                                CONST_LIST_COORDINATE_CITY_ID,
                                listCoordinateCityID
                            )

                            progressBar!!.dismiss()
                            startActivity(intent)

                        }

                        RESPONSE_STATUS_EMPTY -> {

                            val listCoordinate = arrayListOf<String>()
                            val listCoordinateName = arrayListOf<String>()

                            val intent = Intent(this@HomeSalesActivity, MapsActivity::class.java)

                            intent.putExtra(CONST_NEAREST_STORE, true)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
                            intent.putStringArrayListExtra(
                                CONST_LIST_COORDINATE_NAME,
                                listCoordinateName
                            )

                            progressBar!!.dismiss()
                            startActivity(intent)

                        }

                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            progressBar!!.dismiss()
                            handleMessage(
                                this@HomeSalesActivity,
                                TAG_RESPONSE_MESSAGE, "Response failed: ${response.message}"
                            )

                        }

                        else -> {

                            handleMessage(
                                this@HomeSalesActivity,
                                TAG_RESPONSE_CONTACT,
                                getString(R.string.failed_get_data) + response.message
                            )
                            progressBar!!.dismiss()

                        }
                    }


                } catch (e: Exception) {

                    if (e is CancellationException) {
                        return@launch
                    }
                    FirebaseUtils.logErr(this@HomeSalesActivity, "Failed HomeSalesActivity on navigateToNearestStore(). Catch: ${e.message}")
                    handleMessage(
                        this@HomeSalesActivity,
                        TAG_RESPONSE_CONTACT,
                        "Failed run service. Exception " + e.message
                    )
                    progressBar!!.dismiss()

                }

            }

        }, 1000)
    }

    private fun navigateToNearestBasecamp() {
        if (progressBar == null) {
            progressBar = CustomProgressBar(this)
            progressBar!!.setMessage("Memuat data basecamp…")
        }

        progressBar!!.show()

        Handler().postDelayed({

            lifecycleScope.launch {
                try {

                    val response =
                        apiService.getListBaseCamp(distributorID = userDistributorId ?: "0")

                    when (response.status) {
                        RESPONSE_STATUS_OK -> {

                            val listCoordinate = arrayListOf<String>()
                            val listCoordinateName = arrayListOf<String>()
                            val listCoordinateStatus = arrayListOf<String>()
                            val listCoordinateCityID = arrayListOf<String>()

                            for (item in response.results.listIterator()) {
                                listCoordinate.add(item.location_gudang)
                                listCoordinateName.add(item.nama_gudang)
                                listCoordinateStatus.add("blacklist")
                                listCoordinateCityID.add(item.id_city)
                            }

                            val intent = Intent(this@HomeSalesActivity, MapsActivity::class.java)

                            intent.putExtra(CONST_NEAREST_STORE, true)
                            intent.putExtra(CONST_IS_BASE_CAMP, true)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
                            intent.putStringArrayListExtra(
                                CONST_LIST_COORDINATE_NAME,
                                listCoordinateName
                            )
                            intent.putStringArrayListExtra(
                                CONST_LIST_COORDINATE_STATUS,
                                listCoordinateStatus
                            )
                            intent.putStringArrayListExtra(
                                CONST_LIST_COORDINATE_CITY_ID,
                                listCoordinateCityID
                            )

                            progressBar!!.dismiss()
                            startActivity(intent)

                        }

                        RESPONSE_STATUS_EMPTY -> {

                            val listCoordinate = arrayListOf<String>()
                            val listCoordinateName = arrayListOf<String>()

                            val intent = Intent(this@HomeSalesActivity, MapsActivity::class.java)

                            intent.putExtra(CONST_NEAREST_STORE, true)
                            intent.putExtra(CONST_IS_BASE_CAMP, true)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
                            intent.putStringArrayListExtra(
                                CONST_LIST_COORDINATE_NAME,
                                listCoordinateName
                            )

                            progressBar!!.dismiss()
                            startActivity(intent)

                        }

                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            progressBar!!.dismiss()
                            handleMessage(
                                this@HomeSalesActivity,
                                TAG_RESPONSE_MESSAGE, "Response failed: ${response.message}"
                            )

                        }

                        else -> {

                            handleMessage(
                                this@HomeSalesActivity,
                                TAG_RESPONSE_CONTACT,
                                getString(R.string.failed_get_data) + response.message
                            )
                            progressBar!!.dismiss()

                        }
                    }


                } catch (e: Exception) {
                    if (e is CancellationException) {
                        return@launch
                    }
                    FirebaseUtils.logErr(this@HomeSalesActivity, "Failed HomeSalesActivity on navigateToNearestBasecamp(). Catch: ${e.message}")
                    handleMessage(
                        this@HomeSalesActivity,
                        TAG_RESPONSE_CONTACT,
                        "Failed run service. Exception " + e.message
                    )
                    progressBar!!.dismiss()

                }

            }

        }, 1000)
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
            if (e is CancellationException) {
                return
            }
            FirebaseUtils.logErr(this, "Failed HomeSalesActivity on logoutHandler(). Catch: ${e.message}")
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
                        if (data.phone_user == "0") {
                            logoutHandler()
                        } else {
                            sessionManager.setUserLoggedIn(data)
                            binding.fullName.text = sessionManager.fullName()
                                .let { if (!it.isNullOrEmpty()) it else "Selamat Datang" }
                        }

                    }

                    RESPONSE_STATUS_EMPTY -> missingDataHandler()

                    RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                        dismissProgressDialog()
                        handleMessage(
                            this@HomeSalesActivity,
                            TAG_RESPONSE_MESSAGE,
                            "Gagal memuat data pengguna! Message: ${response.message}"
                        )

                    }

                    else -> {

                        handleMessage(
                            this@HomeSalesActivity,
                            TAG_RESPONSE_CONTACT,
                            "Gagal memuat data pengguna! Error: ${response.message}"
                        )
                        dismissProgressDialog()

                    }

                }


            } catch (e: Exception) {
                if (e is CancellationException) {
                    return@launch
                }
                FirebaseUtils.logErr(this@HomeSalesActivity, "Failed HomeSalesActivity on getUserLoggedIn(). Catch: ${e.message}")
                Log.d("TAG USER LOGGED IN", "Failed run service. Exception " + e.message)
            }

        }

    }

    private fun setupDialogSearch(
        storeItems: ArrayList<ContactModel> = ArrayList(),
        basecampItems: ArrayList<BaseCampModel> = ArrayList()
    ) {

        val modalItems = arrayListOf<ModalSearchModel>()
        if (absentMode == ABSENT_MODE_STORE) {
            for (item in storeItems) {
                modalItems.add(
                    ModalSearchModel(
                        id = item.id_contact,
                        title = item.nama,
                        etc = item.maps_url
                    )
                )
            }
            modalItems.add(0, ModalSearchModel(id = "-1", "== Ganti absen dari basecamp =="))
        } else {
            for (item in basecampItems) {
                modalItems.add(
                    ModalSearchModel(
                        id = item.id_gudang,
                        title = item.nama_gudang,
                        etc = item.location_gudang
                    )
                )
            }
            modalItems.add(0, ModalSearchModel(id = "-1", "== Ganti absen dari toko =="))
        }

        searchStoreAbsentModal = SearchModal(this, modalItems)
        searchStoreAbsentModal.label =
            if (absentMode == ABSENT_MODE_STORE) "Pilih Toko" else "Pilih Basecamp"
        searchStoreAbsentModal.searchHint = "Ketik untuk mencari…"
        searchStoreAbsentModal.setOnDismissListener { dismissProgressDialog() }
        searchStoreAbsentModal.setCustomDialogListener(object : SearchModal.SearchModalListener {
            override fun onDataReceived(data: ModalSearchModel) {
                if (data.id == "-1") {
                    absentMode = if (absentMode == ABSENT_MODE_STORE) ABSENT_MODE_BASECAMP
                    else ABSENT_MODE_STORE
                    getListAbsent()
                } else {
                    selectedStore = data
                    binding.selectedStoreContainer.tvFilter.text = data.title
                    sessionManager.selectedStoreAbsent(
                        data.id ?: "",
                        data.title ?: "",
                        data.etc ?: "",
                        absentMode ?: ABSENT_MODE_STORE
                    )
                    if (isSelectStoreOnly) isSelectStoreOnly = false
                    else absentAction()
                }
            }

        })

    }

    private fun getListAbsent() {
        if (absentMode == ABSENT_MODE_STORE) {
            if (listStore.isEmpty()) getListStore()
            else {
                setupDialogSearch(listStore)
                searchStoreAbsentModal.show()
            }
        } else {
            if (listBaseCamp.isEmpty()) getListBasecamp()
            else {
                setupDialogSearch(basecampItems = listBaseCamp)
                searchStoreAbsentModal.show()
            }
        }
    }

    private fun absentAction() {
        dismissProgressDialog()

        var alertTitle = "Absen Kehadiran"
        var alertMessage = "Konfirmasi absen kehadiran sekarang?"

        if (isAbsentMorningNow) {
            alertTitle = "Absen Pulang"
            alertMessage =
                "Melakukan tindakan ini akan membatasi akses ke aplikasi sampai hari berikutnya!\nKonfirmasi absen pulang sekarang? "
        }

        AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle(alertTitle)
            .setMessage(alertMessage)
            .setPositiveButton("Iya") { dialog, _ ->
                executeAbsentAction()
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun executeAbsentAction() {
        if (!absentProgressBar!!.isShowing()) absentProgressBar?.show()

        val mapsUrl = selectedStore?.etc.toString()
        val urlUtility = URLUtility(this)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            if (urlUtility.isLocationEnabled(this)) {

                urlUtility.requestLocationUpdate()

                if (!urlUtility.isUrl(mapsUrl) && mapsUrl.isNotEmpty()) {

                    val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
                    if (status != ConnectionResult.SUCCESS) {
                        showDialogGooglePlayNotAvailable()
                        return
                    }

                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->

                        if (location != null) {
                            // Courier Location
                            val currentLatitude = location.latitude
                            val currentLongitude = location.longitude

                            // Store Location
                            val coordinate = mapsUrl.split(",")
                            val latitude = coordinate[0].toDoubleOrNull()
                            val longitude = coordinate[1].toDoubleOrNull()

                            if (latitude != null && longitude != null) {

                                // Calculate Distance
                                val distance = urlUtility.calculateDistance(
                                    currentLatitude,
                                    currentLongitude,
                                    latitude,
                                    longitude
                                )
                                val shortDistance = "%.3f".format(distance)

                                if (distance > MAX_REPORT_DISTANCE) {
                                    val builder = AlertDialog.Builder(this)
                                    builder.setCancelable(false)
                                    builder.setOnDismissListener {
                                        dismissProgressDialog()
                                    }
                                    builder.setOnCancelListener {
                                        dismissProgressDialog()
                                    }
                                    builder.setTitle("Peringatan!")
                                        .setMessage("Titik anda saat ini $shortDistance km dari titik ${selectedStore?.title}. Cobalah untuk lebih dekat dengan toko!")
                                        .setPositiveButton("Oke") { dialog, _ ->
                                            dismissProgressDialog()
                                            dialog.dismiss()
                                        }
                                        .setNegativeButton("Buka Maps") { dialog, _ ->
                                            val intent = Intent(
                                                this@HomeSalesActivity,
                                                MapsActivity::class.java
                                            )
                                            intent.putExtra(CONST_IS_BASE_CAMP, true)
                                            intent.putExtra(CONST_MAPS, mapsUrl)
                                            intent.putExtra(CONST_MAPS_NAME, selectedStore?.title)
                                            startActivity(intent)

                                            dismissProgressDialog()
                                            dialog.dismiss()
                                        }
                                    builder.show()
                                } else {
                                    executeAbsentReport()
                                }

                            } else {
                                dismissProgressDialog()
                                Toast.makeText(this, "Gagal memproses koordinat", TOAST_SHORT)
                                    .show()
                            }
                        } else {
                            AlertDialog.Builder(this)
                                .setCancelable(false)
                                .setTitle("Gagal memproses lokasi")
                                .setMessage("Cobalah untuk menututup dan membuka ulang aplikasi")
                                .setPositiveButton("Tutup") { dialog, _ ->
                                    finish()
                                    dialog.dismiss()
                                }
                                .show()
                        }

                    }.addOnFailureListener {
                        dismissProgressDialog()
                        handleMessage(
                            this,
                            "LOG REPORT",
                            "Gagal mendapatkan lokasi anda. Err: " + it.message
                        )
//                            Toast.makeText(this, "Gagal mendapatkan lokasi anda", TOAST_SHORT).show()
                    }

                } else {
                    dismissProgressDialog()
                    val message =
                        "Anda tidak dapat membuat laporan absen untuk saat ini, silakan hubungi admin untuk memperbarui koordinat toko ini"
                    val actionTitle = "Hubungi Sekarang"
                    customUtility.showPermissionDeniedSnackbar(message, actionTitle) {
                        val chatMessage =
                            "*#Sales Service*\nHalo admin, tolong bantu saya [KETIK PESAN ANDA]"
                        val number =
                            if (!userDistributorNumber.isNullOrEmpty()) userDistributorNumber!! else getString(
                                R.string.topmortar_wa_number
                            )
                        customUtility.navigateChatAdmin(chatMessage, number)
                    }
                }

            } else {
                dismissProgressDialog()
                val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(enableLocationIntent)
            }

        } else {
            dismissProgressDialog()
            checkPermissionsRequirement()
        }
    }

    private fun executeAbsentReport() {

        if (!absentProgressBar!!.isShowing()) absentProgressBar?.show()

        val absentChild = firebaseReference.child(FIREBASE_CHILD_ABSENT)
        val userChild = absentChild.child(userId ?: "0")
        val userLevel = when (userKind) {
            USER_KIND_PENAGIHAN -> AUTH_LEVEL_PENAGIHAN
            USER_KIND_SALES -> AUTH_LEVEL_SALES
            USER_KIND_COURIER -> AUTH_LEVEL_COURIER
            USER_KIND_MARKETING -> AUTH_LEVEL_MARKETING
            else -> ""
        }

        userChild.child("userLevel").setValue(userLevel)
        userChild.child("id").setValue(userId)
        userChild.child("idCity").setValue(userCity)
        userChild.child("username").setValue(userName)
        userChild.child("fullname").setValue(userFullName)
        userChild.child("isOnline").setValue(true)

        lifecycleScope.launch {
            val calendar = checkTimeFromInternet()
            val date = calendar?.let { Date(it.timeInMillis) }
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
            formatter.timeZone = TimeZone.getDefault()

            val absentDateTime = if (date != null) formatter.format(date) else "-"

            if (!isAbsentMorningNow) {

                userChild.child("morningDateTime").setValue(absentDateTime)
                userChild.child("lastSeen").setValue(absentDateTime)

                sessionManager.absentDateTime(absentDateTime)

                checkAbsent()
            } else if (!isAbsentEveningNow) {

                val currentHour = calendar?.get(Calendar.HOUR_OF_DAY)

                if (currentHour != null) {
                    if (currentHour >= 16) {
                        userChild.child("eveningDateTime").setValue(absentDateTime)
                        userChild.child("lastSeen").setValue(absentDateTime)

                        sessionManager.absentDateTime(absentDateTime)
                        checkAbsent()
                    } else {
                        if (absentProgressBar!!.isShowing()) absentProgressBar?.dismiss()
                    }
                } else {
                    if (absentProgressBar!!.isShowing()) absentProgressBar?.dismiss()
                }

            } else {
                if (absentProgressBar!!.isShowing()) absentProgressBar?.dismiss()
            }
        }
    }

    private suspend fun checkTimeFromInternet(): Calendar? {
        return withContext(Dispatchers.IO) {
            val networkTimeMillis = SntpClient.getNetworkTime()
            if (networkTimeMillis != null) {
                Calendar.getInstance().apply {
                    timeInMillis = networkTimeMillis
                }
            } else {
                handleMessage(this@HomeSalesActivity, "NetworkTime", "Gagal mengambil waktu dari internet, coba lagi setelah beberapa saat atau cek koneksi internet anda.")
                null
            }
        }
    }

    private fun checkAbsent() {
        FirebaseUtils.firebaseLogging(this, "Absent", "Start checking")
        try {

            if (absentProgressBar == null) {
                FirebaseUtils.firebaseLogging(this, "Absent", "Init Loading")
                absentProgressBar = CustomProgressBar(this)
                absentProgressBar!!.setCancelable(false)
            }

            absentProgressBar!!.setMessage(getString(R.string.txt_loading) + " 5 / 5")
            if (!absentProgressBar!!.isShowing()) {
                FirebaseUtils.firebaseLogging(this, "Absent", "Show loading")
                absentProgressBar?.show()
            }

            val absentChild = firebaseReference.child(FIREBASE_CHILD_ABSENT)
            val userChild = absentChild.child(userId ?: "0")

            FirebaseUtils.firebaseLogging(this, "Absent", "Reaching firebase server")
            lifecycleScope.launch(Dispatchers.IO) {

                val snapshot = userChild.get().await()

                withContext(Dispatchers.Main) {
                    FirebaseUtils.firebaseLogging(this@HomeSalesActivity, "Absent", "Firebase server reached")
                    if (snapshot.exists()) {
                        FirebaseUtils.firebaseLogging(this@HomeSalesActivity, "Absent", "Snapshot exist")
                        // Do something
                        if (snapshot.child("morningDateTime").exists()) {
                            FirebaseUtils.firebaseLogging(this@HomeSalesActivity, "Absent", "Morning date time exist")
                            val morningDateTime =
                                snapshot.child("morningDateTime").getValue(String::class.java)
                                    .toString()
                            if (morningDateTime.isNotEmpty()) {
                                FirebaseUtils.firebaseLogging(this@HomeSalesActivity, "Absent", "Morning date time not empty")
                                sessionManager.absentDateTime(morningDateTime)

                                val absentMorningDate = DateFormat.format(
                                    morningDateTime,
                                    "yyyy-MM-dd HH:mm:ss",
                                    "yyyy-MM-dd"
                                )

                                if (DateFormat.dateAfterNow(absentMorningDate)) {
                                    FirebaseUtils.firebaseLogging(this@HomeSalesActivity, "Absent", "Morning date time expired")
                                    isAbsentMorningNow = false
                                    lockMenuItem(true)

                                } else {
                                    val serviceIntentDD =
                                        Intent(this@HomeSalesActivity, TrackingService::class.java)
                                    serviceIntentDD.putExtra("userId", userId)
                                    serviceIntentDD.putExtra(
                                        "userDistributorId",
                                        userDistributorId ?: "-start-005-$userName"
                                    )
                                    FirebaseUtils.firebaseLogging(this@HomeSalesActivity, "Absent", "Morning date time start service")
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        startForegroundService(serviceIntentDD)
                                    } else {
                                        startService(serviceIntentDD)
                                    }

                                    isAbsentMorningNow = true
                                    FirebaseUtils.firebaseLogging(this@HomeSalesActivity, "Absent", "Morning date time available")
                                    if (snapshot.child("eveningDateTime").exists()) {
                                        FirebaseUtils.firebaseLogging(this@HomeSalesActivity, "Absent", "Evening date time exist")
                                        val eveningDateTime = snapshot.child("eveningDateTime")
                                            .getValue(String::class.java).toString()

                                        if (eveningDateTime.isNotEmpty()) {
                                            FirebaseUtils.firebaseLogging(this@HomeSalesActivity, "Absent", "Evening date time not empty")
                                            val absentEveningDate = DateFormat.format(
                                                eveningDateTime,
                                                "yyyy-MM-dd HH:mm:ss",
                                                "yyyy-MM-dd"
                                            )

                                            if (DateFormat.dateAfterNow(absentEveningDate)) {
                                                FirebaseUtils.firebaseLogging(this@HomeSalesActivity, "Absent", "Evening date time expired")
                                                isAbsentEveningNow = false
                                                lockMenuItem(false)
                                            } else {
                                                FirebaseUtils.firebaseLogging(this@HomeSalesActivity, "Absent", "Evening date time exist")
                                                val serviceIntent = Intent(
                                                    this@HomeSalesActivity,
                                                    TrackingService::class.java
                                                )
                                                this@HomeSalesActivity.stopService(serviceIntent)
                                                FirebaseUtils.firebaseLogging(this@HomeSalesActivity, "Absent", "Evening date time stop service")
                                                isAbsentEveningNow = true
                                                lockMenuItem(true)
                                            }

                                        } else {
                                            FirebaseUtils.firebaseLogging(this@HomeSalesActivity, "Absent", "Evening date time is empty")
                                            isAbsentEveningNow = false
                                            lockMenuItem(false)
                                        }
                                    } else {
                                        FirebaseUtils.firebaseLogging(this@HomeSalesActivity, "Absent", "Evening date time not exist")
                                        isAbsentEveningNow = false
                                        lockMenuItem(false)
                                    }

                                }

                            } else {
                                FirebaseUtils.firebaseLogging(this@HomeSalesActivity, "Absent", "Morning date time is empty")
                                isAbsentMorningNow = false
                                lockMenuItem(true)
                            }
                        } else {
                            FirebaseUtils.firebaseLogging(this@HomeSalesActivity, "Absent", "Morning date time not exist")
                            isAbsentMorningNow = false
                            lockMenuItem(true)
                        }
                    } else {

                        FirebaseUtils.firebaseLogging(this@HomeSalesActivity, "Absent", "snapshot not exist")
                        isAbsentMorningNow = false
                        lockMenuItem(true)
                    }

                }

            }
        } catch (e: Exception) {
            if (e is CancellationException) {
                return
            }
            FirebaseUtils.logErr(this@HomeSalesActivity, "Failed HomeSalesActivity on checkAbsent(). Catch: ${e.message}")
            lockMenuItem(false)
            handleMessage(
                this,
                "Home Sales Failed",
                "Failed HomeSalesActivity on checkAbsent(). Error: ${e.message}"
            )
        }
    }

    private fun lockMenuItem(state: Boolean) {
        FirebaseUtils.firebaseLogging(this, "Absent", "Lock menu")
        dismissProgressDialog()
        FirebaseUtils.firebaseLogging(this, "Absent", "Loading dismissed")
        isLocked = state

        setListMenu()

        if (isAbsentMorningNow && isAbsentEveningNow) {

            binding.absentTitle.text = getString(R.string.absen_pulang_sudah_tercatat)
            binding.absentDescription.text = getString(R.string.terima_kasih_atas_kinerja_hari_ini)

            binding.btnAbsent.visibility = View.GONE
        } else {

            binding.absentTitle.text = if (state) {
                getString(R.string.yuk_catat_kehadiranmu_hari_ini)
            } else {
                getString(R.string.kehadiranmu_telah_tercatat)
            }
            binding.absentDescription.text = if (state) {
                getString(R.string.absenmu_penting)
            } else {
                getString(R.string.terimakasih_sudah_mencatat_kehadiran_hari_ini_without_clock)
            }

            binding.btnAbsent.backgroundTintList = ContextCompat.getColorStateList(
                this,
                if (state) R.color.status_bid else R.color.red_claret
            )
            binding.btnAbsent.text =
                if (state) getString(R.string.absen_sekarang) else getString(R.string.pulang_sekarang)

            lifecycleScope.launch {
                val currentHour = checkTimeFromInternet()?.get(Calendar.HOUR_OF_DAY)

                if (currentHour != null) {
                    if (isAbsentMorningNow && !isAbsentEveningNow && currentHour < 16) {
                        binding.btnAbsent.visibility = View.GONE
                        binding.absenEveningInfoText.visibility = View.VISIBLE
                    } else {
                        binding.btnAbsent.visibility = View.VISIBLE
                        binding.absenEveningInfoText.visibility = View.GONE
                    }
                } else {
                    binding.btnAbsent.visibility = View.GONE
                    binding.absenEveningInfoText.visibility = View.VISIBLE
                }
            }
        }

    }

    private fun showDialogLockedFeature() {
        var title = getString(R.string.fitur_terkunci)
        var message = getString(R.string.absen_terlebih_dahulu_untuk_membuka)

        if (isAbsentMorningNow && isAbsentEveningNow) {

            title = getString(R.string.absen_pulang_sudah_tercatat)
            message = getString(R.string.terima_kasih_atas_kinerja_hari_ini)
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.oke)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getListStore(showModal: Boolean = true) {

        if (!absentProgressBar!!.isShowing()) {
            absentProgressBar?.setMessage(getString(R.string.txt_loading))
            absentProgressBar?.show()
        }

        lifecycleScope.launch {
            try {

                val response =
                    if (userKind == USER_KIND_PENAGIHAN) apiService.getContactsByDistributor(
                        distributorID = userDistributorId ?: "0"
                    )
                    else apiService.getContacts(
                        cityId = userCity ?: "0",
                        distributorID = userDistributorId ?: "0"
                    )

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        dismissProgressDialog()
                        listStore = response.results
                        setupDialogSearch(listStore)
                        if (showModal) searchStoreAbsentModal.show()

                    }

                    RESPONSE_STATUS_EMPTY -> {

                        dismissProgressDialog()
                        setupDialogSearch(listStore)
                        if (showModal) searchStoreAbsentModal.show()

                    }

                    RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                        dismissProgressDialog()
                        handleMessage(
                            this@HomeSalesActivity,
                            TAG_RESPONSE_MESSAGE, "Response failed: ${response.message}"
                        )

                    }

                    else -> {

                        handleMessage(
                            this@HomeSalesActivity,
                            TAG_RESPONSE_CONTACT,
                            getString(R.string.failed_get_data) + response.message
                        )
                        dismissProgressDialog()

                    }
                }

            } catch (e: Exception) {
                if (e is CancellationException) {
                    return@launch
                }
                FirebaseUtils.logErr(this@HomeSalesActivity, "Failed HomeSalesActivity on getListStore(). Catch: ${e.message}")
                handleMessage(
                    this@HomeSalesActivity,
                    TAG_RESPONSE_CONTACT,
                    "Failed run service. Exception " + e.message
                )
                dismissProgressDialog()

            }

        }

    }

    private fun getListBasecamp(showModal: Boolean = true) {

        if (!absentProgressBar!!.isShowing()) {
            absentProgressBar?.setMessage(getString(R.string.txt_loading))
            absentProgressBar?.show()
        }

        lifecycleScope.launch {
            try {

                val response = apiService.getListBaseCamp(distributorID = userDistributorId ?: "0")

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        dismissProgressDialog()
                        listBaseCamp = response.results
                        setupDialogSearch(basecampItems = listBaseCamp)
                        if (showModal) searchStoreAbsentModal.show()

                    }

                    RESPONSE_STATUS_EMPTY -> {

                        dismissProgressDialog()
                        setupDialogSearch(basecampItems = listBaseCamp)
                        if (showModal) searchStoreAbsentModal.show()

                    }

                    RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                        dismissProgressDialog()
                        handleMessage(
                            this@HomeSalesActivity,
                            TAG_RESPONSE_MESSAGE, "Response failed: ${response.message}"
                        )

                    }

                    else -> {

                        handleMessage(
                            this@HomeSalesActivity,
                            TAG_RESPONSE_CONTACT,
                            getString(R.string.failed_get_data) + response.message
                        )
                        dismissProgressDialog()

                    }
                }

            } catch (e: Exception) {

                if (e is CancellationException) {
                    return@launch
                }
                FirebaseUtils.logErr(this@HomeSalesActivity, "Failed HomeSalesActivity on getListBasecamp(). Catch: ${e.message}")
                handleMessage(
                    this@HomeSalesActivity,
                    TAG_RESPONSE_CONTACT,
                    "Failed run service. Exception " + e.message
                )
                dismissProgressDialog()

            }

        }

    }

    private fun setListMenu() {
        setListVisit()
        setListStoreBasecamp()
        setListOthers()
    }

    private fun setListVisit() {
        val listItem = arrayListOf<HomeMenuSalesModel>()

        if (userKind == USER_KIND_MARKETING) {
            listItem.add(
                HomeMenuSalesModel(
                    icon = R.drawable.store_white,
                    bgColor = R.drawable.bg_green_reseda_round_8,
                    title = "Rencana Visit MG",
                    target = RencanaVisitMGActivity::class.java,
                    isLocked = isLocked
                )
            )
        } else {
            listItem.add(
                HomeMenuSalesModel(
                    icon = R.drawable.store_white,
                    bgColor = R.drawable.bg_green_reseda_round_8,
                    title = "Rencana Visit Sales",
                    target = RencanaVisitActivity::class.java,
                    isLocked = isLocked
                )
            )
            listItem.add(
                HomeMenuSalesModel(
                    icon = R.drawable.store_white,
                    bgColor = R.drawable.bg_green_reseda_round_8,
                    title = "Rencana Visit Penagihan",
                    target = RencanaVisitPenagihanActivity::class.java,
                    isLocked = isLocked
                )
            )
        }

        listItem.add(
            HomeMenuSalesModel(
                icon = R.drawable.gudang_white,
                bgColor = R.drawable.bg_blue_silver_lake_round_8,
                title = "Semua Toko",
                target = MainActivity::class.java,
                isLocked = isLocked
            )
        )

        setMenuItemAdapter(binding.rvVisit, listItem)
    }

    private fun setListStoreBasecamp() {
        val listItem = arrayListOf<HomeMenuSalesModel>()

        listItem.add(
            HomeMenuSalesModel(
                icon = R.drawable.location_white,
                bgColor = R.drawable.bg_redwood_round_8,
                title = "Lihat Toko Terdekat",
                action = { navigateToNearestStore() },
                isLocked = false
            )
        )
        if (userKind != USER_KIND_MARKETING) {
            listItem.add(
                HomeMenuSalesModel(
                    icon = R.drawable.add_white,
                    bgColor = R.drawable.bg_yellow_hunyadi_round_8,
                    title = "Daftarkan Toko Baru",
                    target = NewRoomChatFormActivity::class.java,
                    isLocked = false
                )
            )
        }
        listItem.add(
            HomeMenuSalesModel(
                icon = R.drawable.location_white,
                bgColor = R.drawable.bg_redwood_round_8,
                title = "Lihat Basecamp Terdekat",
                action = { navigateToNearestBasecamp() },
                isLocked = false
            )
        )
        listItem.add(
            HomeMenuSalesModel(
                icon = R.drawable.add_white,
                bgColor = R.drawable.bg_yellow_hunyadi_round_8,
                title = "Daftarkan Basecamp Baru",
                target = AddBaseCampActivity::class.java,
                isLocked = false
            )
        )
        if (userKind != USER_KIND_MARKETING) {
            listItem.add(
                HomeMenuSalesModel(
                    icon = R.drawable.user_add_white,
                    bgColor = R.drawable.bg_charcoal_round_8,
                    title = "Kelola Tukang",
                    target = ListTukangActivity::class.java,
                    isLocked = false
                )
            )
        }
        setMenuItemAdapter(binding.rvStoreBasecamp, listItem)
    }

    private fun setListOthers() {
        val listItem = arrayListOf<HomeMenuSalesModel>()

        if (userKind != USER_KIND_MARKETING) {
            listItem.add(
                HomeMenuSalesModel(
                    icon = R.drawable.bar_chart_fill_white_only,
                    bgColor = R.drawable.bg_blue_silver_lake_round_8,
                    title = "Lihat Data Grafik",
                    target = ChartActivity::class.java,
                    isLocked = false
                )
            )
            listItem.add(
                HomeMenuSalesModel(
                    icon = R.drawable.boxes_stacked_solid,
                    bgColor = R.drawable.bg_blue_indigo_dye_round_8,
                    title = "Lihat Produk",
                    target = ProductsActivity::class.java,
                    isLocked = false
                )
            )
        }
        listItem.add(
            HomeMenuSalesModel(
                icon = R.drawable.file_list_white_only,
                bgColor = R.drawable.bg_active_round_8,
                title = "Lihat Laporan Saya",
                action = { navigateToListReport() },
                isLocked = false
            )
        )
        listItem.add(
            HomeMenuSalesModel(
                icon = R.drawable.user_add_white,
                bgColor = R.drawable.bg_primary_round_8,
                title = "Profil Saya",
                target = UserProfileActivity::class.java,
                isLocked = false
            )
        )

        setMenuItemAdapter(binding.rvOthers, listItem)
    }

    private fun setMenuItemAdapter(
        recyclerView: RecyclerView,
        listItem: ArrayList<HomeMenuSalesModel>
    ) {
        val menuItemAdapter = HomeSalesMenuRV()
        menuItemAdapter.setList(listItem)
        menuItemAdapter.setOnItemClickListener(object : HomeSalesMenuRV.OnItemClickListener {
            override fun onItemClick(item: HomeMenuSalesModel) {
                if (item.isLocked) showDialogLockedFeature()
                else {
                    if (item.target != null) startActivity(
                        Intent(
                            this@HomeSalesActivity,
                            item.target
                        )
                    )
                    else item.action?.invoke()
                }
            }

        })

        recyclerView.apply {
            adapter = menuItemAdapter
            layoutManager = LinearLayoutManager(this@HomeSalesActivity)
        }
    }

// Override Class

    private fun myOnBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finish()
            return
        }

        doubleBackToExitPressedOnce = true
        handleMessage(
            this,
            TAG_ACTION_MAIN_ACTIVITY,
            getString(R.string.tekan_sekali_lagi),
            TOAST_SHORT
        )

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    override fun onStart() {
        super.onStart()
//        checkAbsent()
//        checkLocationPermission()
        Handler(Looper.getMainLooper()).postDelayed({
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    true,
                    sessionManager.userDistributor() ?: "-custom-010",
                    sessionManager.userID() ?: ""
                )
            }
            getUserLoggedIn()
        }, 1000)
    }

    override fun onStop() {
        super.onStop()

        if (sessionManager.isLoggedIn()) {
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    sessionManager.userDistributor() ?: "-custom-010",
                    sessionManager.userID() ?: ""
                )
            }
        }
    }

    override fun onResume() {
        checkLocationPermission()
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (progressBar != null && progressBar!!.isShowing()) {
            progressBar?.dismiss()
        }
        if (absentProgressBar != null && absentProgressBar!!.isShowing()) {
            absentProgressBar?.dismiss()
        }
        if (sessionManager.isLoggedIn()) {
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    sessionManager.userDistributor() ?: "-custom-010",
                    sessionManager.userID() ?: ""
                )
            }
        }
    }
    
    private fun dismissProgressDialog() {
        if (!isFinishing && !isDestroyed) {
            absentProgressBar?.dismiss()
        }
    }

}