@file:Suppress("DEPRECATION")

package com.topmortar.topmortarsales.view.suratJalan

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_COURIER
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_DISTANCE
import com.topmortar.topmortarsales.commons.CONST_INVOICE_ID
import com.topmortar.topmortarsales.commons.CONST_INVOICE_IS_COD
import com.topmortar.topmortarsales.commons.CONST_URI
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_DELIVERY
import com.topmortar.topmortarsales.commons.IMG_PREVIEW_STATE
import com.topmortar.topmortarsales.commons.IS_CLOSING
import com.topmortar.topmortarsales.commons.LOCATION_PERMISSION_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_SUCCESS
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.utils.CompressImageUtil.compressImage
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.model.DeliveryModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

@SuppressLint("SetTextI18n")
class PreviewClosingActivity : AppCompatActivity() {

    private lateinit var icBack: ImageView
    private lateinit var txtTitleBar: TextView

    private lateinit var imgPreview: ImageView
    private lateinit var bottomAction: LinearLayout
    private lateinit var btnUpload: Button

    private lateinit var sessionManager: SessionManager
    private val userID get() = sessionManager.userID()
    private val userDistributorId get() = sessionManager.userDistributor()
    private var imgUri: Uri? = null

    private var isLoading: Boolean = false
    private var isCod: Boolean = false
    private var invoiceId: String? = null
    private var contactId: String? = null
    private var imagePart: MultipartBody.Part? = null
    private var iDistance: Double = -1.0

