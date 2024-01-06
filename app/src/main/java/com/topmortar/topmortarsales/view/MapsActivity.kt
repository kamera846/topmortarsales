package com.topmortar.topmortarsales.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.errors.ApiException
import com.google.maps.model.DirectionsResult
import com.google.maps.model.DirectionsRoute
import com.google.maps.model.TravelMode
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.PlaceAdapter
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_ADMIN
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_BA
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_COURIER
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_SALES
import com.topmortar.topmortarsales.commons.CONNECTION_FAILURE_RESOLUTION_REQUEST
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_DELIVERY_ID
import com.topmortar.topmortarsales.commons.CONST_IS_BASE_CAMP
import com.topmortar.topmortarsales.commons.CONST_IS_TRACKING
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_CITY_ID
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_NAME
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_STATUS
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_MAPS_NAME
import com.topmortar.topmortarsales.commons.CONST_MAPS_STATUS
import com.topmortar.topmortarsales.commons.CONST_NEAREST_STORE
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_DELIVERY
import com.topmortar.topmortarsales.commons.FIREBASE_REFERENCE
import com.topmortar.topmortarsales.commons.GET_COORDINATE
import com.topmortar.topmortarsales.commons.LOCATION_PERMISSION_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_SUCCESS
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_ACTIVE
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_BID
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_DATA
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_PASSIVE
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TOAST_LONG
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_BA
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.URLUtility
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityMapsBinding
import com.topmortar.topmortarsales.modal.FilterTokoModal
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.CityModel
import com.topmortar.topmortarsales.model.DeliveryModel
import com.topmortar.topmortarsales.model.GudangModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private lateinit var sessionManager: SessionManager
    private val userKind get() = sessionManager.userKind().toString()
    private val userID get() = sessionManager.userID().toString()
    private val username get() = sessionManager.userName().toString()
    private val fulllName get() = sessionManager.fullName().toString()
    private val userCityID get() = sessionManager.userCityID().toString()
    private val userDistributorId get() = sessionManager.userDistributor().toString()
    private val userCity get() = sessionManager.userCityID().toString()

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private var routeDirections: Polyline? = null

    private var iMaps: String? = null
    private var iMapsName: String? = null
    private var iMapsStatus: String? = null
    private var iContactID: String? = null
    private var isGetCoordinate = false
    private var isNearestStore = false
    private var isBasecamp = false
    private var listCoordinate: ArrayList<String>? = null
    private var listCoordinateName: ArrayList<String>? = null
    private var listCoordinateStatus: ArrayList<String>? = null
    private var listCoordinateCityID: ArrayList<String>? = null

    private val zoomLevel = 18f
    private val mapsDuration = 2000
    private var selectedLocation: LatLng? = null
    private var currentLatLng: LatLng? = null
    private var lineColor = listOf(R.color.primary_200, R.color.status_passive, R.color.status_passive)

    private lateinit var mLastLocation: Location
    private var mCurrLocationMarker: Marker? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private lateinit var mLocationRequest: LocationRequest

    private lateinit var icBack: ImageView
    private lateinit var etSearch: EditText
    private lateinit var icClear: ImageView
    private lateinit var progressDialog: ProgressDialog

    // Setup Filter
    private lateinit var filterModal: FilterTokoModal
    private var cities: ArrayList<CityModel> = arrayListOf()
    private var selectedStatusID: String = "-1"
    private var selectedVisitedID: String = "-1"
    private var selectedCitiesID: CityModel? = null

    // Setup Route
    private var selectedTargetRoute: Marker? = null
    private var isRouteActive: Boolean = false
    private var isCardNavigationShowing: Boolean = false
    private var listRouteLines: MutableList<Polyline> = mutableListOf()

    // Tracking
    private var database: DatabaseReference? = null
    private var childDelivery: DatabaseReference? = null
    private var childDriver: DatabaseReference? = null
    private var locationCallback: LocationCallback? = null
    private var locationListener: ValueEventListener? = null
    private var courierMarker: Marker? = null
    private var isTracking = false
    private var deliveryID: String? = null

    private var listGudang: ArrayList<GudangModel> = arrayListOf()
    private var selectedCenterPoint: ModalSearchModel? = null
    private lateinit var searchModal: SearchModal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isBasecamp = intent.getBooleanExtra(CONST_IS_BASE_CAMP, false)

        progressDialog = ProgressDialog(this)
