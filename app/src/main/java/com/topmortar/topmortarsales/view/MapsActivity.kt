package com.topmortar.topmortarsales.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.Handler
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
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
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.errors.ApiException
import com.google.maps.model.DirectionsResult
import com.google.maps.model.DirectionsRoute
import com.google.maps.model.TravelMode
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.PlaceAdapter
import com.topmortar.topmortarsales.commons.CONNECTION_FAILURE_RESOLUTION_REQUEST
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_NAME
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.GET_COORDINATE
import com.topmortar.topmortarsales.commons.LOCATION_PERMISSION_REQUEST_CODE
import com.topmortar.topmortarsales.commons.TOAST_LONG
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.URLUtility
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.databinding.ActivityMapsBinding
import java.io.IOException
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private lateinit var sessionManager: SessionManager

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private var routeDirections: Polyline? = null

    private var isGetCoordinate = false
    private var listCoordinate: ArrayList<String>? = null
    private var listCoordinateName: ArrayList<String>? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark))
            lineColor = listOf(R.color.primary_200, R.color.status_passive, R.color.status_passive)
        }

        if (binding.btnGetLatLng.isVisible) {
            mMap.setPadding(0,0,0, convertDpToPx(72, this))
            mMap.setOnMapLongClickListener { latLng -> setPin(latLng, getPlaceNameFromLatLng(latLng), moveCamera = false) }
            if (!sessionManager.pinMapHint()) {
                showDialog(message = "Tekan dan tahan pada peta untuk menandai lokasi")
                sessionManager.pinMapHint(true)
            }
        } else if (binding.btnGetDirection.isVisible) {
            mMap.setPadding(0,0,0, convertDpToPx(72, this))

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) currentLatLng = LatLng(location.latitude, location.longitude)
                }

            binding.btnGetDirection.setOnClickListener {
                toggleDirection()
            }
        }

        mMap.setOnMapClickListener {
            if (binding.recyclerView.isVisible) binding.recyclerView.visibility = View.GONE
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

    private fun dataActivityValidation() {

        val iMaps = intent.getStringExtra(CONST_MAPS)
        isGetCoordinate = intent.getBooleanExtra(GET_COORDINATE, false)
        listCoordinate = intent.getStringArrayListExtra(CONST_LIST_COORDINATE)
        listCoordinateName = intent.getStringArrayListExtra(CONST_LIST_COORDINATE_NAME)

        if (isGetCoordinate) {
            binding.btnGetLatLng.visibility = View.VISIBLE
            binding.searchBar.visibility = View.VISIBLE
        } else {
            binding.btnGetDirection.visibility = View.VISIBLE
        }

        if (!iMaps.isNullOrEmpty()) {

            val urlUtility = URLUtility(this)

            if (urlUtility.isUrl(iMaps)) {
                val mapsUrlPattern1 = Regex("https://goo\\.gl/maps/\\w+")
                val mapsUrlPattern2 = Regex("https://maps\\.app\\.goo\\.gl/\\w+")

                if (mapsUrlPattern1.matches(iMaps) || mapsUrlPattern2.matches(iMaps)) return onFindLocation(iMaps)
                else showDialog(message = "Gagal memproses maps url")
            } else {

                val coordinates = iMaps.trim().split(",")
                if (coordinates.size == 2) {
                    val latitude = coordinates[0].toDoubleOrNull()
                    val longitude = coordinates[1].toDoubleOrNull()

                    if (latitude != null && longitude != null) {
                        val latLng = LatLng(latitude, longitude)
                        etSearch.setText(getPlaceNameFromLatLng(latLng))
                        binding.recyclerView.visibility = View.GONE
                        return initMaps(latLng)
                    } else showDialog(message = "Gagal menavigasi koordinat")
                } else showDialog(message = "Gagal memproses koordinat")

            }

            initMaps()

        } else initMaps()

    }

    private fun searchCoordinate() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Mencari toko terdekat…")
        progressDialog.show()

        Handler().postDelayed({

            val urlUtility = URLUtility(this)
            val limitKm = binding.etKm.text.toString().toDouble()
            mMap.clear()

            for ((i, item) in listCoordinate!!.iterator().withIndex()) {

                if (!urlUtility.isUrl(item)) {

                    val coordinates = item.trim().split(",")
                    if (coordinates.size == 2) {
                        val latitude = coordinates[0].toDoubleOrNull()
                        val longitude = coordinates[1].toDoubleOrNull()

                        if (latitude != null && longitude != null) {

                            val urlUtility = URLUtility(this)
                            val distance = urlUtility.calculateDistance(currentLatLng!!.latitude, currentLatLng!!.longitude, latitude, longitude)

                            if (distance < limitKm) {

                                val latLng = LatLng(latitude, longitude)
                                binding.recyclerView.visibility = View.GONE

                                selectedLocation = latLng
                                mMap.addMarker(
                                    MarkerOptions()
                                        .position(latLng)
                                        .title(listCoordinateName?.get(i))
                                )

                            }

                        }
                    }

                }

            }

            progressDialog.dismiss()
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

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng!!, responsiveZoom.toFloat())

            mMap.animateCamera(cameraUpdate, durationMs, null)

            binding.cardTelusuri.visibility = View.VISIBLE
            binding.btnTelusuri.setOnClickListener { searchCoordinate() }

        }, 2000)
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
                Toast.makeText(this, "${result.routes.size}", TOAST_SHORT).show()

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
                        routeDirections = mMap.addPolyline(polylineOptions)
                    }
                }

                val bounds = LatLngBounds.builder()
                    .include(currentLocation)
                    .include(destination)
                    .build()
                val updateCamera = CameraUpdateFactory.newLatLngBounds(bounds, 100)
                mMap.animateCamera(updateCamera, mapsDuration, null)

                Handler().postDelayed({
                    val button = binding.btnGetDirection
                    val img = binding.btnGetDirectionImg
                    val title = binding.btnGetDirectionTitle
                    button.setBackgroundResource(R.drawable.bg_passive_round)
                    img.setImageDrawable(getDrawable(R.drawable.direction_line_white))
                    title.text = "Matikan Navigasi"
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
    private fun initMaps(latLng: LatLng? = null) {

        if (latLng != null) setPin(latLng, getPlaceNameFromLatLng(latLng))
        else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        currentLatLng = LatLng(location.latitude, location.longitude)
                        setPin(currentLatLng!!, "Lokasi Saya")

                        if (!listCoordinate.isNullOrEmpty()) searchCoordinate()
                    }
                }
        }

    }

    private fun setPin(latLng: LatLng, placeName: String, moveCamera: Boolean = true) {

        selectedLocation = latLng
        mMap.clear()
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(placeName)
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
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initMaps()
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
        if (!isGetCoordinate && routeDirections != null) {
            toggleDirection()
        } else super.onBackPressed()
    }
}