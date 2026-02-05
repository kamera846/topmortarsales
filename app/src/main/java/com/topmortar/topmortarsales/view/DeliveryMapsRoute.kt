package com.topmortar.topmortarsales.view

// Impor dari Google Maps Services Java Client
// Gunakan alias untuk menghindari konflik nama LatLng

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.DirectionsApiRequest
import com.google.maps.DistanceMatrixApi
import com.google.maps.GeoApiContext
import com.google.maps.errors.ApiException
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.databinding.ActivityDeliveryMapsRouteBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import com.google.maps.model.LatLng as MapsLatLng

class DeliveryMapsRoute : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityDeliveryMapsRouteBinding

    var isOptimalRoute = false

    data class Lokasi(
        val nama: String,
        val mapsUrl: String, // Format: "lintang,bujur"
        var jarakMeter: Long = 0,
    )

    val rawLocation = listOf(
        Lokasi("Gudang Malang", "-7.898623265582008,112.63029534369707"), // Titik Awal Gudang
        Lokasi(
            "TB SUMBER JAYA BATURETNO",
            "-7.9029354,112.6986352"
        ),           // (23 Sak) - (3 Hari)
        Lokasi(
            "TB SUMBER LANCAR 1",
            "-7.922275409971633,112.62625090777874"
        ),  // (22 Sak) - (2 Hari)
        Lokasi(
            "TB DINAR",
            "-7.995199182394503,112.6160440966487"
        ),             // (12 Sak) - (2 Hari)
        Lokasi(
            "TB Mustika Jaya 2",
            "-7.8647475,112.6469088"
        ),                  // (33 Sak) - (1 hari)
        Lokasi(
            "UD CANDRA",
            "-7.9434273,112.6909412"
        ),                          // (11 Sak) - (1 hari)
        Lokasi(
            "TB Karya Abadi",
            "-7.911990216394035,112.59995184838772"
        ),      // (56 Sak) - (0 hari)
        Lokasi(
            "cv duta steel",
            "-7.903456884142581,112.60672073811293"
        ),       // (22 Sak) - (0 hari)
        Lokasi(
            "UD SUMBER BENING",
            "-7.863783674622038,112.68382888287306"
        ),    // (22 Sak) - (0 hari)
    )

    var daftarLokasi = listOf<Lokasi>()

    val API_KEY = "AIzaSyBNtWfOO3C9WWbyf_jBtqRCTgKc7HUW-40"
    val TAG = "DirectionsClient"

    // Inisialisasi GeoApiContext
    private val geoApiContext: GeoApiContext by lazy {
        GeoApiContext.Builder()
            .apiKey(API_KEY)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge()

        binding = ActivityDeliveryMapsRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_delivery) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.titleBar.icBack.setOnClickListener { finish() }
        binding.titleBar.tvTitleBar.text = "Rute Pengiriman"

        daftarLokasi = rawLocation.map { it.copy() }

        binding.optimalRouteToggleButton.setOnClickListener {
            isOptimalRoute = !isOptimalRoute

            mMap.clear()

            if (isOptimalRoute) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val sortedList = urutkanRute()
                    withContext(Dispatchers.Main) {
                        daftarLokasi = sortedList
                        addMarkersAndZoom()
                        getMultiWaypointDirections()
                    }
                }
            } else {
                daftarLokasi = rawLocation.map { it.copy() }
                addMarkersAndZoom()
                getMultiWaypointDirections()
            }

            if (isOptimalRoute) binding.optimalRouteToggleButton.text = "Matikan Rute Optimal"
            else binding.optimalRouteToggleButton.text = "Hidupkan Rute Optimal"
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (daftarLokasi.size < 2) {
            Toast.makeText(this, "Minimal 2 lokasi diperlukan.", Toast.LENGTH_LONG).show()
            return
        }

        mMap.setPadding(0, 0, 0, convertDpToPx(100, this@DeliveryMapsRoute))
        mMap.uiSettings.isZoomControlsEnabled = true

        addMarkersAndZoom()
        getMultiWaypointDirections()
    }

    // --- LOGIKA UTAMA: MENGAMBIL RUTE ---

    private fun getMultiWaypointDirections() {
        // 1. Validasi dan Persiapan Data
        val originLocation = daftarLokasi.first()
        val destinationLocation = daftarLokasi.last()

        val originMapsLatLng = parseToMapsLatLng(originLocation.mapsUrl)
        val destinationMapsLatLng = parseToMapsLatLng(destinationLocation.mapsUrl)

        // 2. Buat Array Waypoints
        val waypointsArray = daftarLokasi.subList(1, daftarLokasi.size - 1)
            .map { location ->
                DirectionsApiRequest.Waypoint(parseToMapsLatLng(location.mapsUrl))
            }.toTypedArray()

        // 3. Eksekusi Permintaan di IO Thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val directionsResult = DirectionsApi.newRequest(geoApiContext)
                    .mode(TravelMode.DRIVING)
                    .avoid(DirectionsApi.RouteRestriction.HIGHWAYS)
                    .origin(originMapsLatLng)
                    .destination(destinationMapsLatLng)
                    .waypoints(*waypointsArray) // Menggunakan spread operator (*) untuk array
                    .optimizeWaypoints(isOptimalRoute) // Meminta Google mengoptimalkan urutan Waypoints
                    .await() // Melakukan panggilan sinkron (blocking) di background thread
//                if (isOptimalRoute) directionsResult.optimizeWaypoints(true)
//                directionsResult.await()

                // 4. Update UI di Main Thread
                withContext(Dispatchers.Main) {
                    if (directionsResult.routes.isNotEmpty()) {
                        drawRouteOnMap(directionsResult)
                    } else {
                        Toast.makeText(
                            this@DeliveryMapsRoute,
                            "Tidak ada rute ditemukan.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: ApiException) {
                // Termasuk REQUEST_DENIED (API Key issue) atau ZERO_RESULTS
                Log.e(TAG, "Directions API Error: ${e.message}", e)
                handleError("Directions API Error: ${e.message}")
            } catch (e: IOException) {
                // Masalah jaringan
                Log.e(TAG, "Network Error: ${e.message}", e)
                handleError("Gagal memuat rute. Cek koneksi internet.")
            } catch (e: Exception) {
                Log.e(TAG, "Unknown Error: ${e.message}", e)
                handleError("Terjadi kesalahan tidak terduga.")
            }
        }
    }

    // --- LOGIKA PEMBANTU ---

    private fun parseToMapsLatLng(url: String): MapsLatLng {
        val parts = url.split(",")
        return MapsLatLng(parts[0].toDouble(), parts[1].toDouble())
    }

    private fun handleError(message: String) {
        Toast.makeText(this@DeliveryMapsRoute, message, Toast.LENGTH_LONG).show()
    }

    // --- LOGIKA MENGGAMBAR PETA ---

    private fun drawRouteOnMap(result: DirectionsResult) {
        // Hanya ambil rute pertama (yang dioptimalkan oleh optimizeWaypoints(true))
        val route = result.routes[0]
        val overviewPolyline = route.overviewPolyline.decodePath()

        // Konversi hasil MapsLatLng (dari Directions Client) ke Maps SDK LatLng
        val routePoints = overviewPolyline.map {
            LatLng(it.lat, it.lng)
        }

        // --- TUGAS 1: GAMBAR POLYLINE DASAR BIRU ---
        mMap.addPolyline(
            PolylineOptions()
                .addAll(routePoints)
                .width(8f)
                .color(getColor(R.color.primary50))
        )

        // Sesuaikan kamera untuk mencakup seluruh rute
        val boundsBuilder = LatLngBounds.Builder()
        overviewPolyline.forEach { p ->
            boundsBuilder.include(LatLng(p.lat, p.lng))
        }

        try {
            val bounds = boundsBuilder.build()
            val padding = 150
            val updateCamera = CameraUpdateFactory.newLatLngBounds(bounds, padding)
            mMap.animateCamera(updateCamera)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Bounds tidak dapat dibangun setelah rute: ${e.message}")
        }
    }

    private fun addMarkersAndZoom() {
        val boundsBuilder = LatLngBounds.Builder()

        for ((index, lokasi) in daftarLokasi.withIndex()) {
            val point = LatLng(
                lokasi.mapsUrl.split(",")[0].toDouble(),
                lokasi.mapsUrl.split(",")[1].toDouble()
            )

            val markerColor: Int
            val markerTitle: String
            val markerIndex: Int = index

            when (index) {
                0 -> {
                    // Marker untuk Titik Awal (Origin) - Hijau
                    markerColor = getColor(R.color.status_active)
                    markerTitle = "START: ${lokasi.nama}"
                }

                daftarLokasi.lastIndex -> {
                    // Marker untuk Titik Akhir (Destination) - Merah
                    markerColor = getColor(R.color.blue_silver_lake)
                    markerTitle = "END: ${lokasi.nama}"
                }

                else -> {
                    // Marker untuk Waypoint - Biru Langit
                    markerColor = getColor(R.color.primary_300)
                    markerTitle = "Stop ${markerIndex}: ${lokasi.nama}"
                }
            }

            val customIcon = createCustomMarkerIcon(markerIndex, markerColor)

            mMap.addMarker(
                MarkerOptions()
                    .position(point)
                    .title(markerTitle)
                    .icon(customIcon)
                    .anchor(0.5f, 0.9f)
            )

            boundsBuilder.include(point)
        }

        if (daftarLokasi.isNotEmpty()) {
            try {
                val bounds = boundsBuilder.build()
                val padding = 150
                val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                mMap.animateCamera(cu)
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Bounds tidak dapat dibangun: ${e.message}")
            }
        }
    }

    // Fungsi Utilitas: Menggambar teks (nomor) di atas marker default dengan Border
    private fun createCustomMarkerIcon(index: Int, colorSolid: Int): BitmapDescriptor {

        val markerSize = 100 // Ukuran kotak bitmap (tetap sama)
        val bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // --- PENGATURAN UMUM ---
        val center = markerSize / 2f
        val circleRadius = 28f
        val textYOffset = 10f // Jarak teks dari tengah (agar sesuai dengan pin)

        // --- 1. SETUP PAINT UNTUK BORDER (STROKE) ---
        val backgroundPaint = Paint()
        backgroundPaint.color = getColor(R.color.white) // Warna background: Hitam
        backgroundPaint.style = Paint.Style.FILL // Penting: Menggambar garis luar
        backgroundPaint.isAntiAlias = true

        val borderPaint = Paint()
        borderPaint.color = getColor(R.color.darkLight) // Warna border: Hitam
        borderPaint.style = Paint.Style.STROKE // Penting: Menggambar garis luar
        borderPaint.strokeWidth = 3f // Ketebalan border (sesuaikan)
        borderPaint.isAntiAlias = true

        // --- 2. SETUP PAINT UNTUK ISI (FILL) ---
        val fillPaint = Paint()
        fillPaint.color = colorSolid // Warna isi (dari HUE)
        fillPaint.style = Paint.Style.FILL
        fillPaint.isAntiAlias = true

        // --- 3. GAMBAR BORDER ---
        canvas.drawCircle(
            center,
            center - textYOffset,
            circleRadius + 6f,
            backgroundPaint
        ) // Tambahkan sedikit ukuran agar border terlihat

        canvas.drawCircle(
            center,
            center - textYOffset,
            circleRadius + 6f,
            borderPaint
        ) // Tambahkan sedikit ukuran agar border terlihat

        // --- 4. GAMBAR ISI ---
        canvas.drawCircle(center, center - textYOffset, circleRadius, fillPaint)

        // --- 5. GAMBAR TEKS (NOMOR INDEX) ---
        val textPaint = Paint()
        textPaint.color = getColor(R.color.white) // Warna teks
        textPaint.textSize = 30f
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isAntiAlias = true

        val text =
            if (index == 0) "S" else if (index == daftarLokasi.lastIndex) "E" else index.toString()

        val xPos = center
        val yPos = (center - textYOffset) - ((textPaint.descent() + textPaint.ascent()) / 2f)

        canvas.drawText(text, xPos, yPos, textPaint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun tentukanTitikTerjauh(daftar: List<Lokasi>): Lokasi? {
        if (daftar.size < 2) return null

        val titikAwalStr = daftar[0].mapsUrl.split(",")
        val latAwal = titikAwalStr[0].trim().toDouble()
        val lonAwal = titikAwalStr[1].trim().toDouble()

        var titikTerjauh: Lokasi? = null
        var jarakMaksimum = -1f

        // Mulai iterasi dari indeks ke-1 (melewati titik awal)
        for (i in 1 until daftar.size) {
            val koordinatSekarang = daftar[i].mapsUrl.split(",")
            val latTujuan = koordinatSekarang[0].trim().toDouble()
            val lonTujuan = koordinatSekarang[1].trim().toDouble()

            // Hitung jarak menggunakan Location.distanceBetween
            val hasilJarak = FloatArray(1)
            Location.distanceBetween(latAwal, lonAwal, latTujuan, lonTujuan, hasilJarak)

            val jarakSekarang = hasilJarak[0]

            if (jarakSekarang > jarakMaksimum) {
                jarakMaksimum = jarakSekarang
                titikTerjauh = daftar[i]
            }
        }

        return titikTerjauh
    }

    fun urutkanRute(): List<Lokasi> {
        val waypointsArray = daftarLokasi.map { location -> location.mapsUrl }.toTypedArray()

        try {
            val context = GeoApiContext.Builder()
                .apiKey(API_KEY)
                .build()

            val request = DistanceMatrixApi.newRequest(context)
                .origins(daftarLokasi.first().mapsUrl)
                .destinations(*waypointsArray)
                .await()

            val elements = request.rows[0].elements

            for (i in daftarLokasi.indices) {
                daftarLokasi[i].jarakMeter = elements[i].distance.inMeters
            }

            return daftarLokasi.sortedBy { it.jarakMeter }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return daftarLokasi // Kembalikan list awal jika gagal
    }
}