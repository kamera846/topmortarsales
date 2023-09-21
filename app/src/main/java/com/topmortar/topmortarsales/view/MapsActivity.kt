package com.topmortar.topmortarsales.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.TOAST_LONG
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.utils.URLUtility
import com.topmortar.topmortarsales.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionCode = 1
    private var selectedLocation: LatLng? = null
    private var currentLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        onSelectedLocation()
        onFindLocation()
    }

    private fun onFindLocation() {
        binding.btnFindLocation.setOnClickListener {
            val mapsUrl = "${ binding.inputMapsUrl.text }"
            val urlUtility = URLUtility(this)

            urlUtility.fetchOriginalUrl(mapsUrl) { originalUrl ->
                if (originalUrl.isNotEmpty()) {
                    val latLng = urlUtility.getLatLng(originalUrl)
                    if (latLng != null) initMaps(latLng)
                    else Toast.makeText(this, "Failed to find coordinate", TOAST_SHORT).show()
                } else Toast.makeText(this, "Failed to process the URL", TOAST_SHORT).show()
            }

        }
    }

    private fun onSelectedLocation() {
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
}