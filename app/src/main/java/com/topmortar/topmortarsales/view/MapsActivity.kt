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
import com.topmortar.topmortarsales.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionCode = 1
    private var selectedLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Di dalam onCreate
        val confirmLocationButton = findViewById<Button>(R.id.confirmLocationButton)
        confirmLocationButton.setOnClickListener {
            if (selectedLocation != null) {
                // Lakukan apa yang diperlukan dengan lokasi yang dipilih
                Toast.makeText(this@MapsActivity, "Lat: ${selectedLocation!!.latitude} \nLong: ${selectedLocation!!.longitude}", TOAST_LONG).show()
            } else {
                // Jika pengguna belum memilih lokasi, beri pesan kesalahan atau tampilkan peringatan.
                Toast.makeText(this, "Pilih lokasi terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun initMaps() {
        // Mendapatkan lokasi perangkat saat ini
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(currentLatLng)
                            .title("Lokasi Saya")
                    )
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                }
            }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Memeriksa izin lokasi
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true // Aktifkan tampilan lokasi perangkat saat ini
            mMap.uiSettings.isMyLocationButtonEnabled = true // Aktifkan tombol lokasi perangkat saat ini

            initMaps()

            // Tambahkan listener klik pada peta untuk memilih lokasi
            mMap.setOnMapClickListener { latLng ->
                selectedLocation = latLng
                mMap.clear() // Hapus marker sebelumnya
                mMap.addMarker(
                    MarkerOptions()
                        .position(selectedLocation!!)
                        .title("Lokasi Dipilih")
                )
            }
        } else {
            // Jika izin lokasi tidak diberikan, munculkan permintaan izin kepada pengguna
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
    }

    // Metode untuk menangani hasil permintaan izin
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