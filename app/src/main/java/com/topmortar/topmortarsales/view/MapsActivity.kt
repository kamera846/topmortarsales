package com.topmortar.topmortarsales.view

import android.Manifest
import android.annotation.SuppressLint
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
import android.os.AsyncTask
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
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
import com.google.firebase.database.ValueEventListener
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.errors.ApiException
import com.google.maps.model.TravelMode
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.PlaceAdapter
import com.topmortar.topmortarsales.adapter.recyclerview.UserTrackingRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.CONNECTION_FAILURE_RESOLUTION_REQUEST
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_COURIER_ID
import com.topmortar.topmortarsales.commons.CONST_DELIVERY_ID
import com.topmortar.topmortarsales.commons.CONST_IS_BASE_CAMP
import com.topmortar.topmortarsales.commons.CONST_IS_TRACKING
import com.topmortar.topmortarsales.commons.CONST_IS_TRACKING_COURIER
import com.topmortar.topmortarsales.commons.CONST_IS_TRACKING_HISTORY
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_CITY_ID
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_NAME
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_STATUS
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_MAPS_NAME
import com.topmortar.topmortarsales.commons.CONST_MAPS_STATUS
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_NEAREST_STORE
import com.topmortar.topmortarsales.commons.CONST_NEAREST_STORE_HIDE_FILTER
import com.topmortar.topmortarsales.commons.CONST_NEAREST_STORE_WITH_DEFAULT_RANGE
import com.topmortar.topmortarsales.commons.DETAIL_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_ABSENT
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_DELIVERY
import com.topmortar.topmortarsales.commons.GET_COORDINATE
import com.topmortar.topmortarsales.commons.IS_CLOSING
import com.topmortar.topmortarsales.commons.LOCATION_PERMISSION_REQUEST_CODE
import com.topmortar.topmortarsales.commons.MANAGE_USER_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_SUCCESS
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_ACTIVE
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_BID
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_DATA
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_PASSIVE
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TOAST_LONG
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN_CITY
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.USER_KIND_PENAGIHAN
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.utils.CustomProgressBar
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.EventBusUtils
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.URLUtility
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
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
import com.topmortar.topmortarsales.model.UserAbsentModel
import com.topmortar.topmortarsales.view.suratJalan.ListSuratJalanActivity
import com.topmortar.topmortarsales.view.user.AllUserTrackingActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

