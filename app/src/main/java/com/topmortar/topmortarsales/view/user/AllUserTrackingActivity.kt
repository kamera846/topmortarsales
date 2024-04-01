package com.topmortar.topmortarsales.view.user

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.recyclerview.AllUserTrackingRVA
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_ABSENT
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.utils.EventBusUtils
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.databinding.ActivityAllUserTrackingBinding
import com.topmortar.topmortarsales.model.UserAbsentModel
import org.greenrobot.eventbus.EventBus

class AllUserTrackingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllUserTrackingBinding
    private lateinit var sessionManager: SessionManager
    private val userDistributorId get() = sessionManager.userDistributor()
    private val userKind get() = sessionManager.userKind()
    private val userCity get() = sessionManager.userCityID()

    // Tracking
    private var firebaseReference: DatabaseReference? = null
    private var childAbsent: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        binding = ActivityAllUserTrackingBinding.inflate(layoutInflater)
        sessionManager = SessionManager(this)

        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.titleBar.icBack.visibility = View.VISIBLE
        binding.titleBar.icBack.setOnClickListener { finish() }

        binding.titleBar.tvTitleBar.text = "Daftar Pengguna Yang Dilacak"

        getList()
    }

    private fun getList() {

        loadingState(true)
        firebaseReference = FirebaseUtils().getReference(distributorId = userDistributorId ?: "null")
        childAbsent = firebaseReference?.child(FIREBASE_CHILD_ABSENT)

        try {
            childAbsent?.orderByChild("lastTracking")?.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val listUserTracking = arrayListOf<UserAbsentModel>()

                        for (item in snapshot.children) {
                            val userIdCity = item.child("idCity").getValue(String::class.java)
                            val userLat = item.child("lat").getValue(Double::class.java)
                            val userLng = item.child("lng").getValue(Double::class.java)

                            if (userLat != null && userLng != null) {
                                if (userKind == USER_KIND_ADMIN) {
                                    listUserTracking.add(
                                        UserAbsentModel(
                                            eveningDateTime = item.child("eveningDateTime").getValue(String::class.java) ?: "",
                                            fullname = item.child("fullname").getValue(String::class.java) ?: "",
                                            id = item.child("id").getValue(String::class.java) ?: "",
                                            isOnline = item.child("isOnline").getValue(Boolean::class.java) ?: false,
                                            lastSeen = item.child("lastSeen").getValue(String::class.java) ?: "",
                                            lastTracking = item.child("lastTracking").getValue(String::class.java) ?: "",
                                            lat = item.child("lat").getValue(Double::class.java) ?: 0.0,
                                            lng = item.child("lng").getValue(Double::class.java) ?: 0.0,
                                            morningDateTime = item.child("morningDateTime").getValue(String::class.java) ?: "",
                                            username = item.child("username").getValue(String::class.java) ?: "",
                                        )
                                    )
                                } else if (!userIdCity.isNullOrEmpty() && userCity == userIdCity) {
                                    listUserTracking.add(
                                        UserAbsentModel(
                                            eveningDateTime = item.child("eveningDateTime").getValue(String::class.java) ?: "",
                                            fullname = item.child("fullname").getValue(String::class.java) ?: "",
                                            id = item.child("id").getValue(String::class.java) ?: "",
                                            isOnline = item.child("isOnline").getValue(Boolean::class.java) ?: false,
                                            lastSeen = item.child("lastSeen").getValue(String::class.java) ?: "",
                                            lastTracking = item.child("lastTracking").getValue(String::class.java) ?: "",
                                            lat = item.child("lat").getValue(Double::class.java) ?: 0.0,
                                            lng = item.child("lng").getValue(Double::class.java) ?: 0.0,
                                            morningDateTime = item.child("morningDateTime").getValue(String::class.java) ?: "",
                                            username = item.child("username").getValue(String::class.java) ?: "",
                                        )
                                    )
                                }
                            }
                        }

                        if (listUserTracking.isNotEmpty()) {

                            val rvAdapter = AllUserTrackingRVA()
                            listUserTracking.sortByDescending { it.lastTracking }
                            rvAdapter.setList(ArrayList(listUserTracking))
                            rvAdapter.setOnItemClickListener(object: AllUserTrackingRVA.OnItemClickListener {
                                override fun onItemClick(item: UserAbsentModel) {
                                    // Do something here
                                    val messageEvent = EventBusUtils.UserAbsentModelEvent(item)
                                    EventBus.getDefault().post(messageEvent)
                                    finish()
                                }
                            })

                            binding.rvList.apply {
                                layoutManager = LinearLayoutManager(this@AllUserTrackingActivity)
                                adapter = rvAdapter
                            }

                            loadingState(false)
                        } else {
                            loadingState(true, "Belum ada pengguna yang bisa dilacak")
                        }
                    } else {
                        loadingState(true, "Belum ada pengguna yang bisa dilacak")
                        handleMessage(this@AllUserTrackingActivity, TAG_RESPONSE_CONTACT,
                            "Belum ada pengguna yang bisa dilacak"
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    loadingState(true, "Memuat data dibatalkan")
                    handleMessage(this@AllUserTrackingActivity, TAG_RESPONSE_CONTACT,
                        "Failed run service. Exception $error"
                    )
                }

            })
        } catch (e: Error) {
            loadingState(true, "Gagal memuat data. Error: ${e.message}")
            handleMessage(this, TAG_RESPONSE_CONTACT, e.message.toString())
        }
    }

    private fun loadingState(state: Boolean, message: String = getString(R.string.txt_loading)) {

        binding.txtLoading.text = message

        if (state) {

            binding.rlLoading.visibility = View.VISIBLE
            binding.rvList.visibility = View.GONE

        } else {

            binding.rlLoading.visibility = View.GONE
            binding.rvList.visibility = View.VISIBLE

        }

    }

}