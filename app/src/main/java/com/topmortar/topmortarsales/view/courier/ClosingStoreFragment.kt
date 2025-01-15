package com.topmortar.topmortarsales.view.courier

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.ContactsRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_COURIER
import com.topmortar.topmortarsales.commons.CONST_ADDRESS
import com.topmortar.topmortarsales.commons.CONST_BIRTHDAY
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_DATE
import com.topmortar.topmortarsales.commons.CONST_KTP
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_OWNER
import com.topmortar.topmortarsales.commons.CONST_PAYMENT_METHOD
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.CONST_PROMO
import com.topmortar.topmortarsales.commons.CONST_REPUTATION
import com.topmortar.topmortarsales.commons.CONST_STATUS
import com.topmortar.topmortarsales.commons.CONST_TERMIN
import com.topmortar.topmortarsales.commons.CONST_WEEKLY_VISIT_STATUS
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_DELIVERY
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.EventBusUtils
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.FragmentClosingStoreBinding
import com.topmortar.topmortarsales.model.ContactModel
import com.topmortar.topmortarsales.model.DeliveryModel
import com.topmortar.topmortarsales.view.contact.DetailContactActivity
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * A fragment representing a list of Items.
 */
class ClosingStoreFragment : Fragment() {

    private var _binding: FragmentClosingStoreBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private val userFullName get() = sessionManager.fullName().toString()
    private val userID get() = sessionManager.userID().toString()
    private val userCityID get() = sessionManager.userCityID().toString()
    private val userDistributorID get() = sessionManager.userDistributor().toString()

    // Delivery
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var deliveryId = ""
    private lateinit var firebaseReference: DatabaseReference
    private lateinit var childDelivery: DatabaseReference
    private lateinit var childDriver: DatabaseReference

    private lateinit var badgeRefresh: LinearLayout

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
        _binding = FragmentClosingStoreBinding.inflate(inflater, container, false)
        val view = binding.root

        badgeRefresh = view.findViewById(R.id.badgeRefresh)

        sessionManager = SessionManager(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        deliveryId = "$AUTH_LEVEL_COURIER$userID"
        val userDistributorIds = sessionManager.userDistributor()
        firebaseReference = FirebaseUtils().getReference(distributorId = userDistributorIds ?: "-firebase-008")
        childDelivery = firebaseReference.child(FIREBASE_CHILD_DELIVERY)
        childDriver = childDelivery.child(deliveryId)

        binding.btnFabAdmin.setOnClickListener { navigateChatAdmin() }

        getContacts()
        binding.swipeRefreshLayout.setOnRefreshListener { getContacts() }

        return view
    }

