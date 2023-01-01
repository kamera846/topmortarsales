package com.topmortar.topmortarsales.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.utils.URLUtility
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

class TestMapsDistanceActivity : AppCompatActivity() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private lateinit var txtDistance: TextView
    private lateinit var webView: WebView
    private lateinit var urlUtility: URLUtility
    private var originalUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_maps_distance)

        txtDistance = findViewById(R.id.txt_distance)
        webView = findViewById(R.id.webView)

        val shortenedUrl = "https://maps.app.goo.gl/qWWshnVabAAJem9a9"
//        FetchOriginalUrlTask().execute(shortenedUrl)

        urlUtility = URLUtility(this)

        urlUtility.fetchOriginalUrl(shortenedUrl) { originalUrl ->
            if (originalUrl.isNotEmpty()) {

                this.originalUrl = originalUrl
                getDistance()

            } else {

                Toast.makeText(this, "Gagal mendapatkan URL asli", TOAST_SHORT).show()

            }
        }

    }

    private fun getDistance() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val distance = urlUtility.getDistance(originalUrl!!)
            if (distance != null) {
                val shortDistance = "%.1f".format(distance).toDouble()

                if (distance > 0.2) txtDistance.text = "$shortDistance Maaf, jarak anda telah melebihi 200 meter."
                else txtDistance.text = "$shortDistance Jarak anda sudah lebih dekat dari 200 meter."
            } else txtDistance.text = "Failed to get distance"
        } else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDistance()
            } else {
                Toast.makeText(this, "Izin ditolak!", TOAST_SHORT).show()
            }
        }
    }


//    private inner class FetchOriginalUrlTask : AsyncTask<String, Void, String>() {
//        override fun doInBackground(vararg params: String?): String {
//            val shortenedUrl = params[0]
//
//            try {
//                val url = URL(shortenedUrl)
//                val connection = url.openConnection() as HttpURLConnection
//
//                // Set up the connection
//                connection.requestMethod = "HEAD"
//                connection.instanceFollowRedirects = false
//                connection.connect()
//
//                // Check for redirection (HTTP 3xx)
//                if (connection.responseCode in 300..399) {
//                    val locationHeader = connection.getHeaderField("Location")
//                    if (locationHeader != null) {
//                        return locationHeader
//                    }
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//
//            return ""
//        }
//
//        override fun onPostExecute(result: String) {
//            super.onPostExecute(result)
//
//            if (result.isNotEmpty()) {
//                // The result is the original URL
//                // You can use it as needed
////                txtDistance.text = "$result"
//                println("Original URL: $result")
//                Toast.makeText(this@TestMapsDistanceActivity, "$result", TOAST_SHORT).show()
//                getDistance(result)
//            } else {
//                // Unable to retrieve the original URL
//                println("Failed to retrieve the original URL")
//                Toast.makeText(this@TestMapsDistanceActivity, "Failed to retrieve the original URL", TOAST_SHORT).show()
//            }
//        }
//    }
//
//    private fun getDistance(mapsUrl: String) {
//        // Check for location permissions and request if necessary
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
//            if (location != null) {
//                val currentLatitude = location.latitude
//                val currentLongitude = location.longitude
//
//                // Extract coordinates from the Google Maps URL
////                val mapsUrl = "https://goo.gl/maps/xn2XZP2Uosfbnone6"
//                val regex = Pattern.compile("@(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)")
//                val matcher = regex.matcher(mapsUrl)
//                if (matcher.find() && matcher.groupCount() == 2) {
//                    val urlLatitude = matcher.group(1).toDouble()
//                    val urlLongitude = matcher.group(2).toDouble()
//
//                    // Calculate the distance between the two points
//                    val distance = calculateDistance(currentLatitude, currentLongitude, urlLatitude, urlLongitude)
//                    val shortDistance = "%.1f".format(distance).toDouble()
//
//                    if (distance > 0.2) txtDistance.text = "$shortDistance Maaf, jarak anda telah melebihi 200 meter."
//                    else txtDistance.text = "$shortDistance Jarak anda sudah lebih dekat dari 200 meter."
//
//                    // Now you have the distance in kilometers
//                    // You can use or display this distance as needed
//                } else {
//                    Toast.makeText(this, "Invalid URL", TOAST_SHORT).show()
//                    // Handle invalid URL
//                }
//            } else {
//                Toast.makeText(this, "Location unavailable", TOAST_SHORT).show()
//                // Handle case where location is unavailable
//            }
//        } else {
//            // Request location permission from the user
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
//        }
//    }
//
//    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
//        val radius = 6371 // Earth's radius in kilometers
//        val dLat = Math.toRadians(lat2 - lat1)
//        val dLon = Math.toRadians(lon2 - lon1)
//        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
//                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
//                Math.sin(dLon / 2) * Math.sin(dLon / 2)
//        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
//        return radius * c
//    }
}
