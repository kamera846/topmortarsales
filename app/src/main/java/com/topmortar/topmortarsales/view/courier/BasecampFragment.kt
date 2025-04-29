package com.topmortar.topmortarsales.view.courier

import android.app.Activity
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
import com.topmortar.topmortarsales.adapter.recyclerview.BaseCampRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_IS_BASE_CAMP
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.EDIT_CONTACT
import com.topmortar.topmortarsales.commons.REQUEST_BASECAMP_FRAGMENT
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.RESULT_BASECAMP_FRAGMENT
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN_CITY
import com.topmortar.topmortarsales.commons.utils.EventBusUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.FragmentBasecampBinding
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.BaseCampModel
import com.topmortar.topmortarsales.model.CityModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import com.topmortar.topmortarsales.view.reports.NewReportActivity
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import kotlin.coroutines.cancellation.CancellationException

/**
 * A fragment representing a list of Items.
 */
class BasecampFragment : Fragment() {

    private lateinit var binding: FragmentBasecampBinding

    private lateinit var sessionManager: SessionManager
    private lateinit var userKind: String
    private lateinit var userCity: String
    private lateinit var userID: String
    private val userDistributorid get() = sessionManager.userDistributor().toString()

    private lateinit var badgeRefresh: LinearLayout
    private lateinit var searchModal: SearchModal
    private var citiesResults: ArrayList<CityModel>? = null
    private var selectedCity: ModalSearchModel? = null


    private var listener: CounterItem? = null
    interface CounterItem {
        fun counterItem(count: Int)
    }
    fun setCounterItem(listener: CounterItem) {
        this.listener = listener
    }
    fun syncNow() {
        getContacts()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBasecampBinding.inflate(inflater, container, false)
        val view = binding.root

        badgeRefresh = view.findViewById(R.id.badgeRefresh)

        sessionManager = SessionManager(requireContext())
        userKind = sessionManager.userKind().toString()
        userCity = sessionManager.userCityID().toString()
        userID = sessionManager.userID().toString()
        binding.btnFabAdd.setOnClickListener { navigateFab() }

        getCities()
        binding.swipeRefreshLayout.setOnRefreshListener { getCities() }

        return view
    }

    private fun getContacts() {

        loadingState(true)
        showBadgeRefresh(false)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                var response = apiService.getListBaseCamp(distributorID = userDistributorid)
                if (selectedCity != null) response = apiService.getListBaseCamp(distributorID = userDistributorid, cityId = selectedCity!!.id!!)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        setRecyclerView(response.results)
                        loadingState(false)
                        showBadgeRefresh(false)
                        listener?.counterItem(response.results.size)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Belum ada basecamp!")
                        showBadgeRefresh(false)
                        listener?.counterItem(0)

                    }
                    else -> {

                        handleMessage(requireContext(), TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        loadingState(true, getString(R.string.failed_request))
                        showBadgeRefresh(true)

                    }
                }


            } catch (e: Exception) {

                if (e is CancellationException) {
                    return@launch
                }
                handleMessage(requireContext(), TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))
                showBadgeRefresh(true)

            }

        }

    }

    private fun setRecyclerView(listItem: ArrayList<BaseCampModel>) {

        val rvAdapter = BaseCampRecyclerViewAdapter(listItem, object: BaseCampRecyclerViewAdapter.ItemClickListener {
            override fun onItemClick(data: BaseCampModel?) {
                navigateItemAction(data)
            }

        })

        context?.let { ctx ->
            binding.rvChatList.layoutManager = LinearLayoutManager(ctx)
        }

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

    private fun navigateItemAction(data: BaseCampModel? = null) {

        if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_ADMIN_CITY) {

            val intent = Intent(requireContext(), AddBaseCampActivity::class.java)
            intent.putExtra(EDIT_CONTACT, true)
            intent.putExtra(CONST_CONTACT_ID, data?.id_gudang)
            intent.putExtra(CONST_PHONE, data?.nomorhp_gudang)
            intent.putExtra(CONST_NAME, data?.nama_gudang)
            intent.putExtra(CONST_LOCATION, data?.id_city)
            intent.putExtra(CONST_MAPS, data?.location_gudang)
            someActivityResultLauncher.launch(intent)
        } else {

            val intent = Intent(requireContext(), NewReportActivity::class.java)
            intent.putExtra(CONST_IS_BASE_CAMP, true)
            intent.putExtra(CONST_CONTACT_ID, data?.id_gudang)
            intent.putExtra(CONST_NAME, data?.nama_gudang)
            intent.putExtra(CONST_MAPS, data?.location_gudang)
            (requireContext() as Activity).startActivity(intent)
        }

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

    private fun navigateFab() {

        val intent = Intent(requireContext(), AddBaseCampActivity::class.java)
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
    }

    private fun showBadgeRefresh(action: Boolean) {
        val tvTitle = badgeRefresh.findViewById<TextView>(R.id.tvTitle)
        val icClose = badgeRefresh.findViewById<ImageView>(R.id.icClose)
        icClose.setOnClickListener { badgeRefresh.visibility = View.GONE }

        if (action) {
            badgeRefresh.visibility = View.VISIBLE
            tvTitle.setOnClickListener { getContacts() }
        } else badgeRefresh.visibility = View.GONE
    }

    private val someActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // Handle the result
        val resultCode = result.resultCode
        val data = result.data
        // Process the result

        if (resultCode == RESULT_BASECAMP_FRAGMENT) {
            val newData = data?.getStringExtra(REQUEST_BASECAMP_FRAGMENT)
            if (!newData.isNullOrEmpty() && newData == SYNC_NOW) getContacts()
        }
    }

    private fun getCities() {

        loadingState(true)
        showBadgeRefresh(false)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getCities(distributorID = userDistributorid)

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
                        getContacts()
                        binding.filterLayout.componentFilter.visibility = View.VISIBLE
//                        binding.filterLayout.componentFilter.visibility = View.GONE

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(requireActivity(), "LIST CITY", "Daftar kota kosong!")
                        getContacts()

                    }
                    else -> {

                        handleMessage(requireActivity(), TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        loadingState(true, getString(R.string.failed_get_data))
                        showBadgeRefresh(true)

                    }
                }


            } catch (e: Exception) {

                if (e is CancellationException) {
                    return@launch
                }
                handleMessage(requireActivity(), TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))
                showBadgeRefresh(true)

            }

        }
    }

    private fun setupDialogSearch(items: ArrayList<ModalSearchModel> = ArrayList()) {

        searchModal = SearchModal(requireActivity(), items)
        searchModal.setCustomDialogListener(object: SearchModal.SearchModalListener{
            override fun onDataReceived(data: ModalSearchModel) {
                if (data.id == "-1") {
                    selectedCity = null
                    binding.filterLayout.tvFilter.text = getString(R.string.tidak_ada_filter)
                } else {
                    selectedCity = data
                    binding.filterLayout.tvFilter.text = data.title
                }
                getContacts()
            }

        })
        searchModal.searchHint = "Ketik untuk mencari…"

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) binding.filterLayout.componentFilter.background = AppCompatResources.getDrawable(requireContext(), R.color.black_400)
        else binding.filterLayout.componentFilter.background = AppCompatResources.getDrawable(requireContext(), R.color.light)
        binding.filterLayout.componentFilter.visibility = View.GONE
        binding.filterLayout.componentFilter.setOnClickListener {
            searchModal.show()
        }

    }

}