//        progressDialog.setTitle("Mencari ${if (isBasecamp) "basecamp" else "toko"} terdekat")
//        progressDialog.setMessage("Sedang memuat…")
        progressDialog.setMessage("Mencari ${if (isBasecamp) "basecamp" else "toko"} terdekat…")
        progressDialog.setCancelable(false)

        checkLocationPermission()

    }

    private fun checkLocationPermission() {
        val urlUtility = URLUtility(this)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (urlUtility.isLocationEnabled(this)) {

                val urlUtility = URLUtility(this)
                urlUtility.requestLocationUpdate()

                initView()

            } else {
                val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(enableLocationIntent)
            }
        } else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
    }

    private fun initView() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.maps_key))
        }
        placesClient = Places.createClient(this)

        icBack = findViewById(R.id.ic_close_search)
        etSearch = findViewById(R.id.et_search_box)
        icClear = findViewById(R.id.ic_clear_search)

        icBack.visibility = View.GONE
        icClear.setOnClickListener { etSearch.setText("") }

        val padding16 = convertDpToPx(16, this)
        etSearch.setPadding(padding16, padding16, padding16, padding16)
        etSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) icClear.visibility = View.VISIBLE
                else icClear.visibility = View.GONE
                searchLocation()
            }

        })

        binding.btnGetDistance.visibility = View.GONE
        binding.btnGetLatLng.visibility = View.GONE
        binding.btnGetDirection.visibility = View.GONE
    }

    @SuppressLint("MissingPermission")
    private fun setupMaps() {

        onCalculate()
        onGetLatLng()
        searchLocation()
        dataActivityValidation()

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isTiltGesturesEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isScrollGesturesEnabledDuringRotateOrZoom = true

        if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_SALES) {
            if (isNearestStore) binding.llFilter.visibility = View.VISIBLE
            binding.llFilter.setOnClickListener {
                setupFilterTokoModal()
                filterModal.show()
            }
        }

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark))
            lineColor = listOf(R.color.primary_200, R.color.status_passive, R.color.status_passive)
        }

        if (binding.btnGetLatLng.isVisible) {
            mMap.setPadding(0,0,0, convertDpToPx(80, this))
            mMap.setOnMapLongClickListener { latLng -> setPin(latLng, getPlaceNameFromLatLng(latLng), moveCamera = false) }
            if (!sessionManager.pinMapHint()) {
                showDialog(message = "Tekan dan tahan pada peta untuk menandai lokasi")
                sessionManager.pinMapHint(true)
            }
        } else if (binding.btnGetDirection.isVisible) {
            mMap.setPadding(0,0,0, convertDpToPx(80, this))

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) currentLatLng = LatLng(location.latitude,location.longitude)
//                    if (location != null) currentLatLng = LatLng(-7.952356,112.692583)
                }

            binding.btnGetDirection.setOnClickListener {
                toggleDirection()
            }
        } else if (binding.llFilter.isVisible) {
            mMap.setPadding(0,convertDpToPx(36, this),0,0)
        } else mMap.setPadding(0,0,0,convertDpToPx(16, this))

        mMap.setOnMapClickListener {
            if (binding.recyclerView.isVisible) binding.recyclerView.visibility = View.GONE
        }

        mMap.setOnMarkerClickListener {
            if (!isGetCoordinate && isNearestStore) {
                isCardNavigationShowing = true
                selectedTargetRoute = it
                selectedLocation = LatLng(it.position.latitude, it.position.longitude)
                toggleDrawRoute()
            }
            return@setOnMarkerClickListener false
        }

        binding.cardBack.visibility = View.VISIBLE
        binding.cardBack.setOnClickListener { backHandler() }
    }

    private fun toggleDrawRoute() {
        if (isNearestStore) {
            if (binding.llFilter.isVisible) mMap.setPadding(0, convertDpToPx(32, this@MapsActivity),0, convertDpToPx(16, this@MapsActivity))
            else mMap.setPadding(0,0,0, convertDpToPx(16, this@MapsActivity))
            if (selectedTargetRoute == null) {
                binding.cardGetDirection.visibility = View.GONE
                binding.cardTelusuri.visibility = View.VISIBLE
                isCardNavigationShowing = false
            } else {
                binding.cardGetDirection.visibility = View.VISIBLE
                binding.cardTelusuri.visibility = View.GONE
                if (!isRouteActive) {
                    binding.textTitleTarget.text = selectedTargetRoute?.title
                    binding.textTargetRute.text = "Petunjuk rute menuju ke lokasi ${if (isBasecamp) "basecamp" else "toko"}"

                    val itemToFind = "${selectedTargetRoute?.position?.latitude},${selectedTargetRoute?.position?.longitude}"
                    val indexOfItem = listCoordinate!!.indexOf(itemToFind)
                    val selectedStatus = if (indexOfItem != -1) listCoordinateStatus!![indexOfItem] else ""
                    val imgDrawable = when (selectedStatus.lowercase(Locale.getDefault())) {
                        STATUS_CONTACT_DATA -> R.drawable.store_location_status_data
                        STATUS_CONTACT_ACTIVE -> R.drawable.store_location_status_active
                        STATUS_CONTACT_PASSIVE -> R.drawable.store_location_status_passive
                        STATUS_CONTACT_BID -> R.drawable.store_location_status_biding
                        else -> {
                            if (selectedCenterPoint != null && selectedCenterPoint?.etc == itemToFind) {
                                binding.textTargetRute.text = "Petunjuk rute menuju ke lokasi gudang"
                                R.drawable.gudang
                            } else R.drawable.store_location_status_blacklist
                        }
                    }
                    binding.imgTargetRoute.setImageDrawable(getDrawable(imgDrawable))

                }
                binding.btnDrawRoute.setOnClickListener {
                    toggleBtnDrawRoute()
                }
            }
        }
    }

    private fun toggleBtnDrawRoute() {
        if (routeDirections == null) {

            if (currentLatLng == null) showDialog(message = "Gagal menemukan lokasi Anda saat ini. Pastikan lokasi Anda aktif dan coba buka kembali peta")
            else if (selectedLocation == null) showDialog(message = "Gagal menemukan lokasi target")
            else {
                isRouteActive = true
                getDirections()
            }

        } else if (routeDirections != null) {

            // Remove line routes
            for (polyline in listRouteLines) {
                polyline.remove()
            }
            listRouteLines.clear()

            Handler().postDelayed({
                val button = binding.btnDrawRoute
                val img = binding.btnDrawRouteImg
                val title = binding.btnDrawRouteTitle
                button.setBackgroundResource(R.drawable.bg_primary_round)
                img.setImageDrawable(getDrawable(R.drawable.direction_white))
                title.text = "Aktifkan Navigasi"

                // Live Location Update with Firebase Realtime Database
                stopTracking()
            }, 500)

            val limitKm = binding.etKm.text.toString().toDouble()
            val responsiveZoom = when {
                limitKm >= 1 -> when {
                    limitKm >= 18 -> 10
                    limitKm >= 13 -> 11
                    limitKm >= 8 -> 12
                    limitKm >= 3 -> 13
                    else -> 14
                }
                else -> 15
            }
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(selectedLocation!!.latitude, selectedLocation!!.longitude), responsiveZoom.toFloat())
            mMap.animateCamera(cameraUpdate, 2000, null)

            routeDirections!!.remove()
            routeDirections = null
            selectedTargetRoute = null
            isRouteActive = false

            toggleDrawRoute()
        }
    }

    private fun toggleDirection() {
        if (routeDirections == null) {

            if (currentLatLng == null) showDialog(message = "Gagal menemukan lokasi Anda saat ini. Pastikan lokasi Anda aktif dan coba buka kembali peta")
            else if (selectedLocation == null) showDialog(message = "Gagal menemukan lokasi target")
            else getDirections()

        } else if (routeDirections != null) {
            mMap.clear()
            setupMaps()
            routeDirections!!.remove()
            routeDirections = null
            Handler().postDelayed({
                val button = binding.btnGetDirection
                val img = binding.btnGetDirectionImg
                val title = binding.btnGetDirectionTitle
                button.setBackgroundResource(R.drawable.bg_primary_round)
                img.setImageDrawable(getDrawable(R.drawable.direction_white))
                title.text = "Aktifkan Navigasi"
            }, 500)
        }
    }

    private fun showDialog(title: String = "Perhatian!", message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Oke") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    @SuppressLint("MissingPermission")
    private fun dataActivityValidation() {

        iMaps = intent.getStringExtra(CONST_MAPS)
        iMapsName = intent.getStringExtra(CONST_MAPS_NAME)
        iMapsStatus = intent.getStringExtra(CONST_MAPS_STATUS)
        iContactID = intent.getStringExtra(CONST_CONTACT_ID)
        deliveryID = intent.getStringExtra(CONST_DELIVERY_ID)
        isGetCoordinate = intent.getBooleanExtra(GET_COORDINATE, false)
        isNearestStore = intent.getBooleanExtra(CONST_NEAREST_STORE, false)
        isTracking = intent.getBooleanExtra(CONST_IS_TRACKING, false)
        listCoordinate = intent.getStringArrayListExtra(CONST_LIST_COORDINATE)
        listCoordinateName = intent.getStringArrayListExtra(CONST_LIST_COORDINATE_NAME)
        listCoordinateStatus = intent.getStringArrayListExtra(CONST_LIST_COORDINATE_STATUS)
        listCoordinateCityID = intent.getStringArrayListExtra(CONST_LIST_COORDINATE_CITY_ID)

        if (isGetCoordinate) {
            binding.btnGetLatLng.visibility = View.VISIBLE
            binding.searchBar.visibility = View.VISIBLE
        } else {
            if (!isNearestStore && !isTracking) {

                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) currentLatLng = LatLng(location.latitude,location.longitude)
//                    if (location != null) currentLatLng = LatLng(-7.952356,112.692583)
                    }
                binding.cardGetDirection.visibility = View.VISIBLE
                if (binding.llFilter.isVisible) mMap.setPadding(0, convertDpToPx(32, this@MapsActivity),0, convertDpToPx(16, this@MapsActivity))
                else mMap.setPadding(0,0,0, convertDpToPx(16, this@MapsActivity))
                binding.textTitleTarget.text = iMapsName
                binding.textTargetRute.text = "Petunjuk rute menuju ke lokasi ${if (isBasecamp) "basecamp" else "toko"}"

                val imgDrawable = R.drawable.store_location_status_blacklist
                binding.imgTargetRoute.setImageDrawable(getDrawable(imgDrawable))
                binding.btnDrawRoute.setOnClickListener {
                    toggleBtnDrawRoute()
                }
            }
        }

        if (!iMaps.isNullOrEmpty()) {

            val urlUtility = URLUtility(this)

            if (urlUtility.isUrl(iMaps!!)) {
                val mapsUrlPattern1 = Regex("https://goo\\.gl/maps/\\w+")
                val mapsUrlPattern2 = Regex("https://maps\\.app\\.goo\\.gl/\\w+")

                if (mapsUrlPattern1.matches(iMaps!!) || mapsUrlPattern2.matches(iMaps!!)) return onFindLocation(iMaps!!)
                else showDialog(message = "Gagal memproses maps url")
            } else {

                val coordinates = iMaps!!.trim().split(",")
                if (coordinates.size == 2) {
                    val latitude = coordinates[0].toDoubleOrNull()
                    val longitude = coordinates[1].toDoubleOrNull()

                    if (latitude != null && longitude != null) {
                        val latLng = LatLng(latitude, longitude)
                        etSearch.setText(getPlaceNameFromLatLng(latLng))
                        binding.recyclerView.visibility = View.GONE
                        return initMaps(latLng, iMapsName)
                    } else showDialog(message = "Gagal menavigasi koordinat")
                } else showDialog(message = "Gagal memproses koordinat")

            }

            initMaps()

        } else initMaps()

    }

    @SuppressLint("SuspiciousIndentation")
    private fun searchCoordinate() {
        if (!progressDialog.isShowing) {
            progressDialog.show()
//            Handler().postDelayed({
//                progressDialog.setMessage("Sedang memuat lebih banyak data ${if (isBasecamp) "basecamp" else "toko"}…")
//            }, 1000)
        }

        Handler().postDelayed({

            val urlUtility = URLUtility(this)
            val limitKm = binding.etKm.text.toString().toDouble()
            var currentTotal = 0

            mMap.clear()

            var centerPointLatLng = currentLatLng
            println(selectedCenterPoint)
            if (selectedCenterPoint != null && selectedCenterPoint?.id != "-1") {
                val coordinates = selectedCenterPoint?.etc!!.trim().split(",")
                centerPointLatLng = if (coordinates.size == 2) {
                    val latitude = coordinates[0].toDoubleOrNull()
                    val longitude = coordinates[1].toDoubleOrNull()
                    LatLng(latitude!!, longitude!!)
                } else currentLatLng

                val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.gudang)

                val newWidth = convertDpToPx(50, this@MapsActivity)
                val newHeight = convertDpToPx(50, this@MapsActivity)

                val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false)

                val markerOptions = MarkerOptions()
                    .position(centerPointLatLng!!)
                    .title(selectedCenterPoint?.title!!)
                    .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))

                mMap.addMarker(markerOptions)
            }

            CoroutineScope(Dispatchers.Default).launch {
                withContext(Dispatchers.Main) {
                    // Start For Loop
                    for ((i, item) in listCoordinate!!.iterator().withIndex()) {

                        if (!urlUtility.isUrl(item)) {

                            val coordinates = item.trim().split(",")
                            if (coordinates.size == 2) {
                                val latitude = coordinates[0].toDoubleOrNull()
                                val longitude = coordinates[1].toDoubleOrNull()

                                if (latitude != null && longitude != null) {

                                    val urlUtility = URLUtility(this@MapsActivity)
                                    val distance = urlUtility.calculateDistance(centerPointLatLng!!.latitude, centerPointLatLng.longitude, latitude, longitude)

                                    if (distance < limitKm) {

                                        val latLng = LatLng(latitude, longitude)
                                        binding.recyclerView.visibility = View.GONE

                                        val iconDrawable = when (listCoordinateStatus?.get(i)) {
                                            STATUS_CONTACT_DATA -> R.drawable.store_location_status_data
                                            STATUS_CONTACT_ACTIVE -> R.drawable.store_location_status_active
                                            STATUS_CONTACT_PASSIVE -> R.drawable.store_location_status_passive
                                            STATUS_CONTACT_BID -> R.drawable.store_location_status_biding
                                            else -> R.drawable.store_location_status_blacklist
                                        }

                                        val originalBitmap = BitmapFactory.decodeResource(resources, iconDrawable)

                                        val newWidth = convertDpToPx(40, this@MapsActivity)
                                        val newHeight = convertDpToPx(40, this@MapsActivity)

                                        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false)

                                        selectedLocation = latLng
                                        val markerOptions = MarkerOptions()
                                            .position(latLng)
                                            .title(listCoordinateName?.get(i))
                                            .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))

                                        val onlyStatus = selectedStatusID != "-1" && selectedCitiesID == null && listCoordinateStatus!![i] == selectedStatusID.lowercase(Locale.getDefault())
                                        val onlyCities = selectedCitiesID != null && selectedStatusID == "-1" && listCoordinateCityID!![i] == selectedCitiesID?.id_city
                                        val statusAndCities = selectedStatusID != "-1" && listCoordinateStatus!![i] == selectedStatusID.lowercase(Locale.getDefault()) && selectedCitiesID != null && listCoordinateCityID!![i] == selectedCitiesID?.id_city

                                        if (statusAndCities) {
                                            mMap.addMarker(markerOptions)
                                            currentTotal ++
                                        } else if (onlyStatus) {
                                            mMap.addMarker(markerOptions)
                                            currentTotal ++
                                        } else if (onlyCities) {
                                            mMap.addMarker(markerOptions)
                                            currentTotal ++
                                        } else if (selectedStatusID == "-1" && selectedCitiesID == null) {
                                            mMap.addMarker(markerOptions)
                                            currentTotal ++
                                        }

                                    }

                                }
                            }

                        }

                    } // End For Loop

                    var textFilter = ""

                    if (selectedStatusID != "-1" || selectedVisitedID != "-1" || selectedCitiesID != null) {
                        textFilter += if (selectedCitiesID != null && selectedCitiesID?.id_city != "-1") selectedCitiesID?.nama_city else ""
                        textFilter += if (selectedStatusID != "-1") if (textFilter.isNotEmpty()) ", $selectedStatusID" else selectedStatusID else ""
                        textFilter += if (selectedVisitedID != "-1") if (textFilter.isNotEmpty()) ", $selectedVisitedID" else selectedVisitedID else ""
                    } else textFilter = getString(R.string.tidak_ada_filter)

                    binding.tvFilter.text = "$textFilter ($currentTotal)"

                    progressDialog.dismiss()
