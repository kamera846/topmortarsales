package com.topmortar.topmortarsales.view.delivery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.recyclerview.HistoryDeliveryRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_IS_BASE_CAMP
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.EDIT_CONTACT
import com.topmortar.topmortarsales.commons.REQUEST_BASECAMP_FRAGMENT
import com.topmortar.topmortarsales.commons.RESULT_BASECAMP_FRAGMENT
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN_CITY
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.databinding.FragmentDeliveryEndedBinding
import com.topmortar.topmortarsales.model.BaseCampModel
import com.topmortar.topmortarsales.view.courier.AddBaseCampActivity
import com.topmortar.topmortarsales.view.reports.NewReportActivity

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
        getList()

        return view
    }

    private fun getList() {

        loadingState(true)
        showBadgeRefresh(false)

        Handler().postDelayed({
            loadingState(true, "Belum ada pengiriman yang diselesaikan!")
        }, 1000)

//        lifecycleScope.launch {
//            try {
//
//                val apiService: ApiService = HttpClient.create()
//                val response = when (userKind) {
//                    USER_KIND_ADMIN -> apiService.getListBaseCamp(distributorID = userDistributorid)
//                    else -> apiService.getListBaseCamp(distributorID = userDistributorid, cityId = userCity)
//                }
//
//                when (response.status) {
//                    RESPONSE_STATUS_OK -> {
//
//                        setRecyclerView(response.results)
//                        loadingState(false)
//                        showBadgeRefresh(false)
//                        listener?.counterItem(response.results.size)
//
//                    }
//                    RESPONSE_STATUS_EMPTY -> {
//
//                        loadingState(true, "Belum ada basecamp!")
//                        showBadgeRefresh(false)
//                        listener?.counterItem(0)
//
//                    }
//                    else -> {
//
//                        handleMessage(requireContext(), TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
//                        loadingState(true, getString(R.string.failed_request))
//                        showBadgeRefresh(true)
//
//                    }
//                }
//
//
//            } catch (e: Exception) {
//
//                handleMessage(requireContext(), TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
//                loadingState(true, getString(R.string.failed_request))
//                showBadgeRefresh(true)
//
//            }
//
//        }

    }

    private fun setRecyclerView(listItem: ArrayList<BaseCampModel>) {

        val rvAdapter = HistoryDeliveryRecyclerViewAdapter(listItem, object: HistoryDeliveryRecyclerViewAdapter.ItemClickListener {
            override fun onItemClick(data: BaseCampModel?) {
                navigateItemAction(data)
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

    private val someActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // Handle the result
        val resultCode = result.resultCode
        val data = result.data
        // Process the result

        if (resultCode == RESULT_BASECAMP_FRAGMENT) {
            val data = data?.getStringExtra(REQUEST_BASECAMP_FRAGMENT)
            if (!data.isNullOrEmpty() && data == SYNC_NOW) getList()
        }
    }

}