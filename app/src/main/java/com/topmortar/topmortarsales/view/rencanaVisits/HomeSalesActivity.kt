@file:Suppress("DEPRECATION")

package com.topmortar.topmortarsales.view.rencanaVisits

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_COURIER
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_SALES
import com.topmortar.topmortarsales.commons.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
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
import com.topmortar.topmortarsales.commons.LOCATION_PERMISSION_REQUEST_CODE
import com.topmortar.topmortarsales.commons.LOGGED_OUT
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.MAX_REPORT_DISTANCE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_ACTION_MAIN_ACTIVITY
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.services.TrackingService
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.URLUtility
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityHomeSalesBinding
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.ContactModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import com.topmortar.topmortarsales.view.MainActivity
import com.topmortar.topmortarsales.view.MapsActivity
import com.topmortar.topmortarsales.view.SplashScreenActivity
import com.topmortar.topmortarsales.view.contact.NewRoomChatFormActivity
import com.topmortar.topmortarsales.view.reports.ReportsActivity
import com.topmortar.topmortarsales.view.user.UserProfileActivity
import kotlinx.coroutines.launch
import java.util.Calendar

@SuppressLint("SetTextI18n")
class HomeSalesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeSalesBinding
    private lateinit var apiService: ApiService

    private lateinit var sessionManager: SessionManager
    private val userId get() = sessionManager.userID()
    private val userCity get() = sessionManager.userCityID()
    private val userName get() = sessionManager.userName()
    private val userFullName get() = sessionManager.fullName()
    private val userDistributorId get() = sessionManager.userDistributor()
    private val userDistributorNumber get() = sessionManager.userDistributorNumber()
    private val selectedStoreDefaultID get() = sessionManager.selectedStoreAbsentID()
    private val selectedStoreDefaultTitle get() = sessionManager.selectedStoreAbsentTitle()
    private val selectedStoreDefaultCoordinate get() = sessionManager.selectedStoreAbsentCoordinate()

    private var doubleBackToExitPressedOnce = false
    private var isAbsentMorningNow = false
    private var isAbsentEveningNow = false
    private var isLocked = false

    private lateinit var firebaseReference: DatabaseReference
    private var absentProgressDialog : ProgressDialog? = null
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var customUtility : CustomUtility

    private lateinit var searchStoreAbsentModal: SearchModal
    private var selectedStore: ModalSearchModel? = null
    private var listStore: ArrayList<ContactModel> = arrayListOf()
    private var isSelectStoreOnly = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        binding = ActivityHomeSalesBinding.inflate(layoutInflater)
        sessionManager = SessionManager(this)

        setContentView(binding.root)

        if (absentProgressDialog == null) {
            absentProgressDialog = ProgressDialog(this)
            absentProgressDialog!!.setMessage(getString(R.string.txt_loading))
            absentProgressDialog!!.setCancelable(false)
        }

        firebaseReference = FirebaseUtils().getReference(distributorId = userDistributorId.toString())
        apiService = HttpClient.create()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        customUtility = CustomUtility(this)

        binding.selectedStoreContainer.tvLabel.text = "Lokasi absen:"

        if (selectedStoreDefaultID.isNullOrEmpty()) binding.selectedStoreContainer.tvFilter.text = "Pilih toko"
        else {
            binding.selectedStoreContainer.tvFilter.text = selectedStoreDefaultTitle
            selectedStore = ModalSearchModel(
                id = selectedStoreDefaultID,
                title = selectedStoreDefaultTitle,
                etc = selectedStoreDefaultCoordinate
            )
        }
        setupDialogSearch()

        checkLocationPermission()
        CustomUtility(this).setUserStatusOnline(true, userDistributorId.toString(), userId.toString())

    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkGpsStatus()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun checkGpsStatus() {
        val locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!isGpsEnabled) {
            // GPS tidak aktif, munculkan dialog untuk mengaktifkannya
            showGpsDisabledDialog()
        } else {
            initView()
        }
    }

    private fun showGpsDisabledDialog() {
        AlertDialog.Builder(this)
            .setMessage("Aplikasi memerlukan lokasi untuk berfungsi. Aktifkan lokasi sekarang?")
            .setCancelable(false)
            .setPositiveButton("Ya") { _, _ ->
                // Buka pengaturan untuk mengaktifkan GPS
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(intent, LOCATION_PERMISSION_REQUEST_CODE)
            }
            .show()
    }

    private fun initView() {

        binding.fullName.text = userFullName

        binding.rencanaVisit.setOnClickListener { if (isLocked) showDialogLockedFeature() else navigateToTargetVisit() }
        binding.allStore.setOnClickListener { if (isLocked) showDialogLockedFeature() else navigateToAllStore()}
        binding.nearestStoreItem.setOnClickListener { navigateToNearestStore()}
        binding.registerNewStore.setOnClickListener { navigateToRegisterStore()}
        binding.myProfileItem.setOnClickListener { navigateToMyProfile() }
        binding.btnLogout.setOnClickListener { logoutConfirmation() }
        binding.reportDetail.setOnClickListener { navigateToListReport() }

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) binding.selectedStoreContainer.componentFilter.background = AppCompatResources.getDrawable(this, R.color.black_400)
        else binding.selectedStoreContainer.componentFilter.background = AppCompatResources.getDrawable(this, R.color.light)
        binding.selectedStoreContainer.componentFilter.setOnClickListener {
            isSelectStoreOnly = true
            if (listStore.isEmpty()) getListStore()
            else {
                setupDialogSearch(listStore)
                searchStoreAbsentModal.show()
            }
        }

        binding.btnAbsent.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {

                        absentProgressDialog?.show()
                        if (selectedStore == null) {
                            if (listStore.isEmpty()) getListStore()
                            else {
                                setupDialogSearch(listStore)
                                searchStoreAbsentModal.show()
                            }
                        } else absentAction()

                    } else {
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
                                    BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
                                )
                                localDialog.dismiss()
                            }
                            .show()
                    }
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                    absentAction()
                    absentProgressDialog?.show()
                    if (selectedStore == null) {
                        if (listStore.isEmpty()) getListStore()
                        else {
                            setupDialogSearch(listStore)
                            searchStoreAbsentModal.show()
                        }
                    } else absentAction()
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
                }
            }
        }

        lockMenuItem(true)

        checkAbsent()

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
        startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_CODE)
    }

    private fun navigateToMyProfile() {
        Log.d("USER CLICKED", "Clicked")
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

    private fun setupDialogSearch(items: ArrayList<ContactModel> = ArrayList()) {

        val modalItems = arrayListOf<ModalSearchModel>()
        for (item in items) {
            modalItems.add(
                ModalSearchModel(
                    id = item.id_contact,
                    title = item.nama,
                    etc = item.maps_url
                )
            )
        }

        searchStoreAbsentModal = SearchModal(this, modalItems)
        searchStoreAbsentModal.label = "Pilih Toko"
        searchStoreAbsentModal.searchHint = "Ketik untuk mencari…"
        searchStoreAbsentModal.setOnDismissListener { absentProgressDialog?.dismiss() }
        searchStoreAbsentModal.setCustomDialogListener(object : SearchModal.SearchModalListener {
            override fun onDataReceived(data: ModalSearchModel) {
                selectedStore = data
                binding.selectedStoreContainer.tvFilter.text = data.title
                sessionManager.selectedStoreAbsent(data.id ?: "", data.title ?: "", data.etc ?: "")
                if (isSelectStoreOnly) isSelectStoreOnly = false
                else absentAction()
            }

        })
    }

    private fun absentAction() {
        absentProgressDialog?.dismiss()

        var alertTitle = "Absen Kehadiran"
        var alertMessage = "Konfirmasi absen kehadiran sekarang?"

        if (isAbsentMorningNow) {
            alertTitle = "Absen Pulang"
            alertMessage = "Melakukan tindakan ini akan membatasi akses ke aplikasi sampai hari berikutnya!\nKonfirmasi absen pulang sekarang? "
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
        if (!absentProgressDialog!!.isShowing) absentProgressDialog?.show()

        val mapsUrl = selectedStore?.etc.toString()
        val urlUtility = URLUtility(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (urlUtility.isLocationEnabled(this)) {

                urlUtility.requestLocationUpdate()

                if (!urlUtility.isUrl(mapsUrl) && mapsUrl.isNotEmpty()) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location ->

                        // Courier Location
                        val currentLatitude = location.latitude
                        val currentLongitude = location.longitude

                        // Store Location
                        val coordinate = mapsUrl.split(",")
                        val latitude = coordinate[0].toDoubleOrNull()
                        val longitude = coordinate[1].toDoubleOrNull()

                        if (latitude != null && longitude != null) {

                            // Calculate Distance
                            val distance = urlUtility.calculateDistance(currentLatitude, currentLongitude, latitude, longitude)
                            val shortDistance = "%.3f".format(distance)

                            if (distance > MAX_REPORT_DISTANCE) {
                                val builder = AlertDialog.Builder(this)
                                builder.setCancelable(false)
                                builder.setOnDismissListener {
                                    absentProgressDialog?.dismiss()
                                }
                                builder.setOnCancelListener {
                                    absentProgressDialog?.dismiss()
                                }
                                builder.setTitle("Peringatan!")
                                    .setMessage("Titik anda saat ini $shortDistance km dari titik ${ selectedStore?.title }. Cobalah untuk lebih dekat dengan toko!")
                                    .setPositiveButton("Oke") { dialog, _ ->
                                        absentProgressDialog?.dismiss()
                                        dialog.dismiss()
                                    }
                                    .setNegativeButton("Buka Maps") { dialog, _ ->
                                        val intent = Intent(this@HomeSalesActivity, MapsActivity::class.java)
                                        intent.putExtra(CONST_IS_BASE_CAMP, true)
                                        intent.putExtra(CONST_MAPS, mapsUrl)
                                        intent.putExtra(CONST_MAPS_NAME, selectedStore?.title)
                                        startActivity(intent)

                                        absentProgressDialog?.dismiss()
                                        dialog.dismiss()
                                    }
                                builder.show()
                            } else {
                                executeAbsentReport(shortDistance)
                            }

                        } else {
                            absentProgressDialog?.dismiss()
                            Toast.makeText(this, "Gagal memproses koordinat", TOAST_SHORT).show()
                        }

                    }.addOnFailureListener {
                        absentProgressDialog?.dismiss()
                        handleMessage(this, "LOG REPORT", "Gagal mendapatkan lokasi anda. Err: " + it.message)
//                            Toast.makeText(this, "Gagal mendapatkan lokasi anda", TOAST_SHORT).show()
                    }

                } else {
                    absentProgressDialog?.dismiss()
                    val message = "Anda tidak dapat membuat laporan absen untuk saat ini, silakan hubungi admin untuk memperbarui koordinat toko ini"
                    val actionTitle = "Hubungi Sekarang"
                    customUtility.showPermissionDeniedSnackbar(message, actionTitle) {
                        val chatMessage = "*#Sales Service*\nHalo admin, tolong bantu saya [KETIK PESAN ANDA]"
                        val number = if (!userDistributorNumber.isNullOrEmpty()) userDistributorNumber!! else getString(R.string.topmortar_wa_number)
                        customUtility.navigateChatAdmin(chatMessage, number)
                    }
                }

            } else {
                absentProgressDialog?.dismiss()
                val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(enableLocationIntent)
            }

        } else {
            absentProgressDialog?.dismiss()
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun executeAbsentReport(shortDistance: String = "") {

        if (!absentProgressDialog!!.isShowing) absentProgressDialog?.show()

        val absentChild = firebaseReference.child(FIREBASE_CHILD_ABSENT)
        val userChild = absentChild.child(userId.toString())

        userChild.child("id").setValue(userId)
        userChild.child("username").setValue(userName)
        userChild.child("fullname").setValue(userFullName)
        userChild.child("isOnline").setValue(true)

        if (!isAbsentMorningNow) {

            val absentDateTime = DateFormat.now()
            userChild.child("morningDateTime").setValue(absentDateTime)
            userChild.child("lastSeen").setValue(absentDateTime)

            sessionManager.absentDateTime(absentDateTime)

            if (!CustomUtility(this@HomeSalesActivity).isServiceRunning(
                    TrackingService::class.java)) {
                val serviceIntent = Intent(this@HomeSalesActivity, TrackingService::class.java)
                serviceIntent.putExtra("userId", userId)
                serviceIntent.putExtra("userDistributorId", userDistributorId)
                serviceIntent.putExtra("deliveryId", AUTH_LEVEL_COURIER + userId)
                this@HomeSalesActivity.startService(serviceIntent)
            }

            absentProgressDialog?.dismiss()
            checkAbsent()
        } else {

            val absentDateTime = DateFormat.now()
            userChild.child("eveningDateTime").setValue(absentDateTime)
            userChild.child("lastSeen").setValue(absentDateTime)

            sessionManager.absentDateTime(absentDateTime)

            val serviceIntent = Intent(this@HomeSalesActivity, TrackingService::class.java)
            this@HomeSalesActivity.stopService(serviceIntent)

            absentProgressDialog?.dismiss()
            checkAbsent()
        }

        return
        lifecycleScope.launch {
            try {

                var visitReport = "Absen masuk\n•by system•"
                if (isAbsentMorningNow) visitReport = "Absen pulang\n•by system•"

                val response = apiService.makeVisitReport(
                    idContact = createPartFromString(selectedStore?.id!!),
                    idUser = createPartFromString(userId!!),
                    laporanVisit = createPartFromString(visitReport),
                    distanceVisit = createPartFromString(shortDistance)
                )

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            val absentChild = firebaseReference.child(FIREBASE_CHILD_ABSENT)
                            val userChild = absentChild.child(userId.toString())

                            userChild.child("id").setValue(userId)
                            userChild.child("username").setValue(userName)
                            userChild.child("fullname").setValue(userFullName)
                            userChild.child("isOnline").setValue(true)

                            if (!isAbsentMorningNow) {

                                val absentDateTime = DateFormat.now()
                                userChild.child("morningDateTime").setValue(absentDateTime)
                                userChild.child("lastSeen").setValue(absentDateTime)

                                sessionManager.absentDateTime(absentDateTime)

                                if (!CustomUtility(this@HomeSalesActivity).isServiceRunning(
                                        TrackingService::class.java)) {
                                    val serviceIntent = Intent(this@HomeSalesActivity, TrackingService::class.java)
                                    serviceIntent.putExtra("userId", userId)
                                    serviceIntent.putExtra("userDistributorId", userDistributorId)
                                    serviceIntent.putExtra("deliveryId", AUTH_LEVEL_COURIER + userId)
                                    this@HomeSalesActivity.startService(serviceIntent)
                                }

                                absentProgressDialog?.dismiss()
                                checkAbsent()
                            } else {

                                val absentDateTime = DateFormat.now()
                                userChild.child("eveningDateTime").setValue(absentDateTime)
                                userChild.child("lastSeen").setValue(absentDateTime)

                                sessionManager.absentDateTime(absentDateTime)

                                val serviceIntent = Intent(this@HomeSalesActivity, TrackingService::class.java)
                                this@HomeSalesActivity.stopService(serviceIntent)

                                absentProgressDialog?.dismiss()
                                checkAbsent()
                            }

                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            absentProgressDialog?.dismiss()
                            handleMessage(this@HomeSalesActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim laporan absen! Message: ${ responseBody.message }")

                        }
                        else -> {

                            absentProgressDialog?.dismiss()
                            handleMessage(this@HomeSalesActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))

                        }
                    }
                } else {

                    absentProgressDialog?.dismiss()
                    handleMessage(this@HomeSalesActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim laporan absen! Error: " + response.message())

                }

            } catch (e: Exception) {

                handleMessage(this@HomeSalesActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)

            }

        }
    }

    private fun checkAbsent() {

        if (absentProgressDialog == null) {
            absentProgressDialog = ProgressDialog(this)
            absentProgressDialog!!.setMessage(getString(R.string.txt_loading))
            absentProgressDialog!!.setCancelable(false)
        }

        absentProgressDialog!!.show()

        val absentChild = firebaseReference.child(FIREBASE_CHILD_ABSENT)
        val userChild = absentChild.child(userId.toString())

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
                                lockMenuItem(true)
                                Log.d("ABSENT COURIER", "Belum absen pagi")
                            } else {

                                Log.d("ABSENT COURIER", "Sudah absen pagi")
                                isAbsentMorningNow = true
                                if (snapshot.child("eveningDateTime").exists()) {
                                    val eveningDateTime = snapshot.child("eveningDateTime").getValue(String::class.java).toString()

                                    if (eveningDateTime.isNotEmpty()) {
                                        val absentEveningDate = DateFormat.format(eveningDateTime, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd")

                                        if (DateFormat.dateAfterNow(absentEveningDate)) {
                                            if (!CustomUtility(this@HomeSalesActivity).isServiceRunning(TrackingService::class.java)) {
                                                val serviceIntent = Intent(this@HomeSalesActivity, TrackingService::class.java)
                                                serviceIntent.putExtra("userId", userId)
                                                serviceIntent.putExtra("userDistributorId", userDistributorId)
                                                serviceIntent.putExtra("deliveryId", AUTH_LEVEL_COURIER + userId)
                                                this@HomeSalesActivity.startService(serviceIntent)
                                            }
                                            Log.d("ABSENT COURIER", "Belum absen sore")
                                            isAbsentEveningNow = false
                                            lockMenuItem(false)
                                        } else {
                                            val serviceIntent = Intent(this@HomeSalesActivity, TrackingService::class.java)
                                            this@HomeSalesActivity.stopService(serviceIntent)
                                            Log.d("ABSENT COURIER", "Sudah absen sore")
                                            isAbsentEveningNow = true
                                            lockMenuItem(true)
                                        }

                                    } else {
                                        if (!CustomUtility(this@HomeSalesActivity).isServiceRunning(TrackingService::class.java)) {
                                            val serviceIntent = Intent(this@HomeSalesActivity, TrackingService::class.java)
                                            serviceIntent.putExtra("userId", userId)
                                            serviceIntent.putExtra("userDistributorId", userDistributorId)
                                            serviceIntent.putExtra("deliveryId", AUTH_LEVEL_COURIER + userId)
                                            this@HomeSalesActivity.startService(serviceIntent)
                                        }
                                        Log.d("ABSENT COURIER", "Tgl absen sore tidak ada")
                                        isAbsentEveningNow = false
                                        lockMenuItem(false)
                                    }
                                } else {
                                    if (!CustomUtility(this@HomeSalesActivity).isServiceRunning(TrackingService::class.java)) {
                                        val serviceIntent = Intent(this@HomeSalesActivity, TrackingService::class.java)
                                        serviceIntent.putExtra("userId", userId)
                                        serviceIntent.putExtra("userDistributorId", userDistributorId)
                                        serviceIntent.putExtra("deliveryId", AUTH_LEVEL_COURIER + userId)
                                        this@HomeSalesActivity.startService(serviceIntent)
                                    }
                                    Log.d("ABSENT COURIER", "Kolom absen sore tidak ada")
                                    isAbsentEveningNow = false
                                    lockMenuItem(false)
                                }

                            }

                        } else {
                            Log.d("ABSENT COURIER", "Tgl absen pagi tidak ada")
                            isAbsentMorningNow = false
                            lockMenuItem(true)
                        }
                    } else {
                        Log.d("ABSENT COURIER", "Kolom absen pagi tidak ada")
                        isAbsentMorningNow = false
                        lockMenuItem(true)
                    }
                } else {
                    Log.d("ABSENT COURIER", "Snapshot tidak ada")
                    isAbsentMorningNow = false
                    lockMenuItem(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Do something
                isAbsentMorningNow = false
                isAbsentEveningNow = false
                lockMenuItem(true)
            }

        })
    }

    private fun lockMenuItem(state: Boolean) {
        absentProgressDialog?.dismiss()
        isLocked = state

        binding.rencanaVisit.alpha = if (state) 0.5f else 1f
        binding.allStore.alpha = if (state) 0.5f else 1f

        binding.rencanaVisitChevron.setImageResource(if (state) R.drawable.lock_dark else R.drawable.chevron_right_dark)
        binding.allStoreChevron.setImageResource(if (state) R.drawable.lock_dark else R.drawable.chevron_right_dark)

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

            binding.btnAbsent.backgroundTintList = ContextCompat.getColorStateList(this, if (state) R.color.status_bid else R.color.red_claret)
            binding.btnAbsent.text = if (state) getString(R.string.absen_sekarang) else getString(R.string.pulang_sekarang)

            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY) // Mengambil jam saat ini dalam format 24 jam