//                    progressDialog.setMessage("Sedang memuat…")

                    val durationMs = 2000
                    val responsiveZoom = when {
                        limitKm >= 1 -> when {
                            limitKm >= 18 -> 10
                            limitKm >= 13 -> 11
                            limitKm >= 8 -> 12
                            limitKm >= 3 -> 13
                            else -> 14
                        }
                        else -> 15
                    }
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(centerPointLatLng!!, responsiveZoom.toFloat())
                    mMap.animateCamera(cameraUpdate, durationMs, null)

                    if (currentTotal > 0) {
                        binding.textTitleTotalNearest.text = "Penelusuran ${if (isBasecamp) "Basecamp" else "Toko"} Terdekat"
                        binding.textTotalNearest.text = "$currentTotal ${if (isBasecamp) "basecamp" else "toko"} ${ if (selectedStatusID != "-1") "$selectedStatusID " else "" }ditemukan dalam radius $limitKm km"
                    } else {
                        showDialog(message = "Tidak menemukan ${if (isBasecamp) "basecamp" else "toko"} ${ if (selectedStatusID != "-1") "$selectedStatusID " else "" }di sekitar anda saat ini dalam radius jarak $limitKm km")
                        binding.textTitleTotalNearest.text = "Penelusuran ${if (isBasecamp) "Basecamp" else "Toko"} Terdekat"
                        binding.textTotalNearest.text = "Tidak dapat menemukan ${if (isBasecamp) "basecamp" else "toko"} ${ if (selectedStatusID != "-1") "$selectedStatusID " else "" }dalam radius $limitKm km"
                    }

                    binding.cardTelusuri.visibility = View.VISIBLE
                    if (listGudang.isNotEmpty()) {
                        binding.centerPointContainer.visibility = View.VISIBLE
                        binding.centerPointMore.setOnClickListener {
                            searchModal.show()
                        }
                    } else binding.centerPointContainer.visibility = View.GONE
                    if (binding.llFilter.isVisible) mMap.setPadding(0, convertDpToPx(32, this@MapsActivity),0, convertDpToPx(16, this@MapsActivity))
                    else mMap.setPadding(0,0,0, convertDpToPx(16, this@MapsActivity))
                    binding.btnTelusuri.setOnClickListener {
                        if (binding.etKm.toString().isNotEmpty()) {
                            binding.etKm.error = null
                            binding.etKm.clearFocus()
                            searchCoordinate()
                        } else {
                            binding.etKm.error = "1-100"
                            binding.etKm.requestFocus()
                        }
                    }
                    binding.btnMinusKm.setOnClickListener {
                        binding.etKm.clearFocus()
                        binding.etKm.error = null
                        val etKm = binding.etKm.text.toString().toInt()
                        if (etKm > 1) binding.etKm.setText("${etKm - 1}")
                    }
                    binding.btnPlusKm.setOnClickListener {
                        binding.etKm.clearFocus()
                        binding.etKm.error = null
                        val etKm = binding.etKm.text.toString().toInt()
                        if (etKm < 100) binding.etKm.setText("${etKm + 1}")
                    }
                    binding.etKm.addTextChangedListener(object: TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        }

                        override fun afterTextChanged(s: Editable?) {
                            val etKm = s.toString()
                            if (etKm.isNotEmpty()) {
                                if (etKm.toInt() < 1) binding.etKm.setText("${1}")
                                else if (etKm.toInt() > 100) binding.etKm.setText("${100}")
                            }
                        }

                    })
                }
            }

        }, 100)
    }

    private fun onFindLocation(mapsUrl: String) {

        val urlUtility = URLUtility(this)

        urlUtility.fetchOriginalUrl(mapsUrl) { originalUrl ->

            if (originalUrl.isNotEmpty()) {

                val latLng = urlUtility.getLatLng(originalUrl)
                if (latLng == null) {
                    val placeName = getPlaceNameFromMapsUrl(originalUrl)
                    if (!placeName.isNullOrEmpty()) {
                        etSearch.setText(placeName)
                        binding.recyclerView.visibility = View.GONE
                        searchLocation(placeName)
                    } else showDialog(message = "Gagal menemukan koordinatnya. Silakan pilih manual di peta!")

                } else initMaps(latLng)

            } else showDialog(message = "Gagal memproses URL")

        }

    }

    private fun getPlaceNameFromMapsUrl(mapsUrl: String): String? {
        val startIndex = mapsUrl.indexOf("/place/") + "/place/".length
        val endIndex = mapsUrl.indexOf("/data=")

        if (startIndex != -1 && endIndex != -1) return mapsUrl.substring(startIndex, endIndex).replace("+", " ")

        return null
    }

    private fun getPlaceNameFromLatLng(latLng: LatLng?): String {

        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latLng!!.latitude, latLng.longitude, 1)

        return if (addresses!!.isNotEmpty()) addresses[0].getAddressLine(0) else "Nama Lokasi Tidak Ditemukan"

    }

    private fun onGetLatLng() {
        binding.btnGetLatLng.setOnClickListener {

            if (selectedLocation != null) {

                val resultIntent = Intent()
                resultIntent.putExtra("latitude", selectedLocation!!.latitude)
                resultIntent.putExtra("longitude", selectedLocation!!.longitude)
                setResult(RESULT_OK, resultIntent)
                finish()

            } else Toast.makeText(this, "Tidak ada lokasi yang ditandai", Toast.LENGTH_SHORT).show()

        }
    }

    private fun onCalculate() {

        val btnGetDistance = findViewById<Button>(R.id.btnGetDistance)
        btnGetDistance.setOnClickListener {

            if (selectedLocation != null) {

                val urlUtility = URLUtility(this)
                val distance = urlUtility.calculateDistance(currentLatLng!!.latitude, currentLatLng!!.longitude, selectedLocation!!.latitude, selectedLocation!!.longitude)
                val shortDistance = "%.3f".format(distance).toDouble()

                val message: String = if (distance > 0.2) "$shortDistance Sorry, your distance has exceeded 200 meters."
                else "$shortDistance Congratulation, your distance is closer than 200 meters."

                Toast.makeText(this@MapsActivity, message, TOAST_LONG).show()

            } else Toast.makeText(this, "Not selected location", Toast.LENGTH_SHORT).show()

        }

    }

    private fun getDirections() {
        val currentLocation = currentLatLng ?: return
        val destination = selectedLocation ?: return

        val directions = DirectionsApi.newRequest(getGeoContext())
            .mode(TravelMode.DRIVING) // Ganti dengan mode perjalanan yang sesuai
            .origin(com.google.maps.model.LatLng(currentLocation.latitude, currentLocation.longitude))
            .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
            .optimizeWaypoints(true)
            .alternatives(true)

        try {
            val result = directions.await()

            if (result.routes.isNotEmpty()) {
                val listPolylineOptions = arrayListOf<PolylineOptions>()

                // Gambar rute pada peta
                for (i in 0..result.routes.size) {
                    if (i < result.routes.size && i < 3) {
                        val route = result.routes[i]
                        val overviewPolyline = route.overviewPolyline.decodePath()
                        val polylineOptions = PolylineOptions()
                        polylineOptions.width(15f)
                        polylineOptions.color(getColor(lineColor[i]))
                        for (latLng in overviewPolyline) {
                            polylineOptions.add(LatLng(latLng.lat, latLng.lng))
                        }
                        listPolylineOptions.add(polylineOptions)
                    }
                }

                for (i in listPolylineOptions.size - 1 downTo 0) {
                    routeDirections = mMap.addPolyline(listPolylineOptions[i])
                    listRouteLines.add(routeDirections!!)
                }

                val bounds = LatLngBounds.builder()
                    .include(currentLocation)
                    .include(destination)
                    .build()
                val updateCamera = CameraUpdateFactory.newLatLngBounds(bounds, 100)
                mMap.animateCamera(updateCamera, mapsDuration, null)

                Handler().postDelayed({
                    val button = binding.btnDrawRoute
                    val img = binding.btnDrawRouteImg
                    val title = binding.btnDrawRouteTitle
                    button.setBackgroundResource(R.drawable.bg_passive_round)
                    img.setImageDrawable(getDrawable(R.drawable.direction_line_white))
                    title.text = "Matikan Navigasi"

                    // Live Location Update with Firebase Realtime Database
                    startTracking()
                }, 500)

            } else {
                Toast.makeText(this, "Tidak ada rute ditemukan", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Gagal memuat navigasi", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Toast.makeText(this, "Gagal memuat navigasi", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } catch (e: IOException) {
            Toast.makeText(this, "Gagal memuat navigasi", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    private fun getDirections2() {
        val currentLocation = currentLatLng ?: return
        val destination = selectedLocation ?: return
        var directionsResults: List<DirectionsRoute>? = null

        val directionsApiRequest = DirectionsApi.newRequest(getGeoContext())
            .origin(com.google.maps.model.LatLng(currentLocation.latitude, currentLocation.longitude))
            .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
            .mode(TravelMode.DRIVING)
            .optimizeWaypoints(true)
            .alternatives(true) // Mengaktifkan opsi jalur alternatif

        directionsApiRequest.setCallback(object : PendingResult.Callback<DirectionsResult> {
            override fun onResult(result: DirectionsResult) {
                directionsResults = result.routes.toList()

                // Menampilkan semua jalur yang tersedia dengan warna berbeda
                for ((index, route) in directionsResults!!.withIndex()) {
                    val color = if (index == 0) {
                        R.color.primary
                    } else {
                        R.color.primary_600 // Warna untuk jalur lebih lambat
                    }

                    val overviewPolyLine = route.overviewPolyline.decodePath()
                    val polylineOptions = PolylineOptions()
//                        .addAll(route.overviewPolyline.decodePath())
                        .width(10f)
                        .color(color)

                    for (latLng in overviewPolyLine) {
                        polylineOptions.add(LatLng(latLng.lat, latLng.lng))
                    }

                    mMap.addPolyline(polylineOptions)
                }

                // Menandai jalur yang sudah dilewati dengan warna Hijau
                val completedRouteIndex = 0 // Ganti dengan indeks jalur yang sudah dilewati
                val completedRoute = directionsResults!![completedRouteIndex]
                val completeOverviewPolyLine = completedRoute.overviewPolyline.decodePath()
                val completedPolylineOptions = PolylineOptions()
//                    .addAll(completedRoute.overviewPolyline.decodePath())
                    .width(10f)
                    .color(R.color.primary_300) // Warna untuk jalur yang sudah dilewati

                for (latLng in completeOverviewPolyLine) {
                    completedPolylineOptions.add(LatLng(latLng.lat, latLng.lng))
                }
                mMap.addPolyline(completedPolylineOptions)

                // Zoom ke rute pertama
                val bounds = LatLngBounds.builder()
                    .include(currentLocation)
                    .include(destination)
                    .build()
                val updateCamera = CameraUpdateFactory.newLatLngBounds(bounds, 100)
                mMap.animateCamera(updateCamera, mapsDuration, null)
            }

            override fun onFailure(e: Throwable) {
                // Tangani kesalahan permintaan API di sini
            }
        })
    }

    private fun getGeoContext(): GeoApiContext {

        return GeoApiContext.Builder()
            .apiKey(getString(R.string.maps_key)) // Ganti dengan kunci API Google Maps Anda
            .build()

    }


    @SuppressLint("MissingPermission")
    private fun initMaps(latLng: LatLng? = null, latLngName: String? = null) {

        if (latLng != null) setPin(latLng, latLngName ?: getPlaceNameFromLatLng(latLng))
        else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        currentLatLng = LatLng(location.latitude, location.longitude)
//                        currentLatLng = LatLng(-7.952356,112.692583)
                        if (isTracking) setupTracking()
                        else setPin(currentLatLng!!, "Lokasi Saya")

                        if (isNearestStore && binding.llFilter.isVisible) {
                            getCities()
                            getListGudang()
//                            if (userKind == USER_KIND_ADMIN) getListGudang()
                        } else if (isNearestStore) {
                            searchCoordinate()
                            getListGudang()
//                            if (userKind == USER_KIND_ADMIN) getListGudang()
                        }
                    }
                }
        }

    }

    private fun setPin(latLng: LatLng, placeName: String, moveCamera: Boolean = true) {

        val iconDrawable = R.drawable.store_location_status_blacklist
        val originalBitmap = BitmapFactory.decodeResource(resources, iconDrawable)

        val newWidth = convertDpToPx(40, this)
        val newHeight = convertDpToPx(40, this)

        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false)

        selectedLocation = latLng
        mMap.clear()
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(placeName)
                .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
        )

        if (moveCamera) {

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel)
            mMap.animateCamera(cameraUpdate, mapsDuration, null)

        }
        binding.recyclerView.visibility = View.GONE

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
                val customUtility = CustomUtility(this)
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    val message = "Izin lokasi diperlukan untuk fitur ini. Izinkan aplikasi mengakses lokasi perangkat."
                    customUtility.showPermissionDeniedSnackbar(message) { ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE) }
                } else customUtility.showPermissionDeniedDialog("Izin lokasi diperlukan untuk fitur ini. Harap aktifkan di pengaturan aplikasi.")
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            setupMaps()

        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        buildGoogleApiClient()
        mMap.isMyLocationEnabled = true
    }

    private fun buildGoogleApiClient(){
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API).build()
        mGoogleApiClient!!.connect()
    }

    @SuppressLint("MissingPermission")
    private fun searchLocation(placeName: String? = null) {
        if (!placeName.isNullOrEmpty()) {

            var addressList: List<Address>? = null

            val geoCoder = Geocoder(this)
            try { addressList = geoCoder.getFromLocationName(placeName, 1) }
            catch (e: IOException) { e.printStackTrace() }

            val address = addressList!![0]
            val latLng = LatLng(address.latitude, address.longitude)

            initMaps(latLng)

        } else {

            val location = "${ etSearch.text }"

            if (!location.isNullOrEmpty()) {
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(location)
                    .build()

                placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->

                        val predictions = response.autocompletePredictions
                        val placeIds = predictions.map { prediction: AutocompletePrediction -> prediction.placeId }
                        val placeNames = predictions.map { prediction: AutocompletePrediction -> prediction.getPrimaryText(null).toString() }
                        val placeAddress = predictions.map { prediction: AutocompletePrediction -> prediction.getSecondaryText(null).toString() }
//                        val placeDistance = predictions.map { prediction: AutocompletePrediction -> prediction.distanceMeters.toString() }

                        if (placeNames.isNotEmpty()) {
                            val placeAdapter = PlaceAdapter(placeNames, placeAddress) { position ->

                                val fields = listOf(Place.Field.LAT_LNG)
                                val request = FetchPlaceRequest.builder(placeIds[position], fields).build()

                                placesClient.fetchPlace(request)
                                    .addOnSuccessListener { response: FetchPlaceResponse ->

                                        val place = response.place
                                        val latLng = place.latLng

                                        if (latLng != null) setPin(latLng, placeNames[position])
                                        else {
                                            Toast.makeText(this, "Gagal menampilkan koordinat", TOAST_SHORT).show()
                                            binding.recyclerView.visibility = View.GONE
                                        }

                                    }
                                    .addOnFailureListener { _: Exception ->
                                        Toast.makeText(this, "Gagal menampilkan lokasi", TOAST_SHORT).show()
                                        binding.recyclerView.visibility = View.GONE
                                    }
                            }

                            binding.recyclerView.apply {
                                layoutManager = LinearLayoutManager(this@MapsActivity)
                                adapter = placeAdapter
                                if (isGetCoordinate) this.visibility = View.VISIBLE
                            }
                        } else binding.recyclerView.visibility = View.GONE

                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menemukan lokasi", TOAST_SHORT).show()
                        binding.recyclerView.visibility = View.GONE
                    }
            } else binding.recyclerView.visibility = View.GONE

        }
    }

    private fun setupFilterTokoModal() {

        if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_SALES) {
            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) binding.llFilter.background = getDrawable(R.color.black_400)
            else binding.llFilter.background = getDrawable(R.color.light)

            binding.llFilter.visibility = View.VISIBLE

            filterModal = FilterTokoModal(this)
            if (userKind == USER_KIND_ADMIN) {
                filterModal.setStatuses(selected = selectedStatusID)
                filterModal.setCities(items = cities, selected = selectedCitiesID)
            } else if (userKind == USER_KIND_SALES) filterModal.setStatuses(selected = selectedStatusID)
            filterModal.setSendFilterListener(object: FilterTokoModal.SendFilterListener {
                override fun onSendFilter(
                    selectedStatusID: String,
                    selectedVisitedID: String,
                    selectedCitiesID: CityModel?
                ) {

                    this@MapsActivity.selectedStatusID = selectedStatusID
                    this@MapsActivity.selectedVisitedID = selectedVisitedID
                    this@MapsActivity.selectedCitiesID = selectedCitiesID

//                    handleMessage(this@MapsActivity, "Filter Maps", "$selectedStatusID : $selectedVisitedID : ${selectedCitiesID?.id_city}")
                    searchCoordinate()

                }

            })
        }
    }

    private fun getCities() {

        progressDialog.show()

        // Get Cities
        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getCities(distributorID = userDistributorId)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        cities = response.results
                        val items: ArrayList<ModalSearchModel> = ArrayList()

                        items.add(ModalSearchModel("-1", "Hapus filter"))
                        for (i in 0 until cities.size) {
                            val data = cities[i]
                            items.add(ModalSearchModel(data.id_city, "${data.nama_city} - ${data.kode_city}"))
                        }

//                        setupFilterContacts(items)

                        setupFilterTokoModal()
                        searchCoordinate()
                    }
                    RESPONSE_STATUS_EMPTY -> {

//                        handleMessage(this@MapsActivity, "LIST CITY", "Daftar kota kosong!")
                        progressDialog.dismiss()

                    }
                    else -> {

//                        handleMessage(this@MapsActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        progressDialog.dismiss()

                    }
                }


            } catch (e: Exception) {

//                handleMessage(this@MapsActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                progressDialog.dismiss()

            }

        }
    }

    override fun onLocationChanged(location: Location) {
        mLastLocation = location

        if (mCurrLocationMarker != null) mCurrLocationMarker!!.remove()

        val latLng = LatLng(location.latitude, location.longitude)
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("Posisi Terbaru")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

        mCurrLocationMarker = mMap.addMarker(markerOptions)

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.moveCamera(CameraUpdateFactory.zoomTo(zoomLevel))

        if (mGoogleApiClient != null) LocationServices.getFusedLocationProviderClient(this)

    }

    override fun onConnected(p0: Bundle?) {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 1000
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ){
            LocationServices.getFusedLocationProviderClient(this)
        }
    }

    override fun onConnectionSuspended(p0: Int) { mGoogleApiClient?.connect() }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        if (connectionResult.hasResolution()) {

            try { connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST) }
            catch (e: IntentSender.SendIntentException) { e.printStackTrace() }

        } else Toast.makeText(this, "Koneksi ke Layanan Google Play gagal", TOAST_SHORT).show()
    }

    override fun onBackPressed() {
        backHandler()
    }

    private fun getListGudang() {

        binding.centerPointLoading.visibility = View.VISIBLE
        binding.centerPointTitle.visibility = View.GONE
        binding.centerPointMoreIcon.visibility = View.GONE

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = when (userKind) {
                    USER_KIND_ADMIN -> apiService.getListGudang(distributorID = userDistributorId)
//                    else -> apiService.getListGudang(distributorID = userDistributorId)
                    else -> apiService.getListGudang(cityId = userCity, distributorID = userDistributorId)
                }

                when (response.status) {
                    RESPONSE_STATUS_OK, RESPONSE_STATUS_SUCCESS -> {

                        listGudang = response.results
                        listGudang.add(0, GudangModel("-1", "Lokasi Saya", "${currentLatLng!!.latitude},${currentLatLng!!.longitude}"))
                        selectedCenterPoint = ModalSearchModel(listGudang[0].id_warehouse, listGudang[0].nama_warehouse, listGudang[0].location_warehouse)
                        binding.centerPointTitle.text = selectedCenterPoint?.title

                        binding.centerPointLoading.visibility = View.GONE
                        binding.centerPointTitle.visibility = View.VISIBLE
                        binding.centerPointMoreIcon.visibility = View.VISIBLE

                        val items: ArrayList<ModalSearchModel> = ArrayList()
                        for (i in 0 until listGudang.size) {
                            val data = listGudang[i]
                            val title = data.nama_warehouse + if (data.kode_city.isNotEmpty()) " - " + data.kode_city else ""
                            items.add(ModalSearchModel(data.id_warehouse, title, data.location_warehouse))
                        }
                        setupDialogSearch(items)

                    } RESPONSE_STATUS_EMPTY -> {

                        binding.centerPointContainer.visibility = View.GONE

                    } else -> {

                        handleMessage(this@MapsActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        binding.centerPointTitle.visibility = View.GONE
                        binding.centerPointMoreIcon.visibility = View.GONE
                        binding.centerPointLoading.visibility = View.VISIBLE
                        binding.centerPointLoading.text = getString(R.string.failed_get_data)

                    }
                }

            } catch (e: Exception) {

                handleMessage(this@MapsActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                binding.centerPointTitle.visibility = View.GONE
                binding.centerPointMoreIcon.visibility = View.GONE
                binding.centerPointLoading.visibility = View.VISIBLE
                binding.centerPointLoading.text = getString(R.string.failed_get_data)

            }

        }
    }

    private fun setupDialogSearch(items: ArrayList<ModalSearchModel> = ArrayList()) {

        searchModal = SearchModal(this, items)
        searchModal.setCustomDialogListener(object: SearchModal.SearchModalListener {
            override fun onDataReceived(data: ModalSearchModel) {
                selectedCenterPoint = data
                binding.centerPointTitle.text = selectedCenterPoint?.title
            }

        })

        searchModal.label = "Pilih Opsi Gudang"
        searchModal.searchHint = "Ketik untuk mencari…"
    }

    private fun backHandler() {
        if (!isGetCoordinate && !isTracking) {
            if (routeDirections != null) toggleBtnDrawRoute()
            else if (isCardNavigationShowing) {
                selectedTargetRoute = null
                toggleDrawRoute()
            } else super.onBackPressed()
        } else super.onBackPressed()
    }

    private fun startTracking() {

        val userLevel = when (sessionManager.userKind()) {
            USER_KIND_ADMIN -> AUTH_LEVEL_ADMIN
            USER_KIND_COURIER -> AUTH_LEVEL_COURIER
            USER_KIND_BA -> AUTH_LEVEL_BA
            else -> AUTH_LEVEL_SALES
        }

        val deliveryId = "$userLevel$userID"
        database = FirebaseDatabase.getInstance().getReference(FIREBASE_REFERENCE)
        childDelivery = database?.child(FIREBASE_CHILD_DELIVERY)
        childDriver = childDelivery?.child(deliveryId)

        val courierModel = DeliveryModel.Courier(
            id = userID,
            name = fulllName
        )

        val storeModel = DeliveryModel.Store(
            id = iContactID!!,
            name = iMapsName!!,
            lat = selectedLocation!!.latitude,
            lng = selectedLocation!!.longitude,
        )

        val deliveryModel = DeliveryModel.Delivery(
            id = deliveryId,
            start_datetime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) DateFormat.now() else "",
            start_lat = currentLatLng!!.latitude,
            start_lng = currentLatLng!!.longitude,
            store = storeModel,
            courier = courierModel
        )

        childDriver?.setValue(deliveryModel)

        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(mapsDuration.toLong())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                val driverLocation = locationResult.lastLocation!!
                childDriver!!.child("lat").setValue(driverLocation.latitude)
                childDriver!!.child("lng").setValue(driverLocation.longitude)

            }
        }

        // Listener From Firebase
        locationListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val driver = dataSnapshot.getValue(DeliveryModel.Delivery::class.java)
                if (driver != null) {
                    val iconDrawable = R.drawable.store_location_status_biding
                    val originalBitmap = BitmapFactory.decodeResource(resources, iconDrawable)

                    val newWidth = convertDpToPx(40, this@MapsActivity)
                    val newHeight = convertDpToPx(40, this@MapsActivity)

                    val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false)

