package com.topmortar.topmortarsales.view.gudang

import android.app.Activity
import android.content.Intent
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
import com.topmortar.topmortarsales.commons.CONST_IS_BASE_CAMP
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.EDIT_CONTACT
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityManageGudangBinding
import com.topmortar.topmortarsales.model.GudangModel
import com.topmortar.topmortarsales.view.reports.NewReportActivity
import kotlinx.coroutines.launch

class ManageGudangActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var _binding: ActivityManageGudangBinding? = null
    private val binding get() = _binding!!
    private val userKind get() = sessionManager.userKind()!!
    private val userCity get() = sessionManager.userCityID()!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)
        _binding = ActivityManageGudangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBarDark.tvTitleBar.text = "Kelola Gudang"
        binding.titleBarDark.icBack.setOnClickListener { finish() }
        binding.btnFabAdd.setOnClickListener { navigateFab() }

        getContacts()

    }

    private fun getContacts() {

        loadingState(true)
        showBadgeRefresh(false)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = when (userKind) {
                    USER_KIND_ADMIN -> apiService.getListGudang()
                    else -> apiService.getListGudang(cityId = userCity)
                }

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        setRecyclerView(response.results)
                        loadingState(false)
                        showBadgeRefresh(false)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Belum ada basecamp!")
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

        if (userKind == USER_KIND_ADMIN) {

            val intent = Intent(this@ManageGudangActivity, FormGudangActivity::class.java)
            intent.putExtra(EDIT_CONTACT, true)
            intent.putExtra(CONST_CONTACT_ID, data?.id_gudang)
            intent.putExtra(CONST_PHONE, data?.nomorhp_gudang)
            intent.putExtra(CONST_NAME, data?.nama_gudang)
            intent.putExtra(CONST_LOCATION, data?.id_city)
            intent.putExtra(CONST_MAPS, data?.location_gudang)
            startActivity(intent)
        } else {

            val intent = Intent(this@ManageGudangActivity, NewReportActivity::class.java)
            intent.putExtra(CONST_IS_BASE_CAMP, true)
            intent.putExtra(CONST_CONTACT_ID, data?.id_gudang)
            intent.putExtra(CONST_NAME, data?.nama_gudang)
            intent.putExtra(CONST_MAPS, data?.location_gudang)
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
        startActivity(intent)

    }

    private fun showBadgeRefresh(action: Boolean) {
        val badgeRefresh = findViewById<LinearLayout>(R.id.badgeRefresh)
        val tvTitle = binding.badgeRefresh.tvTitle
        val icClose = binding.badgeRefresh.icClose
        icClose.setOnClickListener { badgeRefresh.visibility = View.GONE }

        if (action) {
            badgeRefresh.visibility = View.VISIBLE
            tvTitle.setOnClickListener { getContacts() }
        } else badgeRefresh.visibility = View.GONE
    }

    override fun onBackPressed() {
        finish()
    }
}