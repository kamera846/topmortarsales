package com.topmortar.topmortarsales.view.gudang

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.recyclerview.GudangRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_DATE
import com.topmortar.topmortarsales.commons.CONST_IS_BASE_CAMP
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.EDIT_CONTACT
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.REQUEST_BASECAMP_FRAGMENT
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_SUCCESS
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN_CITY
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityManageGudangBinding
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.CityModel
import com.topmortar.topmortarsales.model.GudangModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import com.topmortar.topmortarsales.view.reports.NewReportActivity
import kotlinx.coroutines.launch

class ManageGudangActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var _binding: ActivityManageGudangBinding? = null
    private val binding get() = _binding!!
    private val userKind get() = sessionManager.userKind()!!
    private val userCity get() = sessionManager.userCityID()!!
    private val userDistributorId get() = sessionManager.userDistributor()!!

    private lateinit var searchModal: SearchModal
    private var selectedCity: ModalSearchModel? = null
    private var citiesResults: ArrayList<CityModel> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)
        _binding = ActivityManageGudangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBarDark.tvTitleBar.text = "Kelola Gudang"
        binding.titleBarDark.icBack.setOnClickListener { finish() }
        binding.btnFabAdd.setOnClickListener { navigateFab() }

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) binding.llFilter.componentFilter.background = getDrawable(R.color.black_400)
        else binding.llFilter.componentFilter.background = getDrawable(R.color.light)
        binding.llFilter.componentFilter.visibility = View.GONE
        binding.llFilter.componentFilter.setOnClickListener {
            searchModal.show()
        }

        getList()
        if (userKind == USER_KIND_ADMIN) getCities()

    }

    private fun getList() {

        loadingState(true)
        showBadgeRefresh(false)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = when (userKind) {
                    USER_KIND_ADMIN -> {
                        if (selectedCity != null) apiService.getListGudang(cityId = selectedCity?.id!!, distributorID = userDistributorId)
                        else apiService.getListGudang(distributorID = userDistributorId)
                    } else -> apiService.getListGudang(cityId = userCity, distributorID = userDistributorId)
                }

                when (response.status) {
                    RESPONSE_STATUS_OK, RESPONSE_STATUS_SUCCESS -> {

                        setRecyclerView(response.results)
                        loadingState(false)
                        showBadgeRefresh(false)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Belum ada gudang!")
                        showBadgeRefresh(false)

                    }
                    else -> {

                        handleMessage(this@ManageGudangActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        loadingState(true, getString(R.string.failed_request))
                        showBadgeRefresh(true)

                    }
                }

            } catch (e: Exception) {

                handleMessage(this@ManageGudangActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))
                showBadgeRefresh(true)

            }

        }

    }

    private fun setRecyclerView(listItem: ArrayList<GudangModel>) {

        val rvAdapter = GudangRecyclerViewAdapter(listItem, object: GudangRecyclerViewAdapter.ItemClickListener {
            override fun onItemClick(data: GudangModel?) {
                navigateItemAction(data)
            }

        })

        binding.rvChatList.layoutManager = LinearLayoutManager(this@ManageGudangActivity)
        binding.rvChatList.adapter = rvAdapter
        binding.rvChatList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

    private fun navigateItemAction(data: GudangModel? = null) {

        if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_ADMIN_CITY) {

            val intent = Intent(this@ManageGudangActivity, FormGudangActivity::class.java)
            intent.putExtra(EDIT_CONTACT, true)
            intent.putExtra(CONST_CONTACT_ID, data?.id_warehouse)
            intent.putExtra(CONST_PHONE, data?.nomorhp_warehouse)
            intent.putExtra(CONST_NAME, data?.nama_warehouse)
            intent.putExtra(CONST_LOCATION, data?.id_city)
            intent.putExtra(CONST_MAPS, data?.location_warehouse)
            intent.putExtra(CONST_DATE, data?.created_at)
            startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_CODE)
        } else {

            val intent = Intent(this@ManageGudangActivity, NewReportActivity::class.java)
            intent.putExtra(CONST_IS_BASE_CAMP, true)
            intent.putExtra(CONST_CONTACT_ID, data?.id_warehouse)
            intent.putExtra(CONST_NAME, data?.nama_warehouse)
            intent.putExtra(CONST_MAPS, data?.location_warehouse)
            (this@ManageGudangActivity as Activity).startActivity(intent)
        }

    }

    private fun loadingState(state: Boolean, message: String = getString(R.string.txt_loading)) {

        binding.txtLoading.text = message

        if (state) {

            binding.txtLoading.visibility = View.VISIBLE
            binding.rvChatList.visibility = View.GONE

        } else {

            binding.txtLoading.visibility = View.GONE
            binding.rvChatList.visibility = View.VISIBLE

        }

    }

    private fun navigateFab() {

        val intent = Intent(this@ManageGudangActivity, FormGudangActivity::class.java)
        startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_CODE)

    }

    private fun showBadgeRefresh(action: Boolean) {
        val badgeRefresh = findViewById<LinearLayout>(R.id.badgeRefresh)
        val tvTitle = binding.badgeRefresh.tvTitle
        val icClose = binding.badgeRefresh.icClose
        icClose.setOnClickListener { badgeRefresh.visibility = View.GONE }

        if (action) {
            badgeRefresh.visibility = View.VISIBLE
            tvTitle.setOnClickListener { getList() }
        } else badgeRefresh.visibility = View.GONE
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAIN_ACTIVITY_REQUEST_CODE) {
            val resultData = data?.getStringExtra(REQUEST_BASECAMP_FRAGMENT)

            if (resultData == SYNC_NOW) {

                getList()

            }
        }
    }

    private fun getCities() {

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getCities(distributorID = userDistributorId)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        citiesResults = response.results
                        val items: ArrayList<ModalSearchModel> = ArrayList()

                        for (i in 0 until citiesResults.size) {
                            val data = citiesResults[i]
                            items.add(ModalSearchModel(data.id_city, "${data.nama_city} - ${data.kode_city}"))
                        }
                        items.add(0, ModalSearchModel("-1", "Hapus Filter"))

                        setupDialogSearch(items)
                        binding.llFilter.componentFilter.visibility = View.VISIBLE

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(this@ManageGudangActivity, "LIST CITY", "Daftar kota kosong!")

                    }
                    else -> {

                        handleMessage(this@ManageGudangActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))

                    }
                }


            } catch (e: Exception) {

                handleMessage(this@ManageGudangActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)

            }

        }
    }

    private fun setupDialogSearch(items: ArrayList<ModalSearchModel> = ArrayList()) {

        searchModal = SearchModal(this, items)
        searchModal.setCustomDialogListener(object: SearchModal.SearchModalListener{
            override fun onDataReceived(data: ModalSearchModel) {
                if (data.id == "-1") {
                    selectedCity = null
                    binding.llFilter.tvFilter.text = getString(R.string.tidak_ada_filter)
                } else {
                    selectedCity = data
                    binding.llFilter.tvFilter.text = data.title
                }
                getList()
            }

        })
        searchModal.searchHint = "Ketik untuk mencari…"

    }

}