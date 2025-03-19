package com.topmortar.topmortarsales.view.reports

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.recyclerview.ReportsRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.ALL_REPORT
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_BA
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_COURIER
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_PENAGIHAN
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_FULL_NAME
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_USER_ID
import com.topmortar.topmortarsales.commons.CONST_USER_LEVEL
import com.topmortar.topmortarsales.commons.LAYOUT_GRID
import com.topmortar.topmortarsales.commons.LAYOUT_ROW
import com.topmortar.topmortarsales.commons.NORMAL_REPORT
import com.topmortar.topmortarsales.commons.PENAGIHAN_REPORT_RENVI
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.SALES_REPORT_RENVI
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.USER_KIND_BA
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.USER_KIND_PENAGIHAN
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityReportsBinding
import com.topmortar.topmortarsales.modal.DetailReportModal
import com.topmortar.topmortarsales.model.ReportVisitModel
import com.topmortar.topmortarsales.view.rencanaVisits.HomeSalesActivity
import kotlinx.coroutines.launch
import java.util.Calendar

class ReportsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var binding: ActivityReportsBinding

    private val userID get() = sessionManager.userID().toString()
    private val userKind get() = sessionManager.userKind().toString()
    private val userDistributorId get() = sessionManager.userDistributor().toString()
    private val userDistributorIds get() = sessionManager.userDistributor()
    private var iUserID: String? = null
    private var contactID: String? = null
    private var contactName: String? = null
    private var userFullName: String? = null
    private var userLevel: String? = null
    private var isCourier = false
    private var isBA = false
    private var isPenagihan = false
    private var activeFilter = ALL_REPORT
    private val layoutStatus get() = sessionManager.layoutReportStatus()

    private lateinit var datePicker: DatePickerDialog
    private var selectedDate: Calendar = Calendar.getInstance()

    private var notificationOpened = false
    private var nTargetIntent: String? = null
    private var nVisitId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar!!.hide()
        applyMyEdgeToEdge()

        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        iUserID = intent.getStringExtra(CONST_USER_ID)
        contactID = intent.getStringExtra(CONST_CONTACT_ID)
        contactName = intent.getStringExtra(CONST_NAME)
        userFullName = intent.getStringExtra(CONST_FULL_NAME)
        userLevel = intent.getStringExtra(CONST_USER_LEVEL)

        val isPendingIntent = intent.getStringExtra("notification_intent")
        if (isPendingIntent != null) nTargetIntent = isPendingIntent.toString()
        val visitId = intent.getStringExtra("nVisitId")
        if (visitId != null) nVisitId = visitId.toString()

        if (userKind == USER_KIND_COURIER) isCourier = true
        else if (userLevel == AUTH_LEVEL_COURIER) isCourier = true

        if (userKind == USER_KIND_BA) isBA = true
        else if (userLevel == AUTH_LEVEL_BA) isBA = true

        if (userKind == USER_KIND_PENAGIHAN) isPenagihan = true
        else if (userLevel == AUTH_LEVEL_PENAGIHAN) isPenagihan = true

        if (isCourier) binding.titleBarDark.tvTitleBar.text = if (!contactName.isNullOrEmpty()) contactName else "Laporan Kurir"
        else if (isBA) binding.titleBarDark.tvTitleBar.text = if (!contactName.isNullOrEmpty()) contactName else "Laporan BA"
        else if (isPenagihan) binding.titleBarDark.tvTitleBar.text = if (!contactName.isNullOrEmpty()) contactName else "Laporan Penagihan"
        else binding.titleBarDark.tvTitleBar.text = if (!contactName.isNullOrEmpty()) contactName else "Laporan Sales"
        binding.titleBarDark.tvTitleBarDescription.visibility = View.VISIBLE
