@file:Suppress("DEPRECATION")

package com.topmortar.topmortarsales.view.rencanaVisits

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.recyclerview.RencanaVisitRVA
import com.topmortar.topmortarsales.commons.ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.CONST_ADDRESS
import com.topmortar.topmortarsales.commons.CONST_BIRTHDAY
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_DATE
import com.topmortar.topmortarsales.commons.CONST_INVOICE_ID
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
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RENVI_SOURCE
import com.topmortar.topmortarsales.commons.RENVI_WEEKLY
import com.topmortar.topmortarsales.commons.REPORT_SOURCE
import com.topmortar.topmortarsales.commons.REPORT_TYPE_IS_PAYMENT
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.SALES_REPORT_RENVI
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.utils.EventBusUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.FragmentJatemBinding
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.CityModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import com.topmortar.topmortarsales.model.RencanaVisitModel
import com.topmortar.topmortarsales.view.contact.DetailContactActivity
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

/**
 * A fragment representing a list of Items.
 */
class TagihMingguanFragment : Fragment() {

    private var _binding: FragmentJatemBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var userDistributorId : String
    private lateinit var userKind: String
    private lateinit var userCity: String
    private lateinit var userID: String

    private lateinit var badgeRefresh: LinearLayout
    private lateinit var searchModal: SearchModal
    private var citiesResults: ArrayList<CityModel>? = null
    private var selectedCity: ModalSearchModel? = null
    private var listItem: ArrayList<RencanaVisitModel> = arrayListOf()
    private var totalProcess = 0
    private var processed = 0
    private var percentage = 0
//    private lateinit var progressDialog: ProgressDialog

    private var reportSource = SALES_REPORT_RENVI
    private var listener: CounterItem? = null
    private var onSelectedItemListener: OnSelectedItemListener? = null
    private lateinit var apiService: ApiService
    private lateinit var rvAdapter: RencanaVisitRVA
    interface CounterItem {
        fun counterItem(count: Int)
    }
    interface OnSelectedItemListener {
        fun selectedItems(items: ArrayList<RencanaVisitModel>)
    }
    fun setCounterItem(listener: CounterItem) {
        this.listener = listener
    }
    fun setReportSource(source: String) {
        this.reportSource = source
    }
    fun syncNow() {
        if (userKind == USER_KIND_ADMIN) getCities()
        else getList()
    }
    fun isSelectBarActive(state: Boolean) {
        this.rvAdapter.clearSelections()
        this.rvAdapter.setSelectBarActive(state)
        this.binding.swipeRefreshLayout.isEnabled = !state
        if (userKind == USER_KIND_ADMIN) {
            if (state) binding.llFilter.componentFilter.visibility = View.GONE
            else binding.llFilter.componentFilter.visibility = View.VISIBLE
        }
        val eventBusInt = EventBusUtils.IntEvent(0)
        EventBus.getDefault().post(eventBusInt)
    }
    fun onConfirmSelected() {
        rvAdapter.getSelectedItems()
    }

    fun getAllListItem(): ArrayList<RencanaVisitModel> {
        return listItem
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJatemBinding.inflate(inflater, container, false)
        val view = binding.root

        badgeRefresh = view.findViewById(R.id.badgeRefresh)

        sessionManager = SessionManager(requireContext())
        userDistributorId = sessionManager.userDistributor().toString()
        userKind = sessionManager.userKind().toString()
        userCity = sessionManager.userCityID().toString()
        userID = sessionManager.userID().toString()

        apiService = HttpClient.create()
//        progressDialog = ProgressDialog(requireContext())
//        progressDialog.setMessage(getString(R.string.txt_loading))
//        progressDialog.setCancelable(false)

        if (userKind == USER_KIND_ADMIN) getCities()
        else getList()

        binding.swipeRefreshLayout.setOnRefreshListener {
            if (userKind == USER_KIND_ADMIN) getCities()
            else getList()
        }

        return view
    }

