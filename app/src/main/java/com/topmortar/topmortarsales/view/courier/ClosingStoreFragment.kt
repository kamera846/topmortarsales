package com.topmortar.topmortarsales.view.courier

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.ContactsRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.CONST_ADDRESS
import com.topmortar.topmortarsales.commons.CONST_BIRTHDAY
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_KTP
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_OWNER
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.CONST_PROMO
import com.topmortar.topmortarsales.commons.CONST_STATUS
import com.topmortar.topmortarsales.commons.CONST_TERMIN
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.utils.EventBusUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.FragmentClosingStoreBinding
import com.topmortar.topmortarsales.model.ContactModel
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
    private lateinit var userKind: String
    private lateinit var userCity: String
    private lateinit var userID: String

    private var listener: CounterItem? = null
    interface CounterItem {
        fun counterItem(count: Int)
    }
    fun setCounterItem(listener: CounterItem) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClosingStoreBinding.inflate(inflater, container, false)
        val view = binding.root

        sessionManager = SessionManager(requireContext())
        userKind = sessionManager.userKind().toString()
        userCity = sessionManager.userCityID().toString()
        userID = sessionManager.userID().toString()
        binding.btnFabAdmin.setOnClickListener { navigateChatAdmin() }

        getContacts()

        return view
    }

    private fun getContacts() {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getCourierStore(processNumber = "1", courierId = userID)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        setRecyclerView(response.results)
                        loadingState(false)
                        listener?.counterItem(response.results.size)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Daftar kontak kosong!")

                    }
                    else -> {

                        handleMessage(requireContext(), TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        loadingState(true, getString(R.string.failed_request))

                    }
                }


            } catch (e: Exception) {

                handleMessage(requireContext(), TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))

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
            intent.putExtra(CONST_TERMIN, data.termin_payment)
            intent.putExtra(CONST_PROMO, data.id_promo)
        }

        (requireContext() as Activity).startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_CODE)

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

    private fun navigateChatAdmin() {
        val phoneNumber = getString(R.string.topmortar_wa_number)
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

}