//        println("Laporan Visit: Contact Name $contactName")
//        println("Laporan Visit: User ID $iUserID")
//        println("Laporan Visit: User Full Name $userFullName")
        if (!contactName.isNullOrEmpty()) binding.titleBarDark.tvTitleBarDescription.text = "Daftar laporan ${if (iUserID.isNullOrEmpty()) "saya" else "$userFullName"} di toko ini"
        else binding.titleBarDark.tvTitleBarDescription.text = "Daftar laporan ${if (userFullName.isNullOrEmpty()) "" else "$userFullName"}"

        if (userKind != USER_KIND_COURIER && userLevel != AUTH_LEVEL_COURIER && userKind != USER_KIND_BA && userLevel != AUTH_LEVEL_BA) {
            val contentWidht = convertDpToPx(40, this)
            val contentHeight = convertDpToPx(40, this)
            val paddingHorizontal = convertDpToPx(8, this)
            val paddingVertival = convertDpToPx(8, this)
            binding.titleBarDark.icMore.visibility = View.VISIBLE
            binding.titleBarDark.icMore.layoutParams.width = contentWidht
            binding.titleBarDark.icMore.layoutParams.height = contentHeight
            binding.titleBarDark.icMore.setPadding(paddingHorizontal,paddingVertival,paddingHorizontal,paddingVertival)
            (binding.titleBarDark.icMore.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0,0,paddingHorizontal,0)
            binding.titleBarDark.icRow.layoutParams.width = contentWidht
            binding.titleBarDark.icRow.layoutParams.height = contentHeight
            binding.titleBarDark.icRow.setPadding(paddingHorizontal,paddingVertival,paddingHorizontal,paddingVertival)
            binding.titleBarDark.icGrid.layoutParams.width = contentWidht
            binding.titleBarDark.icGrid.layoutParams.height = contentHeight
            binding.titleBarDark.icGrid.setPadding(paddingHorizontal,paddingVertival,paddingHorizontal,paddingVertival)
            binding.titleBarDark.icMore.setOnClickListener { showPopupMenu() }
        }

        if (layoutStatus == LAYOUT_ROW) binding.titleBarDark.icGrid.visibility = View.VISIBLE
        else binding.titleBarDark.icRow.visibility = View.VISIBLE
        binding.swipeRefreshLayout.setOnRefreshListener { setDatePickerDialog() }

        CustomUtility(this).setUserStatusOnline(true, userDistributorIds ?: "-custom-013", userID)

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

                            val data = responseBody.results

                            var filterMessage = "laporan"
                            val filterResults: ArrayList<ReportVisitModel> = when (activeFilter) {
                                SALES_REPORT_RENVI -> {
                                    filterMessage = "laporan sales"
                                    val list = ArrayList(data.filter { it.source_visit == SALES_REPORT_RENVI })
//                                    println("List $activeFilter size is ${list.size}")
                                    list
                                }

                                PENAGIHAN_REPORT_RENVI -> {
                                    filterMessage = "laporan penagihan"
                                    val list = ArrayList(data.filter { it.source_visit == PENAGIHAN_REPORT_RENVI })
//                                    println("List $activeFilter size is ${list.size}")
                                    list
                                }

                                NORMAL_REPORT -> {
                                    val list = ArrayList(data.filter { it.source_visit != SALES_REPORT_RENVI && it.source_visit != PENAGIHAN_REPORT_RENVI })
//                                    println("List $activeFilter size is ${list.size}")
                                    list
                                }

                                else -> {
//                                    println("List $activeFilter size is ${data.size}")
                                    data
                                }
                            }

                            if (filterResults.isEmpty()) {
//                                println("Filter results $activeFilter is empty")
                                loadingState(true, "Belum ada $filterMessage.")
                            } else {
//                                println("Filter results $activeFilter is good")
                                setRecyclerView(filterResults)
                                loadingState(false)
                            }

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

                FirebaseUtils.logErr(this@ReportsActivity, "Failed ReportsActivity on getList(). Catch: ${e.message}")
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

        mAdapter.setLayoutStatus(layoutStatus)
        mAdapter.setLayoutStatus(layoutStatus)

        binding.recyclerView.apply {
            layoutManager = if (layoutStatus == LAYOUT_ROW) LinearLayoutManager(this@ReportsActivity) else GridLayoutManager(this@ReportsActivity, 2)
            adapter = mAdapter

            mAdapter.setOnItemClickListener(object : ReportsRecyclerViewAdapter.OnItemClickListener{
                override fun onItemClick(item: ReportVisitModel) {
                    showModalDetail(item)
                }

            })
        }

        if (nTargetIntent != null && nTargetIntent == "to_detail_visit" && nVisitId != null && !notificationOpened) {
            val item= items.find { it.id_visit == nVisitId }
            if (item != null) {
                notificationOpened = true
                showModalDetail(item)
            }
        }

    }

    private fun showModalDetail(item: ReportVisitModel) {
        val modalDetail = DetailReportModal(this@ReportsActivity)
        modalDetail.setData(item)
        if (isCourier || isBA) modalDetail.setIsCourier(true)
        else modalDetail.setIsCourier(false)
        if (contactID.isNullOrEmpty()) modalDetail.setWithName(true)
        modalDetail.setUserFullName(iUserID, contactName, userFullName)
        modalDetail.show()
    }

    private fun loadingState(state: Boolean, message: String = getString(R.string.txt_loading)) {

        binding.txtLoading.text = message

        if (state) {

            binding.txtLoading.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE

            binding.swipeRefreshLayout.isRefreshing = message === getString(R.string.txt_loading)

        } else {

            binding.txtLoading.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE

            binding.swipeRefreshLayout.isRefreshing = false

        }

    }

    private fun initClickHandler() {

        binding.titleBarDark.icBack.setOnClickListener {
            if (isTaskRoot) {
                val intent = Intent(this, HomeSalesActivity::class.java)
                startActivity(intent)
                finish()
            } else finish()
        }
        binding.titleBarDark.icGrid.setOnClickListener {
            sessionManager.layoutReportStatus(LAYOUT_GRID)
            binding.titleBarDark.icRow.visibility = View.VISIBLE
            binding.titleBarDark.icGrid.visibility = View.GONE
            setDatePickerDialog()
        }
        binding.titleBarDark.icRow.setOnClickListener {
            sessionManager.layoutReportStatus(LAYOUT_ROW)
            binding.titleBarDark.icRow.visibility = View.GONE
            binding.titleBarDark.icGrid.visibility = View.VISIBLE
            setDatePickerDialog()
        }

    }

    private fun showPopupMenu() {
        val popupMenu = PopupMenu(this, binding.titleBarDark.icMore)
        popupMenu.inflate(R.menu.option_report_type_menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.option_all -> {
                    activeFilter = ALL_REPORT
                    getList()
                    true
                } R.id.option_normal -> {
                    activeFilter = NORMAL_REPORT
                    getList()
                    true
                } R.id.option_sales -> {
                    activeFilter = SALES_REPORT_RENVI
                    getList()
                    true
                } R.id.option_penagihan -> {
                    activeFilter = PENAGIHAN_REPORT_RENVI
                    getList()
                    true
                } else -> false
            }
        }
        popupMenu.show()
    }

    override fun onStart() {
        super.onStart()
//        EventBus.getDefault().register(this)
        Handler(Looper.getMainLooper()).postDelayed({
            if (sessionManager.isLoggedIn()) {
                if (CustomUtility(this).isUserWithOnlineStatus()) {
                    CustomUtility(this).setUserStatusOnline(
                        true,
                        sessionManager.userDistributor() ?: "-custom-013",
                        sessionManager.userID() ?: ""
                    )
                }
            }
        }, 1000)
    }

    override fun onStop() {
        super.onStop()
//        EventBus.getDefault().unregister(this)
        if (sessionManager.isLoggedIn()) {
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    sessionManager.userDistributor() ?: "-custom-013",
                    sessionManager.userID() ?: ""
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (sessionManager.isLoggedIn()) {
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    sessionManager.userDistributor() ?: "-custom-013",
                    sessionManager.userID() ?: ""
                )
            }
        }
    }

    override fun onBackPressed() {

        if (isTaskRoot) {
            val intent = Intent(this, HomeSalesActivity::class.java)
            startActivity(intent)
            finish()
        } else super.onBackPressed()

    }
}