    private fun getList() {

        loadingState(true)
        showBadgeRefresh(false)

        lifecycleScope.launch {
            try {

                val response = when (userKind) {
                    USER_KIND_ADMIN -> {
                        if (selectedCity != null) apiService.targetWeekly(idCity = selectedCity?.id!!)
                        else apiService.targetWeeklyDst(idDistributor = userDistributorId)
                    } else -> apiService.targetWeekly(idCity = userCity)
                }

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        listItem = arrayListOf()
                        processed = 0
                        percentage = 0
                        totalProcess = response.results.size
//                        progressDialog.show()
                        LoopingTask(response.results).execute()

//                        listItem = response.results
//                        listItem.sortBy { it.created_at }
//
//                        setRecyclerView(listItem)
//                        loadingState(false)
//                        showBadgeRefresh(false)
//                        listener?.counterItem(listItem.size)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        listItem = arrayListOf()
                        setRecyclerView(listItem)
                        loadingState(true, "Belum ada daftar tagihan mingguan!")
                        showBadgeRefresh(false)
                        listener?.counterItem(0)

                    }
                    else -> {

                        listItem = arrayListOf()
                        setRecyclerView(listItem)
                        handleMessage(requireContext(), TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        loadingState(true, getString(R.string.failed_request))
                        showBadgeRefresh(true)

                    }
                }

            } catch (e: Exception) {

                listItem = arrayListOf()
                setRecyclerView(listItem)
                handleMessage(requireContext(), TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))
                showBadgeRefresh(true)

            }

        }

    }

    private inner class LoopingTask(private var items: ArrayList<RencanaVisitModel>) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            for (item in items.listIterator()) {
//                Handler(Looper.getMainLooper()).postDelayed({
                    if (item.is_new == "1") item.date_counter = item.date_invoice
                    else item.date_counter = item.created_at ?: "0000-00-00"

                    listItem.add(item)

                    processed ++
                    percentage = (processed * 100) / totalProcess
//                    println(getString(R.string.txt_loading) + "($percentage%)")
                    requireActivity().runOnUiThread {
                        binding.txtLoading.text = getString(R.string.txt_loading) + "($percentage%)"
                    }
//                }, 100)
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            listItem.sortBy { it.date_counter }

            setRecyclerView(listItem)
            loadingState(false)
            showBadgeRefresh(false)
            listener?.counterItem(listItem.size)
        }
    }

    private fun setRecyclerView(listItem: ArrayList<RencanaVisitModel>) {

        if (isAdded) {
            rvAdapter = RencanaVisitRVA(listItem, object: RencanaVisitRVA.ItemClickListener {
                override fun onItemClick(data: RencanaVisitModel?) {
                    context?.let { ctx ->
                        val intent = Intent(ctx, DetailContactActivity::class.java)

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
                            intent.putExtra(CONST_DATE, data.created_at_store)
                            intent.putExtra(CONST_WEEKLY_VISIT_STATUS, data.tagih_mingguan)
                            intent.putExtra(REPORT_SOURCE, SALES_REPORT_RENVI)
                            intent.putExtra(RENVI_SOURCE, RENVI_WEEKLY)
                            intent.putExtra(CONST_INVOICE_ID, data.id_invoice)
                            intent.putExtra(REPORT_TYPE_IS_PAYMENT, true)
                        }

                        startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_CODE)
                    }
                }

                override fun updateSelectedCount(count: Int?) {
                    val eventBusInt = EventBusUtils.IntEvent(count)
                    EventBus.getDefault().post(eventBusInt)
                }

            })

            rvAdapter.callback = { result ->
                onSelectedItemListener?.selectedItems(result)
            }
            rvAdapter.setType("tagihMingguan")

            context?.let { ctx ->
                binding.rvChatList.layoutManager = LinearLayoutManager(ctx)
            }

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

    private fun showBadgeRefresh(action: Boolean) {
        val tvTitle = badgeRefresh.findViewById<TextView>(R.id.tvTitle)
        val icClose = badgeRefresh.findViewById<ImageView>(R.id.icClose)
        icClose.setOnClickListener { badgeRefresh.visibility = View.GONE }

        if (action) {
            badgeRefresh.visibility = View.VISIBLE
            tvTitle.setOnClickListener { getList() }
        } else badgeRefresh.visibility = View.GONE
    }

    private fun getCities() {

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getCities(distributorID = userDistributorId)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        citiesResults = response.results
                        val items: ArrayList<ModalSearchModel> = ArrayList()

                        for (i in 0 until citiesResults!!.size) {
                            val data = citiesResults!![i]
                            items.add(ModalSearchModel(data.id_city, "${data.nama_city} - ${data.kode_city}"))
                        }
                        items.add(0, ModalSearchModel("-1", "Hapus Filter"))

                        setupDialogSearch(items)
                        binding.llFilter.componentFilter.visibility = View.VISIBLE

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(requireActivity(), "LIST CITY", "Daftar kota kosong!")

                    }
                    else -> {

                        handleMessage(requireActivity(), TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))

                    }
                }


            } catch (e: Exception) {

                handleMessage(requireActivity(), TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)

            } finally {
                getList()
            }

        }
    }

    private fun setupDialogSearch(items: ArrayList<ModalSearchModel> = ArrayList()) {

        searchModal = SearchModal(requireActivity(), items)
        searchModal.setCustomDialogListener(object: SearchModal.SearchModalListener{
            override fun onDataReceived(data: ModalSearchModel) {
                if (data.id == "-1") {
                    selectedCity = null
                    binding.llFilter.tvFilter.text = getString(R.string.tidak_ada_filter)
                } else {
                    selectedCity = data
                    binding.llFilter.tvFilter.text = data.title
                }
                getList()
            }

        })
        searchModal.searchHint = "Ketik untuk mencari…"

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) binding.llFilter.componentFilter.background = AppCompatResources.getDrawable(requireContext(), R.color.black_400)
        else binding.llFilter.componentFilter.background = AppCompatResources.getDrawable(requireContext(), R.color.light)
        binding.llFilter.componentFilter.visibility = View.GONE
        binding.llFilter.componentFilter.setOnClickListener {
            searchModal.show()
        }
        binding.llFilter.tvFilter.text = selectedCity?.title ?: getString(R.string.tidak_ada_filter)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSelectedItemListener) {
            onSelectedItemListener = context
        } else {
            throw ClassCastException("$context must implement OnSelectedItemsListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        onSelectedItemListener = null
    }

}