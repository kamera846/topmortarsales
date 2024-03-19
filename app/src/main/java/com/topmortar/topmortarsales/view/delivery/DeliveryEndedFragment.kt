package com.topmortar.topmortarsales.view.delivery

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
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.R.anim
import com.topmortar.topmortarsales.R.color
import com.topmortar.topmortarsales.R.string
import com.topmortar.topmortarsales.adapter.recyclerview.HistoryDeliveryRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.CONST_DELIVERY_ID
import com.topmortar.topmortarsales.commons.CONST_IS_TRACKING_HISTORY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.FragmentDeliveryEndedBinding
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.CityModel
import com.topmortar.topmortarsales.model.DeliveryModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import com.topmortar.topmortarsales.view.MapsActivity
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 */
class DeliveryEndedFragment : Fragment() {

    private var _binding: FragmentDeliveryEndedBinding? = null
    private val binding get() = _binding!!

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
        getList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeliveryEndedBinding.inflate(inflater, container, false)
        val view = binding.root

        badgeRefresh = view.findViewById(R.id.badgeRefresh)

        sessionManager = SessionManager(requireContext())
        userKind = sessionManager.userKind().toString()
        userCity = sessionManager.userCityID().toString()
        userID = sessionManager.userID().toString()

        if (userKind == USER_KIND_ADMIN) getCities()
        getList()

        return view
    }

    private fun getList() {

        loadingState(true)
        showBadgeRefresh(false)

//        Handler(Looper.getMainLooper()).postDelayed({
//            loadingState(true, "Belum ada pengiriman yang diselesaikan!")
//        }, 1000)
//
//        return

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = when (userKind) {
                    USER_KIND_ADMIN -> {
                        if (selectedCity != null) apiService.getDeliveryByCity(cityId = selectedCity?.id!!, distributorID = userDistributorid)
                        else apiService.getDelivery(distributorID = userDistributorid)
                    } else -> apiService.getDeliveryByCity(cityId = userCity, distributorID = userDistributorid)
                }

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        setRecyclerView(response.results)
                        loadingState(false)
                        showBadgeRefresh(false)
                        listener?.counterItem(response.results.size)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Belum ada kiriman yang diselesaikan hari ini!")
                        showBadgeRefresh(false)
                        listener?.counterItem(0)

                    }
                    else -> {

                        handleMessage(requireContext(), TAG_RESPONSE_CONTACT, getString(string.failed_get_data))
                        loadingState(true, getString(string.failed_request))
                        showBadgeRefresh(true)

                    }
                }

            } catch (e: Exception) {

                handleMessage(requireContext(), TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(string.failed_request))
                showBadgeRefresh(true)

            }

        }

    }

    private fun setRecyclerView(listItem: ArrayList<DeliveryModel.History>) {

        val rvAdapter = HistoryDeliveryRecyclerViewAdapter(listItem, object: HistoryDeliveryRecyclerViewAdapter.ItemClickListener {
            override fun onItemClick(data: DeliveryModel.History?) {
                val intent = Intent(requireContext(), MapsActivity::class.java)
                intent.putExtra(CONST_IS_TRACKING_HISTORY, true)
                intent.putExtra(CONST_DELIVERY_ID, data?.id_delivery)
                startActivity(intent)
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
                                anim.rv_item_fade_slide_down
                            )
                        )
                        lastScrollPosition = firstVisibleItemPosition
                    }
                } else lastScrollPosition = -1
            }
        })

    }

    private fun loadingState(state: Boolean, message: String = getString(string.txt_loading)) {

        binding.txtLoading.text = message

        if (state) {

            binding.txtLoading.visibility = View.VISIBLE
            binding.rvChatList.visibility = View.GONE

        } else {

            binding.txtLoading.visibility = View.GONE
            binding.rvChatList.visibility = View.VISIBLE

        }

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

    private fun getCities() {

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
                        binding.llFilter.componentFilter.visibility = View.VISIBLE
//                        binding.llFilter.componentFilter.visibility = View.GONE

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(requireActivity(), "LIST CITY", "Daftar kota kosong!")

                    }
                    else -> {

                        handleMessage(requireActivity(), TAG_RESPONSE_CONTACT, getString(string.failed_get_data))

                    }
                }


            } catch (e: Exception) {

                handleMessage(requireActivity(), TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)

            }

        }
    }

    private fun setupDialogSearch(items: ArrayList<ModalSearchModel> = ArrayList()) {

        searchModal = SearchModal(requireActivity(), items)
        searchModal.setCustomDialogListener(object: SearchModal.SearchModalListener{
            override fun onDataReceived(data: ModalSearchModel) {
                if (data.id == "-1") {
                    selectedCity = null
                    binding.llFilter.tvFilter.text = getString(string.tidak_ada_filter)
                } else {
                    selectedCity = data
                    binding.llFilter.tvFilter.text = data.title
                }
                getList()
            }

        })
        searchModal.searchHint = "Ketik untuk mencari…"

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) binding.llFilter.componentFilter.background = AppCompatResources.getDrawable(requireContext(), color.black_400)
        else binding.llFilter.componentFilter.background = AppCompatResources.getDrawable(requireContext(), color.light)
        binding.llFilter.componentFilter.visibility = View.GONE
        binding.llFilter.componentFilter.setOnClickListener {
            searchModal.show()
        }

    }

}