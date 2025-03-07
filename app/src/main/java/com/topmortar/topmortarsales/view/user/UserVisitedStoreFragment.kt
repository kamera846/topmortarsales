package com.topmortar.topmortarsales.view.user

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.ContactsRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.BID_VISITED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN_CITY
import com.topmortar.topmortarsales.commons.utils.EventBusUtils
import com.topmortar.topmortarsales.commons.utils.PhoneHandler
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.FragmentUserVisitedStoreBinding
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.ContactModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.Calendar

/**
 * A fragment representing a list of Items.
 */
@SuppressLint("SetTextI18n")
class UserVisitedStoreFragment : Fragment(), ContactsRecyclerViewAdapter.ItemClickListener,
    SearchModal.SearchModalListener {

//    private var _binding: FragmentUserVisitedStoreBinding? = null
    private lateinit var binding: FragmentUserVisitedStoreBinding

    private lateinit var sessionManager: SessionManager
    private lateinit var userKind: String
    private lateinit var userCity: String
    private val userID get() = sessionManager.userID().toString()
    private val userDistributorId get() = sessionManager.userDistributor().toString()
    private var userCityParam: String? = ""
    private var selectedCity: ModalSearchModel? = null

    // Initialize Search Engine
    private var isSearchActive = false

    // Initialize Filter Month
    private val listMonthInt = arrayListOf(0,1,2,3,4,5,6,7,8,9,10,11,12)
    private val listMonthString = arrayListOf("Tidak ada filter","Januari","Februari","Maret","April","Mei","Juni","Juli","Agustus","September","Oktober","November","Desember")
    private var selectedMonth = 0

    private var listener: CounterItem? = null
    interface CounterItem {
        fun counterItem(count: Int)
    }
    fun setCounterItem(listener: CounterItem) {
        this.listener = listener
    }

    fun setUserCityParam(id: String?) {
        this.userCityParam = id
    }

    private var userIDParam: String? = ""
    fun setUserIdParam(id: String?) {
        this.userIDParam = id
    }

    fun syncNow() {
        getContacts()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserVisitedStoreBinding.inflate(inflater, container, false)
        val view = binding.root

        sessionManager = SessionManager(requireContext())
        userKind = sessionManager.userKind().toString()
        userCity = sessionManager.userCityID().toString()

        // Get the current theme mode (light or dark)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) binding.filterBox.background = AppCompatResources.getDrawable(requireContext(), R.color.black_400)
        else binding.filterBox.background = AppCompatResources.getDrawable(requireContext(), R.color.light)
        binding.filterBox.setOnClickListener { showDropdownMenu() }

        toggleFilter(Calendar.getInstance().get(Calendar.MONTH)+1)

//        setupSearchBox()
//        getContacts()

        return view
    }

    private fun getContacts() {
        loadingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getContactsUserBid(userId = userIDParam!!.ifEmpty { userID }, visit = BID_VISITED, month = selectedMonth.toString())
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
                            adapter = ContactsRecyclerViewAdapter(response.results, this@UserVisitedStoreFragment)
                        }

//                        if (userKind == USER_KIND_ADMIN) getCities()
//                        else loadingState(false)
                        listener?.counterItem(response.results.size)
                        binding.tvFilter.text = "${listMonthString[selectedMonth]} (${response.results.size} Toko)"
                        loadingState(false)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        listener?.counterItem(0)
                        binding.tvFilter.text = listMonthString[selectedMonth]
                        loadingState(true, "Belum ada kunjungan!")

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
                                adapter = ContactsRecyclerViewAdapter(responseBody.results, this@UserVisitedStoreFragment)
                            }
                            binding.tvFilter.text = "$textFilter (${responseBody.results.size})"
                            loadingState(false)

                        }
                        RESPONSE_STATUS_EMPTY -> {

                            loadingState(true, "Tidak menemukan hasil dari pencarian!")
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

                handleMessage(requireContext(), TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))

            }

        }

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

    private fun showDropdownMenu() {
        val popupMenu = PopupMenu(requireContext(), binding.filterBox, Gravity.END)
        popupMenu.menuInflater.inflate(R.menu.option_list_month, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
            when (item?.itemId) {
                R.id.option_januari -> {
                    toggleFilter(1)
                    return@setOnMenuItemClickListener  true
                }
                R.id.option_februari -> {
                    toggleFilter(2)
                    return@setOnMenuItemClickListener  true
                }
                R.id.option_maret -> {
                    toggleFilter(3)
                    return@setOnMenuItemClickListener  true
                }
                R.id.option_april -> {
                    toggleFilter(4)
                    return@setOnMenuItemClickListener  true
                }
                R.id.option_mei -> {
                    toggleFilter(5)
                    return@setOnMenuItemClickListener  true
                }
                R.id.option_juni -> {
                    toggleFilter(6)
                    return@setOnMenuItemClickListener  true
                }
                R.id.option_juli -> {
                    toggleFilter(7)
                    return@setOnMenuItemClickListener  true
                }
                R.id.option_agustus -> {
                    toggleFilter(8)
                    return@setOnMenuItemClickListener  true
                }
                R.id.option_september -> {
                    toggleFilter(9)
                    return@setOnMenuItemClickListener  true
                }
                R.id.option_oktober -> {
                    toggleFilter(10)
                    return@setOnMenuItemClickListener  true
                }
                R.id.option_november -> {
                    toggleFilter(11)
                    return@setOnMenuItemClickListener  true
                }
                R.id.option_desember -> {
                    toggleFilter(12)
                    return@setOnMenuItemClickListener  true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }

        popupMenu.show()
    }

    private fun toggleFilter(month: Int = 0) {
        selectedMonth = listMonthInt[month]
        binding.tvFilter.text = listMonthString[month]
        getContacts()
    }

}