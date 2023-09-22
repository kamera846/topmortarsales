package com.topmortar.topmortarsales.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.snackbar.Snackbar
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.PlaceAdapter
import com.topmortar.topmortarsales.commons.CONNECTION_FAILURE_RESOLUTION_REQUEST
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.GET_COORDINATE
import com.topmortar.topmortarsales.commons.LOCATION_PERMISSION_REQUEST_CODE
import com.topmortar.topmortarsales.commons.REQUEST_EDIT_CONTACT_COORDINATE
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TOAST_LONG
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.utils.URLUtility
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.databinding.ActivityMapsBinding
import java.io.IOException
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient

    private var isGetCoordinate = false

    private val zoomLevel = 18f
    private var selectedLocation: LatLng? = null
    private var currentLatLng: LatLng? = null

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
    }

    private fun dataActivityValidation() {

        val iMaps = intent.getStringExtra(CONST_MAPS)
        isGetCoordinate = intent.getBooleanExtra(GET_COORDINATE, false)

        if (isGetCoordinate) binding.btnGetLatLng.visibility = View.VISIBLE

        if (!iMaps.isNullOrEmpty()) {

            val urlUtility = URLUtility(this)

            if (urlUtility.isUrl(iMaps)) {
                val mapsUrlPattern1 = Regex("https://goo\\.gl/maps/\\w+")
                val mapsUrlPattern2 = Regex("https://maps\\.app\\.goo\\.gl/\\w+")

                if (mapsUrlPattern1.matches(iMaps) || mapsUrlPattern2.matches(iMaps)) return onFindLocation(iMaps)
                else Toast.makeText(this, "Failed to process maps url", TOAST_SHORT).show()
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
                    } else Toast.makeText(this, "Failed navigate to coordinate", TOAST_SHORT).show()
                } else Toast.makeText(this, "Failed to process coordinate", TOAST_SHORT).show()

            }

            initMaps()

        } else initMaps()

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
                    } else Toast.makeText(this, "Failed to find coordinate", TOAST_SHORT).show()

                } else initMaps(latLng)

            } else Toast.makeText(this, "Failed to process the URL", TOAST_SHORT).show()

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

        return if (addresses!!.isNotEmpty()) addresses[0].getAddressLine(0) else "Location Name Not Found"

    }

    private fun onGetLatLng() {
        binding.btnGetLatLng.setOnClickListener {

            if (selectedLocation != null) {

//                Toast.makeText(this@MapsActivity, "Coordinate: ${ selectedLocation!!.latitude },${ selectedLocation!!.longitude }", TOAST_LONG).show()
                val resultIntent = Intent()
                resultIntent.putExtra("latitude", selectedLocation!!.latitude)
                resultIntent.putExtra("longitude", selectedLocation!!.longitude)
                setResult(RESULT_OK, resultIntent)
                finish()

            } else Toast.makeText(this, "Not selected location", Toast.LENGTH_SHORT).show()

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

    @SuppressLint("MissingPermission")
    private fun initMaps(latLng: LatLng? = null) {

        if (latLng != null) setPin(latLng, getPlaceNameFromLatLng(latLng))
        else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        currentLatLng = LatLng(location.latitude, location.longitude)
                        setPin(currentLatLng!!, "My Current Location")
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
            val durationMs = 2000 // 1 second
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel)

            mMap.animateCamera(cameraUpdate, durationMs, null)
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

            onCalculate()
            onGetLatLng()
            searchLocation()
            dataActivityValidation()

            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isCompassEnabled = true
            mMap.uiSettings.isZoomControlsEnabled = true
            mMap.uiSettings.isTiltGesturesEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true

            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark))

//            initMaps()

            if (binding.btnGetLatLng.isVisible) {
                mMap.setPadding(0,0,0, convertDpToPx(64, this))
                mMap.setOnMapLongClickListener { latLng -> setPin(latLng, getPlaceNameFromLatLng(latLng), moveCamera = false) }
                Snackbar.make(
                    findViewById(android.R.id.content), // Replace with your root view
                    "Press and hold on the map to mark a location",
                    Snackbar.LENGTH_LONG
                ).show()
            }

            mMap.setOnMapClickListener {
                if (binding.recyclerView.isVisible) binding.recyclerView.visibility = View.GONE
            }

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

//            setPin(latLng, getPlaceNameFromLatLng(latLng))
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
                                            Toast.makeText(this, "Failed direct to coordinate", TOAST_SHORT).show()
                                            binding.recyclerView.visibility = View.GONE
                                        }

                                    }
                                    .addOnFailureListener { _: Exception ->
                                        Toast.makeText(this, "Failure direct to location", TOAST_SHORT).show()
                                        binding.recyclerView.visibility = View.GONE
                                    }
                            }

                            binding.recyclerView.apply {
                                layoutManager = LinearLayoutManager(this@MapsActivity)
                                adapter = placeAdapter
                                this.visibility = View.VISIBLE
                            }
                        } else binding.recyclerView.visibility = View.GONE

                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to find place", TOAST_SHORT).show()
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
        markerOptions.title("Current Position")
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

        } else Toast.makeText(this, "Connection to Google Play Services failed", TOAST_SHORT).show()
    }
}