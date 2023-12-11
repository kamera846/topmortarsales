package com.topmortar.topmortarsales.view.user

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.UsersRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_BA
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_COURIER
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_SALES
import com.topmortar.topmortarsales.commons.CONST_FULL_NAME
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.CONST_USER_ID
import com.topmortar.topmortarsales.commons.CONST_USER_LEVEL
import com.topmortar.topmortarsales.commons.MANAGE_USER_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.model.UserModel
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class ManageUserActivity : AppCompatActivity(), UsersRecyclerViewAdapter.ItemClickListener {

    private lateinit var scaleAnimation: Animation

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
    private var doubleBackToExitPressedOnce = false

    // Initialize Search Engine
    private val searchDelayMillis = 500L
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var previousSearchTerm = ""
    private var isSearchActive = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)

        setContentView(R.layout.activity_manage_user)

        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_anim)

        initVariable()
        initClickHandler()
        getList()

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

    }

    private fun getList() {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getUsers(distributorID = userDistributorId)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        setRecyclerView(response.results)
                        loadingState(false)
//                        loadingState(true, "Success get data!")

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Daftar kontak kosong!")

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

        } else {

            rlLoading.visibility = View.GONE
            rvListItem.visibility = View.VISIBLE

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
        }

        startActivityForResult(intent, MANAGE_USER_ACTIVITY_REQUEST_CODE)

    }

    override fun onItemClick(data: UserModel?) {
        if (data?.level_user == AUTH_LEVEL_SALES || data?.level_user == AUTH_LEVEL_COURIER || data?.level_user == AUTH_LEVEL_BA) navigateDetailUser(data)
        else navigateAddUser(data)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MANAGE_USER_ACTIVITY_REQUEST_CODE) {

            val resultData = data?.getStringExtra("$MANAGE_USER_ACTIVITY_REQUEST_CODE")

            if (resultData == SYNC_NOW) {

                getList()

            }

        }

    }

}