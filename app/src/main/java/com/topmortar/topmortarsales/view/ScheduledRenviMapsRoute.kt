package com.topmortar.topmortarsales.view

// Impor dari Google Maps Services Java Client
// Gunakan alias untuk menghindari konflik nama LatLng

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.google.maps.GeoApiContext
import com.google.maps.errors.ApiException
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import com.topmortar.topmortarsales.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import com.google.maps.model.LatLng as MapsLatLng

// --- 1. DEFINISI DATA KONSTAN ---

data class RouteInstruction(
    val instruction: String,
    val distance: String,
    val duration: String
)

data class Lokasi(
    val nama: String,
    val mapsUrl: String // Format: "lintang,bujur"
)

val daftarLokasi = listOf(
    Lokasi("TOP MORTAR LAWANG", "-7.8676267,112.694805"),
    Lokasi("TB Tunggal Jaya", "-7.9644144,112.6693"),
    Lokasi("Tb Dani Keramik", "-7.911684698517194,112.63951946049929"),
    Lokasi("TB Dinoyo jaya", "-7.94391591693179,112.61096231639385"),
    Lokasi("tb jaya makmur ngenep", "-7.89339,112.6213683"),
    Lokasi("TK RAKHA", "-7.959005900000001,112.6276244"),
    Lokasi("TB TRI MAKMUR", "-7.898564816807956,112.63045459985733"),
    Lokasi("UD Candi Agung", "-7.943957092215743,112.69544385373592"),
    Lokasi("TB DHINA JAYA", "-7.897313499999999,112.6338865"),
    Lokasi("TOKO BARU", "-7.906576538920516,112.63972330838442"),
    Lokasi("UD Dwi Jaya", "-7.969576399999999,112.657388"),
    Lokasi("TB Aries Jaya", "-7.9333736,112.6138548"),
    Lokasi("TB Sriwijaya", "-7.905114681059461,112.62231510132551"),
    Lokasi("TB ANAM", "-7.959818234761582,112.63068426400424"),
    Lokasi("Sutiwijaya", "-7.959007700000001,112.6473347"),
    Lokasi("TB BOSS", "-7.900759499999998,112.6037699"),

)

const val API_KEY = "AIzaSyBNtWfOO3C9WWbyf_jBtqRCTgKc7HUW-40"
const val TAG = "DirectionsClient"

class ScheduledRenviMapsRoute : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    // Inisialisasi GeoApiContext
    private val geoApiContext: GeoApiContext by lazy {
        GeoApiContext.Builder()
            .apiKey(API_KEY)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scheduled_renvi_maps_route)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (daftarLokasi.size < 2) {
            Toast.makeText(this, "Minimal 2 lokasi diperlukan.", Toast.LENGTH_LONG).show()
            return
        }

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
                    .optimizeWaypoints(true) // Meminta Google mengoptimalkan urutan Waypoints
                    .await() // Melakukan panggilan sinkron (blocking) di background thread

                // 4. Update UI di Main Thread
                withContext(Dispatchers.Main) {
                    if (directionsResult.routes.isNotEmpty()) {
                        drawRouteOnMap(directionsResult)
                    } else {
                        Toast.makeText(this@ScheduledRenviMapsRoute, "Tidak ada rute ditemukan.", Toast.LENGTH_LONG).show()
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
        Toast.makeText(this@ScheduledRenviMapsRoute, message, Toast.LENGTH_LONG).show()
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
            val point = LatLng(lokasi.mapsUrl.split(",")[0].toDouble(), lokasi.mapsUrl.split(",")[1].toDouble())

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
        canvas.drawCircle(center, center - textYOffset, circleRadius + 6f, backgroundPaint) // Tambahkan sedikit ukuran agar border terlihat

        canvas.drawCircle(center, center - textYOffset, circleRadius + 6f, borderPaint) // Tambahkan sedikit ukuran agar border terlihat

        // --- 4. GAMBAR ISI ---
        canvas.drawCircle(center, center - textYOffset, circleRadius, fillPaint)

        // --- 5. GAMBAR TEKS (NOMOR INDEX) ---
        val textPaint = Paint()
        textPaint.color = getColor(R.color.white) // Warna teks
        textPaint.textSize = 30f
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isAntiAlias = true

        val text = if (index == 0) "S" else if (index == daftarLokasi.lastIndex) "E" else index.toString()

        val xPos = center
        val yPos = (center - textYOffset) - ((textPaint.descent() + textPaint.ascent()) / 2f)

        canvas.drawText(text, xPos, yPos, textPaint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}