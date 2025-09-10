package com.topmortar.topmortarsales.view.city

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.CityRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityManageCityBinding
import com.topmortar.topmortarsales.modal.AddCityModal
import com.topmortar.topmortarsales.model.CityModel
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class ManageCityActivity : AppCompatActivity(), CityRecyclerViewAdapter.ItemClickListener,
    AddCityModal.AddCityModalInterface {

    private lateinit var scaleAnimation: Animation

    private lateinit var rlLoading: RelativeLayout
    private lateinit var rlParent: RelativeLayout
    private lateinit var txtLoading: TextView
    private lateinit var titleBar: TextView
    private lateinit var rvListItem: RecyclerView
    private lateinit var llTitleBar: LinearLayout
    private lateinit var llSearchBox: LinearLayout
    private lateinit var btnFab: FloatingActionButton
    private lateinit var icBack: ImageView
    private lateinit var icSearch: ImageView
    private lateinit var icCloseSearch: ImageView
    private lateinit var icClearSearch: ImageView
    private lateinit var etSearchBox: EditText

    // Global
    private lateinit var sessionManager: SessionManager
    private lateinit var binding: ActivityManageCityBinding
    private val userDistributorId get() = sessionManager.userDistributor().toString()
    private lateinit var addCityModal: AddCityModal
    private var isRequestSync = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge()

        sessionManager = SessionManager(this)

        binding = ActivityManageCityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_anim)

        initVariable()
        initClickHandler()
        getList()
        binding.swipeRefreshLayout.setOnRefreshListener { getList() }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                myOnBackPressed()
            }

        })

    }

    private fun initVariable() {

        addCityModal = AddCityModal(this, lifecycleScope)
        addCityModal.initializeInterface(this)

        rlLoading = findViewById(R.id.rl_loading)
        rlParent = findViewById(R.id.rl_parent)
        txtLoading = findViewById(R.id.txt_loading)
        rvListItem = findViewById(R.id.rv_chat_list)
        llTitleBar = findViewById(R.id.title_bar)
        llSearchBox = findViewById(R.id.search_box)
        btnFab = findViewById(R.id.btn_fab)
        icBack = llTitleBar.findViewById(R.id.ic_back)
        icSearch = llTitleBar.findViewById(R.id.ic_search)
        titleBar = llTitleBar.findViewById(R.id.tv_title_bar)
        icCloseSearch = findViewById(R.id.ic_close_search)
        icClearSearch = findViewById(R.id.ic_clear_search)
        etSearchBox = findViewById(R.id.et_search_box)

        // Set Title Bar
        icBack.visibility = View.VISIBLE
//        icSearch.visibility = View.VISIBLE
        titleBar.text = "Kelola Kota"

    }

    private fun initClickHandler() {
        icBack.setOnClickListener { backHandler() }
        btnFab.setOnClickListener { addCityModal.show() }
    }

    private fun getList() {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getCities(distributorID = userDistributorId)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        setRecyclerView(response.results)
                        loadingState(false)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Daftar kontak kosong!")

                    }
                    else -> {

                        handleMessage(this@ManageCityActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        loadingState(true, getString(R.string.failed_request))

                    }
                }


            } catch (e: Exception) {

                if (e is CancellationException) {
                    return@launch
                }
                FirebaseUtils.logErr(this@ManageCityActivity, "Failed ManageCityActivity on getList(). Catch: ${e.message}")
                handleMessage(this@ManageCityActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))

            }

        }

    }

    private fun setRecyclerView(listItem: ArrayList<CityModel>) {
        val rvAdapter = CityRecyclerViewAdapter(this@ManageCityActivity)
        rvAdapter.setListItem(listItem)

        rvListItem.apply {
            layoutManager = LinearLayoutManager(this@ManageCityActivity)
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

        txtLoading.text = message

        if (state) {

            rlLoading.visibility = View.VISIBLE
            rvListItem.visibility = View.GONE

            binding.swipeRefreshLayout.isRefreshing = message === getString(R.string.txt_loading)

        } else {

            rlLoading.visibility = View.GONE
            rvListItem.visibility = View.VISIBLE
            binding.swipeRefreshLayout.isRefreshing = false

        }

    }

    override fun onItemClick(data: CityModel?) {

    }

    override fun onSubmit(status: Boolean) {
        if (status) {
            getList()
            isRequestSync = true
        }
    }

    private fun backHandler() {
        if (isRequestSync) {
            val resultIntent = Intent()
            resultIntent.putExtra("$MAIN_ACTIVITY_REQUEST_CODE", SYNC_NOW)
            setResult(RESULT_OK, resultIntent)
            finish()
        } else finish()
    }

    private fun myOnBackPressed() {
        if (isRequestSync) {
            val resultIntent = Intent()
            resultIntent.putExtra("$MAIN_ACTIVITY_REQUEST_CODE", SYNC_NOW)
            setResult(RESULT_OK, resultIntent)
            finish()
        } else finish()
    }

}