    private fun getContacts() {

        loadingState(true)
        showBadgeRefresh(false)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getCourierStoreClosing(idCity = userCityID, distributorID = userDistributorID)
//                val response = apiService.getCourierStore(processNumber = "1", courierId = userID)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

//                        response.results[0].id_contact = "909"
//                        response.results[0].nama = "Toko Rafli"

                        val contacts = response.results
//                        val realItem = response.results[0]
//                        val contacts = arrayListOf<ContactModel>()
//
//                        for (i in 0 until 10) {
//                            val objek = realItem.copy()
//                            objek.id_contact = (objek.id_contact.toInt() + i).toString()
//                            objek.nama = objek.nama + " " + i
//                            contacts.add(objek)
//                        }

                        // Get a reference to your database
                        val deliveryId = AUTH_LEVEL_COURIER + userID
                        val userDistributorIds = sessionManager.userDistributor()
                        val firebaseReference = FirebaseUtils().getReference(distributorId = userDistributorIds ?: "-firebase-009")
                        val myRef: DatabaseReference = firebaseReference.child("$FIREBASE_CHILD_DELIVERY/$deliveryId")

                        // Add a ValueEventListener to retrieve the data
                        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                // The dataSnapshot contains the data from the database
                                if (dataSnapshot.exists()) {
                                    myRef.child("stores").addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.exists()) {
                                                val deliveryStore = arrayListOf<DeliveryModel.Store>()
                                                for (item in snapshot.children) {
                                                    val data = item.getValue(DeliveryModel.Store::class.java)!!
                                                    deliveryStore.add(data)
                                                }

                                                for ((i, contact) in contacts.withIndex()) {
                                                    val findItem = deliveryStore.find { it.id == contact.id_contact && it.idSuratJalan == contact.id_surat_jalan }
                                                    contacts[i].deliveryStatus = "Pengiriman sedang berlangsung"
                                                    if (findItem == null) {
                                                        startDelivery(contact)
                                                    }
                                                }

                                                // Remove expired item
                                                for (store in deliveryStore.iterator()) {

                                                    val findItem = contacts.find { it.id_contact == store.id }

                                                    if (findItem == null) {

                                                        val storeRef = myRef.child("stores")
                                                        if (store.id.isNotEmpty()) storeRef.child(store.id).removeValue()

                                                    }

                                                }

                                                setRecyclerView(contacts)
                                                loadingState(false)
                                                showBadgeRefresh(false)
                                                listener?.counterItem(response.results.size)
                                            } else {

                                                for ((i, contact) in contacts.withIndex()) {

                                                    contacts[i].deliveryStatus = "Pengiriman sedang berlangsung"
                                                    startDelivery(contact)
                                                }

                                                setRecyclerView(contacts)
                                                loadingState(false)
                                                showBadgeRefresh(false)
                                                listener?.counterItem(response.results.size)
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            setRecyclerView(contacts)
                                            loadingState(false)
                                            showBadgeRefresh(false)
                                            listener?.counterItem(response.results.size)
                                        }

                                    })

                                } else {
                                    setRecyclerView(contacts)
                                    loadingState(false)
                                    showBadgeRefresh(false)
                                    listener?.counterItem(response.results.size)
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Handle errors
                                setRecyclerView(contacts)
                                loadingState(false)
                                showBadgeRefresh(false)
                                listener?.counterItem(response.results.size)
                            }
                        })

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Belum ada pengiriman!")
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

    private fun setRecyclerView(listItem: ArrayList<ContactModel>) {

        val rvAdapter = ContactsRecyclerViewAdapter(listItem, object: ContactsRecyclerViewAdapter.ItemClickListener {
            override fun onItemClick(data: ContactModel?) {
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

    private fun navigateDetailContact(data: ContactModel? = null) {

        val intent = Intent(requireContext(), DetailContactActivity::class.java)

        if (data != null) {
            intent.putExtra(ACTIVITY_REQUEST_CODE, MAIN_ACTIVITY_REQUEST_CODE)
            intent.putExtra(CONST_CONTACT_ID, data.id_contact)
            intent.putExtra(CONST_NAME, data.nama)
            intent.putExtra(CONST_PHONE, data.nomorhp)
            intent.putExtra(CONST_BIRTHDAY, data.tgl_lahir)
            intent.putExtra(CONST_OWNER, data.store_owner)
            intent.putExtra(CONST_LOCATION, data.id_city)
            intent.putExtra(CONST_MAPS, data.maps_url)
            intent.putExtra(CONST_ADDRESS, data.address)
            intent.putExtra(CONST_STATUS, data.store_status)
            intent.putExtra(CONST_KTP, data.ktp_owner)
            intent.putExtra(CONST_PAYMENT_METHOD, data.payment_method)
            intent.putExtra(CONST_TERMIN, data.termin_payment)
            intent.putExtra(CONST_PROMO, data.id_promo)
            intent.putExtra(CONST_REPUTATION, data.reputation)
            intent.putExtra(CONST_DATE, data.created_at)
            intent.putExtra(CONST_WEEKLY_VISIT_STATUS, data.tagih_mingguan)
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

    private fun navigateChatAdmin() {
        val distributorNumber = sessionManager.userDistributorNumber()!!
        val phoneNumber = distributorNumber.ifEmpty { getString(R.string.topmortar_wa_number) }
        val message = "*#Courier Service*\nHalo admin, tolong bantu saya [KETIK PESAN ANDA]"

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}")

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "Gagal mengarahkan ke whatsapp", TOAST_SHORT).show()
        }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        if (resultCode == RESULT_OK) {
            val resultData = data?.getStringExtra("$MAIN_ACTIVITY_REQUEST_CODE")
            if (!resultData.isNullOrEmpty() && resultData == SYNC_NOW) getContacts()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startDelivery(contact: ContactModel) {
        val targetLatLng = CustomUtility(requireContext()).latLngConverter(contact.maps_url)
        if (targetLatLng != null) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { currentLatLng: Location ->

                    val courierModel = DeliveryModel.Courier(
                        id = userID,
                        name = userFullName
                    )

                    val store = DeliveryModel.Store(
                        id = contact.id_contact,
                        idSuratJalan = contact.id_surat_jalan,
                        name = contact.nama,
                        lat = targetLatLng.latitude,
                        lng = targetLatLng.longitude,
                        startDatetime = DateFormat.now(),
                        startLat = currentLatLng.latitude,
                        startLng = currentLatLng.longitude,
                    )

                    childDriver.child("id").setValue(deliveryId)
                    childDriver.child("idSuratJalan").setValue(deliveryId)
                    childDriver.child("lat").setValue(currentLatLng.latitude)
                    childDriver.child("lng").setValue(currentLatLng.longitude)
                    childDriver.child("courier").setValue(courierModel)
                    childDriver.child("stores/${store.id}").setValue(store)

                }.addOnFailureListener {
                    handleMessage(requireContext(), "onStartDelivery", "Failed get user lastLocation")
                    Log.e("onStartDelivery", "Failed get user lastLocation: $it")
                }
        }
    }

}