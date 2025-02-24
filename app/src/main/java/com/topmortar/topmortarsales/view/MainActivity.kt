@file:Suppress("DEPRECATION")

package com.topmortar.topmortarsales.view

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DatabaseReference
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.ContactsRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.CONST_ADDRESS
import com.topmortar.topmortarsales.commons.CONST_BIRTHDAY
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_DATE
import com.topmortar.topmortarsales.commons.CONST_KTP
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_CITY_ID
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_NAME
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_STATUS
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_MAPS
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_NEAREST_STORE
import com.topmortar.topmortarsales.commons.CONST_NEAREST_STORE_HIDE_FILTER
import com.topmortar.topmortarsales.commons.CONST_NEAREST_STORE_WITH_DEFAULT_RANGE
import com.topmortar.topmortarsales.commons.CONST_OWNER
import com.topmortar.topmortarsales.commons.CONST_PAYMENT_METHOD
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.CONST_PROMO
import com.topmortar.topmortarsales.commons.CONST_REPUTATION
import com.topmortar.topmortarsales.commons.CONST_STATUS
import com.topmortar.topmortarsales.commons.CONST_TERMIN
import com.topmortar.topmortarsales.commons.CONST_WEEKLY_VISIT_STATUS
import com.topmortar.topmortarsales.commons.FIREBASE_CHILD_AUTH
import com.topmortar.topmortarsales.commons.LOGGED_OUT
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.SEARCH_CLEAR
import com.topmortar.topmortarsales.commons.SEARCH_CLOSE
import com.topmortar.topmortarsales.commons.SEARCH_OPEN
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_ACTION_MAIN_ACTIVITY
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN_CITY
import com.topmortar.topmortarsales.commons.USER_KIND_BA
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.USER_KIND_MARKETING
import com.topmortar.topmortarsales.commons.USER_KIND_PENAGIHAN
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.utils.AppUpdateHelper
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityMainBinding
import com.topmortar.topmortarsales.modal.FilterTokoModal
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.CityModel
import com.topmortar.topmortarsales.model.ContactModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import com.topmortar.topmortarsales.view.city.ManageCityActivity
import com.topmortar.topmortarsales.view.contact.DetailContactActivity
import com.topmortar.topmortarsales.view.contact.NewRoomChatFormActivity
import com.topmortar.topmortarsales.view.courier.ManageBasecampActivity
import com.topmortar.topmortarsales.view.delivery.DeliveryActivity
import com.topmortar.topmortarsales.view.gudang.ManageGudangActivity
import com.topmortar.topmortarsales.view.product.ProductsActivity
import com.topmortar.topmortarsales.view.rencanaVisits.RencanaVisitActivity
import com.topmortar.topmortarsales.view.rencanaVisits.RencanaVisitPenagihanActivity
import com.topmortar.topmortarsales.view.skill.ManageSkillActivity
import com.topmortar.topmortarsales.view.tukang.ListTukangActivity
import com.topmortar.topmortarsales.view.user.ManageUserActivity
import com.topmortar.topmortarsales.view.user.UserProfileActivity
import kotlinx.coroutines.launch
import java.util.Locale

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity(), SearchModal.SearchModalListener,
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var scaleAnimation: Animation

    private lateinit var rlLoading: RelativeLayout
    private lateinit var rlParent: RelativeLayout
    private lateinit var txtLoading: TextView
    private lateinit var rvListChat: RecyclerView
    private lateinit var llTitleBar: LinearLayout
    private lateinit var llSearchBox: LinearLayout
    private lateinit var btnFab: FloatingActionButton
    private lateinit var btnFabAdmin: FloatingActionButton
    private lateinit var icMore: ImageView
    private lateinit var icSearch: ImageView
    private lateinit var icCloseSearch: ImageView
    private lateinit var icClearSearch: ImageView
    private lateinit var etSearchBox: EditText
    private lateinit var tvTitleBarDescription: TextView

    // Global
    private lateinit var sessionManager: SessionManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var filterModal: FilterTokoModal
    private var selectedCity: ModalSearchModel? = null
    private var doubleBackToExitPressedOnce = false
    private val userCity get() = sessionManager.userCityID().toString()
    private val userKind get() = sessionManager.userKind().toString()
    private val userId get() = sessionManager.userID().toString()
    private val userDistributorId get() = sessionManager.userDistributor().toString()
    private lateinit var rvAdapter: ContactsRecyclerViewAdapter
    private var contacts: ArrayList<ContactModel> = arrayListOf()
    private var cities: ArrayList<CityModel> = arrayListOf()
    private lateinit var firebaseReference: DatabaseReference
    private lateinit var progressDialog: ProgressDialog
    private lateinit var searchCityForMaps: SearchModal

    // Initialize Search Engine
    private val searchDelayMillis = 500L
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var previousSearchTerm = ""
    private var isSearchActive = false
    private var selectedItemsId = mutableSetOf<String>()
    private var isSelectItemActive = false
    private var isSearchContactByCity = false
    private var selectedItemCount = 0
    private var totalProcess = 0
    private var processed = 0
    private var percentage = 0

    // Setup Filter
    private var selectedStatusID: String = "-1"
    private var selectedVisitedID: String = "-1"
    private var selectedCitiesID: CityModel? = null

    private lateinit var listCoordinate: ArrayList<String>
    private lateinit var listCoordinateName: ArrayList<String>
    private lateinit var listCoordinateStatus: ArrayList<String>
    private lateinit var listCoordinateCityID: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this@MainActivity)
        val userDistributorIds = sessionManager.userDistributor()
        firebaseReference = FirebaseUtils().getReference(distributorId = userDistributorIds ?: "-firebase-002")

        val isLoggedIn = sessionManager.isLoggedIn()
        if (!isLoggedIn || userId.isEmpty() || userCity.isEmpty() || userKind.isEmpty()|| userDistributorId.isEmpty()) return missingDataHandler()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppUpdateHelper.initialize()
        AppUpdateHelper.checkForUpdate(this)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage(getString(R.string.txt_loading))

        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_anim)

        if (CustomUtility(this).isUserWithOnlineStatus()) {
            CustomUtility(this).setUserStatusOnline(
                true,
                sessionManager.userDistributor() ?: "-custom-001",
                sessionManager.userID().toString()
            )
        }

        val popupMenu = binding.navView

        val syncNowItem = popupMenu.menu.findItem(R.id.nav_sync_now)
        val chartItem = popupMenu.menu.findItem(R.id.nav_chart)
        val searchItem = popupMenu.menu.findItem(R.id.nav_search)
        val productsItem = popupMenu.menu.findItem(R.id.nav_products)
        val userItem = popupMenu.menu.findItem(R.id.nav_user)
        val tukangItem = popupMenu.menu.findItem(R.id.nav_tukang)
        val myProfile = popupMenu.menu.findItem(R.id.nav_my_profile)
        val cityItem = popupMenu.menu.findItem(R.id.nav_city)
        val skillItem = popupMenu.menu.findItem(R.id.nav_skill)
        val basecamp = popupMenu.menu.findItem(R.id.nav_basecamp)
        val gudang = popupMenu.menu.findItem(R.id.nav_gudang)
        val delivery = popupMenu.menu.findItem(R.id.nav_delivery)
        val rencanaVisitGroup = popupMenu.menu.findItem(R.id.rencana_visit_group)
        val rencanaVisit = popupMenu.menu.findItem(R.id.rencana_visit)
        val rencanaVisitPenagihan = popupMenu.menu.findItem(R.id.rencana_visit_penagihan)
        val cityStore = popupMenu.menu.findItem(R.id.nav_city_store)

        searchItem.isVisible = false
        syncNowItem.isVisible = false
        if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_ADMIN_CITY) {
            if (userKind == USER_KIND_ADMIN) {
                chartItem.isVisible = true
                cityItem.isVisible = true
                skillItem.isVisible = true
                tukangItem.isVisible = true
                cityStore.isVisible = true
            }
            productsItem.isVisible = true
            userItem.isVisible = true
            basecamp.isVisible = true
            gudang.isVisible = true
            delivery.isVisible = true
            rencanaVisitGroup.isVisible = true
            rencanaVisit.isVisible = true
            rencanaVisitPenagihan.isVisible = true
        }

        if (sessionManager.userKind() != USER_KIND_SALES) {
            myProfile.isVisible = false
        }
        binding.navView.setNavigationItemSelectedListener(this)
        binding.titleBar.icMenu.visibility = View.VISIBLE
        binding.titleBar.icMenu.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }

        initVariable()
        initClickHandler()
        loadingState(true)
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (isSearchActive) {
                searchContact()
            } else {
                if (userKind == USER_KIND_ADMIN) getCities()
                else getContacts()
            }
        }
        if (userKind == USER_KIND_ADMIN) getCities()
        else getContacts()

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_sync_now -> {
                getUserLoggedIn()
            }
            R.id.nav_chart -> {
                val intent = Intent(this@MainActivity, ChartActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_nearest_store -> {
                navigateChecklocation()
            }
            R.id.nav_city_store -> {
                searchCityForMaps.show()
            }
            R.id.nav_my_profile -> {
                val intent = Intent(this@MainActivity, UserProfileActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_search -> {
                toggleSearchEvent(SEARCH_OPEN)
            }
            R.id.nav_products -> {
                startActivity(Intent(this@MainActivity, ProductsActivity::class.java))
            }
            R.id.nav_user -> {
                startActivity(Intent(this@MainActivity, ManageUserActivity::class.java))
            }
            R.id.nav_city -> {
                val intent = Intent(this@MainActivity, ManageCityActivity::class.java)
                intent.putExtra(ACTIVITY_REQUEST_CODE, MAIN_ACTIVITY_REQUEST_CODE)
                startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_CODE)
            }
            R.id.nav_tukang -> {
                startActivity(Intent(this@MainActivity, ListTukangActivity::class.java))
            }
            R.id.nav_skill -> {
                startActivity(Intent(this@MainActivity, ManageSkillActivity::class.java))
            }
            R.id.nav_basecamp -> {
                startActivity(Intent(this@MainActivity, ManageBasecampActivity::class.java))
            }
            R.id.nav_gudang -> {
                startActivity(Intent(this@MainActivity, ManageGudangActivity::class.java))
            }
            R.id.nav_delivery -> {
                startActivity(Intent(this@MainActivity, DeliveryActivity::class.java))
            }
            R.id.nav_logout -> {
                logoutConfirmation()
            }
            R.id.rencana_visit -> {
                startActivity(Intent(this@MainActivity, RencanaVisitActivity::class.java))
            }
            R.id.rencana_visit_penagihan -> {
                startActivity(Intent(this@MainActivity, RencanaVisitPenagihanActivity::class.java))
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun initVariable() {

        rlLoading = findViewById(R.id.rl_loading)
        rlParent = findViewById(R.id.rl_parent)
        txtLoading = findViewById(R.id.txt_loading)
        rvListChat = findViewById(R.id.rv_chat_list)
        llTitleBar = findViewById(R.id.title_bar)
        llSearchBox = findViewById(R.id.search_box)
        btnFab = findViewById(R.id.btn_fab)
        btnFabAdmin = findViewById(R.id.btn_fab_admin)
        icMore = llTitleBar.findViewById(R.id.ic_more)
        icSearch = llTitleBar.findViewById(R.id.ic_search)
        tvTitleBarDescription = llTitleBar.findViewById(R.id.tv_title_bar_description)
        icCloseSearch = findViewById(R.id.ic_close_search)
        icClearSearch = findViewById(R.id.ic_clear_search)
        etSearchBox = findViewById(R.id.et_search_box)

        // Set Title Bar
        if (userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN || userKind == USER_KIND_MARKETING) {
            icMore.visibility = View.VISIBLE
            binding.titleBar.icMenu.visibility = View.GONE
            tvTitleBarDescription.visibility = View.GONE
            binding.titleBar.tvTitleBar.text = "Semua Toko"
            binding.titleBar.icBack.visibility = View.VISIBLE
            binding.titleBar.icBack.setOnClickListener { if (isSelectItemActive) toggleSelectItem() else finish() }
        } else {
            if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_ADMIN_CITY) {
                val contentWidht = convertDpToPx(40, this)
                val contentHeight = convertDpToPx(40, this)
                val paddingHorizontal = convertDpToPx(8, this)
                val paddingVertival = convertDpToPx(8, this)
                binding.titleBar.icMenu.visibility = View.VISIBLE
                binding.titleBar.icMenu.layoutParams.width = contentWidht
                binding.titleBar.icMenu.layoutParams.height = contentHeight
                binding.titleBar.icMenu.setPadding(paddingHorizontal,paddingVertival,paddingHorizontal,paddingVertival)
                (binding.titleBar.icMenu.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0,0,paddingHorizontal,0)
                binding.titleBar.icSearch.layoutParams.width = contentWidht
                binding.titleBar.icSearch.layoutParams.height = contentHeight
                binding.titleBar.icSearch.setPadding(paddingHorizontal,paddingVertival,paddingHorizontal,paddingVertival)
                icMore.visibility = View.GONE

                val headerView = binding.navView.getHeaderView(0)
                val navFullName = headerView.findViewById<TextView>(R.id.nav_full_name)
                val navUserName = headerView.findViewById<TextView>(R.id.navUserName)

                sessionManager.fullName().let {
                    if (!it.isNullOrEmpty()) {
                        navFullName.visibility = View.VISIBLE
                        navFullName.text = it
                    } else {
                        navFullName.visibility = View.GONE
                    }
                }
                sessionManager.userName().let {
                    if (!it.isNullOrEmpty()) {
                        navUserName.visibility = View.VISIBLE
                        navUserName.text = it
                    } else {
                        navUserName.visibility = View.GONE
                    }
                }
            } else {
                binding.titleBar.icMenu.visibility = View.GONE
                icMore.visibility = View.VISIBLE
            }
            binding.titleBar.icBack.visibility = View.GONE
            tvTitleBarDescription.text = sessionManager.userName().let { if (!it.isNullOrEmpty()) "Halo, $it" else ""}
            tvTitleBarDescription.visibility = tvTitleBarDescription.text.let { if (it.isNotEmpty()) View.VISIBLE else View.GONE }
            binding.titleBar.tvTitleBar.setPadding(convertDpToPx(16, this), 0, 0, 0)
            binding.titleBar.tvTitleBarDescription.setPadding(convertDpToPx(16, this), 0, 0, 0)
            etSearchBox.setPadding(0, 0, convertDpToPx(16, this), 0)
        }

        // Set Floating Action Button
        if (sessionManager.userKind() == USER_KIND_SALES || sessionManager.userKind() == USER_KIND_PENAGIHAN) btnFab.visibility = View.VISIBLE
        if (sessionManager.userKind() != USER_KIND_COURIER) icSearch.visibility = View.VISIBLE
        if (sessionManager.userKind() == USER_KIND_COURIER) btnFabAdmin.visibility = View.VISIBLE

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initClickHandler() {

        btnFab.setOnClickListener { navigateAddNewRoom() }
        btnFabAdmin.setOnClickListener { navigateChatAdmin() }
        icMore.setOnClickListener { showPopupMenu() }
        icSearch.setOnClickListener { toggleSearchEvent(SEARCH_OPEN) }
        icCloseSearch.setOnClickListener { toggleSearchEvent(SEARCH_CLOSE) }
        icClearSearch.setOnClickListener { etSearchBox.setText("") }
        rlLoading.setOnTouchListener { _, event -> blurSearchBox(event) }
        rvListChat.setOnTouchListener { _, event -> blurSearchBox(event) }
        binding.llFilter.setOnClickListener {
            setupFilterTokoModal()
            showFilterModal()
        }
        binding.titleBar.icConfirmSelect.setOnClickListener {
            if (selectedItemCount > 0) {
                rvAdapter.getSelectedItems()
            }
        }
    }

    private fun loadingState(state: Boolean, message: String = getString(R.string.txt_loading)) {

        txtLoading.text = message

        if (state) {

            rlLoading.visibility = View.VISIBLE
            rvListChat.visibility = View.GONE

            binding.swipeRefreshLayout.isRefreshing = message === getString(R.string.txt_loading)

        } else {

            rlLoading.visibility = View.GONE
            rvListChat.visibility = View.VISIBLE
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
            Toast.makeText(this, "Gagal mengarahkan ke whatsapp", TOAST_SHORT).show()
        }

    }

    private fun navigateAddNewRoom(data: ContactModel? = null) {

        toggleSearchEvent(SEARCH_CLOSE)

        val intent = Intent(this@MainActivity, NewRoomChatFormActivity::class.java)

        if (data != null) {
            intent.putExtra(CONST_CONTACT_ID, data.id_contact)
            intent.putExtra(CONST_NAME, data.nama)
            intent.putExtra(CONST_PHONE, data.nomorhp)
            intent.putExtra(CONST_BIRTHDAY, data.tgl_lahir)
            intent.putExtra(CONST_OWNER, data.store_owner)
            intent.putExtra(ACTIVITY_REQUEST_CODE, MAIN_ACTIVITY_REQUEST_CODE)
            intent.putExtra(CONST_LOCATION, data.id_city)
        }

        startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_CODE)

    }

    private fun navigateDetailContact(data: ContactModel? = null) {

        val intent = Intent(this@MainActivity, DetailContactActivity::class.java)

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

        startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_CODE)

    }

    private fun navigateChecklocation() {
        progressDialog.show()

        Handler(Looper.getMainLooper()).postDelayed({

            lifecycleScope.launch {
                try {

                    val apiService: ApiService = HttpClient.create()
                    val response = when (userKind) {
                        USER_KIND_ADMIN, USER_KIND_PENAGIHAN, USER_KIND_MARKETING -> apiService.getContactsByDistributor(distributorID = userDistributorId)
                        USER_KIND_COURIER -> apiService.getCourierStore(processNumber = "1", courierId = userId)
                        else -> apiService.getContacts(cityId = userCity, distributorID = userDistributorId)
                    }

                    when (response.status) {
                        RESPONSE_STATUS_OK -> {

                            listCoordinate = arrayListOf()
                            listCoordinateName = arrayListOf()
                            listCoordinateStatus = arrayListOf()
                            listCoordinateCityID = arrayListOf()

                            totalProcess = response.results.size
                            LoopingTask(response.results).execute()

//                            for (item in response.results.listIterator()) {
//                                listCoordinate.add(item.maps_url)
//                                listCoordinateName.add(item.nama)
//                                listCoordinateStatus.add(item.store_status)
//                                listCoordinateCityID.add(item.id_city)
//                            }
//
//                            val intent = Intent(this@MainActivity, MapsActivity::class.java)
//
//                            intent.putExtra(CONST_NEAREST_STORE, true)
//                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
//                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)
//                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_STATUS, listCoordinateStatus)
//                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_CITY_ID, listCoordinateCityID)
//
//                            progressDialog.dismiss()
//                            startActivity(intent)

                        }
                        RESPONSE_STATUS_EMPTY -> {

                            val listCoordinate = arrayListOf<String>()
                            val listCoordinateName = arrayListOf<String>()

                            val intent = Intent(this@MainActivity, MapsActivity::class.java)

                            intent.putExtra(CONST_NEAREST_STORE, true)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
                            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)

                            progressDialog.dismiss()
                            startActivity(intent)

                        }
                        else -> {

                            handleMessage(this@MainActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                            progressDialog.dismiss()

                        }
                    }


                } catch (e: Exception) {

                    handleMessage(this@MainActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                    progressDialog.dismiss()

                }

            }

        }, 1000)
    }

    private fun setupDialogSearchForMaps(items: ArrayList<ModalSearchModel> = ArrayList()) {

        searchCityForMaps = SearchModal(this, items)
        searchCityForMaps.setCustomDialogListener(object: SearchModal.SearchModalListener{
            override fun onDataReceived(data: ModalSearchModel) {
                data.id?.let { getContactsForMaps(it) }
            }

        })
        searchCityForMaps.searchHint = "Ketik untuk mencari…"

    }

    private fun showPopupMenu() {
        val popupMenu = PopupMenu(this@MainActivity, icMore)
        popupMenu.inflate(R.menu.option_main_menu)

        val syncNow = popupMenu.menu.findItem(R.id.option_sync_now)
        val searchItem = popupMenu.menu.findItem(R.id.option_search)
        val userItem = popupMenu.menu.findItem(R.id.option_user)
        val myProfile = popupMenu.menu.findItem(R.id.option_my_profile)
        val selectedStore = popupMenu.menu.findItem(R.id.selected_store)
        val cityItem = popupMenu.menu.findItem(R.id.option_city)
        val skillItem = popupMenu.menu.findItem(R.id.option_skill)
        val basecamp = popupMenu.menu.findItem(R.id.option_basecamp)
        val gudang = popupMenu.menu.findItem(R.id.option_gudang)
        val delivery = popupMenu.menu.findItem(R.id.option_delivery)
        val rencanaVisit = popupMenu.menu.findItem(R.id.rencana_visit)
        val rencanaVisitPenagihan = popupMenu.menu.findItem(R.id.rencana_visit_penagihan)
        val logout = popupMenu.menu.findItem(R.id.option_logout)

        searchItem.isVisible = false
        if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_ADMIN_CITY) {
            if (userKind == USER_KIND_ADMIN) {
                userItem.isVisible = true
                cityItem.isVisible = true
                skillItem.isVisible = true
                basecamp.isVisible = true
                gudang.isVisible = true
                delivery.isVisible = true
                rencanaVisit.isVisible = true
                rencanaVisitPenagihan.isVisible = true
            } else {
                userItem.isVisible = true
                basecamp.isVisible = true
                gudang.isVisible = true
                delivery.isVisible = true
                rencanaVisit.isVisible = true
                rencanaVisitPenagihan.isVisible = true
            }
        }

        if (sessionManager.userKind() != USER_KIND_SALES) {
            myProfile.isVisible = false
        }

        if (userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN || userKind == USER_KIND_MARKETING) {
            syncNow.isVisible = false
            myProfile.isVisible = false
            logout.isVisible = false
            selectedStore.isVisible = true
        }

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.option_sync_now -> {
                    getUserLoggedIn()
                    true
                }
                R.id.nearest_store -> {
                    navigateChecklocation()
                    true
                }
                R.id.selected_store -> {
                    toggleSelectItem()
                    true
                }
                R.id.option_my_profile -> {
                    val intent = Intent(this@MainActivity, UserProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.option_search -> {
                    toggleSearchEvent(SEARCH_OPEN)
                    true
                }
                R.id.option_user -> {
                    startActivity(Intent(this@MainActivity, ManageUserActivity::class.java))
                    true
                }
                R.id.option_city -> {
                    val intent = Intent(this@MainActivity, ManageCityActivity::class.java)
                    intent.putExtra(ACTIVITY_REQUEST_CODE, MAIN_ACTIVITY_REQUEST_CODE)
                    startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_CODE)
                    true
                }
                R.id.option_skill -> {
                    startActivity(Intent(this@MainActivity, ManageSkillActivity::class.java))
                    true
                }
                R.id.option_basecamp -> {
                    startActivity(Intent(this@MainActivity, ManageBasecampActivity::class.java))
                    true
                }
                R.id.option_gudang -> {
                    startActivity(Intent(this@MainActivity, ManageGudangActivity::class.java))
                    true
                }
                R.id.option_delivery -> {
                    startActivity(Intent(this@MainActivity, DeliveryActivity::class.java))
                    true
                }
                R.id.option_logout -> {
                    logoutConfirmation()
                    true
                }
                R.id.rencana_visit -> {
                    startActivity(Intent(this@MainActivity, RencanaVisitActivity::class.java))
                    true
                }
                R.id.rencana_visit_penagihan -> {
                    startActivity(Intent(this@MainActivity, RencanaVisitPenagihanActivity::class.java))
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun blurSearchBox(event: MotionEvent): Boolean {

        if (isSearchActive && TextUtils.isEmpty(etSearchBox.text)) {
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_SCROLL || event.action == MotionEvent.ACTION_MOVE) {
                toggleSearchEvent(SEARCH_CLOSE)
                return true
            }
        }
        return false
    }

    private fun toggleSearchEvent(state: String) {

        val animationDuration = 200L

        val fadeIn = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_in)
        fadeIn.duration = animationDuration
        val fadeOut = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_out)
        fadeOut.duration = animationDuration
        val slideInFromLeft = AnimationUtils.loadAnimation(this@MainActivity,
            R.anim.fade_slide_in_from_left
        )
        slideInFromLeft.duration = animationDuration
        val slideOutToRight = AnimationUtils.loadAnimation(this@MainActivity,
            R.anim.fade_slide_out_to_right
        )
        slideOutToRight.duration = animationDuration
        val slideInFromRight = AnimationUtils.loadAnimation(this@MainActivity,
            R.anim.fade_slide_in_from_right
        )
        slideInFromRight.duration = animationDuration
        val slideOutToLeft = AnimationUtils.loadAnimation(this@MainActivity,
            R.anim.fade_slide_out_to_left
        )
        slideOutToLeft.duration = animationDuration

        if (state == SEARCH_OPEN && !isSearchActive) {

            llSearchBox.visibility = View.VISIBLE

            llSearchBox.startAnimation(slideInFromLeft)
            llTitleBar.startAnimation(slideOutToRight)

            Handler(Looper.getMainLooper()).postDelayed({
                llTitleBar.visibility = View.GONE
                etSearchBox.requestFocus()
                isSearchActive = true
            }, animationDuration)

            etSearchBox.addTextChangedListener(object: TextWatcher {
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

//                            toggleSearchEvent(SEARCH_CLEAR)
                            searchContact(searchTerm)
                        }

                        searchRunnable?.let { searchHandler.postDelayed(it, searchDelayMillis) }

                    }

                }

                override fun afterTextChanged(s: Editable?) {}

            })

        }

        if (state == SEARCH_CLOSE && isSearchActive) {

            llTitleBar.visibility = View.VISIBLE

            llTitleBar.startAnimation(slideInFromRight)
            llSearchBox.startAnimation(slideOutToLeft)

            Handler(Looper.getMainLooper()).postDelayed({
                llSearchBox.visibility = View.GONE
                etSearchBox.clearFocus()
                isSearchActive = false
            }, animationDuration)

            if (etSearchBox.text.toString() != "") etSearchBox.setText("")

        }

        if (state == SEARCH_CLEAR) {

            if (TextUtils.isEmpty(etSearchBox.text)) {

                if (icClearSearch.visibility == View.VISIBLE) {

                    icClearSearch.startAnimation(fadeOut)
                    Handler(Looper.getMainLooper()).postDelayed({
                        icClearSearch.visibility = View.GONE
                    }, animationDuration)

                }

            } else {

                if (icClearSearch.visibility == View.GONE) {

                    etSearchBox.clearFocus()

                    icClearSearch.startAnimation(fadeIn)
                    Handler(Looper.getMainLooper()).postDelayed({
                        icClearSearch.visibility = View.VISIBLE
                    }, animationDuration)

                }

            }

        }

    }

    private fun getContacts() {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = when (userKind) {
                    USER_KIND_COURIER -> apiService.getCourierStore(processNumber = "1", courierId = userId)
                    else -> {
                        val statusFilter = selectedStatusID.toLowerCase(Locale.ROOT)
                        val cityID = if (userKind == USER_KIND_ADMIN) selectedCitiesID?.id_city
                        else userCity

                        if (cityID != null && statusFilter != "-1") {
                            apiService.getContacts(cityId = cityID, status = statusFilter, distributorID = userDistributorId)
                        } else if (cityID != null) {
                            apiService.getContacts(cityId = cityID, distributorID = userDistributorId)
                        } else if (statusFilter != "-1" ) {
                            apiService.getContactsByStatus(status = statusFilter, distributorID = userDistributorId)
                        } else apiService.getContactsByDistributor(distributorID = userDistributorId)
                    }
                }

                var textFilter = ""

                if (selectedStatusID != "-1" || selectedVisitedID != "-1" || selectedCitiesID != null) {
                    textFilter += if (selectedCitiesID != null && selectedCitiesID?.id_city != "-1") selectedCitiesID?.nama_city else ""
                    textFilter += if (selectedStatusID != "-1") if (textFilter.isNotEmpty()) ", $selectedStatusID" else selectedStatusID else ""
                    textFilter += if (selectedVisitedID != "-1") if (textFilter.isNotEmpty()) ", $selectedVisitedID" else selectedVisitedID else ""
                } else textFilter = getString(R.string.tidak_ada_filter)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        contacts = response.results
                        setRecyclerView(response.results)
                        binding.tvFilter.text = "$textFilter (${response.results.size})"
                        loadingState(false)
                        setupFilterTokoModal()

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Daftar kontak kosong!")
                        binding.tvFilter.text = "$textFilter (${response.results.size})"

                    }
                    else -> {

                        handleMessage(this@MainActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        loadingState(true, getString(R.string.failed_request))

                    }
                }

                checkSwipeRefreshLayoutHint()


            } catch (e: Exception) {

                handleMessage(this@MainActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))

            }

        }

    }

    private fun getContactsForMaps(cityID: String) {

        progressDialog.show()
        isSearchContactByCity = true

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getContacts(cityId = cityID, distributorID = userDistributorId)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        listCoordinate = arrayListOf()
                        listCoordinateName = arrayListOf()
                        listCoordinateStatus = arrayListOf()
                        listCoordinateCityID = arrayListOf()

                        totalProcess = response.results.size
                        LoopingTask(response.results).execute()

                    }
                    RESPONSE_STATUS_EMPTY -> {
                        handleMessage(this@MainActivity, TAG_RESPONSE_CONTACT, "Daftar kontak kosong!")
                    }
                    else -> {
                        handleMessage(this@MainActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                    }
                }

            } catch (e: Exception) {
                handleMessage(this@MainActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
            } finally {
                progressDialog.dismiss()
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

                        cities = response.results
                        val items: ArrayList<ModalSearchModel> = ArrayList()

//                        items.add(ModalSearchModel("-1", "Hapus filter"))
                        for (i in 0 until cities.size) {
                            val data = cities[i]
                            items.add(ModalSearchModel(data.id_city, "${data.nama_city} - ${data.kode_city}"))
                        }
                        setupDialogSearchForMaps(items)
                        setupFilterTokoModal()
                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(this@MainActivity, "LIST CITY", "Daftar kota kosong!")

                    }
                    else -> {

                        handleMessage(this@MainActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))

                    }
                }


            } catch (e: Exception) {

                handleMessage(this@MainActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)

            }

            getContacts()

        }
    }

    private fun setupFilterTokoModal() {

        if (userKind != USER_KIND_COURIER && userKind != USER_KIND_BA) {
            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) binding.llFilter.background = AppCompatResources.getDrawable(this, R.color.black_400)
            else binding.llFilter.background = AppCompatResources.getDrawable(this, R.color.light)

            binding.llFilter.visibility = View.VISIBLE

            filterModal = FilterTokoModal(this)
            if (userKind == USER_KIND_ADMIN) {
                filterModal.setStatuses(selected = selectedStatusID)
                filterModal.setCities(items = cities, selected = selectedCitiesID)
            } else if (userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN || userKind == USER_KIND_MARKETING || userKind == USER_KIND_ADMIN_CITY) filterModal.setStatuses(selected = selectedStatusID)
            filterModal.setSendFilterListener(object: FilterTokoModal.SendFilterListener {
                override fun onSendFilter(
                    selectedStatusID: String,
                    selectedVisitedID: String,
                    selectedCitiesID: CityModel?
                ) {

                    this@MainActivity.selectedStatusID = selectedStatusID
                    this@MainActivity.selectedVisitedID = selectedVisitedID
                    this@MainActivity.selectedCitiesID = selectedCitiesID

                    if (isSearchActive) searchContact()
                    else getContacts()
                }

            })
        }
    }

    private fun showFilterModal() {
        filterModal.show()
    }

    private fun getUserLoggedIn(onlySession: Boolean = false) {

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.detailUser(userId = userId)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        val data = response.results[0]
                        if (data.phone_user == "0") {
                            logoutHandler()
                        } else {
                            sessionManager.setUserLoggedIn(data)

                            if (userKind != USER_KIND_SALES && userKind != USER_KIND_PENAGIHAN && userKind != USER_KIND_MARKETING) {
                                tvTitleBarDescription.text = sessionManager.userName().let { if (!it.isNullOrEmpty()) "Halo, $it" else ""}
                                tvTitleBarDescription.visibility = tvTitleBarDescription.text.let { if (it.isNotEmpty()) View.VISIBLE else View.GONE }
                            }

                            if (!onlySession) {
                                if (isSearchActive) {
                                    searchContact()
                                } else {
                                    if (userKind == USER_KIND_ADMIN) getCities()
                                    else getContacts()
                                }
                            }
                        }

                    } RESPONSE_STATUS_EMPTY -> missingDataHandler()

                }

            } catch (e: Exception) {
                Log.d("TAG USER LOGGED IN", "Failed run service. Exception " + e.message)
            }

        }

    }

    private fun searchContact(key: String = "${etSearchBox.text}") {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val searchKey = createPartFromString(key)

                val statusFilter = selectedStatusID.toLowerCase(Locale.ROOT)
                val cityID = if (userKind == USER_KIND_ADMIN) selectedCitiesID?.id_city
                else userCity

                val apiService: ApiService = HttpClient.create()
                val response = if (cityID != null && statusFilter != "-1") {
                    apiService.searchContact(cityId = createPartFromString(cityID), status = createPartFromString(statusFilter), key = searchKey, distributorID = createPartFromString(userDistributorId))
                } else if (cityID != null) {
                    apiService.searchContact(cityId = createPartFromString(cityID), key = searchKey, distributorID = createPartFromString(userDistributorId))
                } else if (statusFilter != "-1" ) {
                    apiService.searchContactByStatus(status = createPartFromString(statusFilter), key = searchKey, distributorID = createPartFromString(userDistributorId))
                } else apiService.searchContact(key = searchKey, distributorID = createPartFromString(userDistributorId))

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    var textFilter = ""

                    if (selectedStatusID != "-1" || selectedVisitedID != "-1" || selectedCitiesID != null) {
                        textFilter += if (selectedCitiesID != null && selectedCitiesID?.id_city != "-1") selectedCitiesID?.nama_city else ""
                        textFilter += if (selectedStatusID != "-1") if (textFilter.isNotEmpty()) ", $selectedStatusID" else selectedStatusID else ""
                        textFilter += if (selectedVisitedID != "-1") if (textFilter.isNotEmpty()) ", $selectedVisitedID" else selectedVisitedID else ""
                    } else textFilter = getString(R.string.tidak_ada_filter)

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            contacts = responseBody.results
                            setRecyclerView(responseBody.results)
                            binding.tvFilter.text = "$textFilter (${responseBody.results.size})"
                            loadingState(false)

                        }
                        RESPONSE_STATUS_EMPTY -> {

                            loadingState(true, "Daftar kontak kosong!")
                            binding.tvFilter.text = "$textFilter (${responseBody.results.size})"

                        }
                        else -> {

                            handleMessage(this@MainActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                            loadingState(true, getString(R.string.failed_request))

                        }
                    }

                } else {

                    handleMessage(this@MainActivity, TAG_RESPONSE_CONTACT, "Failed get data! Message: " + response.message())
                    loadingState(true, getString(R.string.failed_request))

                }


            } catch (e: Exception) {

                handleMessage(this@MainActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))

            }

        }

    }

    private fun setRecyclerView(listItem: ArrayList<ContactModel>) {

        rvAdapter = ContactsRecyclerViewAdapter(listItem, object: ContactsRecyclerViewAdapter.ItemClickListener {
            override fun onItemClick(data: ContactModel?) {
                navigateDetailContact(data)
            }

            override fun updateSelectedItem(count: Int, selectedItemsId: MutableSet<String>) {
                if (isSelectItemActive) {
                    this@MainActivity.selectedItemsId = selectedItemsId
                    selectedItemCount = count
                    binding.titleBar.tvTitleBar.text = "$selectedItemCount Item terpilih"
                    if (selectedItemCount > 0)
                        binding.titleBar.icConfirmSelect.alpha = 1f
                    else
                        binding.titleBar.icConfirmSelect.alpha =
                            if (CustomUtility(this@MainActivity).isDarkMode()) 0.2f
                            else 0.5f
                }
            }

        })

        rvAdapter.setSelectItemState(isSelectItemActive)
        rvAdapter.setSelectedItemsId(selectedItemsId)
        rvAdapter.callback = { result ->
            onSelectedItems(result)
        }
        rvListChat.layoutManager = LinearLayoutManager(this@MainActivity)
        rvListChat.adapter = rvAdapter
        rvListChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

    private fun logoutConfirmation() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Logout")
            .setMessage("Apakah anda yakin ingin keluar?")
            .setNegativeButton("Tidak") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Iya") { dialog, _ ->

                dialog.dismiss()
                logoutHandler()

            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun missingDataHandler() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Data Tidak Lengkap Terdeteksi")
            .setCancelable(false)
            .setMessage("Data login yang tidak lengkap telah terdeteksi, silakan coba login kembali!")
            .setPositiveButton("Oke") { dialog, _ ->

                dialog.dismiss()
                logoutHandler()

            }
        val dialog = builder.create()
        dialog.show()
    }

    @SuppressLint("HardwareIds")
    private fun logoutHandler() {

        // Firebase Auth Session
        try {
            val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            val model = Build.MODEL
            val manufacturer = Build.MANUFACTURER

            val authChild = firebaseReference.child(FIREBASE_CHILD_AUTH)
            val userChild = authChild.child(sessionManager.userName() + sessionManager.userID())
            val userDevices = userChild.child("devices")
            var userDeviceText = "$manufacturer$model$androidId"
            userDeviceText = userDeviceText.replace(".", "_").replace(",", "_").replace(" ", "")
            val userDevice = userDevices.child(userDeviceText)

            userDevice.child("logout_at").setValue(DateFormat.now())
            userDevice.child("login_at").setValue("")
        } catch (e: Exception) {
            Log.d("Firebase Auth", "$e")
        }

        sessionManager.setLoggedIn(LOGGED_OUT)
        sessionManager.setUserLoggedIn(null)

        val intent = Intent(this@MainActivity, SplashScreenActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun checkSwipeRefreshLayoutHint() {
        val swipeRefreshHint = sessionManager.swipeRefreshHint()

        if (!swipeRefreshHint) {
            binding.includeSwipeRefreshHint.swipeRefreshHint.visibility = View.VISIBLE
            binding.includeSwipeRefreshHint.btnTrySwipeRefresh.setOnClickListener {
                binding.includeSwipeRefreshHint.swipeRefreshHint.visibility = View.GONE
                sessionManager.swipeRefreshHint(true)
            }
        } else binding.includeSwipeRefreshHint.swipeRefreshHint.visibility = View.GONE
    }

    private fun toggleSelectItem() {

        isSelectItemActive = !isSelectItemActive

        selectedItemCount = 0
        rvAdapter.clearSelections()
        rvAdapter.setSelectItemState(isSelectItemActive)
        binding.swipeRefreshLayout.isEnabled = !isSelectItemActive

        if (isSelectItemActive) {
            icMore.visibility = View.GONE
            binding.btnFab.visibility = View.GONE
            binding.titleBar.icConfirmSelect.visibility = View.VISIBLE
            binding.titleBar.tvTitleBar.text = "0 Item terpilih"

            binding.titleBar.icConfirmSelect.alpha =
                if (CustomUtility(this@MainActivity).isDarkMode()) 0.2f
                else 0.5f

        } else {
            icMore.visibility = View.VISIBLE
            if (userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN) binding.btnFab.visibility = View.VISIBLE
            binding.titleBar.icConfirmSelect.visibility = View.GONE
            binding.titleBar.tvTitleBar.text = "Semua Toko"
        }

    }

    private fun onSelectedItems(items: ArrayList<ContactModel>) {

        progressDialog.show()

        listCoordinate = arrayListOf()
        listCoordinateName = arrayListOf()
        listCoordinateStatus = arrayListOf()
        listCoordinateCityID = arrayListOf()

        totalProcess = items.size
        LoopingTask(items).execute()
    }

    private inner class LoopingTask(private var items: ArrayList<ContactModel>) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            for (item in items.listIterator()) {
                listCoordinate.add(item.maps_url)
                listCoordinateName.add(item.nama)
                listCoordinateStatus.add(item.store_status)
                listCoordinateCityID.add(item.id_city)

                processed ++
                percentage = (processed * 100) / totalProcess
                progressDialog.setMessage(getString(R.string.txt_loading) + "($percentage%)")
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            val intent = Intent(this@MainActivity, MapsActivity::class.java)
            intent.putExtra(CONST_NEAREST_STORE, true)
            if (isSelectItemActive || isSearchContactByCity) {
                intent.putExtra(CONST_NEAREST_STORE_HIDE_FILTER, true)
                intent.putExtra(CONST_NEAREST_STORE_WITH_DEFAULT_RANGE, -1)
            }
            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)
            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_STATUS, listCoordinateStatus)
            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_CITY_ID, listCoordinateCityID)

            progressDialog.dismiss()
            progressDialog.setMessage(getString(R.string.txt_loading))
            isSearchContactByCity = false
            startActivity(intent)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MAIN_ACTIVITY_REQUEST_CODE) {

            val resultData = data?.getStringExtra("$MAIN_ACTIVITY_REQUEST_CODE")

            if (resultData == SYNC_NOW) {

                getUserLoggedIn()

            }

        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) binding.drawerLayout.closeDrawer(GravityCompat.START)
        else {

            if (isSearchActive) toggleSearchEvent(SEARCH_CLOSE)

            else if (isSelectItemActive) toggleSelectItem()

            else {

                if (userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN || userKind == USER_KIND_MARKETING) super.onBackPressed()
                else {

                    if (doubleBackToExitPressedOnce) {
                        super.onBackPressed()
                        return
                    }

                    this@MainActivity.doubleBackToExitPressedOnce = true
                    handleMessage(this@MainActivity, TAG_ACTION_MAIN_ACTIVITY, getString(R.string.tekan_sekali_lagi), TOAST_SHORT)

                    Handler(Looper.getMainLooper()).postDelayed({
                        doubleBackToExitPressedOnce = false
                    }, 2000)

                }

            }

        }

    }

    override fun onStart() {
        super.onStart()
        Handler(Looper.getMainLooper()).postDelayed({
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    true,
                    sessionManager.userDistributor() ?: "-custom-001",
                    sessionManager.userID().toString()
                )
            }
            getUserLoggedIn(true)
        }, 1000)
    }

    override fun onStop() {
        super.onStop()

        if (sessionManager.isLoggedIn()) {
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    sessionManager.userDistributor() ?: "-custom-001",
                    sessionManager.userID().toString()
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (sessionManager.isLoggedIn()) {
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    sessionManager.userDistributor() ?: "-custom-001",
                    sessionManager.userID().toString()
                )
            }
        }
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