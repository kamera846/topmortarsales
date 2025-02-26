package com.topmortar.topmortarsales.view.user

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.UsersRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_ADMIN
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_ADMIN_CITY
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_BA
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_COURIER
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_MARKETING
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_PENAGIHAN
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_SALES
import com.topmortar.topmortarsales.commons.CONST_FULL_NAME
import com.topmortar.topmortarsales.commons.CONST_IS_NOTIFY
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.CONST_USER_CITY
import com.topmortar.topmortarsales.commons.CONST_USER_ID
import com.topmortar.topmortarsales.commons.CONST_USER_LEVEL
import com.topmortar.topmortarsales.commons.EMPTY_FIELD_VALUE
import com.topmortar.topmortarsales.commons.MANAGE_USER_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityManageUserBinding
import com.topmortar.topmortarsales.model.UserModel
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
@Suppress("DEPRECATION")
class ManageUserActivity : AppCompatActivity(), UsersRecyclerViewAdapter.ItemClickListener {

    private lateinit var scaleAnimation: Animation
    private lateinit var binding: ActivityManageUserBinding

    private lateinit var rlLoading: RelativeLayout
    private lateinit var rlParent: RelativeLayout
    private lateinit var txtLoading: TextView
    private lateinit var titleBar: TextView
    private lateinit var rvListItem: RecyclerView
    private lateinit var llTitleBar: LinearLayout
    private lateinit var llSearchBox: LinearLayout
    private lateinit var btnFab: FloatingActionButton
    private lateinit var icBack: ImageView
    private lateinit var icSearch: ImageView
    private lateinit var icCloseSearch: ImageView
    private lateinit var icClearSearch: ImageView
    private lateinit var etSearchBox: EditText

    // Global
    private lateinit var sessionManager: SessionManager
    private val userDistributorId get() = sessionManager.userDistributor().toString()
    private val userKind get() = sessionManager.userKind().toString()
    private val userCityID get() = sessionManager.userCityID().toString()
    private var users: ArrayList<UserModel> = arrayListOf()
    private var activeFilter = EMPTY_FIELD_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge()

        sessionManager = SessionManager(this)
        binding = ActivityManageUserBinding.inflate(layoutInflater)

