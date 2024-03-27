package com.topmortar.topmortarsales.view.reports

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.recyclerview.ReportsRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_BA
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_COURIER
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_FULL_NAME
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_USER_ID
import com.topmortar.topmortarsales.commons.CONST_USER_LEVEL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.USER_KIND_BA
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.USER_KIND_PENAGIHAN
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityReportsBinding
import com.topmortar.topmortarsales.modal.DetailReportModal
import com.topmortar.topmortarsales.model.ReportVisitModel
import kotlinx.coroutines.launch
import java.util.Calendar

class ReportsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var binding: ActivityReportsBinding

    private val userID get() = sessionManager.userID().toString()
    private val userKind get() = sessionManager.userKind().toString()
    private val userDistributorId get() = sessionManager.userDistributor().toString()
    private var iUserID: String? = null
    private var contactID: String? = null
    private var contactName: String? = null
    private var userFullName: String? = null
    private var userLevel: String? = null
    private var isCourier = false
    private var isBA = false

    private lateinit var datePicker: DatePickerDialog
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar!!.hide()
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        iUserID = intent.getStringExtra(CONST_USER_ID)
        contactID = intent.getStringExtra(CONST_CONTACT_ID)
        contactName = intent.getStringExtra(CONST_NAME)
        userFullName = intent.getStringExtra(CONST_FULL_NAME)
        userLevel = intent.getStringExtra(CONST_USER_LEVEL)

        if (userKind == USER_KIND_COURIER) isCourier = true
        else if (userLevel == AUTH_LEVEL_COURIER) isCourier = true

        if (userKind == USER_KIND_BA) isBA = true
        else if (userLevel == AUTH_LEVEL_BA) isBA = true

        if (isCourier) binding.titleBarDark.tvTitleBar.text = if (!contactName.isNullOrEmpty()) contactName else "Laporan Kurir"
        else if (isBA) binding.titleBarDark.tvTitleBar.text = if (!contactName.isNullOrEmpty()) contactName else "Laporan BA"
        else binding.titleBarDark.tvTitleBar.text = if (!contactName.isNullOrEmpty()) contactName else "Laporan Sales"
        binding.titleBarDark.tvTitleBarDescription.visibility = View.VISIBLE
        if (!contactName.isNullOrEmpty()) binding.titleBarDark.tvTitleBarDescription.text = "Daftar laporan ${if (iUserID.isNullOrEmpty()) "saya" else ""} di toko ini"
        else binding.titleBarDark.tvTitleBarDescription.text = "Daftar laporan ${if (userFullName.isNullOrEmpty()) "" else "$userFullName"}"

        CustomUtility(this).setUserStatusOnline(true, userDistributorId, userID)

        setDatePickerDialog()
        initClickHandler()

    }

    private fun setDatePickerDialog() {

        datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, day)

                // Do something with the selected date
                val formattedDate = DateFormat.format(selectedDate)
                binding.tvFilter.text = formattedDate
                getList()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )

        binding.llFilter.setOnClickListener { datePicker.show() }

        // Set Default Selected Date
        val formattedDate = DateFormat.format(selectedDate)
        binding.tvFilter.text = formattedDate

        getList()

    }

    private fun getList() {
        loadingState(true)
        binding.llFilter.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val apiService: ApiService = HttpClient.create()
                val response = when (isCourier || isBA) {
                    true -> {
                        if (contactID.isNullOrEmpty()) {
                            apiService.listAllCourierReport(idUser = if (iUserID.isNullOrEmpty()) userID else iUserID!!)
                        } else apiService.listCourierReport(idUser = if (iUserID.isNullOrEmpty()) userID else iUserID!!, idGudang = contactID!!)
                    } else -> {
                        if (contactID.isNullOrEmpty()) {
                            apiService.listAllReport(idUser = if (iUserID.isNullOrEmpty()) userID else iUserID!!)
                        } else apiService.listReport(idUser = if (iUserID.isNullOrEmpty()) userID else iUserID!!, idContact = contactID!!)
                    }
                }

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            setRecyclerView(responseBody.results)
//                            binding.llFilter.visibility = View.VISIBLE
                            loadingState(false)

                        }
                        RESPONSE_STATUS_EMPTY -> {

                            loadingState(true, "Belum ada laporan.")
//                            binding.llFilter.visibility = View.VISIBLE

                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            val message = "Gagal memuat laporan! Message: ${ responseBody.message }"
                            handleMessage(this@ReportsActivity, TAG_RESPONSE_MESSAGE, message)
                            loadingState(true, message)
                            binding.llFilter.visibility = View.GONE

                        }
                        else -> {

                            val message = "Gagal memuat laporan!: ${ responseBody.message }"
                            handleMessage(this@ReportsActivity, TAG_RESPONSE_MESSAGE, message)
                            loadingState(true, message)
                            binding.llFilter.visibility = View.GONE

                        }
                    }

                } else {

                    val message = "Gagal memuat laporan! Error: " + response.message()
                    handleMessage(this@ReportsActivity, TAG_RESPONSE_MESSAGE, message)
                    loadingState(true, message)
                    binding.llFilter.visibility = View.GONE

                }

            } catch (e: Exception) {

                val message = "Failed run service. Exception " + e.message
                handleMessage(this@ReportsActivity, TAG_RESPONSE_MESSAGE, message)
                loadingState(true, message)
                binding.llFilter.visibility = View.GONE

            }

        }

    }

    private fun setRecyclerView(items: ArrayList<ReportVisitModel>) {

        val mAdapter = ReportsRecyclerViewAdapter()
        mAdapter.setList(items)
        if (isCourier || isBA) mAdapter.setIsCourier(true)
        else mAdapter.setIsCourier(false)
        if (contactID.isNullOrEmpty()) mAdapter.setWithName(true)

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@ReportsActivity, 2)
            adapter = mAdapter

            mAdapter.setOnItemClickListener(object : ReportsRecyclerViewAdapter.OnItemClickListener{
                override fun onItemClick(item: ReportVisitModel) {
                    val modalDetail = DetailReportModal(this@ReportsActivity)
                    modalDetail.setData(item)
                    if (isCourier || isBA) modalDetail.setIsCourier(true)
                    else modalDetail.setIsCourier(false)
                    if (contactID.isNullOrEmpty()) modalDetail.setWithName(true)
                    modalDetail.show()
                }

            })
        }
    }

    private fun loadingState(state: Boolean, message: String = getString(R.string.txt_loading)) {

        binding.txtLoading.text = message

        if (state) {

            binding.txtLoading.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE

        } else {

            binding.txtLoading.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE

        }

    }

    private fun initClickHandler() {

        binding.titleBarDark.icBack.setOnClickListener { finish() }

    }

    override fun onStart() {
        super.onStart()
//        EventBus.getDefault().register(this)
        Handler(Looper.getMainLooper()).postDelayed({
            if (sessionManager.isLoggedIn()) {
                if (sessionManager.userKind() == USER_KIND_COURIER || sessionManager.userKind() == USER_KIND_SALES || sessionManager.userKind() == USER_KIND_PENAGIHAN) {
                    CustomUtility(this).setUserStatusOnline(
                        true,
                        sessionManager.userDistributor().toString(),
                        sessionManager.userID().toString()
                    )
                }
            }
        }, 1000)
    }

    override fun onStop() {
        super.onStop()
//        EventBus.getDefault().unregister(this)
        if (sessionManager.isLoggedIn()) {
            if (sessionManager.userKind() == USER_KIND_COURIER || sessionManager.userKind() == USER_KIND_SALES || sessionManager.userKind() == USER_KIND_PENAGIHAN) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    sessionManager.userDistributor().toString(),
                    sessionManager.userID().toString()
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (sessionManager.isLoggedIn()) {
            if (sessionManager.userKind() == USER_KIND_COURIER || sessionManager.userKind() == USER_KIND_SALES || sessionManager.userKind() == USER_KIND_PENAGIHAN) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    sessionManager.userDistributor().toString(),
                    sessionManager.userID().toString()
                )
            }
        }
    }
}