package com.topmortar.topmortarsales.view.delivery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.recyclerview.SuratJalanNotClosingRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_DELIVERY_ID
import com.topmortar.topmortarsales.commons.CONST_IS_TRACKING
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_DELIVERY
import com.topmortar.topmortarsales.commons.REQUEST_BASECAMP_FRAGMENT
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.RESULT_BASECAMP_FRAGMENT
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.FragmentDeliveryTargetBinding
import com.topmortar.topmortarsales.model.DeliveryModel
import com.topmortar.topmortarsales.model.SuratJalanNotClosingModel
import com.topmortar.topmortarsales.view.MapsActivity
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 */
class DeliveryTargetFragment : Fragment() {

    private var _binding: FragmentDeliveryTargetBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseReference : DatabaseReference
    private lateinit var sessionManager: SessionManager
    private lateinit var userDistributorId : String
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
        _binding = FragmentDeliveryTargetBinding.inflate(inflater, container, false)
        val view = binding.root

        badgeRefresh = view.findViewById(R.id.badgeRefresh)

        sessionManager = SessionManager(requireContext())
        userDistributorId = sessionManager.userDistributor().toString()
        userKind = sessionManager.userKind().toString()
        userCity = sessionManager.userCityID().toString()
        userID = sessionManager.userID().toString()

        firebaseReference = FirebaseUtils().getReference(distributorId = userDistributorId)

        getList()

        return view
    }

    private fun getList() {

        loadingState(true)
        showBadgeRefresh(false)

//        Handler().postDelayed({
//            loadingState(true, "Belum ada pengiriman yang diselesaikan!")
//        }, 1000)
//
//        return

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
//                val response = when (userKind) {
//                    USER_KIND_ADMIN -> apiService.sjNotClosing()
//                    else -> apiService.sjNotClosing(idCity = userID)
//                }
                val response = apiService.sjNotClosing()

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        // Get a reference to your database
                        val myRef: DatabaseReference = firebaseReference.child(
                            FIREBASE_CHILD_DELIVERY
                        )

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
                                                storeData.courier = delivery.child("courier").getValue(
                                                    DeliveryModel.Courier::class.java)!!
                                                storeData.deliveryId = delivery.child("id").getValue(String::class.java)!!
                                                storeList.add(storeData)
                                            }
                                        }
                                    }

                                    if (storeList.isNotEmpty()) {
                                        val storeTarget = response.results

                                        for ((i, item) in storeTarget.withIndex()) {
                                            val findStoreProcessed = storeList.find { it.id == item.id_contact }
                                            if (findStoreProcessed != null) {
                                                storeTarget[i].deliveryId = findStoreProcessed.deliveryId
                                                storeTarget[i].dateProcessed = findStoreProcessed.startDatetime
                                            }
                                        }

                                        setRecyclerView(storeTarget)
                                        loadingState(false)
                                        showBadgeRefresh(false)
                                        listener?.counterItem(response.results.size)
                                    } else {
                                        setRecyclerView(response.results)
                                        loadingState(false)
                                        showBadgeRefresh(false)
                                        listener?.counterItem(response.results.size)
                                    }
                                } else {
                                    setRecyclerView(response.results)
                                    loadingState(false)
                                    showBadgeRefresh(false)
                                    listener?.counterItem(response.results.size)
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Handle errors
                                handleMessage(requireContext(), TAG_RESPONSE_CONTACT,
                                    "Failed run service. Exception $databaseError"
                                )

                                setRecyclerView(response.results)
                                loadingState(false)
                                showBadgeRefresh(false)
                                listener?.counterItem(response.results.size)
                            }
                        })

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Belum ada target kiriman!")
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

    private fun setRecyclerView(listItem: ArrayList<SuratJalanNotClosingModel>) {

        val rvAdapter = SuratJalanNotClosingRecyclerViewAdapter(listItem, object: SuratJalanNotClosingRecyclerViewAdapter.ItemClickListener {
            override fun onItemClick(data: SuratJalanNotClosingModel?) {
                if (data != null && data.dateProcessed.isNotEmpty()) {
                    val intent = Intent(requireContext(), MapsActivity::class.java)
                    intent.putExtra(CONST_IS_TRACKING, true)
                    intent.putExtra(CONST_DELIVERY_ID, data.deliveryId)
                    intent.putExtra(CONST_CONTACT_ID, data.id_contact)
                    startActivity(intent)
                }
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

    private fun navigateItemAction() {

        val intent = Intent(requireContext(), HistoryDeliveryActivity::class.java)
        (requireContext() as Activity).startActivity(intent)

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