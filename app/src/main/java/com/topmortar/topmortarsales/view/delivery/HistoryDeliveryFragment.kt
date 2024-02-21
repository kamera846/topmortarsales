package com.topmortar.topmortarsales.view.delivery

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.recyclerview.HistoryDeliveryRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.CONST_DELIVERY_ID
import com.topmortar.topmortarsales.commons.CONST_IS_TRACKING
import com.topmortar.topmortarsales.commons.CONST_IS_TRACKING_HISTORY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.FragmentHistoryDeliveryBinding
import com.topmortar.topmortarsales.model.DeliveryModel
import com.topmortar.topmortarsales.view.MapsActivity
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 */
class HistoryDeliveryFragment : Fragment() {

    private var _binding: FragmentHistoryDeliveryBinding? = null
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
        _binding = FragmentHistoryDeliveryBinding.inflate(inflater, container, false)
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

//        Handler().postDelayed({
//            loadingState(true, "Belum ada riwayat pengiriman!")
//        }, 1000)
//
//        return

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getDelivery(idCourier = userID, distributorID = userDistributorid)

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

                handleMessage(requireContext(), TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))
                showBadgeRefresh(true)

            }

        }

    }

    private fun setRecyclerView(listItem: ArrayList<DeliveryModel.History>) {

        val rvAdapter = HistoryDeliveryRecyclerViewAdapter(listItem, object: HistoryDeliveryRecyclerViewAdapter.ItemClickListener {
            override fun onItemClick(data: DeliveryModel.History?) {
                val intent = Intent(requireContext(), MapsActivity::class.java)
                intent.putExtra(CONST_IS_TRACKING, true)
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
                                R.anim.rv_item_fade_slide_down
                            )
                        )
                        lastScrollPosition = firstVisibleItemPosition
                    }
                } else lastScrollPosition = -1
            }
        })

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

}