//                    val courierMarker = MarkerOptions()
//                        .position(LatLng(driver.lat, driver.lng))
//                        .title("Kurir)
//                        .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
//                    mMap.addMarker(courierMarker)

                    val latLng = LatLng(driver.lat, driver.lng)

                    if (courierMarker == null) {
                        if (ActivityCompat.checkSelfPermission(
                                this@MapsActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                this@MapsActivity,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }
                        mMap.isMyLocationEnabled = false
                        courierMarker = mMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title("Kurir")
                                .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
                        )
                    } else courierMarker?.position = latLng

//                    val bounds = LatLngBounds.builder()
//                        .include(latLng)
//                        .include(selectedLocation ?: return)
//                        .build()
//                    val updateCamera = CameraUpdateFactory.newLatLngBounds(bounds, 100)
//                    mMap.animateCamera(updateCamera, mapsDuration, null)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                handleMessage(this@MapsActivity, "MAPS LISTENER", "Failed listening location update: $databaseError")
            }
        }
        childDriver?.addValueEventListener(locationListener!!)

        // Request location updates
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())
    }

    private fun stopTracking() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (locationCallback != null) {
            courierMarker?.remove()
            childDriver?.removeEventListener(locationListener!!)
            fusedLocationClient.removeLocationUpdates(locationCallback!!)
            mMap.isMyLocationEnabled = true

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    childDriver!!.child("end_lat").setValue(location?.latitude)
                    childDriver!!.child("end_lng").setValue(location?.longitude)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        childDriver!!.child("end_datetime").setValue(DateFormat.now())
                    }
                }
        }
    }

    private fun setupTracking() {

        database = FirebaseDatabase.getInstance().getReference(FIREBASE_REFERENCE)
        childDelivery = database?.child(FIREBASE_CHILD_DELIVERY)
        childDriver = childDelivery?.child(deliveryID.toString())

        val courierDrawable = R.drawable.store_location_status_biding
        val storeDrawable = R.drawable.store_location_status_blacklist
        var delivery: DeliveryModel.Delivery? = null

        childDriver?.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (delivery == null) {

                    delivery = snapshot.getValue(DeliveryModel.Delivery::class.java)

                    val startLatLng = LatLng(delivery!!.start_lat, delivery!!.start_lng)
                    val courierLatLng = LatLng(delivery!!.lat, delivery!!.lng)
                    val destinationLatLng = LatLng(delivery!!.store!!.lat, delivery!!.store!!.lng)

                    courierMarker = mMap.addMarker(
                        MarkerOptions()
                            .position(courierLatLng)
                            .title(delivery!!.courier?.name ?: "Kurir")
                            .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap(courierDrawable)))
                    )
                    mMap.addMarker(
                        MarkerOptions()
                            .position(destinationLatLng)
                            .title(delivery!!.store?.name ?: "Toko")
                            .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap(storeDrawable)))
                    )

                    val directions = DirectionsApi.newRequest(getGeoContext())
                        .mode(TravelMode.DRIVING) // Ganti dengan mode perjalanan yang sesuai
                        .origin(com.google.maps.model.LatLng(startLatLng.latitude, startLatLng.longitude))
                        .destination(com.google.maps.model.LatLng(destinationLatLng.latitude, destinationLatLng.longitude))
                        .optimizeWaypoints(true)
                        .alternatives(true)

                    try {
                        val result = directions.await()

                        if (result.routes.isNotEmpty()) {
                            val listPolylineOptions = arrayListOf<PolylineOptions>()

                            // Gambar rute pada peta
                            for (i in 0..result.routes.size) {
                                if (i < result.routes.size && i < 3) {
                                    val route = result.routes[i]
                                    val overviewPolyline = route.overviewPolyline.decodePath()
                                    val polylineOptions = PolylineOptions()
                                    polylineOptions.width(15f)
                                    polylineOptions.color(getColor(lineColor[i]))
                                    for (latLng in overviewPolyline) {
                                        polylineOptions.add(LatLng(latLng.lat, latLng.lng))
                                    }
                                    listPolylineOptions.add(polylineOptions)
                                }
                            }

                            for (i in listPolylineOptions.size - 1 downTo 0) {
                                routeDirections = mMap.addPolyline(listPolylineOptions[i])
                                listRouteLines.add(routeDirections!!)
                            }

                            val bounds = LatLngBounds.builder()
                                .include(startLatLng)
                                .include(destinationLatLng)
                                .build()
                            val updateCamera = CameraUpdateFactory.newLatLngBounds(bounds, 100)
                            mMap.animateCamera(updateCamera, mapsDuration, null)

                        } else {
                            Toast.makeText(this@MapsActivity, "Tidak ada rute ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: ApiException) {
                        Toast.makeText(this@MapsActivity, "Gagal memuat navigasi", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    } catch (e: InterruptedException) {
                        Toast.makeText(this@MapsActivity, "Gagal memuat navigasi", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    } catch (e: IOException) {
                        Toast.makeText(this@MapsActivity, "Gagal memuat navigasi", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }
                else {

                    delivery = snapshot.getValue(DeliveryModel.Delivery::class.java)

                    val courierLatLng = LatLng(delivery!!.lat, delivery!!.lng)
                    if (courierMarker == null) {
                        courierMarker = mMap.addMarker(
                            MarkerOptions()
                                .position(courierLatLng)
                                .title(delivery!!.courier?.name ?: "Kurir")
                                .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap(courierDrawable)))
                        )
                    } else courierMarker?.position = courierLatLng
                }
            }

            override fun onCancelled(error: DatabaseError) {
                handleMessage(this@MapsActivity, TAG_RESPONSE_CONTACT,
                    "Failed run service. Exception $error"
                )
            }

        })

        return
    }

    private fun resizedBitmap(drawable: Int): Bitmap {

        val originalBitmap = BitmapFactory.decodeResource(resources, drawable)

        val newWidth = convertDpToPx(40, this@MapsActivity)
        val newHeight = convertDpToPx(40, this@MapsActivity)

        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false)
    }
}