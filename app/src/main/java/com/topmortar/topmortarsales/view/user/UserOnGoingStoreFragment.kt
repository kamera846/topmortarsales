package com.topmortar.topmortarsales.view.user

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.ContactsRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN_CITY
import com.topmortar.topmortarsales.commons.utils.EventBusUtils
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.PhoneHandler
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.FragmentUserOnGoingStoreBinding
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.ContactModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * A fragment representing a list of Items.
 */
class UserOnGoingStoreFragment : Fragment(), ContactsRecyclerViewAdapter.ItemClickListener,
    SearchModal.SearchModalListener {

    private var _binding: FragmentUserOnGoingStoreBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private val userDistributorId get() = sessionManager.userDistributor().toString()
    private lateinit var userKind: String
    private lateinit var userCity: String
    private lateinit var userID: String
    private var userCityParam: String? = ""
    private lateinit var searchModal: SearchModal
    private var selectedCity: ModalSearchModel? = null

    // Initialize Search Engine
    private val searchDelayMillis = 500L
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var previousSearchTerm = ""
    private var isSearchActive = false

    fun setUserCityParam(id: String?) {
        this.userCityParam = id
    }

    private var userIDParam: String? = ""
    fun setUserIdParam(id: String?) {
        this.userIDParam = id
    }


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
        _binding = FragmentUserOnGoingStoreBinding.inflate(inflater, container, false)
        val view = binding.root

        sessionManager = SessionManager(requireContext())
        userKind = sessionManager.userKind().toString()
        userCity = sessionManager.userCityID().toString()
        userID = sessionManager.userID().toString()

//        setupSearchBox()
        getContacts()

        return view
    }

    private fun setupSearchBox() {

        val padding16 = convertDpToPx(16, requireContext())
        val padding8 = convertDpToPx(8, requireContext())
        binding.searchBox.vBorderTop.visibility = View.VISIBLE
        binding.searchBox.icCloseSearch.visibility = View.GONE
        binding.searchBox.etSearchBox.setPadding(padding16,0,padding16,0)
        binding.searchBox.icClearSearch.visibility = View.INVISIBLE
        binding.searchBox.icClearSearch.setPadding(padding8,padding8,padding8,padding8)
        val params = binding.searchBox.icClearSearch.layoutParams
        params.width = convertDpToPx(40, requireContext())
        params.height = convertDpToPx(40, requireContext())
        binding.searchBox.icClearSearch.layoutParams = params

        binding.searchBox.etSearchBox.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val searchTerm = s.toString()

                if (searchTerm != previousSearchTerm) {
                    previousSearchTerm = searchTerm

                    searchRunnable?.let { searchHandler.removeCallbacks(it) }

                    searchRunnable = Runnable {
                        searchContact(searchTerm)
                    }

                    searchRunnable?.let { searchHandler.postDelayed(it, searchDelayMillis) }

                }

            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty()) {
                    binding.searchBox.icClearSearch.visibility = View.VISIBLE
                    isSearchActive = true
                } else {
                    binding.searchBox.icClearSearch.visibility = View.INVISIBLE
                    isSearchActive = false
                }
            }

        })

        binding.searchBox.icClearSearch.setOnClickListener {
            binding.searchBox.etSearchBox.setText("")
            isSearchActive = false
        }

        getContacts()

    }

    private fun getContacts() {
        loadingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getContactsUserBid(userId = userIDParam!!.ifEmpty { userID })
//                val response = when (userKind) {
//                    USER_KIND_ADMIN -> {
//                        if (selectedCity != null ) {
//                            if (selectedCity!!.id != "-1") apiService.getContacts(cityId = selectedCity!!.id!!) else apiService.getContacts(cityId = userCityParam!!)
//                        } else apiService.getContacts(cityId = userCityParam!!)
//                    } else -> apiService.getContacts(cityId = userCity)
//                }

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        binding.recyclerView.apply {
                            layoutManager = LinearLayoutManager(requireContext())
                            adapter = ContactsRecyclerViewAdapter(response.results, this@UserOnGoingStoreFragment)
                        }

//                        if (userKind == USER_KIND_ADMIN) getCities()
//                        else loadingState(false)
                        listener?.counterItem(response.results.size)
                        loadingState(false)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        listener?.counterItem(0)
                        loadingState(true, "Daftar kontak kosong!")

                    }
                    else -> {

                        handleMessage(requireContext(), TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        loadingState(true, getString(R.string.failed_request))

                    }
                }


            } catch (e: Exception) {

                FirebaseUtils.logErr(requireContext(), "Failed UserOnGoingStoreFragment on getContacts(). Catch: ${e.message}")
                handleMessage(requireContext(), TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))

            }

        }
    }

    private fun searchContact(key: String = "${binding.searchBox.etSearchBox.text}") {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val searchKey = createPartFromString(PhoneHandler.formatPhoneNumber62(key))
                val searchCity = createPartFromString(userCityParam!!.ifEmpty { userCity })
                val distributorId = createPartFromString(userDistributorId)

                val apiService: ApiService = HttpClient.create()
                val response = if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_ADMIN_CITY) {
                    if (selectedCity != null ) {
                        if (selectedCity!!.id != "-1") {
                            val cityId = createPartFromString(selectedCity!!.id!!)
                            apiService.searchContact(key = searchKey, cityId = cityId, distributorID = distributorId)
                        } else apiService.searchContact(key = searchKey, cityId = searchCity, distributorID = distributorId)
                    } else apiService.searchContact(key = searchKey, cityId = searchCity, distributorID = distributorId)
                } else apiService.searchContact(cityId = searchCity, key = searchKey, distributorID = distributorId)

                if (response.isSuccessful) {

                    val responseBody = response.body()!!
                    val textFilter = if (selectedCity != null && selectedCity?.id != "-1") selectedCity?.title else getString(R.string.all_cities)

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            binding.recyclerView.apply {
                                layoutManager = LinearLayoutManager(requireContext())
                                adapter = ContactsRecyclerViewAdapter(responseBody.results, this@UserOnGoingStoreFragment)
                            }
                            binding.tvFilter.text = "$textFilter (${responseBody.results.size})"
                            loadingState(false)

                        }
                        RESPONSE_STATUS_EMPTY -> {

                            loadingState(true, "Daftar kontak kosong!")
                            binding.tvFilter.text = "$textFilter (${responseBody.results.size})"

                        }
                        else -> {

                            handleMessage(requireContext(), TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                            loadingState(true, getString(R.string.failed_request))

                        }
                    }

                } else {

                    handleMessage(requireContext(), TAG_RESPONSE_CONTACT, "Failed get data! Message: " + response.message())
                    loadingState(true, getString(R.string.failed_request))

                }


            } catch (e: Exception) {

                FirebaseUtils.logErr(requireContext(), "Failed UserOnGoingStoreFragment on searchContact(). Catch: ${e.message}")
                handleMessage(requireContext(), TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))

            }

        }

    }

    private fun getCities() {

        loadingState(true)

        // Get Cities
        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getCities(distributorID = userDistributorId)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        val results = response.results
                        val items: ArrayList<ModalSearchModel> = ArrayList()

                        items.add(ModalSearchModel("-1", "Hapus filter"))
                        for (i in 0 until results.size) {
                            val data = results[i]
                            items.add(ModalSearchModel(data.id_city, "${data.nama_city} - ${data.kode_city}"))
                        }

                        setupFilterContacts(items)
                        loadingState(false)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(requireContext(), "LIST CITY", "Daftar kota kosong!")
                        loadingState(false)

                    }
                    else -> {

                        handleMessage(requireContext(), TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        loadingState(false)

                    }
                }


            } catch (e: Exception) {

                FirebaseUtils.logErr(requireContext(), "Failed UserOnGoingStoreFragment on getCities(). Catch: ${e.message}")
                handleMessage(requireContext(), TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(false)

            }

        }
    }

    private fun setupFilterContacts(items: ArrayList<ModalSearchModel> = ArrayList()) {

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) binding.filterBox.background = requireContext().getDrawable(R.color.black_400)
        else binding.filterBox.background = requireContext().getDrawable(R.color.light)

        binding.filterBox.visibility = View.VISIBLE
        binding.filterBox.setOnClickListener { showSearchModal() }

        searchModal = SearchModal(requireContext(), items)
        searchModal.setCustomDialogListener(this@UserOnGoingStoreFragment)
        searchModal.searchHint = "Masukkan nama kota…"
        searchModal.setOnDismissListener {}
    }

    private fun showSearchModal() {
        val searchKey = if (selectedCity != null) selectedCity!!.title!! else ""
        if (searchKey.isNotEmpty()) searchModal.setSearchKey(searchKey)
        searchModal.show()
    }

    private fun loadingState(state: Boolean, message: String = getString(R.string.txt_loading)) {

        binding.txtLoading.text = message

        if (state) {

            binding.txtLoading.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE

        } else {

            binding.txtLoading.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE

        }

    }

    override fun onItemClick(data: ContactModel?) {
        val messageEvent = EventBusUtils.ContactModelEvent(data)
        EventBus.getDefault().post(messageEvent)
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

    override fun onDataReceived(data: ModalSearchModel) {
        if (selectedCity != null) {
            if (data.id != selectedCity!!.id) {

                if (data.id == "-1") {
                    selectedCity = null
                    binding.tvFilter.text = getString(R.string.all_cities)
                } else {
                    selectedCity = data
                    binding.tvFilter.text = data.title
                }

                if (isSearchActive) searchContact()
                else getContacts()

            }
        } else {
            if (data.id != "-1") {

                selectedCity = data
                binding.tvFilter.text = data.title

                if (isSearchActive) searchContact()
                else getContacts()

            }
        }
    }

}