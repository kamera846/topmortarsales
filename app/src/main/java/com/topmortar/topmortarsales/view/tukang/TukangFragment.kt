package com.topmortar.topmortarsales.view.tukang

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.TukangRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.CONST_ADDRESS
import com.topmortar.topmortarsales.commons.CONST_BIRTHDAY
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_DATE
import com.topmortar.topmortarsales.commons.CONST_KTP
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_OWNER
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.CONST_SKILL
import com.topmortar.topmortarsales.commons.CONST_STATUS
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.utils.EventBusUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.FragmentTukangBinding
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.CityModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import com.topmortar.topmortarsales.model.RencanaVisitModel
import com.topmortar.topmortarsales.model.TukangModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * A fragment representing a list of Items.
 */
class TukangFragment : Fragment() {

    private lateinit var binding: FragmentTukangBinding

    private lateinit var sessionManager: SessionManager
    private lateinit var userKind: String
    private lateinit var userCity: String
    private lateinit var userID: String
    private val userDistributorId get() = sessionManager.userDistributor()

    private lateinit var apiService: ApiService
    private lateinit var badgeRefresh: LinearLayout
    private lateinit var searchModal: SearchModal
    private var citiesResults: ArrayList<CityModel>? = null
    private var selectedCity: ModalSearchModel? = null
    private var listItem: ArrayList<RencanaVisitModel> = arrayListOf()

    private var searchKey: String = ""

    private var listener: CounterItem? = null
    interface CounterItem {
        fun counterItem(count: Int)
    }
    fun setCounterItem(listener: CounterItem) {
        this.listener = listener
    }
    fun syncNow() {
        getList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTukangBinding.inflate(inflater, container, false)
        val view = binding.root

        badgeRefresh = view.findViewById(R.id.badgeRefresh)

        sessionManager = SessionManager(requireContext())
        userKind = sessionManager.userKind().toString()
        userCity = sessionManager.userCityID().toString()
        userID = sessionManager.userID().toString()
        binding.btnFabAdmin.setOnClickListener { navigateAddTukang() }

        apiService = HttpClient.create()

        if (userKind == USER_KIND_ADMIN) getCities()
        else getList()

        binding.swipeRefreshLayout.setOnRefreshListener {
            if (userKind == USER_KIND_ADMIN) getCities()
            else getList()
        }

        return view
    }

