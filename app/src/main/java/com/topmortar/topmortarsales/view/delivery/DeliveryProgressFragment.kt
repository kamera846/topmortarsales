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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.recyclerview.DeliveryRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_DELIVERY_ID
import com.topmortar.topmortarsales.commons.CONST_IS_TRACKING
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_DELIVERY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.FragmentDeliveryProgressBinding
import com.topmortar.topmortarsales.model.DeliveryModel
import com.topmortar.topmortarsales.view.MapsActivity
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 */
class DeliveryProgressFragment : Fragment() {

    private var _binding: FragmentDeliveryProgressBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseReference : DatabaseReference
    private lateinit var sessionManager: SessionManager
    private val userKind get() = sessionManager.userKind().toString()
    private val userDistributorId get() = sessionManager.userDistributor().toString()
    private val userCity get() = sessionManager.userCityID().toString()

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
        _binding = FragmentDeliveryProgressBinding.inflate(inflater, container, false)
        val view = binding.root

        badgeRefresh = view.findViewById(R.id.badgeRefresh)

        sessionManager = SessionManager(requireContext())
        firebaseReference = FirebaseUtils().getReference(distributorId = userDistributorId)

        getList()

        return view
    }

    private fun getList() {

        loadingState(true)
        showBadgeRefresh(false)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()

                val response = when (userKind) {
                    USER_KIND_ADMIN -> {
//                        if (selectedCity != null) apiService.sjNotClosing(idCity = selectedCity?.id!!, distributorID = userDistributorid)
//                        else apiService.sjNotClosing(distributorID = userDistributorid)
                        apiService.sjNotClosing(distributorID = userDistributorId)
                    } else -> apiService.sjNotClosing(idCity = userCity, distributorID = userDistributorId)
                }

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        // List Target Store
                        val listStores = response.results

                        // Get a reference to your database
                        val myRef: DatabaseReference = firebaseReference.child(FIREBASE_CHILD_DELIVERY)

                        // Add a ValueEventListener to retrieve the data
                        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                // The dataSnapshot contains the data from the database
                                if (dataSnapshot.exists()) {
                                    val storeList = arrayListOf<DeliveryModel.Store>()

                                    for (delivery in dataSnapshot.children) {
                                        if (delivery.child("stores").exists()) {
                                            for (store in delivery.child("stores").children) {
                                                val storeData = store.getValue(DeliveryModel.Store::class.java)!!
                                                storeData.courier = delivery.child("courier").getValue(DeliveryModel.Courier::class.java)!!
                                                storeData.deliveryId = delivery.child("id").getValue(String::class.java)!!

                                                val validation = listStores.find { it.id_contact == storeData.id }
                                                if (validation != null) storeList.add(storeData)
                                            }
                                        }
                                    }

                                    if (storeList.isNotEmpty()) {
                                        listener?.counterItem(storeList.size)
                                        setRecyclerView(storeList)
                                        loadingState(false)
                                        showBadgeRefresh(false)
                                    } else {
                                        listener?.counterItem(0)
                                        loadingState(true, "Belum ada pengiriman yang berlangsung.")
                                        showBadgeRefresh(false)
                                    }
                                } else {
                                    listener?.counterItem(0)
                                    loadingState(true, "Belum ada pengiriman yang berlangsung.")
                                    showBadgeRefresh(false)
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Handle errors
                                handleMessage(requireContext(), TAG_RESPONSE_CONTACT,
                                    "Failed run service. Exception $databaseError"
                                )
                                loadingState(true, getString(R.string.failed_request))
                                showBadgeRefresh(true)
                            }
                        })


                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Belum ada pengiriman yang berlangsung.")
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

                handleMessage(requireContext(), TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message + e.stackTraceToString())
                loadingState(true, getString(R.string.failed_request))
                showBadgeRefresh(true)

            }

        }

    }

    private fun setRecyclerView(listItem: ArrayList<DeliveryModel.Store>) {
        val rvAdapter = DeliveryRecyclerViewAdapter(listItem, object: DeliveryRecyclerViewAdapter.ItemClickListener {
            override fun onItemClick(data: DeliveryModel.Store?) {
                // Do Something
                val intent = Intent(requireContext(), MapsActivity::class.java)
                intent.putExtra(CONST_IS_TRACKING, true)
                intent.putExtra(CONST_DELIVERY_ID, data?.deliveryId)
                intent.putExtra(CONST_CONTACT_ID, data?.id)
                startActivity(intent)
            }

        })

        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerview.adapter = rvAdapter
        binding.recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
            binding.recyclerview.visibility = View.GONE

        } else {

            binding.txtLoading.visibility = View.GONE
            binding.recyclerview.visibility = View.VISIBLE

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