//            if (isAbsentMorningNow && !isAbsentEveningNow && currentHour < 16) {
//                binding.btnAbsent.visibility = View.GONE
//                binding.absenEveningInfoText.visibility = View.VISIBLE
//            } else {
                binding.btnAbsent.visibility = View.VISIBLE
                binding.absenEveningInfoText.visibility = View.GONE
//            }
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

        if (!absentProgressDialog!!.isShowing) absentProgressDialog?.show()

        lifecycleScope.launch {
            try {

                val response = apiService.getContacts(cityId = userCity ?: "0", distributorID = userDistributorId ?: "0")

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        absentProgressDialog?.dismiss()
                        listStore = response.results
                        setupDialogSearch(listStore)
                        if (showModal) searchStoreAbsentModal.show()

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        absentProgressDialog?.dismiss()
                        setupDialogSearch(listStore)
                        if (showModal) searchStoreAbsentModal.show()

                    }
                    else -> {

                        handleMessage(this@HomeSalesActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        absentProgressDialog?.dismiss()

                    }
                }

            } catch (e: Exception) {

                handleMessage(this@HomeSalesActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                absentProgressDialog?.dismiss()

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
        handleMessage(this, TAG_ACTION_MAIN_ACTIVITY, getString(R.string.tekan_sekali_lagi), TOAST_SHORT)

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    override fun onStart() {
        super.onStart()
        checkAbsent()
        Handler(Looper.getMainLooper()).postDelayed({
            if (sessionManager.userKind() == USER_KIND_SALES) {
                CustomUtility(this).setUserStatusOnline(
                    true,
                    sessionManager.userDistributor().toString(),
                    sessionManager.userID().toString()
                )
            }
        }, 1000)
    }

    override fun onStop() {
        super.onStop()

        if (sessionManager.isLoggedIn()) {
            if (sessionManager.userKind() == USER_KIND_SALES) {
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
            if (sessionManager.userKind() == USER_KIND_SALES) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    sessionManager.userDistributor().toString(),
                    sessionManager.userID().toString()
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MAIN_ACTIVITY_REQUEST_CODE) {
            val resultData = data?.getStringExtra("$MAIN_ACTIVITY_REQUEST_CODE")

            if (resultData == SYNC_NOW) {

                getListStore(showModal = false)

            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            checkLocationPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) checkLocationPermission()
            else {
                AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Izin Diperlukan")
                    .setMessage("Izin lokasi diperlukan untuk fitur ini. Harap aktifkan di pengaturan aplikasi.")
                    .setPositiveButton(getString(R.string.open_settings)) { localDialog, _ ->
                        localDialog.dismiss()
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", packageName, null)
                        startActivityForResult(intent, LOCATION_PERMISSION_REQUEST_CODE)
                    }
                    .show()
            }
        } else if (requestCode == BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) checkLocationPermission()
            else {
                val message = getString(R.string.bg_service_location_permission_message)
                val title = getString(R.string.bg_service_location_permission_title)
                AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(getString(R.string.open_settings)) { localDialog, _ ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                                BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
                            )
                        }
                        localDialog.dismiss()
                    }
                    .show()
            }
        }
    }
}