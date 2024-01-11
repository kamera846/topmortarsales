package com.topmortar.topmortarsales.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.recyclerview.DeliveryRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.CONST_DELIVERY_ID
import com.topmortar.topmortarsales.commons.CONST_IS_TRACKING
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_DELIVERY
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityDeliveryBinding
import com.topmortar.topmortarsales.model.DeliveryModel


class DeliveryActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private val userDistributorId get() = sessionManager.userDistributor()
    private lateinit var binding: ActivityDeliveryBinding
    private lateinit var apiService: ApiService
    private lateinit var firebaseReference : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        sessionManager = SessionManager(this)
        binding = ActivityDeliveryBinding.inflate(layoutInflater)
        apiService = HttpClient.create()

        setContentView(binding.root)

        firebaseReference = FirebaseUtils().getReference(distributorId = userDistributorId!!)

        binding.titleBarDark.icBack.setOnClickListener { finish() }

        getList()
    }

    private fun getList() {

        loadingState(true)

        // Get a reference to your database
        val myRef: DatabaseReference = firebaseReference.child(FIREBASE_CHILD_DELIVERY)

        // Add a ValueEventListener to retrieve the data
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // The dataSnapshot contains the data from the database
                val deliveryList = arrayListOf<DeliveryModel.Delivery>()

                for (childSnapshot in dataSnapshot.children) {
                    // Convert each child node to a Delivery object
                    val delivery = childSnapshot.getValue(DeliveryModel.Delivery::class.java)
                    delivery?.let {
                        deliveryList.add(it)
                    }
                }

                if (deliveryList.isNotEmpty()) {
                    setRecyclerView(deliveryList)
                    loadingState(false)
                } else {
                    loadingState(true, "Belum ada pengiriman yang berjalan!")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                handleMessage(this@DeliveryActivity, TAG_RESPONSE_CONTACT,
                    "Failed run service. Exception $databaseError"
                )
                loadingState(true, getString(R.string.failed_request))
            }
        })

    }

    private fun setRecyclerView(listItem: ArrayList<DeliveryModel.Delivery>) {
        val rvAdapter = DeliveryRecyclerViewAdapter(listItem, object: DeliveryRecyclerViewAdapter.ItemClickListener {
            override fun onItemClick(data: DeliveryModel.Delivery?) {
                // Do Something
                val intent = Intent(this@DeliveryActivity, MapsActivity::class.java)
                intent.putExtra(CONST_IS_TRACKING, true)
                intent.putExtra(CONST_DELIVERY_ID, data?.id)
                startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_CODE)
            }

        })

        binding.rvList.apply {
            layoutManager = LinearLayoutManager(this@DeliveryActivity)
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

        binding.txtLoading.text = message

        if (state) {

            binding.txtLoading.visibility = View.VISIBLE
            binding.rvList.visibility = View.GONE

        } else {

            binding.txtLoading.visibility = View.GONE
            binding.rvList.visibility = View.VISIBLE

        }

    }
}