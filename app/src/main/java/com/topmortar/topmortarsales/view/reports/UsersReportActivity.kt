package com.topmortar.topmortarsales.view.reports

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
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.UsersRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_FULL_NAME
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_USER_ID
import com.topmortar.topmortarsales.commons.DETAIL_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.model.UserModel
import kotlinx.coroutines.launch

class UsersReportActivity : AppCompatActivity(), UsersRecyclerViewAdapter.ItemClickListener {

    private lateinit var scaleAnimation: Animation

    private lateinit var rlLoading: RelativeLayout
    private lateinit var rlParent: RelativeLayout
    private lateinit var txtLoading: TextView
    private lateinit var titleBar: TextView
    private lateinit var rvListItem: RecyclerView
    private lateinit var llTitleBar: LinearLayout
    private lateinit var llSearchBox: LinearLayout
    private lateinit var icBack: ImageView
    private lateinit var icSearch: ImageView
    private lateinit var icCloseSearch: ImageView
    private lateinit var icClearSearch: ImageView
    private lateinit var etSearchBox: EditText

    // Global
    private lateinit var sessionManager: SessionManager
    private val userID get() = sessionManager.userID().toString()
    private var iContactID: String = ""
    private var iContactName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge()

        sessionManager = SessionManager(this)

        setContentView(R.layout.activity_users_report)

        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_anim)

        iContactID = intent.getStringExtra(CONST_CONTACT_ID).toString()
        iContactName = intent.getStringExtra(CONST_NAME).toString()

        if (sessionManager.userKind() == USER_KIND_COURIER || sessionManager.userKind() == USER_KIND_SALES) {
            CustomUtility(this).setUserStatusOnline(
                true,
                sessionManager.userDistributor() ?: "-custom-014",
                sessionManager.userID() ?: ""
            )
        }

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
        icBack = llTitleBar.findViewById(R.id.ic_back)
        icSearch = llTitleBar.findViewById(R.id.ic_search)
        titleBar = llTitleBar.findViewById(R.id.tv_title_bar)
        icCloseSearch = findViewById(R.id.ic_close_search)
        icClearSearch = findViewById(R.id.ic_clear_search)
        etSearchBox = findViewById(R.id.et_search_box)

        // Set Title Bar
        icBack.visibility = View.VISIBLE
//        icSearch.visibility = View.VISIBLE
        titleBar.text = "Daftar User"

    }

    private fun initClickHandler() {

        icBack.setOnClickListener { finish() }

    }

    private fun getList() {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.listUsersReport(idUser = userID, idContact = iContactID)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        setRecyclerView(response.results)
                        loadingState(false)
//                        loadingState(true, "Success get data!")

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Belum ada laporan dari sales untuk toko ini!")

                    }
                    else -> {

                        handleMessage(this@UsersReportActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        loadingState(true, getString(R.string.failed_request))

                    }
                }


            } catch (e: Exception) {

                FirebaseUtils.logErr(this@UsersReportActivity, "Failed UsersReportActivity on getList(). Catch: ${e.message}")
                handleMessage(this@UsersReportActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))

            }

        }

    }

    private fun setRecyclerView(listItem: ArrayList<UserModel>) {
        val rvAdapter = UsersRecyclerViewAdapter(this@UsersReportActivity)
        rvAdapter.setListItem(listItem)
        rvAdapter.setIsListReport(true)

        rvListItem.apply {
            layoutManager = LinearLayoutManager(this@UsersReportActivity)
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

    override fun onItemClick(data: UserModel?) {

        val intent = Intent(this@UsersReportActivity, ReportsActivity::class.java)

        intent.putExtra(CONST_CONTACT_ID, iContactID)
        intent.putExtra(CONST_USER_ID, data?.id_user)
        intent.putExtra(CONST_NAME, iContactName)
        intent.putExtra(CONST_FULL_NAME, data?.full_name)

        startActivityForResult(intent, DETAIL_ACTIVITY_REQUEST_CODE)
    }

    override fun onStart() {
        super.onStart()
        Handler(Looper.getMainLooper()).postDelayed({
            if (sessionManager.userKind() == USER_KIND_COURIER || sessionManager.userKind() == USER_KIND_SALES) {
                CustomUtility(this).setUserStatusOnline(
                    true,
                    sessionManager.userDistributor() ?: "-custom-014",
                    sessionManager.userID() ?: ""
                )
            }
        }, 1000)
    }

    override fun onStop() {
        super.onStop()

        if (sessionManager.isLoggedIn()) {
            if (sessionManager.userKind() == USER_KIND_COURIER || sessionManager.userKind() == USER_KIND_SALES) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    sessionManager.userDistributor() ?: "-custom-014",
                    sessionManager.userID() ?: ""
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (sessionManager.isLoggedIn()) {
            if (sessionManager.userKind() == USER_KIND_COURIER || sessionManager.userKind() == USER_KIND_SALES) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    sessionManager.userDistributor() ?: "-custom-014",
                    sessionManager.userID() ?: ""
                )
            }
        }
    }

}