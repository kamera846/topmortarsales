package com.topmortar.topmortarsales.view.courier

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.CONST_IS_BASE_CAMP
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_CITY_ID
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_NAME
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_STATUS
import com.topmortar.topmortarsales.commons.CONST_NEAREST_STORE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_ACTION_MAIN_ACTIVITY
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityHomeCourierBinding
import com.topmortar.topmortarsales.view.MapsActivity
import com.topmortar.topmortarsales.view.user.UserProfileActivity
import kotlinx.coroutines.launch

class HomeCourierActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeCourierBinding

    private lateinit var sessionManager: SessionManager
    private val userId get() = sessionManager.userID()
    private val userCity get() = sessionManager.userCityID()
    private val userFullName get() = sessionManager.fullName()
    private val userDistributorId get() = sessionManager.userDistributor()
    private val userDistributorNumber get() = sessionManager.userDistributor()

    private var doubleBackToExitPressedOnce = false
    private var isAbsentNow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        binding = ActivityHomeCourierBinding.inflate(layoutInflater)
        sessionManager = SessionManager(this)

        setContentView(binding.root)

        initView()
    }

    private fun initView() {

        binding.fullName.text = userFullName

        binding.btnAbsent.setOnClickListener { checkAbsent() }
        binding.deliveryItem.setOnClickListener { if (isAbsentNow) navigateToDelivery() }
        binding.nearestStoreItem.setOnClickListener { if (isAbsentNow) navigateToNearestStore() }
        binding.basecampItem.setOnClickListener { if (isAbsentNow) navigateToBasecamp() }
        binding.nearestBasecampItem.setOnClickListener { if (isAbsentNow) navigateToNearestBasecamp() }
        binding.myProfileItem.setOnClickListener { navigateToMyProfile() }
        binding.contactAdminItem.setOnClickListener { navigateToContactAdmin() }

        lockMenuItem(true)

    }

    private fun checkAbsent() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage(getString(R.string.txt_loading))
        progressDialog.setCancelable(false)
        progressDialog.show()

        Handler().postDelayed({

            lockMenuItem(isAbsentNow)
            progressDialog.dismiss()

        }, 2000)
    }

    private fun lockMenuItem(state: Boolean) {
        isAbsentNow = !state

        binding.deliveryItem.alpha = if (state) 0.5f else 1f
        binding.nearestStoreItem.alpha = if (state) 0.5f else 1f
        binding.basecampItem.alpha = if (state) 0.5f else 1f
        binding.nearestBasecampItem.alpha = if (state) 0.5f else 1f
//        binding.myProfileItem.alpha = if (state) 0.5f else 1f
//        binding.contactAdminItem.alpha = if (state) 0.5f else 1f

        binding.deliveryItemChevron.setImageResource(if (state) R.drawable.lock_dark else R.drawable.chevron_right_dark)
        binding.nearestStoreItemChevron.setImageResource(if (state) R.drawable.lock_dark else R.drawable.chevron_right_dark)
        binding.basecampItemChevron.setImageResource(if (state) R.drawable.lock_dark else R.drawable.chevron_right_dark)
        binding.nearestBasecampItemChevron.setImageResource(if (state) R.drawable.lock_dark else R.drawable.chevron_right_dark)
//        binding.myProfileItemChevron.setImageResource(if (state) R.drawable.lock_dark else R.drawable.chevron_right_dark)
//        binding.contactAdminItemChevron.setImageResource(if (state) R.drawable.lock_dark else R.drawable.chevron_right_dark)

        binding.absentTitle.text = if (state) {
            "Yuk, Catat Kehadiranmu Hari ini!"
        } else {
            "Kehadiranmu Telah Tercatat!"
        }
        binding.absentDescription.text = if (state) {
            "Absenmu penting! Jangan lupa untuk mencatat kehadiranmu sekarang dan ciptakan jejak kerja yang positif."
        } else {
            "Terima kasih sudah mencatat kehadiran hari ini dan jangan lupa untuk mencatat absen pulang juga nanti!"
        }

        binding.btnAbsent.backgroundTintList = ContextCompat.getColorStateList(this, if (state) R.color.status_bid else R.color.red_claret)
        binding.btnAbsent.text = if (state) "Absen Sekarang" else "Pulang Sekarang"

    }

    private fun navigateToDelivery() {
        val intent = Intent(this, CourierActivity::class.java)
        intent.putExtra("tabIndex", 0)
        startActivity(intent)
    }

    private fun navigateToNearestStore() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Memuat data toko…")
        progressDialog.show()

        Handler().postDelayed({

            lifecycleScope.launch {
                try {

                    val apiService: ApiService = HttpClient.create()
                    val response = apiService.getCourierStore(processNumber = "1", courierId = userId ?: "0")

                    when (response.status) {
                        RESPONSE_STATUS_OK -> {

                            val listCoordinate = arrayListOf<String>()
                            val listCoordinateName = arrayListOf<String>()
                            val listCoordinateStatus = arrayListOf<String>()
                            val listCoordinateCityID = arrayListOf<String>()

                            for (item in response.results.listIterator()) {
                                listCoordinate.add(item.maps_url)
                                listCoordinateName.add(item.nama)
                                listCoordinateStatus.add(item.store_status)
                                listCoordinateCityID.add(item.id_city)
                            }

                            val intent = Intent(this@HomeCourierActivity, MapsActivity::class.java)

                            intent.putExtra(CONST_NEAREST_STORE, true)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_STATUS, listCoordinateStatus)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_CITY_ID, listCoordinateCityID)

                            progressDialog.dismiss()
                            startActivity(intent)

                        }
                        RESPONSE_STATUS_EMPTY -> {

                            val listCoordinate = arrayListOf<String>()
                            val listCoordinateName = arrayListOf<String>()

                            val intent = Intent(this@HomeCourierActivity, MapsActivity::class.java)

                            intent.putExtra(CONST_NEAREST_STORE, true)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)

                            progressDialog.dismiss()
                            startActivity(intent)

                        }
                        else -> {

                            handleMessage(this@HomeCourierActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                            progressDialog.dismiss()

                        }
                    }


                } catch (e: Exception) {

                    handleMessage(this@HomeCourierActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                    progressDialog.dismiss()

                }

            }

        }, 1000)
    }

    private fun navigateToBasecamp() {
        val intent = Intent(this, CourierActivity::class.java)
        intent.putExtra("tabIndex", 1)
        startActivity(intent)
    }

    private fun navigateToNearestBasecamp() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Memuat data basecamp…")
        progressDialog.show()

        Handler().postDelayed({

            lifecycleScope.launch {
                try {

                    val apiService: ApiService = HttpClient.create()
                    val response = apiService.getListBaseCamp(cityId = userCity ?: "0", distributorID = userDistributorId ?: "0")

                    when (response.status) {
                        RESPONSE_STATUS_OK -> {

                            val listCoordinate = arrayListOf<String>()
                            val listCoordinateName = arrayListOf<String>()
                            val listCoordinateStatus = arrayListOf<String>()
                            val listCoordinateCityID = arrayListOf<String>()

                            for (item in response.results.listIterator()) {
                                listCoordinate.add(item.location_gudang)
                                listCoordinateName.add(item.nama_gudang)
                                listCoordinateStatus.add("blacklist")
                                listCoordinateCityID.add(item.id_city)
                            }

                            val intent = Intent(this@HomeCourierActivity, MapsActivity::class.java)

                            intent.putExtra(CONST_NEAREST_STORE, true)
                            intent.putExtra(CONST_IS_BASE_CAMP, true)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_STATUS, listCoordinateStatus)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_CITY_ID, listCoordinateCityID)

                            progressDialog.dismiss()
                            startActivity(intent)

                        }
                        RESPONSE_STATUS_EMPTY -> {

                            val listCoordinate = arrayListOf<String>()
                            val listCoordinateName = arrayListOf<String>()

                            val intent = Intent(this@HomeCourierActivity, MapsActivity::class.java)

                            intent.putExtra(CONST_NEAREST_STORE, true)
                            intent.putExtra(CONST_IS_BASE_CAMP, true)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)

                            progressDialog.dismiss()
                            startActivity(intent)

                        }
                        else -> {

                            handleMessage(this@HomeCourierActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                            progressDialog.dismiss()

                        }
                    }


                } catch (e: Exception) {

                    handleMessage(this@HomeCourierActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                    progressDialog.dismiss()

                }

            }

        }, 1000)
    }

    private fun navigateToMyProfile() {
        val intent = Intent(this, UserProfileActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToContactAdmin() {
        val distributorNumber = userDistributorNumber
        val phoneNumber = if (!distributorNumber.isNullOrEmpty()) distributorNumber else getString(R.string.topmortar_wa_number)
        val message = "*#Courier Service*\nHalo admin, tolong bantu saya [KETIK PESAN ANDA]"

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}")

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Gagal mengarahkan ke whatsapp", TOAST_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        doubleBackToExitPressedOnce = true
        handleMessage(this, TAG_ACTION_MAIN_ACTIVITY, "Tekan sekali lagi untuk keluar!", TOAST_SHORT)

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }
}