@OptIn(DelicateCoroutinesApi::class)
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private lateinit var sessionManager: SessionManager
    private val userKind get() = sessionManager.userKind().toString()
    private val userID get() = sessionManager.userID().toString()
    private val userDistributorId get() = sessionManager.userDistributor().toString()
    private val userDistributorIds get() = sessionManager.userDistributor()
    private val userCity get() = sessionManager.userCityID().toString()

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private var routeDirections: Polyline? = null
    private var currentFoundItemTotal: Int = 0
    private var centerPointLatLng: LatLng? = null
    private var limitKm: Double = 0.0

    private var iMaps: String? = null
    private var iMapsName: String? = null
    private var iMapsStatus: String? = null
    private var iContactID: String? = null
    private var isGetCoordinate = false
    private var isNearestStore = false
    private var isNearestStoreHideFilter = false
    private var isNearestStoreDefaultRange = 1
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
    private lateinit var progressBar: CustomProgressBar

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
    private var firebaseReference: DatabaseReference? = null
    private var childDelivery: DatabaseReference? = null
    private var childDriver: DatabaseReference? = null
    private var childAbsent: DatabaseReference? = null
    private var childCourier: DatabaseReference? = null
    private var locationCallback: LocationCallback? = null
    private var locationListener: ValueEventListener? = null
    private var courierTrackingListener: ValueEventListener? = null
    private var courierMarker: Marker? = null
    private var isTracking = false
    private var isTrackingCourier = false
    private var isTrackingHistory = false
    private var deliveryID: String? = null
    private var courierID: String? = null
    private var totalProcess = 0
    private var processed = 0
    private var percentage = 0
    private var selectedLatLng: LatLng? = null

    private val courierDrawable = R.drawable.pin_truck_pink_cyclamen_11zon
    private val locationBlacklistDrawable = R.drawable.store_location_status_blacklist_copy_11zon

    private var listGudang: ArrayList<GudangModel> = arrayListOf()
    private var selectedCenterPoint: ModalSearchModel? = null
    private lateinit var searchModal: SearchModal

    private val bitmapCache = mutableMapOf<Int, Bitmap>()

    private val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val resultData = it.data?.getStringExtra("$DETAIL_ACTIVITY_REQUEST_CODE")
            val isClosingAction = it.data?.getBooleanExtra(IS_CLOSING, false) ?: false

            if (resultData == SYNC_NOW) {
                val resultIntent = Intent()
                resultIntent.putExtra("$DETAIL_ACTIVITY_REQUEST_CODE", SYNC_NOW)
                resultIntent.putExtra(IS_CLOSING, isClosingAction)
                setResult(RESULT_OK, resultIntent)

                finish()
            }
        }
    }

    private val trackingLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val resultData = it.data?.getStringExtra(SYNC_NOW)
            if (resultData == SYNC_NOW) {
                setupTrackingCourier()
            }
        }
    }

    companion object {
        const val ALL_USER_TRACKING_REQUEST = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge()

        sessionManager = SessionManager(this)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isBasecamp = intent.getBooleanExtra(CONST_IS_BASE_CAMP, false)

        progressBar = CustomProgressBar(this)
        progressBar.setMessage("Mencari ${if (isBasecamp) "basecamp" else "toko"} terdekat…")
        progressBar.setCancelable(false)

        if (userKind == USER_KIND_COURIER || userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN){
            CustomUtility(this).setUserStatusOnline(true, userDistributorIds ?: "-custom-002", userID)
        }
        if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_ADMIN_CITY) EventBus.getDefault().register(this)

        checkLocationPermission()

    }

    private fun checkLocationPermission() {
        val urlUtility = URLUtility(this)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (urlUtility.isLocationEnabled(this)) {

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

        if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_ADMIN_CITY || userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN) {
            if (isNearestStore && !isNearestStoreHideFilter && !isBasecamp) binding.llFilter.visibility = View.VISIBLE
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
            mMap.setOnMapLongClickListener { latLng -> setPin(latLng, "Lokasi terpilih", moveCamera = false) }
            if (!sessionManager.pinMapHint()) {
                showDialog(message = "Tekan dan tahan pada peta untuk menandai lokasi")
                sessionManager.pinMapHint(true)
            }
        } else if (binding.btnGetDirection.isVisible) {
            mMap.setPadding(0,0,0, convertDpToPx(80, this))

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) currentLatLng = LatLng(location.latitude,location.longitude)
                }

            binding.btnGetDirection.setOnClickListener {
                toggleDirection()
            }
        } else if (binding.llFilter.isVisible) {
            mMap.setPadding(0,convertDpToPx(36, this),0,0)
        } else mMap.setPadding(0,0,0,convertDpToPx(16, this))

        mMap.setOnMapClickListener {
            if (binding.recyclerView.isVisible) {
                binding.recyclerView.visibility = View.GONE
                binding.rvLoading.visibility = View.GONE
            }
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
                        STATUS_CONTACT_DATA -> R.drawable.store_location_status_data_11zon
                        STATUS_CONTACT_ACTIVE -> R.drawable.store_location_status_active_11zon
                        STATUS_CONTACT_PASSIVE -> R.drawable.store_location_status_passive_11zon
                        STATUS_CONTACT_BID -> R.drawable.store_location_status_biding_11zon
                        else -> {
                            if (selectedCenterPoint != null && selectedCenterPoint?.etc == itemToFind) {
                                binding.textTargetRute.text = "Petunjuk rute menuju ke lokasi gudang"
                                R.drawable.gudang_11zon
                            } else locationBlacklistDrawable
                        }
                    }
                    binding.imgTargetRoute.setImageDrawable(AppCompatResources.getDrawable(this, imgDrawable))

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

        } else {

            // Remove line routes
            for (polyline in listRouteLines) {
                polyline.remove()
            }
            listRouteLines.clear()

            Handler(Looper.getMainLooper()).postDelayed({
                val button = binding.btnDrawRoute
                val img = binding.btnDrawRouteImg
                val title = binding.btnDrawRouteTitle
                button.setBackgroundResource(R.drawable.bg_primary_round)
                img.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.direction_white))
                title.text = "Aktifkan Navigasi"

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

        } else {
            mMap.clear()
            setupMaps()
            routeDirections!!.remove()
            routeDirections = null
            Handler(Looper.getMainLooper()).postDelayed({
                val button = binding.btnGetDirection
                val img = binding.btnGetDirectionImg
                val title = binding.btnGetDirectionTitle
                button.setBackgroundResource(R.drawable.bg_primary_round)
                img.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.direction_white))
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
        courierID = intent.getStringExtra(CONST_COURIER_ID)
        isGetCoordinate = intent.getBooleanExtra(GET_COORDINATE, false)
        isNearestStore = intent.getBooleanExtra(CONST_NEAREST_STORE, false)
        isNearestStoreHideFilter = intent.getBooleanExtra(CONST_NEAREST_STORE_HIDE_FILTER, false)
        isNearestStoreDefaultRange = intent.getIntExtra(CONST_NEAREST_STORE_WITH_DEFAULT_RANGE, 1)
        isTracking = intent.getBooleanExtra(CONST_IS_TRACKING, false)
        isTrackingHistory = intent.getBooleanExtra(CONST_IS_TRACKING_HISTORY, false)
        isTrackingCourier = intent.getBooleanExtra(CONST_IS_TRACKING_COURIER, false)
        listCoordinate = intent.getStringArrayListExtra(CONST_LIST_COORDINATE)
        listCoordinateName = intent.getStringArrayListExtra(CONST_LIST_COORDINATE_NAME)
        listCoordinateStatus = intent.getStringArrayListExtra(CONST_LIST_COORDINATE_STATUS)
        listCoordinateCityID = intent.getStringArrayListExtra(CONST_LIST_COORDINATE_CITY_ID)

        if (isGetCoordinate) {
            binding.btnGetLatLng.visibility = View.VISIBLE
            binding.searchBar.visibility = View.VISIBLE
        } else {
            if (!isNearestStore && !isTracking && !isTrackingCourier && !isTrackingHistory) {

                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) currentLatLng = LatLng(location.latitude,location.longitude)
                    }
                binding.cardGetDirection.visibility = View.VISIBLE
                if (binding.llFilter.isVisible) mMap.setPadding(0, convertDpToPx(32, this@MapsActivity),0, convertDpToPx(16, this@MapsActivity))
                else mMap.setPadding(0,0,0, convertDpToPx(16, this@MapsActivity))
                binding.textTitleTarget.text = iMapsName
                binding.textTargetRute.text = "Petunjuk rute menuju ke lokasi ${if (isBasecamp) "basecamp" else "toko"}"

                val imgDrawable = locationBlacklistDrawable
                binding.imgTargetRoute.setImageDrawable(AppCompatResources.getDrawable(this, imgDrawable))
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
                        etSearch.setText("")
                        binding.recyclerView.visibility = View.GONE
                        binding.rvLoading.visibility = View.GONE
                        return initMaps(latLng, iMapsName)
                    } else showDialog(message = "Gagal menavigasi koordinat")
                } else showDialog(message = "Gagal memproses koordinat")

            }

            initMaps()

        } else initMaps()

    }

    @SuppressLint("SuspiciousIndentation")
    private fun searchCoordinate() {
        if (!progressBar.isShowing()) {
            progressBar.show()
        }

        Handler(Looper.getMainLooper()).postDelayed({

            limitKm = binding.etKm.text.toString().toDouble()
            currentFoundItemTotal = 0

            mMap.clear()

            centerPointLatLng = currentLatLng
            if (selectedCenterPoint != null && selectedCenterPoint?.id != "-1") {
                val coordinates = selectedCenterPoint?.etc!!.trim().split(",")
                centerPointLatLng = if (coordinates.size == 2) {
                    val latitude = coordinates[0].toDoubleOrNull()
                    val longitude = coordinates[1].toDoubleOrNull()
                    LatLng(latitude!!, longitude!!)
                } else currentLatLng

                val newWidth = convertDpToPx(50, this@MapsActivity)
                val newHeight = convertDpToPx(50, this@MapsActivity)

                val markerOptions = MarkerOptions()
                    .position(centerPointLatLng!!)
                    .title(selectedCenterPoint?.title!!)
                    .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap(R.drawable.gudang_11zon, newWidth, newHeight)))

                mMap.addMarker(markerOptions)
            }

            if (isNearestStoreDefaultRange == -1) {

                progressBar.setMessage("Mencari ${listCoordinate?.size} ${ if (isBasecamp) "basecamp" else "toko"}…")
                binding.radiusContainer.visibility = View.GONE

                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(centerPointLatLng!!, 11f)
                mMap.animateCamera(cameraUpdate, 2000, null)
            }

            // Execute looping in background
            totalProcess = listCoordinate!!.size
            processed = 0
            percentage = 0
            SearchCoordinateLoopingTask().execute()
        }, 100)
    }

    private inner class SearchCoordinateLoopingTask : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            val urlUtility = URLUtility(this@MapsActivity)
            var firstItemSelected = false

            for ((i, item) in listCoordinate!!.iterator().withIndex()) {

                processed ++
                percentage = (processed * 100) / totalProcess

                if (isNearestStoreDefaultRange == -1) {
                    runOnUiThread {
                        progressBar.setMessage("Mencari ${listCoordinate?.size} ${ if (isBasecamp) "basecamp" else "toko"}…  ($percentage%)")
                    }
                } else {
                    runOnUiThread {
                        progressBar.setMessage("Mencari ${if (isBasecamp) "basecamp" else "toko"} terdekat… ($percentage%)")
                    }
                }

                if (!urlUtility.isUrl(item)) {

                    val coordinates = item.trim().split(",")
                    if (coordinates.size == 2) {
                        val latitude = coordinates[0].toDoubleOrNull()
                        val longitude = coordinates[1].toDoubleOrNull()

                        if (latitude != null && longitude != null) {

                            val distance = urlUtility.calculateDistance(centerPointLatLng!!.latitude, centerPointLatLng!!.longitude, latitude, longitude)

                            if (distance < limitKm || isNearestStoreDefaultRange == -1) {

                                val latLng = LatLng(latitude, longitude)

                                runOnUiThread {
                                    binding.recyclerView.visibility = View.GONE
                                    binding.rvLoading.visibility = View.GONE
                                }

                                val iconDrawable = when (listCoordinateStatus?.get(i)) {
                                    STATUS_CONTACT_DATA -> R.drawable.store_location_status_data_11zon
                                    STATUS_CONTACT_ACTIVE -> R.drawable.store_location_status_active_11zon
                                    STATUS_CONTACT_PASSIVE -> R.drawable.store_location_status_passive_11zon
                                    STATUS_CONTACT_BID -> R.drawable.store_location_status_biding_11zon
                                    else -> locationBlacklistDrawable
                                }

                                val newWidth = convertDpToPx(40, this@MapsActivity)
                                val newHeight = convertDpToPx(40, this@MapsActivity)

                                selectedLocation = latLng
                                val markerOptions = MarkerOptions()
                                    .position(latLng)
                                    .title(listCoordinateName?.get(i))
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap(iconDrawable, newWidth, newHeight)))

                                val onlyStatus = selectedStatusID != "-1" && selectedCitiesID == null && listCoordinateStatus!![i] == selectedStatusID.lowercase(Locale.getDefault())
                                val onlyCities = selectedCitiesID != null && selectedStatusID == "-1" && listCoordinateCityID!![i] == selectedCitiesID?.id_city
                                val statusAndCities = selectedStatusID != "-1" && listCoordinateStatus!![i] == selectedStatusID.lowercase(Locale.getDefault()) && selectedCitiesID != null && listCoordinateCityID!![i] == selectedCitiesID?.id_city

                                if (statusAndCities || onlyStatus || onlyCities || (selectedStatusID == "-1" && selectedCitiesID == null)) {
                                    runOnUiThread {
                                        mMap.addMarker(markerOptions)
                                    }
                                    currentFoundItemTotal ++
                                    if (isNearestStoreHideFilter && !firstItemSelected) {
                                        selectedLatLng = latLng
                                        firstItemSelected = true
                                    }
                                }

                            }
                        }
                    }
                }

            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            var textFilter = ""

            if (selectedStatusID != "-1" || selectedVisitedID != "-1" || selectedCitiesID != null) {
                textFilter += if (selectedCitiesID != null && selectedCitiesID?.id_city != "-1") selectedCitiesID?.nama_city else ""
                textFilter += if (selectedStatusID != "-1") if (textFilter.isNotEmpty()) ", $selectedStatusID" else selectedStatusID else ""
                textFilter += if (selectedVisitedID != "-1") if (textFilter.isNotEmpty()) ", $selectedVisitedID" else selectedVisitedID else ""
            } else textFilter = getString(R.string.tidak_ada_filter)

            binding.tvFilter.text = "$textFilter ($currentFoundItemTotal)"

            progressBar.dismiss()

            val durationMs = 2000
            var responsiveZoom = when {
                limitKm >= 1 -> when {
                    limitKm >= 18 -> 10
                    limitKm >= 13 -> 11
                    limitKm >= 8 -> 12
                    limitKm >= 3 -> 13
                    else -> 14
                }
                else -> 15
            }

            val isLimitKmDisabled = isNearestStoreDefaultRange == -1
            if (isLimitKmDisabled) responsiveZoom = 10

            if (isNearestStoreHideFilter && selectedLatLng != null) {
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(selectedLatLng!!, responsiveZoom.toFloat())
                mMap.animateCamera(cameraUpdate, durationMs, null)
            } else {
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(centerPointLatLng!!, responsiveZoom.toFloat())
                mMap.animateCamera(cameraUpdate, durationMs, null)
            }

            binding.textTotalNearest.text = "$currentFoundItemTotal / ${listCoordinate?.size} ${if (isBasecamp) "basecamp" else "toko"} ${ if (selectedStatusID != "-1") "$selectedStatusID " else "" }ditemukan ${ if (!isLimitKmDisabled) "dalam radius $limitKm km" else ""}"
            if (currentFoundItemTotal > 0) {
                binding.textTitleTotalNearest.text = "Penelusuran ${if (isBasecamp) "Basecamp" else "Toko"} Terdekat"
            } else {
                showDialog(message = "Tidak menemukan ${if (isBasecamp) "basecamp" else "toko"} ${ if (selectedStatusID != "-1") "$selectedStatusID " else "" }${ if (!isLimitKmDisabled) "di sekitar anda saat ini dalam radius jarak $limitKm km" else ""}")
                binding.textTitleTotalNearest.text = "Penelusuran ${if (isBasecamp) "Basecamp" else "Toko"} Terdekat"
            }

            binding.cardTelusuri.visibility = View.VISIBLE
            if (listGudang.isNotEmpty() && !isNearestStoreHideFilter) {
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
                        binding.rvLoading.visibility = View.GONE
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
        progressBar.setMessage("Mencarikan rute…")
        progressBar.show()

        GlobalScope.launch(Dispatchers.IO) {

            val currentLocation = currentLatLng ?: return@launch
            val destination = selectedLocation ?: return@launch

            val directions = DirectionsApi.newRequest(getGeoContext())
                .mode(TravelMode.DRIVING) // Menggunakan mode perjalanan mengemudi
                .origin(com.google.maps.model.LatLng(currentLocation.latitude, currentLocation.longitude))
                .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                .optimizeWaypoints(true)
                .alternatives(true)

            try {
                val result = directions.await()

                withContext(Dispatchers.Main) {
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

                        Handler(Looper.getMainLooper()).postDelayed({
                            val button = binding.btnDrawRoute
                            val img = binding.btnDrawRouteImg
                            val title = binding.btnDrawRouteTitle
                            button.setBackgroundResource(R.drawable.bg_passive_round)
                            img.setImageDrawable(AppCompatResources.getDrawable(this@MapsActivity, R.drawable.direction_line_white))
                            title.text = "Matikan Navigasi"

                            progressBar.dismiss()
                        }, 500)

                    } else {
                        progressBar.dismiss()
                        runOnUiThread {
                            Toast.makeText(this@MapsActivity, "Tidak ada rute ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            } catch (e: ApiException) {
                progressBar.dismiss()
                handleMessage(this@MapsActivity, "getDirections" , "Gagal memuat navigasi. Err: ${e.message}")
                e.printStackTrace()
            } catch (e: InterruptedException) {
                progressBar.dismiss()
                handleMessage(this@MapsActivity, "getDirections" , "Gagal memuat navigasi. Err: ${e.message}")
                e.printStackTrace()
            } catch (e: IOException) {
                progressBar.dismiss()
                handleMessage(this@MapsActivity, "getDirections" , "Gagal memuat navigasi. Err: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun getGeoContext(): GeoApiContext {

        return GeoApiContext.Builder()
            .apiKey(getString(R.string.maps_key)) // Key API Google Maps
            .build()

    }

    @SuppressLint("MissingPermission")
    private fun initMaps(targetLatLng: LatLng? = null, targetLatLngName: String? = null) {

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
            if (location != null) {
                currentLatLng = LatLng(location.latitude, location.longitude)

                if (targetLatLng != null) {
                    setPin(targetLatLng, targetLatLngName ?: "")
                } else {
                    if (isTracking) setupTracking()
                    else if (isTrackingCourier) setupTrackingCourier()
                    else if (isTrackingHistory) setupTrackingHistory()
                    else setPin(currentLatLng!!, "Lokasi Saya")

                    if (isNearestStore && binding.llFilter.isVisible) {
                        getCities()
                        getListGudang()
                    } else if (isNearestStore) {
                        searchCoordinate()
                        getListGudang()
                    }
                }
            } else showDialog(message = "Gagal menemukan lokasi Anda saat ini. Pastikan lokasi Anda aktif dan coba buka kembali peta")
        }

    }

    private fun setPin(latLng: LatLng, placeName: String, moveCamera: Boolean = true) {

        val newWidth = convertDpToPx(40, this)
        val newHeight = convertDpToPx(40, this)

        selectedLocation = latLng
        mMap.clear()
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(placeName)
                .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap(locationBlacklistDrawable, newWidth, newHeight)))
        )

        if (moveCamera) {

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel)
            mMap.animateCamera(cameraUpdate, mapsDuration, null)

        }
        binding.recyclerView.visibility = View.GONE
        binding.rvLoading.visibility = View.GONE

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

            if (location.isNotEmpty()) {
                if (!binding.recyclerView.isVisible) binding.rvLoading.visibility = View.VISIBLE

                GlobalScope.launch(Dispatchers.IO) {
                    val request = FindAutocompletePredictionsRequest.builder()
                        .setQuery(location)
                        .build()

                    val result = placesClient.findAutocompletePredictions(request)

                    withContext(Dispatchers.Main) {
                        result.addOnSuccessListener { response: FindAutocompletePredictionsResponse ->

                        val predictions = response.autocompletePredictions
                        val placeIds = predictions.map { prediction: AutocompletePrediction -> prediction.placeId }
                        val placeNames = predictions.map { prediction: AutocompletePrediction -> prediction.getPrimaryText(null).toString() }
                        val placeAddress = predictions.map { prediction: AutocompletePrediction -> prediction.getSecondaryText(null).toString() }

                        if (placeNames.isNotEmpty()) {
                            val placeAdapter = PlaceAdapter(placeNames, placeAddress) { position ->

                                val fields = listOf(Place.Field.LAT_LNG)
                                val requestFetch = FetchPlaceRequest.builder(placeIds[position], fields).build()

                                placesClient.fetchPlace(requestFetch)
                                    .addOnSuccessListener { response: FetchPlaceResponse ->

                                        val place = response.place
                                        val latLng = place.latLng

                                        if (latLng != null) setPin(latLng, placeNames[position])
                                        else {
                                            Toast.makeText(this@MapsActivity, "Gagal menampilkan koordinat", TOAST_SHORT).show()
                                            binding.recyclerView.visibility = View.GONE
                                            binding.rvLoading.visibility = View.GONE
                                        }

                                    }
                                    .addOnFailureListener { _: Exception ->
                                        Toast.makeText(this@MapsActivity, "Gagal menampilkan lokasi", TOAST_SHORT).show()
                                        binding.recyclerView.visibility = View.GONE
                                        binding.rvLoading.visibility = View.GONE
                                    }
                            }

                            binding.recyclerView.apply {
                                layoutManager = LinearLayoutManager(this@MapsActivity)
                                adapter = placeAdapter
                                if (isGetCoordinate) {
                                    this.visibility = View.VISIBLE
                                    binding.rvLoading.visibility = View.GONE
                                }
                            }
                        } else {
                            binding.recyclerView.visibility = View.GONE
                            binding.rvLoading.visibility = View.GONE
                        }

                    }.addOnFailureListener {
                            Toast.makeText(this@MapsActivity, "Gagal menemukan lokasi", TOAST_SHORT).show()
                            binding.recyclerView.visibility = View.GONE
                            binding.rvLoading.visibility = View.GONE
                        }
                    }
                }
            } else {
                binding.recyclerView.visibility = View.GONE
                binding.rvLoading.visibility = View.GONE
            }

        }
    }

    private fun setupFilterTokoModal() {

        if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_ADMIN_CITY || userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN) {
            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) binding.llFilter.background = AppCompatResources.getDrawable(this, R.color.black_400)
            else binding.llFilter.background = AppCompatResources.getDrawable(this, R.color.light)

            binding.llFilter.visibility = View.VISIBLE

            filterModal = FilterTokoModal(this)
            if (userKind == USER_KIND_ADMIN) {
                filterModal.setStatuses(selected = selectedStatusID)
                filterModal.setCities(items = cities, selected = selectedCitiesID)
            } else if (userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN || userKind == USER_KIND_ADMIN_CITY) filterModal.setStatuses(selected = selectedStatusID)
            filterModal.setSendFilterListener(object: FilterTokoModal.SendFilterListener {
                override fun onSendFilter(
                    selectedStatusID: String,
                    selectedVisitedID: String,
                    selectedCitiesID: CityModel?
                ) {

                    this@MapsActivity.selectedStatusID = selectedStatusID
                    this@MapsActivity.selectedVisitedID = selectedVisitedID
                    this@MapsActivity.selectedCitiesID = selectedCitiesID

                    searchCoordinate()

                }

            })
        }
    }

    private fun getCities() {

        progressBar.show()

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

                        setupFilterTokoModal()
                        searchCoordinate()
                    }
                    RESPONSE_STATUS_EMPTY -> {

                        progressBar.dismiss()

                    }
                    else -> {

                        progressBar.dismiss()

                    }
                }


            } catch (e: Exception) {

                if (e is CancellationException) {
                    return@launch
                }
                FirebaseUtils.logErr(this@MapsActivity, "Failed MapsActivity on getCities(). Catch: ${e.message}")
                progressBar.dismiss()

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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!isGetCoordinate && !isTracking && !isTrackingCourier && !isTrackingHistory) {
            if (routeDirections != null) toggleBtnDrawRoute()
            else if (isCardNavigationShowing) {
                selectedTargetRoute = null
                toggleDrawRoute()
            } else super.onBackPressed()
        } else {
            if (isTrackingCourier) {
                val resultIntent = Intent()
                resultIntent.putExtra("$MANAGE_USER_ACTIVITY_REQUEST_CODE", SYNC_NOW)
                setResult(RESULT_OK, resultIntent)
                finish()
            } else super.onBackPressed()
        }
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

                if (e is CancellationException) {
                    return@launch
                }
                FirebaseUtils.logErr(this@MapsActivity, "Failed MapsActivity on getListGudang(). Catch: ${e.message}")
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
        if (!isGetCoordinate && !isTracking && !isTrackingCourier && !isTrackingHistory) {
            if (routeDirections != null) toggleBtnDrawRoute()
            else if (isCardNavigationShowing) {
                selectedTargetRoute = null
                toggleDrawRoute()
            } else finish()
        } else {
            if (isTrackingCourier) {
                val resultIntent = Intent()
                resultIntent.putExtra("$MANAGE_USER_ACTIVITY_REQUEST_CODE", SYNC_NOW)
                setResult(RESULT_OK, resultIntent)
                finish()
            } else finish()
        }
    }

    private fun setupTracking() {
        progressBar.setMessage("Mencarikan rute…")
        progressBar.show()

        val userDistributorIds = sessionManager.userDistributor()
        firebaseReference = FirebaseUtils.getReference(distributorId = userDistributorIds ?: "-firebase-003")
        childDelivery = firebaseReference?.child(FIREBASE_CHILD_DELIVERY)
        childDriver = childDelivery?.child(deliveryID!!)
        val childStores = childDriver?.child("stores/$iContactID")
        var deliveryData: DeliveryModel.Delivery? = null

        childDriver?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    if (deliveryData == null) {
                        deliveryData = snapshot.getValue(DeliveryModel.Delivery::class.java)
                        val courierLatLng = LatLng(deliveryData!!.lat, deliveryData!!.lng)

                        childStores?.addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                                if (snapshot.exists()) {
                                    val store = snapshot.getValue(DeliveryModel.Store::class.java)!!

                                    val startLatLng =
                                        LatLng(store.startLat, store.startLng)
                                    val destinationLatLng =
                                        LatLng(store.lat, store.lng)

                                    courierMarker = mMap.addMarker(
                                        MarkerOptions()
                                            .position(courierLatLng)
                                            .title(deliveryData!!.courier?.name ?: "Kurir")
                                            .icon(
                                                BitmapDescriptorFactory.fromBitmap(
                                                    resizedBitmap(courierDrawable)
                                                )
                                            )
                                    )
                                    mMap.addMarker(
                                        MarkerOptions()
                                            .position(destinationLatLng)
                                            .title(store.name)
                                            .icon(
                                                BitmapDescriptorFactory.fromBitmap(
                                                    resizedBitmap(locationBlacklistDrawable)
                                                )
                                            )
                                    )

                                    val directions =
                                        DirectionsApi.newRequest(getGeoContext())
                                            .mode(TravelMode.DRIVING)
                                            .origin(
                                                com.google.maps.model.LatLng(
                                                    startLatLng.latitude,
                                                    startLatLng.longitude
                                                )
                                            )
                                            .destination(
                                                com.google.maps.model.LatLng(
                                                    destinationLatLng.latitude,
                                                    destinationLatLng.longitude
                                                )
                                            )
                                            .optimizeWaypoints(true)
                                            .alternatives(true)

                                    try {
                                        val result = directions.await()

                                        if (result.routes.isNotEmpty()) {
                                            val listPolylineOptions =
                                                arrayListOf<PolylineOptions>()

                                            // Gambar rute pada peta
                                            for (i in 0..result.routes.size) {
                                                if (i < result.routes.size && i < 3) {
                                                    val route = result.routes[i]
                                                    val overviewPolyline =
                                                        route.overviewPolyline.decodePath()
                                                    val polylineOptions =
                                                        PolylineOptions()
                                                    polylineOptions.width(15f)
                                                    polylineOptions.color(
                                                        getColor(
                                                            lineColor[i]
                                                        )
                                                    )
                                                    for (latLng in overviewPolyline) {
                                                        polylineOptions.add(
                                                            LatLng(
                                                                latLng.lat,
                                                                latLng.lng
                                                            )
                                                        )
                                                    }
                                                    listPolylineOptions.add(
                                                        polylineOptions
                                                    )
                                                }
                                            }

                                            for (i in listPolylineOptions.size - 1 downTo 0) {
                                                routeDirections =
                                                    mMap.addPolyline(listPolylineOptions[i])
                                                listRouteLines.add(routeDirections!!)
                                            }

                                            val bounds = LatLngBounds.builder()
                                                .include(startLatLng)
                                                .include(destinationLatLng)
                                                .build()
                                            val updateCamera =
                                                CameraUpdateFactory.newLatLngBounds(
                                                    bounds,
                                                    100
                                                )
                                            mMap.animateCamera(
                                                updateCamera,
                                                mapsDuration,
                                                null
                                            )

                                            Handler(Looper.getMainLooper()).postDelayed({
                                                binding.cardDelivery.visibility = View.VISIBLE
                                                binding.deliveryCourier.text = deliveryData!!.courier?.name
                                                binding.deliveryStore.text = store.name
                                                binding.deliveryDate.text = "Diproses pada " + formatDateYear(store.startDatetime)
                                                binding.btnSuratJalan.setOnClickListener {
                                                    val intent = Intent(this@MapsActivity, ListSuratJalanActivity::class.java)
                                                    intent.putExtra(CONST_CONTACT_ID, store.id)
                                                    intent.putExtra(CONST_NAME, "")
                                                    activityLauncher.launch(intent)
                                                }
                                                binding.courierContainer.setOnClickListener { changeFocusCamera(courierLatLng) }
                                                binding.storeContainer.setOnClickListener { changeFocusCamera(destinationLatLng) }
                                                progressBar.dismiss()

                                                if (ActivityCompat.checkSelfPermission(
                                                        this@MapsActivity,
                                                        Manifest.permission.ACCESS_FINE_LOCATION
                                                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                                        this@MapsActivity,
                                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                                    ) != PackageManager.PERMISSION_GRANTED
                                                ) return@postDelayed
                                                mMap.isMyLocationEnabled = false
                                            }, 500)

                                        } else {
                                            progressBar.dismiss()
                                            handleMessage(this@MapsActivity, "setupTracking" , "Tidak dapat menemukan rute.")
                                        }
                                    } catch (e: ApiException) {
                                        progressBar.dismiss()
                                        handleMessage(this@MapsActivity, "setupTracking" , "Gagal memuat navigasi. Err: ${e.message}")
                                        e.printStackTrace()
                                    } catch (e: InterruptedException) {
                                        progressBar.dismiss()
                                        handleMessage(this@MapsActivity, "setupTracking" , "Gagal memuat navigasi. Err: ${e.message}")
                                        e.printStackTrace()
                                    } catch (e: IOException) {
                                        progressBar.dismiss()
                                        handleMessage(this@MapsActivity, "setupTracking" , "Gagal memuat navigasi. Err: ${e.message}")
                                        e.printStackTrace()
                                    }
                                } else {
                                    progressBar.dismiss()

                                    handleMessage(
                                        this@MapsActivity, "setupTracking",
                                        "Failed run service. Exception child store not found"
                                    )
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                progressBar.dismiss()

                                handleMessage(
                                    this@MapsActivity, "setupTracking",
                                    "Failed run service. Exception $error"
                                )
                            }

                        })
                    } else {

                        deliveryData = snapshot.getValue(DeliveryModel.Delivery::class.java)
                        val courierLatLng = LatLng(deliveryData!!.lat, deliveryData!!.lng)

                        if (courierMarker == null) {
                            courierMarker = mMap.addMarker(
                                MarkerOptions()
                                    .position(courierLatLng)
                                    .title(
                                        deliveryData!!.courier?.name ?: "Kurir"
                                    )
                                    .icon(
                                        BitmapDescriptorFactory.fromBitmap(
                                            resizedBitmap(courierDrawable)
                                        )
                                    )
                            )
                            progressBar.dismiss()
                        } else {
                            progressBar.dismiss()
                            courierMarker?.position = courierLatLng
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.dismiss()

                handleMessage(this@MapsActivity, "setupTracking",
                    "Failed run service. Exception $error"
                )
            }

        })

    }

    private fun setupTrackingCourier() {
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
        mMap.isMyLocationEnabled = false

        progressBar.setMessage("Mendeteksi lokasi pengguna…")
        progressBar.show()

        val userDistributorIds = sessionManager.userDistributor()
        firebaseReference = FirebaseUtils.getReference(distributorId = userDistributorIds ?: "-firebase-004")
        childAbsent = firebaseReference?.child(FIREBASE_CHILD_ABSENT)
        childCourier = childAbsent?.child(courierID.toString())

        courierTrackingListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val courierLat = snapshot.child("lat").getValue(Double::class.java)
                    val courierLng = snapshot.child("lng").getValue(Double::class.java)
                    val courierFullName = snapshot.child("fullname").getValue(String::class.java)
                    val courierIsOnline = snapshot.child("isOnline").getValue(Boolean::class.java)
                    val courierLastTracking = snapshot.child("lastTracking").getValue(String::class.java)

                    if (courierLat != null && courierLng != null) {

                        binding.cardTracking.visibility = View.VISIBLE

                        val courierLatLng = LatLng(courierLat, courierLng)

                        binding.initialName.text = CustomUtility(this@MapsActivity).getInitials(courierFullName ?: "Nama Pengguna")
                        binding.userTrackingName.text = courierFullName
                        binding.userTrackingDescription.text = "Terakhir dilacak " + DateFormat.format("$courierLastTracking", "yyyy-MM-dd HH:mm:ss", "dd MMM yyyy, HH.mm")
                        if (courierIsOnline!!) {
                            binding.userTrackingStatus.text = "Online"
                            binding.userTrackingStatus.setTextColor(getColor(R.color.white))
                            binding.userTrackingStatus.setBackgroundResource(R.drawable.bg_green_reseda_round_8)
                        } else {
                            binding.userTrackingStatus.text = "Offline"
                            binding.userTrackingStatus.setTextColor(getColor(R.color.black_200))
                            binding.userTrackingStatus.setBackgroundResource(R.drawable.bg_light_dark_round)
                        }

                        if (courierMarker == null) {
                            courierMarker = mMap.addMarker(
                                MarkerOptions()
                                    .position(courierLatLng)
                                    .title(
                                        courierFullName ?: "Pengguna"
                                    )
                                    .icon(
                                        BitmapDescriptorFactory.fromBitmap(
                                            resizedBitmap(courierDrawable)
                                        )
                                    )
                            )
                            progressBar.dismiss()
                            changeFocusCamera(courierLatLng)
                        } else {
                            progressBar.dismiss()
                            courierMarker?.position = courierLatLng
                        }
                    } else {

                        progressBar.dismiss()
                        handleMessage(this@MapsActivity, TAG_RESPONSE_CONTACT,
                            "Tidak dapat mendeteksi lokasi pengguna!"
                        )
                    }
                } else {

                    progressBar.dismiss()
                    handleMessage(this@MapsActivity, TAG_RESPONSE_CONTACT,
                        "Tidak dapat mendeteksi lokasi pengguna!"
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.dismiss()

                handleMessage(this@MapsActivity, TAG_RESPONSE_CONTACT,
                    "Failed run service. Exception $error"
                )
            }

        }

        childCourier?.addValueEventListener(courierTrackingListener!!)

        childAbsent?.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val listUserTracking = arrayListOf<UserAbsentModel>()

                    for (item in snapshot.children) {
                        val userId = item.child("id").getValue(String::class.java)
                        val userIdCity = item.child("idCity").getValue(String::class.java)

                        if (!userId.isNullOrEmpty() && userId != courierID) {
                            if (userKind == USER_KIND_ADMIN) {
                                listUserTracking.add(
                                    UserAbsentModel(
                                        eveningDateTime = item.child("eveningDateTime").getValue(String::class.java) ?: "",
                                        fullname = item.child("fullname").getValue(String::class.java) ?: "",
                                        id = item.child("id").getValue(String::class.java) ?: "",
                                        isOnline = item.child("isOnline").getValue(Boolean::class.java) ?: false,
                                        lastSeen = item.child("lastSeen").getValue(String::class.java) ?: "",
                                        lastTracking = item.child("lastTracking").getValue(String::class.java) ?: "",
                                        lat = item.child("lat").getValue(Double::class.java) ?: 0.0,
                                        lng = item.child("lng").getValue(Double::class.java) ?: 0.0,
                                        morningDateTime = item.child("morningDateTime").getValue(String::class.java) ?: "",
                                        username = item.child("username").getValue(String::class.java) ?: "",
                                    )
                                )
                            } else if (!userIdCity.isNullOrEmpty() && userCity == userIdCity) {
                                listUserTracking.add(
                                    UserAbsentModel(
                                        eveningDateTime = item.child("eveningDateTime").getValue(String::class.java) ?: "",
                                        fullname = item.child("fullname").getValue(String::class.java) ?: "",
                                        id = item.child("id").getValue(String::class.java) ?: "",
                                        isOnline = item.child("isOnline").getValue(Boolean::class.java) ?: false,
                                        lastSeen = item.child("lastSeen").getValue(String::class.java) ?: "",
                                        lastTracking = item.child("lastTracking").getValue(String::class.java) ?: "",
                                        lat = item.child("lat").getValue(Double::class.java) ?: 0.0,
                                        lng = item.child("lng").getValue(Double::class.java) ?: 0.0,
                                        morningDateTime = item.child("morningDateTime").getValue(String::class.java) ?: "",
                                        username = item.child("username").getValue(String::class.java) ?: "",
                                    )
                                )
                            }
                        }
                    }

                    if (listUserTracking.isNotEmpty()) {
                        binding.userTrackingSuggestion.visibility = View.VISIBLE

                        // Acak urutan item dalam daftar
                        val shuffledList = listUserTracking.shuffled()
                        val limitedList = shuffledList.take(6)

                        binding.allUserTracking.visibility = View.VISIBLE
                        binding.allUserTracking.setOnClickListener {
                            val intent = Intent(this@MapsActivity, AllUserTrackingActivity::class.java)
                            trackingLauncher.launch(intent)
                        }

                        val rvAdapter = UserTrackingRecyclerViewAdapter()
                        rvAdapter.setList(ArrayList(limitedList))
                        rvAdapter.setOnItemClickListener(object: UserTrackingRecyclerViewAdapter.OnItemClickListener {
                            override fun onItemClick(item: UserAbsentModel) {
                                // Do something here
                                refreshUserTracked(item)
                            }

                        })

                        binding.userTrackingRecyclerView.adapter = rvAdapter
                    } else {
                        binding.userTrackingSuggestion.visibility = View.GONE
                    }
                } else {

                    progressBar.dismiss()
                    handleMessage(this@MapsActivity, TAG_RESPONSE_CONTACT,
                        "Tidak dapat mendeteksi lokasi kurir!"
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.dismiss()

                handleMessage(this@MapsActivity, TAG_RESPONSE_CONTACT,
                    "Failed run service. Exception $error"
                )
            }

        })
    }

    private fun refreshUserTracked(item: UserAbsentModel) {
        if (courierTrackingListener != null) childCourier?.removeEventListener(courierTrackingListener!!)
        courierMarker = null
        mMap.clear()

        Handler(Looper.getMainLooper()).postDelayed({
            courierID = item.id
            setupTrackingCourier()
        }, 100)
    }

    private fun setupTrackingHistory() {
        progressBar.setMessage(getString(R.string.txt_loading))
        progressBar.show()

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getDetailDelivery(idDelivery = deliveryID.toString(), distributorID = userDistributorId)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        val item = response.results[0]

                        val startLatLng = LatLng(item.startLat.toDouble(), item.startLng.toDouble())
                        val destinationLatLng = LatLng(item.lat.toDouble(), item.lng.toDouble())
                        val courierLatLng = LatLng(item.endLat.toDouble(), item.endLng.toDouble())
                        val courierName = item.full_name.let { it.ifEmpty { item.username } }

                        courierMarker = mMap.addMarker(
                            MarkerOptions()
                                .position(courierLatLng)
                                .title(courierName)
                                .icon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        resizedBitmap(courierDrawable)
                                    )
                                )
                        )
                        mMap.addMarker(
                            MarkerOptions()
                                .position(destinationLatLng)
                                .title(item.nama)
                                .icon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        resizedBitmap(locationBlacklistDrawable)
                                    )
                                )
                        )

                        val directions =
                            DirectionsApi.newRequest(getGeoContext())
                                .mode(TravelMode.DRIVING) // Ganti dengan mode perjalanan yang sesuai
                                .origin(
                                    com.google.maps.model.LatLng(
                                        startLatLng.latitude,
                                        startLatLng.longitude
                                    )
                                )
                                .destination(
                                    com.google.maps.model.LatLng(
                                        destinationLatLng.latitude,
                                        destinationLatLng.longitude
                                    )
                                )
                                .optimizeWaypoints(true)
                                .alternatives(true)

                        try {
                            val result = directions.await()

                            if (result.routes.isNotEmpty()) {
                                val listPolylineOptions =
                                    arrayListOf<PolylineOptions>()

                                // Gambar rute pada peta
                                for (i in 0..result.routes.size) {
                                    if (i < result.routes.size && i < 3) {
                                        val route = result.routes[i]
                                        val overviewPolyline =
                                            route.overviewPolyline.decodePath()
                                        val polylineOptions =
                                            PolylineOptions()
                                        polylineOptions.width(15f)
                                        polylineOptions.color(
                                            getColor(
                                                lineColor[i]
                                            )
                                        )
                                        for (latLng in overviewPolyline) {
                                            polylineOptions.add(
                                                LatLng(
                                                    latLng.lat,
                                                    latLng.lng
                                                )
                                            )
                                        }
                                        listPolylineOptions.add(
                                            polylineOptions
                                        )
                                    }
                                }

                                for (i in listPolylineOptions.size - 1 downTo 0) {
                                    routeDirections =
                                        mMap.addPolyline(listPolylineOptions[i])
                                    listRouteLines.add(routeDirections!!)
                                }

                                val bounds = LatLngBounds.builder()
                                    .include(startLatLng)
                                    .include(destinationLatLng)
                                    .build()
                                val updateCamera =
                                    CameraUpdateFactory.newLatLngBounds(
                                        bounds,
                                        100
                                    )
                                mMap.animateCamera(
                                    updateCamera,
                                    mapsDuration,
                                    null
                                )

                                Handler(Looper.getMainLooper()).postDelayed({
                                    binding.cardDelivery.visibility = View.VISIBLE
                                    binding.deliveryCourier.text = courierName
                                    binding.deliveryStore.text = item.nama
                                    binding.deliveryDate.text = "Diproses pada " + formatDateYear(item.startDatetime)
                                    binding.deliveryEndDateContainer.visibility = View.VISIBLE
                                    binding.deliveryEndDate.text = "Diselesaikan pada " + formatDateYear(item.endDatetime)
                                    binding.btnSuratJalan.setOnClickListener {
                                        val intent = Intent(this@MapsActivity, ListSuratJalanActivity::class.java)
                                        intent.putExtra(CONST_CONTACT_ID, item.id_contact)
                                        intent.putExtra(CONST_NAME, "")
                                        activityLauncher.launch(intent)
                                    }
                                    binding.courierContainer.setOnClickListener { changeFocusCamera(courierLatLng) }
                                    binding.storeContainer.setOnClickListener { changeFocusCamera(destinationLatLng) }
                                    progressBar.dismiss()

                                    if (ActivityCompat.checkSelfPermission(
                                            this@MapsActivity,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                            this@MapsActivity,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) return@postDelayed
                                    mMap.isMyLocationEnabled = false
                                }, 500)


                            } else {
                                progressBar.dismiss()
                                handleMessage(this@MapsActivity, "setupTrackingHistory" , "Tidak dapat menemukan rute.")
                            }
                        } catch (e: ApiException) {
                            progressBar.dismiss()
                            handleMessage(this@MapsActivity, "setupTrackingHistory" , "Gagal memuat navigasi. Err: ${e.message}")
                            e.printStackTrace()
                        } catch (e: InterruptedException) {
                            progressBar.dismiss()
                            handleMessage(this@MapsActivity, "setupTrackingHistory" , "Gagal memuat navigasi. Err: ${e.message}")
                            e.printStackTrace()
                        } catch (e: IOException) {
                            progressBar.dismiss()
                            handleMessage(this@MapsActivity, "setupTrackingHistory" , "Gagal memuat navigasi. Err: ${e.message}")
                            e.printStackTrace()
                        }

                    }
                    RESPONSE_STATUS_EMPTY -> {


                        progressBar.dismiss()
                        handleMessage(this@MapsActivity, "setupTrackingHistory",
                            "Tidak dapat menemukan riwayat pengiriman"
                        )

                    }
                    else -> {


                        progressBar.dismiss()
                        handleMessage(this@MapsActivity, "setupTrackingHistory",
                            "Tidak dapat menemukan riwayat pengiriman. Error " + response.message
                        )

                    }
                }

            } catch (e: Exception) {

                if (e is CancellationException) {
                    return@launch
                }
                FirebaseUtils.logErr(this@MapsActivity, "Failed MapsActivity on setupTrackingHistory(). Catch: ${e.message}")
                progressBar.dismiss()
                handleMessage(this@MapsActivity, "setupTrackingHistory",
                    "Failed run service. Exception " + e.message
                )

            }
        }

    }

    private fun resizedBitmap(drawable: Int, targetWidth: Int = convertDpToPx(60, this@MapsActivity), targetHeight: Int = convertDpToPx(60, this@MapsActivity)): Bitmap {
        bitmapCache[drawable]?.let { return it }

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(resources, drawable, options)

        options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
        options.inJustDecodeBounds = false

        val originalBitmap = BitmapFactory.decodeResource(resources, drawable, options)
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, false)

        originalBitmap.recycle()

        bitmapCache[drawable] = scaledBitmap
        return scaledBitmap
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun changeFocusCamera(latLng: LatLng, zoom: Float? = null) {
        val zoomLvl = zoom ?: zoomLevel
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLvl)
        mMap.animateCamera(cameraUpdate, mapsDuration, null)
    }

    private fun formatDateYear(dateString: String, dateStringFormat: String = "yyyy-MM-dd HH:mm:ss"): String {
        val date = SimpleDateFormat(dateStringFormat, Locale.getDefault()).parse(dateString)
        return if (date != null) {
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val dateYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)

            if (currentYear == dateYear.toInt()) DateFormat.format(dateString, dateStringFormat, "dd MMM, HH:mm")
            else DateFormat.format(dateString, dateStringFormat, "dd MMM yyyy, HH:mm")
        } else DateFormat.format(dateString, dateStringFormat, "dd MMM, HH:mm")
    }

    @Subscribe
    fun onEventBus(event: EventBusUtils.UserAbsentModelEvent) {

        refreshUserTracked(event.data!!)
    }

    override fun onStart() {
        super.onStart()

        Handler(Looper.getMainLooper()).postDelayed({
            if (userKind == USER_KIND_COURIER || userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN){
                CustomUtility(this).setUserStatusOnline(true, userDistributorIds ?: "-custom-002", userID)
            }
        }, 1000)

    }

    override fun onStop() {
        super.onStop()

        if (userKind == USER_KIND_COURIER || userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN){
            CustomUtility(this).setUserStatusOnline(false, userDistributorIds ?: "-custom-002", userID)
        }

        if (locationListener != null) childDriver?.removeEventListener(locationListener!!)
        if (locationCallback != null) fusedLocationClient.removeLocationUpdates(locationCallback!!)
        if (courierTrackingListener != null) childCourier?.removeEventListener(courierTrackingListener!!)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::progressBar.isInitialized && progressBar.isShowing()) {
            progressBar.dismiss()
        }

        if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_ADMIN_CITY) EventBus.getDefault().unregister(this)
        if (userKind == USER_KIND_COURIER || userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN){
            CustomUtility(this).setUserStatusOnline(false, userDistributorIds ?: "-custom-002", userID)
        }

        if (locationListener != null) childDriver?.removeEventListener(locationListener!!)
        if (locationCallback != null) fusedLocationClient.removeLocationUpdates(locationCallback!!)
        if (courierTrackingListener != null) childCourier?.removeEventListener(courierTrackingListener!!)

        clearBitmapCache()
    }

    private fun clearBitmapCache() {
        bitmapCache.values.forEach { it.recycle() }
        bitmapCache.clear()
    }
}