    private fun getCities() {

        lifecycleScope.launch {
            try {

                val response = apiService.getCities(distributorID = userDistributorId ?: "")

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        citiesResults = response.results
                        val items: ArrayList<ModalSearchModel> = ArrayList()

                        for (i in 0 until citiesResults!!.size) {
                            val data = citiesResults!![i]
                            items.add(ModalSearchModel(data.id_city, "${data.nama_city} - ${data.kode_city}"))
                        }
                        items.add(0, ModalSearchModel("-1", "Hapus Filter"))

                        setupDialogSearch(items)
                        binding.llFilter.componentFilter.visibility = View.VISIBLE

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(requireActivity(), "LIST CITY", "Daftar kota kosong!")

                    }
                    else -> {

                        handleMessage(requireActivity(), TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))

                    }
                }


            } catch (e: Exception) {

                handleMessage(requireActivity(), TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)

            } finally {
                getList()
            }

        }
    }

    private fun setupDialogSearch(items: ArrayList<ModalSearchModel> = ArrayList()) {

        searchModal = SearchModal(requireActivity(), items)
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

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) binding.llFilter.componentFilter.background = AppCompatResources.getDrawable(requireContext(), R.color.black_400)
        else binding.llFilter.componentFilter.background = AppCompatResources.getDrawable(requireContext(), R.color.light)
        binding.llFilter.componentFilter.visibility = View.GONE
        binding.llFilter.componentFilter.setOnClickListener {
            searchModal.show()
        }
        binding.llFilter.tvFilter.text = selectedCity?.title ?: getString(R.string.tidak_ada_filter)

    }

    private fun getList() {

        loadingState(true)
        showBadgeRefresh(false)

        lifecycleScope.launch {
            try {

                val response = when (userKind) {
                    USER_KIND_ADMIN -> {
                        if (selectedCity != null) apiService.getTukang(cityId = selectedCity?.id!!, searchKey = searchKey)
                        else apiService.getTukangDst(distributorID = userDistributorId!!, searchKey = searchKey)
                    } else -> apiService.getTukang(cityId = userCity, searchKey = searchKey)
                }

                val textFilter = if (selectedCity != null) {
                    selectedCity?.title!!
                } else getString(R.string.tidak_ada_filter)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        setRecyclerView(response.results)
                        loadingState(false)
                        showBadgeRefresh(false)
                        listener?.counterItem(response.results.size)
                        binding.llFilter.tvFilter.text = "$textFilter (${response.results.size})"

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Belum ada tukang!")
                        showBadgeRefresh(false)
                        listener?.counterItem(0)
                        binding.llFilter.tvFilter.text = "$textFilter (${response.results.size})"

                    }
                    else -> {

                        handleMessage(requireContext(), TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        loadingState(true, getString(R.string.failed_request))
                        showBadgeRefresh(true)

                    }
                }


            } catch (e: Exception) {

                handleMessage(requireContext(), TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))
                showBadgeRefresh(true)

            }

        }

    }

    private fun setRecyclerView(listItem: ArrayList<TukangModel>) {

        val rvAdapter = TukangRecyclerViewAdapter(listItem, object: TukangRecyclerViewAdapter.ItemClickListener {
            override fun onItemClick(data: TukangModel?) {
                navigateDetailContact(data)
            }

        })

        binding.rvChatList.layoutManager = LinearLayoutManager(requireContext())
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

    private fun navigateDetailContact(data: TukangModel? = null) {

        val intent = Intent(requireContext(), DetailTukangActivity::class.java)

        if (data != null) {
            intent.putExtra(ACTIVITY_REQUEST_CODE, MAIN_ACTIVITY_REQUEST_CODE)
            intent.putExtra(CONST_CONTACT_ID, data.id_tukang)
            intent.putExtra(CONST_NAME, data.nama)
            intent.putExtra(CONST_PHONE, data.nomorhp)
            intent.putExtra(CONST_BIRTHDAY, data.tgl_lahir)
            intent.putExtra(CONST_OWNER, data.nama_lengkap)
            intent.putExtra(CONST_LOCATION, data.id_city)
            intent.putExtra(CONST_MAPS, data.maps_url)
            intent.putExtra(CONST_ADDRESS, data.address)
            intent.putExtra(CONST_STATUS, data.tukang_status)
            intent.putExtra(CONST_SKILL, data.id_skill)
            intent.putExtra(CONST_KTP, data.ktp_tukang)
            intent.putExtra(CONST_DATE, data.created_at)
        }

//        (requireContext() as Activity).startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_CODE)
        someActivityResultLauncher.launch(intent)

    }

    private fun loadingState(state: Boolean, message: String = getString(R.string.txt_loading)) {

        binding.txtLoading.text = message

        if (state) {

            binding.txtLoading.visibility = View.VISIBLE
            binding.rvChatList.visibility = View.GONE

            binding.swipeRefreshLayout.isRefreshing = message === getString(R.string.txt_loading)

        } else {

            binding.txtLoading.visibility = View.GONE
            binding.rvChatList.visibility = View.VISIBLE

            binding.swipeRefreshLayout.isRefreshing = false

        }

    }

    private fun navigateAddTukang(data: TukangModel? = null) {

        val intent = Intent(requireContext(), AddTukangActivity::class.java)

        if (data != null) {
            intent.putExtra(CONST_CONTACT_ID, data.id_tukang)
            intent.putExtra(CONST_NAME, data.nama)
            intent.putExtra(CONST_PHONE, data.nomorhp)
            intent.putExtra(CONST_BIRTHDAY, data.tgl_lahir)
            intent.putExtra(CONST_OWNER, data.nama_lengkap)
            intent.putExtra(ACTIVITY_REQUEST_CODE, MAIN_ACTIVITY_REQUEST_CODE)
            intent.putExtra(CONST_LOCATION, data.id_city)
            intent.putExtra(CONST_STATUS, data.tukang_status)
            intent.putExtra(CONST_SKILL, data.id_skill)
//            intent.putExtra(CONST_LOCATION, "1")
        }
        someActivityResultLauncher.launch(intent)

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe
    fun onEventBus(event: EventBusUtils.MessageEvent) {
        searchKey = event.message
        searchKey = if (searchKey.startsWith("0")) {
            "62${searchKey.substring(1)}"
        } else if (searchKey.startsWith("8")) {
            "62${searchKey.substring(0)}"
        } else if (searchKey.startsWith("+")) {
            "${searchKey.substring(1)}"
        } else {
            searchKey
        }
        getList()
    }

    private fun showBadgeRefresh(action: Boolean) {
        val tvTitle = badgeRefresh.findViewById<TextView>(R.id.tvTitle)
        val icClose = badgeRefresh.findViewById<ImageView>(R.id.icClose)
        icClose.setOnClickListener { badgeRefresh.visibility = View.GONE }

        if (action) {
            badgeRefresh.visibility = View.VISIBLE
            tvTitle.setOnClickListener { getList() }
        } else badgeRefresh.visibility = View.GONE
    }

    private val someActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // Handle the result
        val resultCode = result.resultCode
        val data = result.data
        // Process the result
        if (resultCode == RESULT_OK) {
            val newData = data?.getStringExtra("$MAIN_ACTIVITY_REQUEST_CODE")
            if (!newData.isNullOrEmpty() && newData == SYNC_NOW) getList()
        }
    }

}