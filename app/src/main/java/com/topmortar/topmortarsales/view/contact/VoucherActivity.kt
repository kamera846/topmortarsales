@file:Suppress("DEPRECATION")

package com.topmortar.topmortarsales.view.contact

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.recyclerview.VoucherRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_IS_BASE_CAMP
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_MAPS_NAME
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.LOCATION_PERMISSION_REQUEST_CODE
import com.topmortar.topmortarsales.commons.MAX_REPORT_DISTANCE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN_CITY
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.USER_KIND_PENAGIHAN
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.URLUtility
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityVoucherBinding
import com.topmortar.topmortarsales.modal.AddVoucherModal
import com.topmortar.topmortarsales.model.VoucherModel
import com.topmortar.topmortarsales.view.MapsActivity
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
class VoucherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoucherBinding
    private lateinit var apiService: ApiService
    private var idContact = ""
    private var contactName = ""
    private var contactMapsUrl = ""
    private var isDistanceToLong = false
    private var shortDistance = ""
    private var voucherModal: AddVoucherModal? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var sessionManager: SessionManager
    private val userKind get() = sessionManager.userKind().toString()
    private val userDistributor get() = sessionManager.userDistributor().toString()
    private val userDistributors get() = sessionManager.userDistributor()
    private val userId get() = sessionManager.userID().toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge()

        binding = ActivityVoucherBinding.inflate(layoutInflater)
        sessionManager = SessionManager(this)
        setContentView(binding.root)

        apiService = HttpClient.create()

        idContact = intent.getStringExtra(CONST_CONTACT_ID).toString()
        contactName = intent.getStringExtra(CONST_NAME).toString()
        contactMapsUrl = intent.getStringExtra(CONST_MAPS).toString()

        binding.titleBarDark.icBack.setOnClickListener { finish() }
        binding.titleBarDark.tvTitleBar.text = "Daftar Voucher"
        binding.titleBarDark.tvTitleBarDescription.text = "Toko $contactName"
        binding.titleBarDark.tvTitleBarDescription.visibility = if (contactName.isNotEmpty()) View.VISIBLE else View.GONE

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (userKind == USER_KIND_COURIER || userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN) {
            CustomUtility(this).setUserStatusOnline(true, userDistributors ?: "-custom-005", userId)
        }

        getList()
        binding.swipeRefreshLayout.setOnRefreshListener { getList() }

    }

    private fun getList() {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val response = apiService.listVoucher(idContact = idContact)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        setRecyclerView(response.results)
                        loadingState(false)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Belum ada voucher!")

                    }
                    else -> {

                        handleMessage(this@VoucherActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        loadingState(true, getString(R.string.failed_request))

                    }
                }

            } catch (e: Exception) {

                handleMessage(this@VoucherActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))

            }

        }

    }

    private fun setRecyclerView(listItem: ArrayList<VoucherModel>) {
        val rvAdapter = VoucherRecyclerViewAdapter(listItem, object: VoucherRecyclerViewAdapter.ItemClickListener {
            override fun onItemClick(data: VoucherModel?) {
                // Do Something

                if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_ADMIN_CITY) {
                    voucherModal = null
                    voucherModal = AddVoucherModal(this@VoucherActivity, lifecycleScope)
                    voucherModal!!.setEditCase(true, data)
                    voucherModal!!.setVoucherId(data!!.id_voucher)
                    voucherModal!!.setContactCoordinate(shortDistance)
                    voucherModal!!.initializeInterface(object: AddVoucherModal.AddVoucherModalInterface {
                        override fun onSubmit(status: Boolean) {
                            // Do Something
                            if (status) getList()
                        }

                    })
                    voucherModal!!.show()
                } else setupShowModal(data!!)
            }

        })

        binding.rvList.apply {
            layoutManager = LinearLayoutManager(this@VoucherActivity)
            adapter = rvAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                private var lastScrollPosition = 0

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy < 0) {
                        // Scrolled up
                        val firstVisibleItemPosition =
                            (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        if (lastScrollPosition != firstVisibleItemPosition) {
                            recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition)?.itemView?.startAnimation(
                                AnimationUtils.loadAnimation(
                                    recyclerView.context,
                                    R.anim.rv_item_fade_slide_down
                                )
                            )
                            lastScrollPosition = firstVisibleItemPosition
                        }
                    } else lastScrollPosition = -1
                }
            })
        }
    }

    private fun loadingState(state: Boolean, message: String = getString(R.string.txt_loading)) {

        binding.txtLoading.text = message

        if (state) {

            binding.txtLoading.visibility = View.VISIBLE
            binding.rvList.visibility = View.GONE

            binding.swipeRefreshLayout.isRefreshing = message === getString(R.string.txt_loading)

        } else {

            binding.txtLoading.visibility = View.GONE
            binding.rvList.visibility = View.VISIBLE

            binding.swipeRefreshLayout.isRefreshing = false

        }

    }

    private fun setupShowModal(data: VoucherModel) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Sedang menghitung jarak...")
        progressDialog.show()

        Handler(Looper.getMainLooper()).postDelayed({

            val mapsUrl = contactMapsUrl
            val urlUtility = URLUtility(this)

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (urlUtility.isLocationEnabled(this)) {

                    urlUtility.requestLocationUpdate()

                    if (!urlUtility.isUrl(mapsUrl) && mapsUrl.isNotEmpty()) {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location ->

                            // Courier Location
                            val currentLatitude = location.latitude
                            val currentLongitude = location.longitude

                            // Store Location
                            val contactMapsUrl = mapsUrl.split(",")
                            val latitude = contactMapsUrl[0].toDoubleOrNull()
                            val longitude = contactMapsUrl[1].toDoubleOrNull()

                            if (latitude != null && longitude != null) {

                                // Calculate Distance
                                val distance = urlUtility.calculateDistance(currentLatitude, currentLongitude, latitude, longitude)
                                shortDistance = "%.3f".format(distance)

                                if (distance > MAX_REPORT_DISTANCE) {
                                    val builder = AlertDialog.Builder(this)
                                    builder.setCancelable(false)
                                    builder.setOnDismissListener { progressDialog.dismiss() }
                                    builder.setOnCancelListener { progressDialog.dismiss() }
                                    builder.setTitle("Peringatan!")
                                        .setMessage("Titik anda saat ini $shortDistance km dari titik toko. Cobalah untuk lebih dekat dengan toko!")
                                        .setPositiveButton("Oke") { dialog, _ ->
                                            progressDialog.dismiss()
                                            isDistanceToLong = true
                                            dialog.dismiss()
                                        }
                                        .setNegativeButton("Buka Maps") { dialog, _ ->
                                            val intent = Intent(this@VoucherActivity, MapsActivity::class.java)
                                            intent.putExtra(CONST_IS_BASE_CAMP, false)
                                            intent.putExtra(CONST_MAPS, mapsUrl)
                                            intent.putExtra(CONST_MAPS_NAME, contactName)
                                            startActivity(intent)

                                            progressDialog.dismiss()
                                            isDistanceToLong = true

                                            dialog.dismiss()
                                        }
                                    builder.show()
                                } else {
                                    progressDialog.dismiss()
                                    isDistanceToLong = false

                                    voucherModal = null
                                    voucherModal = AddVoucherModal(this@VoucherActivity, lifecycleScope)
                                    voucherModal!!.setEditCase(true, data)
                                    voucherModal!!.setVoucherId(data.id_voucher)
                                    voucherModal!!.setContactCoordinate(shortDistance)
                                    voucherModal!!.initializeInterface(object: AddVoucherModal.AddVoucherModalInterface {
                                        override fun onSubmit(status: Boolean) {
                                            // Do Something
                                            if (status) getList()
                                        }

                                    })
                                    voucherModal!!.show()
                                }

                            } else {
                                progressDialog.dismiss()
                                Toast.makeText(this, "Gagal memproses koordinat", TOAST_SHORT).show()
                            }

                        }.addOnFailureListener {
                            progressDialog.dismiss()
                            handleMessage(this, "LOG REPORT", "Gagal mendapatkan lokasi anda. Err: " + it.message)
//                            Toast.makeText(this, "Gagal mendapatkan lokasi anda", TOAST_SHORT).show()
                        }

                    } else {
                        progressDialog.dismiss()
                        val message = "Anda tidak dapat membuat laporan untuk saat ini, silakan hubungi admin untuk memperbarui koordinat toko ini"
                        val actionTitle = "Hubungi Sekarang"
                        val customUtility = CustomUtility(this@VoucherActivity)
                        customUtility.showPermissionDeniedSnackbar(message, actionTitle) {
                            val messageText = "*#Courier Service*\nHalo admin, tolong bantu saya untuk memperbarui koordinat pada toko *${ contactName }*"
                            val distributorNumber = SessionManager(this).userDistributorNumber().toString()
                            customUtility.navigateChatAdmin(messageText, distributorNumber)
                        }
                    }

                } else {
                    progressDialog.dismiss()
                    val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(enableLocationIntent)
                }

            } else {
                progressDialog.dismiss()
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            }

        }, 500)
    }

    override fun onStart() {
        super.onStart()
        Handler(Looper.getMainLooper()).postDelayed({
            if (userKind == USER_KIND_COURIER || userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN) {
                CustomUtility(this).setUserStatusOnline(true, userDistributors ?: "-custom-005", userId)
            }
        }, 1000)
    }

    override fun onStop() {
        super.onStop()
        if (userKind == USER_KIND_COURIER || userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN) {
            CustomUtility(this).setUserStatusOnline(false, userDistributors ?: "-custom-005", userId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (userKind == USER_KIND_COURIER || userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN) {
            CustomUtility(this).setUserStatusOnline(false, userDistributors ?: "-custom-005", userId)
        }
    }
}