        setContentView(binding.root)

        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_anim)

        // Get the current theme mode (light or dark)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) binding.llFilter.componentFilter.background = AppCompatResources.getDrawable(this, R.color.black_400)
        else binding.llFilter.componentFilter.background = AppCompatResources.getDrawable(this, R.color.light)

        initVariable()
        initClickHandler()
        getList()
        binding.swipeRefreshLayout.setOnRefreshListener { getList() }

    }

    private fun initVariable() {

        rlLoading = findViewById(R.id.rl_loading)
        rlParent = findViewById(R.id.rl_parent)
        txtLoading = findViewById(R.id.txt_loading)
        rvListItem = findViewById(R.id.rv_chat_list)
        llTitleBar = findViewById(R.id.title_bar)
        llSearchBox = findViewById(R.id.search_box)
        btnFab = findViewById(R.id.btn_fab)
        icBack = llTitleBar.findViewById(R.id.ic_back)
        icSearch = llTitleBar.findViewById(R.id.ic_search)
        titleBar = llTitleBar.findViewById(R.id.tv_title_bar)
        icCloseSearch = findViewById(R.id.ic_close_search)
        icClearSearch = findViewById(R.id.ic_clear_search)
        etSearchBox = findViewById(R.id.et_search_box)

        // Set Title Bar
        icBack.visibility = View.VISIBLE
//        icSearch.visibility = View.VISIBLE
        titleBar.text = "Kelola Pengguna"

    }

    private fun initClickHandler() {

        btnFab.setOnClickListener { navigateAddUser() }
        icBack.setOnClickListener { finish() }
        binding.llFilter.componentFilter.setOnClickListener { showFilterMenu() }

    }

    private fun getList() {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = when (userKind) {
                    USER_KIND_ADMIN -> apiService.getUsers(distributorID = userDistributorId)
                    else -> apiService.getUsers(cityId = userCityID, distributorID = userDistributorId)
                }

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        binding.llFilter.tvFilter.text = getString(R.string.tidak_ada_filter)
                        activeFilter = EMPTY_FIELD_VALUE

                        val includeFilter = findViewById<LinearLayout>(R.id.ll_filter)
                        includeFilter.visibility = View.VISIBLE

                        users = response.results
                        users.removeIf { it.level_user == AUTH_LEVEL_ADMIN }
                        setRecyclerView(users)
                        loadingState(false)
//                        loadingState(true, "Success get data!")

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Daftar pengguna kosong!")

                    }
                    else -> {

                        handleMessage(this@ManageUserActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        loadingState(true, getString(R.string.failed_request))

                    }
                }


            } catch (e: Exception) {

                handleMessage(this@ManageUserActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))

            }

        }

    }

    private fun setRecyclerView(listItem: ArrayList<UserModel>) {
        val rvAdapter = UsersRecyclerViewAdapter(this@ManageUserActivity)
        rvAdapter.setListItem(listItem)

        rvListItem.apply {
            layoutManager = LinearLayoutManager(this@ManageUserActivity)
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

        txtLoading.text = message

        if (state) {

            rlLoading.visibility = View.VISIBLE
            rvListItem.visibility = View.GONE

            binding.swipeRefreshLayout.isRefreshing = message === getString(R.string.txt_loading)

        } else {

            rlLoading.visibility = View.GONE
            rvListItem.visibility = View.VISIBLE
            binding.swipeRefreshLayout.isRefreshing = false

        }

    }

    private fun navigateAddUser(data: UserModel? = null) {

        val intent = Intent(this@ManageUserActivity, AddUserActivity::class.java)

        if (data != null) {
            intent.putExtra(CONST_USER_ID, data.id_user)
            intent.putExtra(CONST_PHONE, data.phone_user)
            intent.putExtra(CONST_NAME, data.username)
            intent.putExtra(CONST_USER_LEVEL, data.level_user)
            intent.putExtra(CONST_LOCATION, data.id_city)
            intent.putExtra(CONST_FULL_NAME, data.full_name)
            intent.putExtra(CONST_IS_NOTIFY, data.is_notify)
        }

        startActivityForResult(intent, MANAGE_USER_ACTIVITY_REQUEST_CODE)

    }

    private fun navigateDetailUser(data: UserModel? = null) {

        val intent = Intent(this@ManageUserActivity, UserProfileActivity::class.java)

        if (data != null) {
            intent.putExtra(CONST_USER_ID, data.id_user)
            intent.putExtra(CONST_PHONE, data.phone_user)
            intent.putExtra(CONST_NAME, data.username)
            intent.putExtra(CONST_USER_LEVEL, data.level_user)
            intent.putExtra(CONST_LOCATION, data.id_city)
            intent.putExtra(CONST_FULL_NAME, data.full_name)
            intent.putExtra(CONST_IS_NOTIFY, data.is_notify)
            intent.putExtra(CONST_USER_CITY, data.id_city)
        }

        startActivityForResult(intent, MANAGE_USER_ACTIVITY_REQUEST_CODE)

    }

    override fun onItemClick(data: UserModel?) {
        if (data?.level_user == AUTH_LEVEL_SALES || data?.level_user == AUTH_LEVEL_COURIER || data?.level_user == AUTH_LEVEL_BA || data?.level_user == AUTH_LEVEL_PENAGIHAN  || data?.level_user == AUTH_LEVEL_MARKETING) navigateDetailUser(data)
        else navigateAddUser(data)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MANAGE_USER_ACTIVITY_REQUEST_CODE) {

            val resultData = data?.getStringExtra("$MANAGE_USER_ACTIVITY_REQUEST_CODE")

            if (resultData == SYNC_NOW) {

                getList()

            }

        }

    }

    private fun showFilterMenu() {

        val popupMenu = PopupMenu(this, binding.llFilter.componentFilter, Gravity.END)
        popupMenu.menuInflater.inflate(R.menu.option_level_user, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
            when (item?.itemId) {
                R.id.option_none -> {
                    activeFilter = EMPTY_FIELD_VALUE
                    synchFilter()
                    return@setOnMenuItemClickListener  true
                } R.id.option_admin -> {
                    activeFilter = AUTH_LEVEL_ADMIN_CITY
                    synchFilter()
                    return@setOnMenuItemClickListener  true
                } R.id.option_sales -> {
                    activeFilter = AUTH_LEVEL_SALES
                    synchFilter()
                    return@setOnMenuItemClickListener  true
                } R.id.option_courier -> {
                    activeFilter = AUTH_LEVEL_COURIER
                    synchFilter()
                    return@setOnMenuItemClickListener  true
                } R.id.option_ba -> {
                    activeFilter = AUTH_LEVEL_BA
                    synchFilter()
                    return@setOnMenuItemClickListener  true
                } R.id.option_marketing -> {
                    activeFilter = AUTH_LEVEL_MARKETING
                    synchFilter()
                    return@setOnMenuItemClickListener  true
                } R.id.option_penagihan -> {
                    activeFilter = AUTH_LEVEL_PENAGIHAN
                    synchFilter()
                    return@setOnMenuItemClickListener  true
                } else -> return@setOnMenuItemClickListener false
            }
        }

        popupMenu.show()
    }

    private fun synchFilter() {
        if (activeFilter == EMPTY_FIELD_VALUE) getList()
        else {
            val textActiveFilter = "Level " + when (activeFilter) {
                AUTH_LEVEL_ADMIN_CITY -> "admin"
                else -> activeFilter
            }
            binding.llFilter.tvFilter.text = textActiveFilter
            loadingState(true)
            Handler(Looper.getMainLooper()).postDelayed({
                val usersDummy = arrayListOf<UserModel>()
                for (item in users.iterator()) {
                    if (item.level_user == activeFilter) usersDummy.add(item)
                }

                setRecyclerView(usersDummy)
                loadingState(false)
            }, 500)
        }
    }

}