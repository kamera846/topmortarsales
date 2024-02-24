package com.topmortar.topmortarsales.view.courier

import android.Manifest
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
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
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import com.topmortar.topmortarsales.commons.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
import com.topmortar.topmortarsales.commons.CONST_IS_BASE_CAMP
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_CITY_ID
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_NAME
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_STATUS
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_MAPS_NAME
import com.topmortar.topmortarsales.commons.CONST_NEAREST_STORE
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_ABSENT
import com.topmortar.topmortarsales.commons.LOCATION_PERMISSION_REQUEST_CODE
import com.topmortar.topmortarsales.commons.MAX_REPORT_DISTANCE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_ACTION_MAIN_ACTIVITY
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.TOAST_SHORT
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
import com.topmortar.topmortarsales.databinding.ActivityHomeCourierBinding
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.BaseCampModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import com.topmortar.topmortarsales.view.MapsActivity
import com.topmortar.topmortarsales.view.user.UserProfileActivity
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeCourierActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeCourierBinding
    private lateinit var apiService: ApiService

    private lateinit var sessionManager: SessionManager
    private val userId get() = sessionManager.userID()
    private val userCity get() = sessionManager.userCityID()
    private val userName get() = sessionManager.userName()
    private val userFullName get() = sessionManager.fullName()
    private val userDistributorId get() = sessionManager.userDistributor()
    private val userDistributorNumber get() = sessionManager.userDistributorNumber()
    private val selectedBasecampDefaultID get() = sessionManager.selectedBasecampAbsentID()
    private val selectedBasecampDefaultTitle get() = sessionManager.selectedBasecampAbsentTitle()
    private val selectedBasecampDefaultCoordinate get() = sessionManager.selectedBasecampAbsentCoordinate()

    private var doubleBackToExitPressedOnce = false
    private var isAbsentMorningNow = false
    private var isAbsentEveningNow = false
    private var isLocked = false

    private lateinit var firebaseReference : DatabaseReference
    private var absentProgressDialog : ProgressDialog? = null
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var customUtility : CustomUtility

    private lateinit var searchBaseCampAbsentModal: SearchModal
    private var selectedBasecamp: ModalSearchModel? = null
    private var listBasecamp: ArrayList<BaseCampModel> = arrayListOf()
    private var isSelectBasecampOnly = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        binding = ActivityHomeCourierBinding.inflate(layoutInflater)
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

        binding.selectedBasecampContainer.tvLabel.text = "Basecamp:"

        if (selectedBasecampDefaultID.isNullOrEmpty()) binding.selectedBasecampContainer.tvFilter.text = "Pilih basecamp"
        else {
            binding.selectedBasecampContainer.tvFilter.text = selectedBasecampDefaultTitle
            selectedBasecamp = ModalSearchModel(
                id = selectedBasecampDefaultID,
                title = selectedBasecampDefaultTitle,
                etc = selectedBasecampDefaultCoordinate
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

        binding.deliveryItem.setOnClickListener { if (!isLocked) navigateToDelivery() else showDialogLockedFeature() }
        binding.nearestStoreItem.setOnClickListener { if (!isLocked) navigateToNearestStore() else showDialogLockedFeature() }
        binding.basecampItem.setOnClickListener { if (!isLocked) navigateToBasecamp() else showDialogLockedFeature() }
        binding.nearestBasecampItem.setOnClickListener { if (!isLocked) navigateToNearestBasecamp() else showDialogLockedFeature() }
        binding.myProfileItem.setOnClickListener { navigateToMyProfile() }
        binding.contactAdminItem.setOnClickListener { navigateToContactAdmin() }

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) binding.selectedBasecampContainer.componentFilter.background = getDrawable(R.color.black_400)
        else binding.selectedBasecampContainer.componentFilter.background = getDrawable(R.color.light)
        binding.selectedBasecampContainer.componentFilter.setOnClickListener {
            isSelectBasecampOnly = true
            if (listBasecamp.isEmpty()) getListBasecamp()
            else {
                setupDialogSearch(listBasecamp)
                searchBaseCampAbsentModal.show()
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

//                        absentAction()
                        absentProgressDialog?.show()
                        if (selectedBasecamp == null) {
                            if (listBasecamp.isEmpty()) getListBasecamp()
                            else {
                                setupDialogSearch(listBasecamp)
                                searchBaseCampAbsentModal.show()
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
                    if (selectedBasecamp == null) {
                        if (listBasecamp.isEmpty()) getListBasecamp()
                        else {
                            setupDialogSearch(listBasecamp)
                            searchBaseCampAbsentModal.show()
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
            @RequiresApi(Build.VERSION_CODES.O)
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
                            } else {

                                isAbsentMorningNow = true
                                if (snapshot.child("eveningDateTime").exists()) {
                                    val eveningDateTime = snapshot.child("eveningDateTime").getValue(String::class.java).toString()

                                    if (eveningDateTime.isNotEmpty()) {
                                        val absentEveningDate = DateFormat.format(eveningDateTime, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd")

                                        if (DateFormat.dateAfterNow(absentEveningDate)) {
                                            if (!CustomUtility(this@HomeCourierActivity).isServiceRunning(TrackingService::class.java)) {
                                                val serviceIntent = Intent(this@HomeCourierActivity, TrackingService::class.java)
                                                serviceIntent.putExtra("userId", userId)
                                                serviceIntent.putExtra("userDistributorId", userDistributorId)
                                                serviceIntent.putExtra("deliveryId", AUTH_LEVEL_COURIER + userId)
                                                this@HomeCourierActivity.startService(serviceIntent)
                                            }
                                            isAbsentEveningNow = false
                                            lockMenuItem(false)
                                        } else {
                                            val serviceIntent = Intent(this@HomeCourierActivity, TrackingService::class.java)
                                            this@HomeCourierActivity.stopService(serviceIntent)
                                            isAbsentEveningNow = true
                                            lockMenuItem(true)
                                        }

                                    } else {
                                        if (!CustomUtility(this@HomeCourierActivity).isServiceRunning(TrackingService::class.java)) {
                                            val serviceIntent = Intent(this@HomeCourierActivity, TrackingService::class.java)
                                            serviceIntent.putExtra("userId", userId)
                                            serviceIntent.putExtra("userDistributorId", userDistributorId)
                                            serviceIntent.putExtra("deliveryId", AUTH_LEVEL_COURIER + userId)
                                            this@HomeCourierActivity.startService(serviceIntent)
                                        }
                                        isAbsentEveningNow = false
                                        lockMenuItem(false)
                                    }
                                } else {
                                    if (!CustomUtility(this@HomeCourierActivity).isServiceRunning(TrackingService::class.java)) {
                                        val serviceIntent = Intent(this@HomeCourierActivity, TrackingService::class.java)
                                        serviceIntent.putExtra("userId", userId)
                                        serviceIntent.putExtra("userDistributorId", userDistributorId)
                                        serviceIntent.putExtra("deliveryId", AUTH_LEVEL_COURIER + userId)
                                        this@HomeCourierActivity.startService(serviceIntent)
                                    }
                                    isAbsentEveningNow = false
                                    lockMenuItem(false)
                                }

                            }

                        } else {
                            isAbsentMorningNow = false
                            lockMenuItem(true)
                        }
                    } else {
                        isAbsentMorningNow = false
                        lockMenuItem(true)
                    }
                } else {
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

    private fun absentAction() {
        absentProgressDialog?.dismiss()

        var alertTitle = "Absen Kehadiran"
        var alertMessage = "Konfirmasi absen kehadiran sekarang?"

        if (isAbsentMorningNow) {
            alertTitle = "Absen Pulang"
            alertMessage = "Konfirmasi absen pulang sekarang?"
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

        val mapsUrl = selectedBasecamp?.etc.toString()
        val urlUtility = URLUtility(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (urlUtility.isLocationEnabled(this)) {

                urlUtility.requestLocationUpdate()

                if (!urlUtility.isUrl(mapsUrl) && !mapsUrl.isNullOrEmpty()) {
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
                            val urlUtility = URLUtility(this)
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
                                    .setMessage("Titik anda saat ini $shortDistance km dari titik ${ selectedBasecamp?.title }. Cobalah untuk lebih dekat dengan basecamp!")
                                    .setPositiveButton("Oke") { dialog, _ ->
                                        absentProgressDialog?.dismiss()
                                        dialog.dismiss()
                                    }
                                    .setNegativeButton("Buka Maps") { dialog, _ ->
                                        val intent = Intent(this@HomeCourierActivity, MapsActivity::class.java)
                                        intent.putExtra(CONST_IS_BASE_CAMP, true)
                                        intent.putExtra(CONST_MAPS, mapsUrl)
                                        intent.putExtra(CONST_MAPS_NAME, selectedBasecamp?.title)
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
                    val message = "Anda tidak dapat membuat laporan absen untuk saat ini, silakan hubungi admin untuk memperbarui koordinat basecamp ini"
                    val actionTitle = "Hubungi Sekarang"
                    customUtility.showPermissionDeniedSnackbar(message, actionTitle) { navigateChatAdmin() }
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

    private fun lockMenuItem(state: Boolean) {
        absentProgressDialog?.dismiss()
        isLocked = state
//        isAbsentMorningNow = !state

        binding.deliveryItem.alpha = if (state) 0.5f else 1f
        binding.nearestStoreItem.alpha = if (state) 0.5f else 1f
        binding.basecampItem.alpha = if (state) 0.5f else 1f
        binding.nearestBasecampItem.alpha = if (state) 0.5f else 1f
//        binding.myProfileItem.alpha = if (state) 0.5f else 1f
//        binding.contactAdminItem.alpha = if (state) 0.5f else 1f

        binding.deliveryItemChevron.setImageResource(if (state) R.drawable.lock_dark else R.drawable.chevron_right_dark)
        binding.nearestStoreItemChevron.setImageResource(if (state) R.drawable.lock_dark else R.drawable.chevron_right_dark)
        binding.basecampItemChevron.setImageResource(if (state) R.drawable.lock_dark else R.drawable.chevron_right_dark)
        binding.nearestBasecampItemChevron.setImageResource(if (state) R.drawable.lock_dark else R.drawable.chevron_right_dark)
//        binding.myProfileItemChevron.setImageResource(if (state) R.drawable.lock_dark else R.drawable.chevron_right_dark)
//        binding.contactAdminItemChevron.setImageResource(if (state) R.drawable.lock_dark else R.drawable.chevron_right_dark)

        if (isAbsentMorningNow && isAbsentEveningNow) {

            binding.absentTitle.text = "Absen pulang sudah tercatat!"
            binding.absentDescription.text = "Terima kasih atas kinerja hari ini. Beristirahatlah untuk kinerja yang maksimal esok hari!"

            binding.btnAbsent.visibility = View.GONE
        } else {

            binding.absentTitle.text = if (state) {
                "Yuk, Catat Kehadiranmu Hari ini!"
            } else {
                "Kehadiranmu Telah Tercatat!"
            }
            binding.absentDescription.text = if (state) {
                "Absenmu penting! Jangan lupa untuk mencatat kehadiranmu sekarang dan ciptakan jejak kerja yang positif."
            } else {
                "Terima kasih sudah mencatat kehadiran hari ini dan jangan lupa untuk mencatat absen pulang juga setelah lebih dari pukul 16.00 nanti!"
            }

            binding.btnAbsent.backgroundTintList = ContextCompat.getColorStateList(this, if (state) R.color.status_bid else R.color.red_claret)
            binding.btnAbsent.text = if (state) "Absen Sekarang" else "Pulang Sekarang"

            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY) // Mengambil jam saat ini dalam format 24 jam

            if (isAbsentMorningNow && !isAbsentEveningNow && currentHour < 16) {
                binding.btnAbsent.visibility = View.GONE
                binding.absenEveningInfoText.visibility = View.VISIBLE
            } else {
                binding.btnAbsent.visibility = View.VISIBLE
                binding.absenEveningInfoText.visibility = View.GONE
            }
        }

    }

    private fun navigateToDelivery() {
        val intent = Intent(this, CourierActivity::class.java)
        intent.putExtra("tabIndex", 0)
        startActivity(intent)
    }

    private fun navigateToNearestStore() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Memuat data toko…")
        progressDialog.show()

        Handler().postDelayed({

            lifecycleScope.launch {
                try {

                    val response = apiService.getCourierStore(processNumber = "1", courierId = userId ?: "0")

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

                            val intent = Intent(this@HomeCourierActivity, MapsActivity::class.java)

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

                            val intent = Intent(this@HomeCourierActivity, MapsActivity::class.java)

                            intent.putExtra(CONST_NEAREST_STORE, true)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)

                            progressDialog.dismiss()
                            startActivity(intent)

                        }
                        else -> {

                            handleMessage(this@HomeCourierActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                            progressDialog.dismiss()

                        }
                    }


                } catch (e: Exception) {

                    handleMessage(this@HomeCourierActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                    progressDialog.dismiss()

                }

            }

        }, 1000)
    }

    private fun navigateToBasecamp() {
        val intent = Intent(this, CourierActivity::class.java)
        intent.putExtra("tabIndex", 1)
        startActivity(intent)
    }

    private fun navigateToNearestBasecamp() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Memuat data basecamp…")
        progressDialog.show()

        Handler().postDelayed({

            lifecycleScope.launch {
                try {

                    val apiService: ApiService = HttpClient.create()
                    val response = apiService.getListBaseCamp(cityId = userCity ?: "0", distributorID = userDistributorId ?: "0")

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

                            val intent = Intent(this@HomeCourierActivity, MapsActivity::class.java)

                            intent.putExtra(CONST_NEAREST_STORE, true)
                            intent.putExtra(CONST_IS_BASE_CAMP, true)
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

                            val intent = Intent(this@HomeCourierActivity, MapsActivity::class.java)

                            intent.putExtra(CONST_NEAREST_STORE, true)
                            intent.putExtra(CONST_IS_BASE_CAMP, true)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)

                            progressDialog.dismiss()
                            startActivity(intent)

                        }
                        else -> {

                            handleMessage(this@HomeCourierActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                            progressDialog.dismiss()

                        }
                    }


                } catch (e: Exception) {

                    handleMessage(this@HomeCourierActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                    progressDialog.dismiss()

                }

            }

        }, 1000)
    }

    private fun navigateToMyProfile() {
        val intent = Intent(this, UserProfileActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToContactAdmin() {
        val phoneNumber = if (!userDistributorNumber.isNullOrEmpty()) userDistributorNumber else getString(R.string.topmortar_wa_number)
        val message = "*#Courier Service*\nHalo admin, tolong bantu saya [KETIK PESAN ANDA]"

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}")

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Gagal mengarahkan ke whatsapp", TOAST_SHORT).show()
        }
    }

    private fun showDialogLockedFeature() {
        var title = "Fitur terkunci"
        var message = "Absen terlebih dahulu untuk membuka fitur ini"

        if (isAbsentMorningNow && isAbsentEveningNow) {

            title = "Absen pulang sudah tercatat!"
            message = "Terima kasih atas kinerja hari ini. Beristirahatlah untuk kinerja yang maksimal esok hari!"
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Oke") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setupDialogSearch(items: ArrayList<BaseCampModel> = ArrayList()) {

        val modalItems = arrayListOf<ModalSearchModel>()
        for (item in items) {
            modalItems.add(
                ModalSearchModel(
                    id = item.id_gudang,
                    title = item.nama_gudang,
                    etc = item.location_gudang
                )
            )
        }

        searchBaseCampAbsentModal = SearchModal(this, modalItems)
        searchBaseCampAbsentModal.label = "Pilih Basecamp"
        searchBaseCampAbsentModal.searchHint = "Ketik untuk mencari…"
        searchBaseCampAbsentModal.setOnDismissListener { absentProgressDialog?.dismiss() }
        searchBaseCampAbsentModal.setCustomDialogListener(object : SearchModal.SearchModalListener {
            override fun onDataReceived(data: ModalSearchModel) {
                selectedBasecamp = data
                binding.selectedBasecampContainer.tvFilter.text = data.title
                sessionManager.selectedBasecampAbsent(data.id ?: "", data.title ?: "", data.etc ?: "")
                if (isSelectBasecampOnly) isSelectBasecampOnly = false
                else absentAction()
            }

        })
    }

    private fun getListBasecamp() {

        if (!absentProgressDialog!!.isShowing) absentProgressDialog?.show()

        lifecycleScope.launch {
            try {

                val response = apiService.getListBaseCamp(cityId = userCity.toString(), distributorID = userDistributorId.toString())

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        absentProgressDialog?.dismiss()
                        listBasecamp = response.results
                        setupDialogSearch(listBasecamp)
                        searchBaseCampAbsentModal.show()

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        absentProgressDialog?.dismiss()
                        setupDialogSearch(listBasecamp)
                        searchBaseCampAbsentModal.show()

                    }
                    else -> {

                        handleMessage(this@HomeCourierActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        absentProgressDialog?.dismiss()

                    }
                }

            } catch (e: Exception) {

                handleMessage(this@HomeCourierActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                absentProgressDialog?.dismiss()

            }

        }

    }

    private fun executeAbsentReport(shortDistance: String = "") {

        if (!absentProgressDialog!!.isShowing) absentProgressDialog?.show()

        lifecycleScope.launch {
            try {

                var visitReport = "Absen masuk\n•by system•"
                if (isAbsentMorningNow) visitReport = "Absen pulang\n•by system•"

                val response = apiService.makeVisitCourierReport(
                    idGudang = createPartFromString(selectedBasecamp?.id!!),
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

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val absentDateTime = DateFormat.now()
                                    userChild.child("morningDateTime").setValue(absentDateTime)
                                    userChild.child("lastSeen").setValue(absentDateTime)

                                    sessionManager.absentDateTime(absentDateTime)
                                } else {
                                    userChild.child("morningDateTime").setValue("")
                                    userChild.child("lastSeen").setValue("")
                                }

                                if (!CustomUtility(this@HomeCourierActivity).isServiceRunning(TrackingService::class.java)) {
                                    val serviceIntent = Intent(this@HomeCourierActivity, TrackingService::class.java)
                                    serviceIntent.putExtra("userId", userId)
                                    serviceIntent.putExtra("userDistributorId", userDistributorId)
                                    serviceIntent.putExtra("deliveryId", AUTH_LEVEL_COURIER + userId)
                                    this@HomeCourierActivity.startService(serviceIntent)
                                }

                                absentProgressDialog?.dismiss()
                                checkAbsent()
                            } else {

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val absentDateTime = DateFormat.now()
                                    userChild.child("eveningDateTime").setValue(absentDateTime)
                                    userChild.child("lastSeen").setValue(absentDateTime)

                                    sessionManager.absentDateTime(absentDateTime)
                                } else {
                                    userChild.child("eveningDateTime").setValue("")
                                    userChild.child("lastSeen").setValue("")
                                }

                                val serviceIntent = Intent(this@HomeCourierActivity, TrackingService::class.java)
                                this@HomeCourierActivity.stopService(serviceIntent)

                                absentProgressDialog?.dismiss()
                                checkAbsent()
                            }

                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            absentProgressDialog?.dismiss()
                            handleMessage(this@HomeCourierActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim laporan absen! Message: ${ responseBody.message }")

                        }
                        else -> {

                            absentProgressDialog?.dismiss()
                            handleMessage(this@HomeCourierActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))

                        }
                    }
                } else {

                    absentProgressDialog?.dismiss()
                    handleMessage(this@HomeCourierActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim laporan absen! Error: " + response.message())

                }

            } catch (e: Exception) {

                handleMessage(this@HomeCourierActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)

            }

        }
    }

    private fun navigateChatAdmin() {
        val distributorNumber = sessionManager.userDistributorNumber()!!
        val phoneNumber = if (distributorNumber.isNotEmpty()) distributorNumber else getString(R.string.topmortar_wa_number)
        val message = "*#Courier Service*\nHalo admin, tolong bantu saya untuk memperbarui koordinat pada basecamp *${ selectedBasecamp?.title }*"

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}")

        try {
            startActivity(intent)
            finishAffinity()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Gagal menghubungkan ke whatsapp", TOAST_SHORT).show()
        }

    }

// Override Class

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
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                            BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
                        )
                        localDialog.dismiss()
                    }
                    .show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            checkLocationPermission()
        }
    }

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

    override fun onResume() {
        super.onResume()
//        CustomUtility(this).setUserStatusOnline(true, userDistributorId.toString(), userId.toString())
    }

    override fun onPause() {
        super.onPause()
//        CustomUtility(this).setUserStatusOnline(false, userDistributorId.toString(), userId.toString())
    }

    override fun onStart() {
        super.onStart()
        checkAbsent()
        Handler().postDelayed({
            CustomUtility(this).setUserStatusOnline(true, userDistributorId.toString(), userId.toString())
        }, 1000)
    }

    override fun onStop() {
        super.onStop()
        CustomUtility(this).setUserStatusOnline(false, userDistributorId.toString(), userId.toString())
    }


    override fun onDestroy() {
        super.onDestroy()
        CustomUtility(this).setUserStatusOnline(false, userDistributorId.toString(), userId.toString())
    }
}