package com.topmortar.topmortarsales.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.TOAST_LONG
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.utils.URLUtility
import com.topmortar.topmortarsales.databinding.ActivityMapsBinding
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionCode = 1
    private var selectedLocation: LatLng? = null
    private var currentLatLng: LatLng? = null

    private lateinit var mLastLocation: Location
    private var mCurrLocationMarker: Marker? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private lateinit var mLocationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        onCalculate()
        onFindLocation()
//        searchLocation()
    }

    private fun onFindLocation() {
        binding.btnFindLocation.setOnClickListener {
//            val mapsUrl = "https://maps.app.goo.gl/CNguhADL5yhGLNTn8"
            val mapsUrl = "${ binding.inputMapsUrl.text }"
            val urlUtility = URLUtility(this)

            urlUtility.fetchOriginalUrl(mapsUrl) { originalUrl ->
                if (originalUrl.isNotEmpty()) {
                    val latLng = urlUtility.getLatLng(originalUrl)
                    if (latLng != null) initMaps(latLng)
                    else {
                        val placeName = getPlaceNameFromMapsUrl(originalUrl)
                        if (!placeName.isNullOrEmpty()) searchLocation(placeName)
                        else Toast.makeText(this, "Failed to find coordinate", TOAST_SHORT).show()
                    }
//                    Toast.makeText(this, "$originalUrl", TOAST_LONG).show()
                } else Toast.makeText(this, "Failed to process the URL", TOAST_SHORT).show()
            }

        }
    }

    private fun getPlaceNameFromMapsUrl(mapsUrl: String): String? {
        val startIndex = mapsUrl.indexOf("/place/") + "/place/".length
        val endIndex = mapsUrl.indexOf("/data=")

        if (startIndex != -1 && endIndex != -1) {
            return mapsUrl.substring(startIndex, endIndex).replace("+", " ")
        }

        return null
    }


    private fun onCalculate() {
        val confirmLocationButton = findViewById<Button>(R.id.confirmLocationButton)
        confirmLocationButton.setOnClickListener {
            if (selectedLocation != null) {
                val urlUtility = URLUtility(this)
                val distance = urlUtility.calculateDistance(currentLatLng!!.latitude, currentLatLng!!.longitude, selectedLocation!!.latitude, selectedLocation!!.longitude)
                val shortDistance = "%.3f".format(distance).toDouble()

                val message: String = if (distance > 0.2) "$shortDistance Maaf, jarak anda telah melebihi 200 meter."
                else "$shortDistance Jarak anda sudah lebih dekat dari 200 meter."

                Toast.makeText(this@MapsActivity, message, TOAST_LONG).show()
            } else {
                Toast.makeText(this, "Pilih lokasi terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun initMaps(latLng: LatLng? = null) {
        if (latLng != null) {
            selectedLocation = latLng
            mMap.clear() // Hapus marker sebelumnya
            mMap.addMarker(
                MarkerOptions()
                    .position(selectedLocation!!)
                    .title("Selected Location")
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation!!, mMap.maxZoomLevel))
        } else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        currentLatLng = LatLng(location.latitude, location.longitude)
                        selectedLocation = currentLatLng
                        mMap.addMarker(
                            MarkerOptions()
                                .position(currentLatLng!!)
                                .title("My Current Location")
                        )
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng!!, mMap.maxZoomLevel))
                    }
                }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
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
            mMap.isMyLocationEnabled = true // Aktifkan tampilan lokasi perangkat saat ini
            mMap.uiSettings.isMyLocationButtonEnabled = true // Aktifkan tombol lokasi perangkat saat ini

            initMaps()

            mMap.setOnMapClickListener { latLng ->
                selectedLocation = latLng
                mMap.clear() // Hapus marker sebelumnya
                mMap.addMarker(
                    MarkerOptions()
                        .position(selectedLocation!!)
                        .title("Selected Location")
                )
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
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

    private fun searchLocation(placeName: String?) {
        if (!placeName.isNullOrEmpty()) {
            var addressList: List<Address>? = null

            val geoCoder = Geocoder(this)
            try {
                addressList = geoCoder.getFromLocationName(placeName, 1)
            }catch (e: IOException){
                e.printStackTrace()
            }

            val address = addressList!![0]
            val latLng = LatLng(address.latitude, address.longitude)
            mMap.addMarker(MarkerOptions().position(latLng).title(placeName))
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }
        binding.btnFindLocation.setOnClickListener {
            val location = "${ binding.inputMapsUrl.text.trim() }"
            var addressList: List<Address>? = null

            if (location.isNullOrEmpty()){
                Toast.makeText(this, "Failed to find location", Toast.LENGTH_SHORT).show()
            } else{
                val geoCoder = Geocoder(this)
                try {
                    addressList = geoCoder.getFromLocationName(location, 1)
                }catch (e: IOException){
                    e.printStackTrace()
                }

                val address = addressList!![0]
                val latLng = LatLng(address.latitude, address.longitude)
                mMap.addMarker(MarkerOptions().position(latLng).title(location))
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        mLastLocation = location
        if (mCurrLocationMarker != null){
            mCurrLocationMarker!!.remove()
        }

        val latLng = LatLng(location.latitude, location.longitude)
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("Current Position")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        mCurrLocationMarker = mMap.addMarker(markerOptions)

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.moveCamera(CameraUpdateFactory.zoomTo(11f))

        if (mGoogleApiClient != null){
            LocationServices.getFusedLocationProviderClient(this)
        }
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

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }
}