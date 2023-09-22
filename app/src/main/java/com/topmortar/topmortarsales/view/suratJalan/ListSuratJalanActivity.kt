package com.topmortar.topmortarsales.view.suratJalan

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.InvoiceRecyclerViewAdapter
import com.topmortar.topmortarsales.adapter.SuratJalanRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_DATE_INVOICE
import com.topmortar.topmortarsales.commons.CONST_INVOICE_ID
import com.topmortar.topmortarsales.commons.CONST_INVOICE_NUMBER
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_STATUS_INVOICE
import com.topmortar.topmortarsales.commons.CONST_TOTAL_INVOICE
import com.topmortar.topmortarsales.commons.MANAGE_USER_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.view.invoice.DetailInvoiceActivity
import com.topmortar.topmortarsales.model.InvoiceModel
import com.topmortar.topmortarsales.model.SuratJalanModel
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class ListSuratJalanActivity : AppCompatActivity(), SuratJalanRecyclerViewAdapter.ItemClickListener,
    InvoiceRecyclerViewAdapter.ItemClickListener {

    private lateinit var scaleAnimation: Animation

    private lateinit var rlLoading: RelativeLayout
    private lateinit var rlParent: RelativeLayout
    private lateinit var txtLoading: TextView
    private lateinit var titleBar: TextView
    private lateinit var titleBarDescription: TextView
    private lateinit var tvFilter: TextView
    private lateinit var rvListItem: RecyclerView
    private lateinit var llTitleBar: LinearLayout
    private lateinit var llSearchBox: LinearLayout
    private lateinit var llFilter: LinearLayout
    private lateinit var icBack: ImageView
    private lateinit var icSearch: ImageView
    private lateinit var icSyncNow: ImageView
    private lateinit var icOption: ImageView
    private lateinit var icCloseSearch: ImageView
    private lateinit var icClearSearch: ImageView
    private lateinit var etSearchBox: EditText

    // Global
    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService
    private var isListActive: String = LIST_SURAT_JALAN
    private var isFilterInvoice: String = FILTER_NONE
    private var doubleBackToExitPressedOnce = false
    private var contactId: String? = null
    private var iName: String? = null

    // Initialize Search Engine
    private val searchDelayMillis = 500L
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var previousSearchTerm = ""
    private var isSearchActive = false

    companion object {
        const val LIST_SURAT_JALAN = "list_surat_jalan"
        const val LIST_INVOICE = "list_invoice"
        const val FILTER_NONE = "list_none"
        const val FILTER_PAID = "list_paid"
        const val FILTER_UNPAID = "list_unpaid"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)

        setContentView(R.layout.activity_list_invoice)

        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_anim)

        initVariable()
        initClickHandler()
        dataActivityValidation()
        toggleList()

    }

    private fun initVariable() {

        rlLoading = findViewById(R.id.rl_loading)
        rlParent = findViewById(R.id.rl_parent)
        txtLoading = findViewById(R.id.txt_loading)
        rvListItem = findViewById(R.id.rv_chat_list)
        llTitleBar = findViewById(R.id.title_bar)
        llSearchBox = findViewById(R.id.search_box)
        llFilter = findViewById(R.id.ll_filter)
        icBack = llTitleBar.findViewById(R.id.ic_back)
        icSearch = llTitleBar.findViewById(R.id.ic_search)
        icSyncNow = llTitleBar.findViewById(R.id.ic_sync_now)
        icOption = llTitleBar.findViewById(R.id.ic_more)
        titleBar = llTitleBar.findViewById(R.id.tv_title_bar)
        titleBarDescription = llTitleBar.findViewById(R.id.tv_title_bar_description)
        tvFilter = findViewById(R.id.tv_filter)
        icCloseSearch = findViewById(R.id.ic_close_search)
        icClearSearch = findViewById(R.id.ic_clear_search)
        etSearchBox = findViewById(R.id.et_search_box)


        apiService = HttpClient.create()

        // Set Title Bar
        icBack.visibility = View.VISIBLE

        if (sessionManager.userKind() == USER_KIND_ADMIN) icOption.visibility = View.VISIBLE
        else icSyncNow.visibility = View.VISIBLE
//        icSyncNow.visibility = View.VISIBLE

        // Get the current theme mode (light or dark)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) llFilter.background = getDrawable(R.color.black_400)
        else llFilter.background = getDrawable(R.color.light)

        val padding16 = convertDpToPx(16, this)
//        val padding8 = convertDpToPx(8, this)
        titleBar.setPadding(0, 0, padding16, 0)
//        icSyncNow.setPadding(padding16, 0, 0, 0)
//        icOption.setPadding(padding8, 0, padding16, 0)

    }

    private fun initClickHandler() {

        icBack.setOnClickListener { finish() }
        icSyncNow.setOnClickListener { toggleList() }
        icOption.setOnClickListener { showPopupMenu() }
        llFilter.setOnClickListener { showDropdownMenu() }

    }

    private fun dataActivityValidation() {

        contactId = intent.getStringExtra(CONST_CONTACT_ID)
        iName = intent.getStringExtra(CONST_NAME)

    }

    private fun getList() {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val response = apiService.getSuratJalan(processNumber = "2", contactId = contactId!!)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        setRecyclerView(response.results)
                        loadingState(false)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Data surat jalan kosong!")

                    }
                    else -> {

                        handleMessage(this@ListSuratJalanActivity, TAG_RESPONSE_CONTACT, "Failed get data")
                        loadingState(true, getString(R.string.failed_request))

                    }
                }

            } catch (e: Exception) {

                handleMessage(this@ListSuratJalanActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))

            }

        }

    }

    private fun getListInvoice() {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val response = apiService.getInvoices(contactId = contactId!!)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        setRecyclerViewInvoice(response.results)
                        loadingState(false)

                        llFilter.visibility = View.VISIBLE
                        when (isFilterInvoice) {
                            FILTER_PAID -> tvFilter.text = "Paid"
                            FILTER_UNPAID -> tvFilter.text = "Unpaid"
                            else -> tvFilter.text = "None"
                        }

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Data surat jalan kosong!")

                    }
                    else -> {

                        handleMessage(this@ListSuratJalanActivity, TAG_RESPONSE_CONTACT, "Failed get data")
                        loadingState(true, getString(R.string.failed_request))

                    }
                }

            } catch (e: Exception) {

                handleMessage(this@ListSuratJalanActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))

            }

        }

    }

    private fun setRecyclerView(listItem: ArrayList<SuratJalanModel>) {
        val rvAdapter = SuratJalanRecyclerViewAdapter(this@ListSuratJalanActivity)
        rvAdapter.setListItem(listItem)

        rvListItem.apply {
            layoutManager = LinearLayoutManager(this@ListSuratJalanActivity)
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

    private fun setRecyclerViewInvoice(listItem: ArrayList<InvoiceModel>) {
        val rvAdapter = InvoiceRecyclerViewAdapter(this@ListSuratJalanActivity)
        rvAdapter.setListItem(listItem)

        rvListItem.apply {
            layoutManager = LinearLayoutManager(this@ListSuratJalanActivity)
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

    private fun showPopupMenu() {

        val popupMenu = PopupMenu(this@ListSuratJalanActivity, icOption)
        popupMenu.inflate(R.menu.option_invoice_menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.option_sync_now -> {
                    toggleList()
                    true
                }
                R.id.option_surat_jalan -> {
                    if (isListActive != LIST_SURAT_JALAN) toggleList(LIST_SURAT_JALAN)
                    true
                }
                R.id.option_invoices -> {
                    if (isListActive != LIST_INVOICE) toggleList(LIST_INVOICE)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showDropdownMenu() {
        val popupMenu = PopupMenu(this, llFilter, Gravity.END)
        popupMenu.menuInflater.inflate(R.menu.option_filter_invoice_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
            when (item?.itemId) {
                R.id.option_none -> {
                    toggleFilter(FILTER_NONE)
                    return@setOnMenuItemClickListener  true
                } R.id.option_paid -> {
                    toggleFilter(FILTER_PAID)
                    return@setOnMenuItemClickListener  true
                } R.id.option_unpaid -> {
                    toggleFilter(FILTER_UNPAID)
                    return@setOnMenuItemClickListener  true
                } else -> return@setOnMenuItemClickListener false
            }
        }

        popupMenu.show()
    }

    private fun toggleList(list: String? = null) {
        if (list != null) isListActive = list

        llFilter.visibility = View.GONE
        titleBarDescription.visibility = View.VISIBLE

        if (isListActive == LIST_SURAT_JALAN) {
            titleBar.text = if (!iName.isNullOrEmpty()) iName else "Surat Jalan"
            titleBarDescription.text = "Daftar surat jalan pada toko ini"
            getList()
        } else if (isListActive == LIST_INVOICE) {
            titleBar.text = if (!iName.isNullOrEmpty()) iName else "Invoice"
            titleBarDescription.text = "Daftar invoice pada toko ini"
            getListInvoice()
        }
    }

    private fun toggleFilter(filter: String) {
        if (filter != isFilterInvoice) {
            isFilterInvoice = filter
            toggleList()
        }
    }

    private fun loadingState(state: Boolean, message: String = getString(R.string.txt_loading)) {

        txtLoading.text = message

        if (state) {

            rlLoading.visibility = View.VISIBLE
            rvListItem.visibility = View.GONE

        } else {

            rlLoading.visibility = View.GONE
            rvListItem.visibility = View.VISIBLE

        }

    }

    private fun navigateDetailSuratJalan(data: SuratJalanModel? = null) {

        val intent = Intent(this@ListSuratJalanActivity, DetailSuratJalanActivity::class.java)
        intent.putExtra(CONST_INVOICE_ID, data?.id_surat_jalan)
        intent.putExtra(CONST_CONTACT_ID, contactId)
        startActivity(intent)

    }

    private fun navigateDetailInvoice(data: InvoiceModel? = null) {

        val intent = Intent(this@ListSuratJalanActivity, DetailInvoiceActivity::class.java)
        intent.putExtra(CONST_INVOICE_ID, data?.id_invoice)
        intent.putExtra(CONST_INVOICE_NUMBER, data?.no_invoice)
        intent.putExtra(CONST_STATUS_INVOICE, data?.status_invoice)
        intent.putExtra(CONST_TOTAL_INVOICE, data?.total_invoice)
        intent.putExtra(CONST_DATE_INVOICE, data?.date_invoice)
        startActivity(intent)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MANAGE_USER_ACTIVITY_REQUEST_CODE) {

            val resultData = data?.getStringExtra("$MANAGE_USER_ACTIVITY_REQUEST_CODE")

            if (resultData == SYNC_NOW) {

                toggleList()

            }

        }

    }

    override fun onItemClick(data: SuratJalanModel?) {
        navigateDetailSuratJalan(data)
    }
    override fun onItemInvoiceClick(data: InvoiceModel?) {
        navigateDetailInvoice(data)
    }

}