    // Tracking
    private var firebaseReference: DatabaseReference? = null
    private var childDelivery: DatabaseReference? = null
    private var childDriver: DatabaseReference? = null
    private var deliveryId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)

        setContentView(R.layout.activity_preview_closing)

        if (sessionManager.userKind() == USER_KIND_COURIER) CustomUtility(this).setUserStatusOnline(true, userDistributorId ?: "-custom-017", "$userID")

        initVariable()
        initClickHandler()
        dataActivityValidation()

    }

    private fun executeClosing() {
        loadingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.closingInvoice(
                    invoiceId = createPartFromString(invoiceId!!),
                    distance = createPartFromString(iDistance.let { if (it >= 0) it.toString() else "" }),
                    image = imagePart!!
                )

                when (response.status) {
                    RESPONSE_STATUS_OK, RESPONSE_STATUS_SUCCESS -> {

                        createInvoice()
//                        if (!isCod) createInvoice()
//                        else onClosingFinished(response.message)

                    }

                    else -> {

                        handleMessage(
                            this@PreviewClosingActivity,
                            TAG_RESPONSE_CONTACT,
                            "Gagal closing. Error: ${response.message}"
                        )
                        loadingState(false)

                    }
                }

            } catch (e: Exception) {

                handleMessage(
                    this@PreviewClosingActivity,
                    TAG_RESPONSE_CONTACT,
                    "Failed run service. Exception " + e.message
                )
                loadingState(false)

            }

        }

    }

    private fun createInvoice() {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.addInvoice(invoiceId = createPartFromString(invoiceId!!))

                when (response.status) {
                    RESPONSE_STATUS_OK, RESPONSE_STATUS_SUCCESS -> {

                        onClosingFinished(response.message)

                    }

                    else -> {

                        handleMessage(
                            this@PreviewClosingActivity,
                            TAG_RESPONSE_CONTACT,
                            "Gagal closing. Error: ${response.message}"
                        )
                        loadingState(false)

                    }
                }

            } catch (e: Exception) {

                handleMessage(
                    this@PreviewClosingActivity,
                    TAG_RESPONSE_CONTACT,
                    "Failed run service. Exception " + e.message
                )
                loadingState(false)

            }

        }

    }

    private fun initVariable() {
        icBack = findViewById(R.id.ic_back)
        txtTitleBar = findViewById(R.id.tv_title_bar)

        imgPreview = findViewById(R.id.img_preview)
        bottomAction = findViewById(R.id.btn_bottom_action)
        btnUpload = bottomAction.findViewById(R.id.action1)

        // Setup Title Bar
        txtTitleBar.text = "Preview Closing"

        // Set Button Action
        if (sessionManager.userKind() == USER_KIND_COURIER) bottomAction.visibility = View.VISIBLE

    }

    private fun initClickHandler() {
        icBack.setOnClickListener { onBackHandler() }
//        btnUpload.setOnClickListener { onClosingFinished("Success to closing delivery!") }
        btnUpload.setOnClickListener { executeClosing() }

    }

    private fun dataActivityValidation() {
        val invoiceId = intent.getStringExtra(CONST_INVOICE_ID)
        contactId = intent.getStringExtra(CONST_CONTACT_ID)
        isCod = intent.getBooleanExtra(CONST_INVOICE_IS_COD, false)
        iDistance = intent.getDoubleExtra(CONST_DISTANCE, -1.0)

        if (!invoiceId.isNullOrEmpty()) {
            this.invoiceId = invoiceId
        }

        val imgUris = intent.getParcelableArrayListExtra<Uri>(CONST_URI)

        if (!imgUris.isNullOrEmpty()) {

            imgPreview.setImageURI(imgUris[0])
            imgUri = compressImage(this@PreviewClosingActivity, imgUris[0], 50)

            val contentResolver = contentResolver
            val inputStream = contentResolver.openInputStream(imgUri!!)
            val byteArray = inputStream?.readBytes()

            if (byteArray != null) {
                val requestFile: RequestBody =
                    RequestBody.create("image/*".toMediaTypeOrNull(), byteArray)
                imagePart = MultipartBody.Part.createFormData("pic", "image.jpg", requestFile)
            } else handleMessage(this, TAG_RESPONSE_CONTACT, "Gambar tidak ditemukan")

        }

    }

    private fun loadingState(state: Boolean) {
        if (state) {
            isLoading = true
            btnUpload.text = getString(R.string.txt_loading)

        } else {
            isLoading = false
            btnUpload.text = "Closing Sekarang"

        }

    }

    private fun onBackHandler() {
        if (!isLoading) {
            finish()

        } else {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Tunggu sebentar. Proses closing masih berjalan.")
                .setPositiveButton("Oke") { dialog, _ -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()

        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        onBackHandler()
    }

    private fun onClosingFinished(message: String) {

        deliveryId = "$AUTH_LEVEL_COURIER$userID"
        val userDistributorIds = sessionManager.userDistributor()
        firebaseReference = FirebaseUtils().getReference(distributorId = userDistributorIds ?: "-firebase-015")
        childDelivery = firebaseReference?.child(FIREBASE_CHILD_DELIVERY)
        childDriver = childDelivery?.child(deliveryId)

        childDriver?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {

                    val stores = snapshot.child("stores/$contactId")
                    if (stores.exists()) {

                        val courier = snapshot.child("courier").getValue(DeliveryModel.Courier::class.java)
                        val store = stores.getValue(DeliveryModel.Store::class.java)
//                        Log.d("Item to closing", "$latLng")
//                        Log.d("Item to closing", "$courier")
//                        Log.d("Item to closing", "$store")

                        val endDateTime = DateFormat.now()

                        var endLat = "0.0"
                        var endLng = "0.0"
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@PreviewClosingActivity)
                        if (ActivityCompat.checkSelfPermission(
                                this@PreviewClosingActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                this@PreviewClosingActivity,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(this@PreviewClosingActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
                        } else {
                            fusedLocationClient.lastLocation
                                .addOnSuccessListener { location: Location? ->
                                    if (location != null) {
                                        endLat = location.latitude.toString()
                                        endLng = location.longitude.toString()
                                    }

                                    lifecycleScope.launch {
                                        try {

                                            val apiService: ApiService = HttpClient.create()

                                            val response = apiService.saveDelivery(
                                                lat = createPartFromString(store?.lat.toString()),
                                                lng = createPartFromString(store?.lng.toString()),
                                                endDateTime = createPartFromString(endDateTime),
                                                endLat = createPartFromString(endLat),
                                                endLng = createPartFromString(endLng),
                                                startDateTime = createPartFromString(store?.startDatetime.toString()),
                                                startLat = createPartFromString(store?.startLat.toString()),
                                                startLng = createPartFromString(store?.startLng.toString()),
                                                idCourier = createPartFromString(courier?.id.toString()),
                                                idContact = createPartFromString(store?.id.toString()),
                                            )

                                            when (response.status) {
                                                RESPONSE_STATUS_OK, RESPONSE_STATUS_SUCCESS -> {

                                                    finishClosing(message)
                                                    Log.d("Item to closing", "Success save item")

                                                }

                                                else -> {

                                                    finishClosing(message)
                                                    Log.d("Item to closing", "Failed to save item: " + response.message)

                                                }
                                            }

                                        } catch (e: Exception) {

                                            finishClosing(message)
                                            Log.d("Item to closing", "Failed to save item: Error request. " + e.message)

                                        }

                                    }
                                }.addOnFailureListener {
                                    lifecycleScope.launch {
                                        try {

                                            val apiService: ApiService = HttpClient.create()

                                            val response = apiService.saveDelivery(
                                                lat = createPartFromString(store?.lat.toString()),
                                                lng = createPartFromString(store?.lng.toString()),
                                                endDateTime = createPartFromString(endDateTime),
                                                endLat = createPartFromString(endLat),
                                                endLng = createPartFromString(endLng),
                                                startDateTime = createPartFromString(store?.startDatetime.toString()),
                                                startLat = createPartFromString(store?.startLat.toString()),
                                                startLng = createPartFromString(store?.startLng.toString()),
                                                idCourier = createPartFromString(courier?.id.toString()),
                                                idContact = createPartFromString(store?.id.toString()),
                                            )

                                            when (response.status) {
                                                RESPONSE_STATUS_OK, RESPONSE_STATUS_SUCCESS -> {

                                                    finishClosing(message)
                                                    Log.d("Item to closing", "Success save item: With error listening location. " + it.message)

                                                }

                                                else -> {

                                                    finishClosing(message)
                                                    Log.d("Item to closing", "Failed to save item: " + response.message)

                                                }
                                            }

                                        } catch (e: Exception) {

                                            finishClosing(message)
                                            Log.d("Item to closing", "Failed to save item: Error request. " + e.message)

                                        }

                                    }
                                }
                        }

//                        childDelivery?.child("$deliveryId/stores/$contactId")?.removeValue()

//                        childDriver?.child("stores")?.addListenerForSingleValueEvent(object : ValueEventListener {
//                            override fun onDataChange(dataSnapshot: DataSnapshot) {
////                                val isTracking = CustomUtility(this@PreviewClosingActivity).isServiceRunning(TrackingService::class.java)
////                                if (!dataSnapshot.exists() && isTracking) {
////                                    val serviceIntent = Intent(this@PreviewClosingActivity, TrackingService::class.java)
////                                    this@PreviewClosingActivity.stopService(serviceIntent)
////                                }
//                                finishClosing(message)
//                            }
//
//                            override fun onCancelled(error: DatabaseError) {
//                                finishClosing(message)
//                            }
//
//                        })

                    } else {
                        finishClosing(message)
                    }

                } else {
                    finishClosing(message)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                finishClosing(message)
            }

        })

    }

    private fun finishClosing(message: String) {

        val resultIntent = Intent()
        resultIntent.putExtra("$IMG_PREVIEW_STATE", RESULT_OK)
        resultIntent.putExtra(IS_CLOSING, true)
        setResult(RESULT_OK, resultIntent)

        childDelivery?.child("$deliveryId/stores/$contactId")?.removeValue()

        handleMessage(this@PreviewClosingActivity, TAG_RESPONSE_CONTACT, message)
        Handler(Looper.getMainLooper()).postDelayed({
            loadingState(false)
            finish()
        }, 1000)

    }

    override fun onStart() {
        super.onStart()
        Handler(Looper.getMainLooper()).postDelayed({
            if (sessionManager.userKind() == USER_KIND_COURIER) CustomUtility(this).setUserStatusOnline(true, userDistributorId ?: "-custom-017", "$userID")
        }, 1000)
    }

    override fun onStop() {
        super.onStop()
        if (sessionManager.userKind() == USER_KIND_COURIER) CustomUtility(this).setUserStatusOnline(false, userDistributorId ?: "-custom-017", "$userID")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (sessionManager.userKind() == USER_KIND_COURIER) CustomUtility(this).setUserStatusOnline(false, userDistributorId ?: "-custom-017", "$userID")
    }

}