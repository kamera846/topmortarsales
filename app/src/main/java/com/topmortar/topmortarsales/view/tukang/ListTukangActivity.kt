package com.topmortar.topmortarsales.view.tukang

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.TukangRecyclerViewAdapter
import com.topmortar.topmortarsales.adapter.TukangRecyclerViewAdapter.ItemClickListener
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
import com.topmortar.topmortarsales.commons.CONST_SKILL
import com.topmortar.topmortarsales.commons.CONST_STATUS
import com.topmortar.topmortarsales.commons.LOGGED_OUT
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_ACTION_MAIN_ACTIVITY
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.utils.AppUpdateHelper
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.model.TukangModel
import com.topmortar.topmortarsales.view.SplashScreenActivity
import com.topmortar.topmortarsales.view.city.ManageCityActivity
import com.topmortar.topmortarsales.view.skill.ManageSkillActivity
import com.topmortar.topmortarsales.view.user.ManageUserActivity
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class ListTukangActivity : AppCompatActivity(), ItemClickListener {

    private lateinit var scaleAnimation: Animation

    private lateinit var rlLoading: RelativeLayout
    private lateinit var rlParent: RelativeLayout
    private lateinit var txtLoading: TextView
    private lateinit var rvListChat: RecyclerView
    private lateinit var llTitleBar: LinearLayout
    private lateinit var btnFab: FloatingActionButton
    private lateinit var titleBar: TextView
    private lateinit var icMore: ImageView
    private lateinit var tvTitleBarDescription: TextView

    // Global
    private lateinit var sessionManager: SessionManager
    private var doubleBackToExitPressedOnce = false
    private lateinit var userCity: String
    private lateinit var userKind: String
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this@ListTukangActivity)
        userCity = sessionManager.userCityID()!!
        userKind = sessionManager.userKind()!!

        userId = sessionManager.userID()!!
        val isLoggedIn = sessionManager.isLoggedIn()

        if (!isLoggedIn || userId.isEmpty() || userCity.isEmpty() || userKind.isEmpty()) return missingDataHandler()

        setContentView(R.layout.activity_list_tukang)

        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_anim)

        initVariable()
        initClickHandler()
        loadingState(true)
        getTukang()

    }

    private fun initVariable() {

        rlLoading = findViewById(R.id.rl_loading)
        rlParent = findViewById(R.id.rl_parent)
        txtLoading = findViewById(R.id.txt_loading)
        rvListChat = findViewById(R.id.rv_chat_list)
        llTitleBar = findViewById(R.id.title_bar)
        btnFab = findViewById(R.id.btn_fab)
        titleBar = llTitleBar.findViewById(R.id.tv_title_bar)
        icMore = llTitleBar.findViewById(R.id.ic_more)
        tvTitleBarDescription = llTitleBar.findViewById(R.id.tv_title_bar_description)

        // Set Title Bar
        icMore.visibility = View.VISIBLE
        titleBar.text = "List Tukang"
        tvTitleBarDescription.text = sessionManager.userName().let { if (!it.isNullOrEmpty()) "Halo, $it" else ""}
        tvTitleBarDescription.visibility = tvTitleBarDescription.text.let { if (it.isNotEmpty()) View.VISIBLE else View.GONE }


    }

    private fun initClickHandler() {

        btnFab.setOnClickListener { navigateAddTukang() }
        icMore.setOnClickListener { showPopupMenu() }

    }

    private fun loadingState(state: Boolean, message: String = getString(R.string.txt_loading)) {

        txtLoading.text = message

        if (state) {

            rlLoading.visibility = View.VISIBLE
            rvListChat.visibility = View.GONE

        } else {

            rlLoading.visibility = View.GONE
            rvListChat.visibility = View.VISIBLE

        }

    }

    private fun navigateAddTukang(data: TukangModel? = null) {

        val intent = Intent(this@ListTukangActivity, AddTukangActivity::class.java)

        if (data != null) {
            intent.putExtra(CONST_CONTACT_ID, data.id_tukang)
            intent.putExtra(CONST_NAME, data.nama)
            intent.putExtra(CONST_PHONE, data.nomorhp)
            intent.putExtra(CONST_BIRTHDAY, data.tgl_lahir)
            intent.putExtra(CONST_OWNER, data.nama_lengkap)
            intent.putExtra(ACTIVITY_REQUEST_CODE, MAIN_ACTIVITY_REQUEST_CODE)
            intent.putExtra(CONST_LOCATION, data.id_city)
            intent.putExtra(CONST_STATUS, data.tukang_status)
            intent.putExtra(CONST_SKILL, data.id_skill)
//            intent.putExtra(CONST_LOCATION, "1")
        }

        startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_CODE)

    }

    private fun navigateDetailContact(data: TukangModel? = null) {

        val intent = Intent(this@ListTukangActivity, DetailTukangActivity::class.java)

        if (data != null) {
            intent.putExtra(ACTIVITY_REQUEST_CODE, MAIN_ACTIVITY_REQUEST_CODE)
            intent.putExtra(CONST_CONTACT_ID, data.id_tukang)
            intent.putExtra(CONST_NAME, data.nama)
            intent.putExtra(CONST_PHONE, data.nomorhp)
            intent.putExtra(CONST_BIRTHDAY, data.tgl_lahir)
            intent.putExtra(CONST_OWNER, data.nama_lengkap)
            intent.putExtra(CONST_LOCATION, data.id_city)
            intent.putExtra(CONST_MAPS, data.maps_url)
            intent.putExtra(CONST_ADDRESS, data.address)
            intent.putExtra(CONST_STATUS, data.tukang_status)
            intent.putExtra(CONST_SKILL, data.id_skill)
            intent.putExtra(CONST_KTP, data.ktp_tukang)
        }

        startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_CODE)

    }

    private fun showPopupMenu() {
        val popupMenu = PopupMenu(this@ListTukangActivity, icMore)
        popupMenu.inflate(R.menu.option_main_menu)

        val searchItem = popupMenu.menu.findItem(R.id.option_search)
        val userItem = popupMenu.menu.findItem(R.id.option_user)
        val cityItem = popupMenu.menu.findItem(R.id.option_city)
        val nearestStore = popupMenu.menu.findItem(R.id.nearest_store)

        searchItem.isVisible = false
        nearestStore.isVisible = false
        if (sessionManager.userKind() != USER_KIND_ADMIN) {
            userItem.isVisible = false
            cityItem.isVisible = false
        }

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.option_sync_now -> {
                    getTukang()
                    getUserLoggedIn()
                    true
                }
                R.id.option_user -> {
                    startActivity(Intent(this@ListTukangActivity, ManageUserActivity::class.java))
                    true
                }
                R.id.option_city -> {
                    startActivity(Intent(this@ListTukangActivity, ManageCityActivity::class.java))
                    true
                }
                R.id.option_skill -> {
                    startActivity(Intent(this@ListTukangActivity, ManageSkillActivity::class.java))
                    true
                }
                R.id.option_logout -> {
                    logoutConfirmation()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun getTukang() {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getTukang(cityId = userCity)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        setRecyclerView(response.results)
                        loadingState(false)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Data tukang is empty!")

                    }
                    else -> {

                        handleMessage(this@ListTukangActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        loadingState(true, getString(R.string.failed_request))

                    }
                }


            } catch (e: Exception) {

                handleMessage(this@ListTukangActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))

            }

        }

    }

    private fun getUserLoggedIn() {

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.detailUser(userId = userId)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        val data = response.results[0]

                        sessionManager.setUserID(data.id_user)
                        sessionManager.setUserName(data.username)
                        sessionManager.setFullName(data.full_name)
                        sessionManager.setUserCityID(data.id_city)

//                        tvTitleBarDescription.text = sessionManager.fullName().let { if (!it.isNullOrEmpty()) "Halo, $it" else "Halo, ${ sessionManager.userName() }"}
                        tvTitleBarDescription.text = sessionManager.userName().let { if (!it.isNullOrEmpty()) "Halo, $it" else ""}
                        tvTitleBarDescription.visibility = tvTitleBarDescription.text.let { if (it.isNotEmpty()) View.VISIBLE else View.GONE }

                    }
                    RESPONSE_STATUS_EMPTY -> missingDataHandler()
                    else -> Log.d("TAG USER LOGGED IN", "Failed get data!")
                }


            } catch (e: Exception) {
                Log.d("TAG USER LOGGED IN", "Failed run service. Exception " + e.message)
            }

        }

    }

    private fun setRecyclerView(listItem: ArrayList<TukangModel>) {

        val rvAdapter = TukangRecyclerViewAdapter(listItem, this@ListTukangActivity)

        rvListChat.layoutManager = LinearLayoutManager(this@ListTukangActivity)
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
        builder.setTitle("Logout Confirmation")
            .setMessage("Are you sure you want to log out?")
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Yes") { dialog, _ ->

                dialog.dismiss()
                logoutHandler()

            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun missingDataHandler() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Incomplete Data Detected")
            .setMessage("An incomplete login data has been detected, please try to log in again!")
            .setPositiveButton("Yes") { dialog, _ ->

                dialog.dismiss()
                logoutHandler()

            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun logoutHandler() {
        sessionManager.setLoggedIn(LOGGED_OUT)
        sessionManager.setUserKind("")
        sessionManager.setUserID("")
        sessionManager.setUserName("")
        sessionManager.setFullName("")
        sessionManager.setUserCityID("")

        val intent = Intent(this@ListTukangActivity, SplashScreenActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MAIN_ACTIVITY_REQUEST_CODE) {

            val resultData = data?.getStringExtra("$MAIN_ACTIVITY_REQUEST_CODE")

            if (resultData == SYNC_NOW) {

                getTukang()

            }

        }

    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this@ListTukangActivity.doubleBackToExitPressedOnce = true
        handleMessage(this@ListTukangActivity, TAG_ACTION_MAIN_ACTIVITY, "Tekan sekali lagi untuk keluar!", TOAST_SHORT)

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    override fun onItemClick(data: TukangModel?) {

        navigateDetailContact(data)

    }

    override fun onResume() {

        super.onResume()
        // Check apps for update
        AppUpdateHelper.checkForUpdates(this)
        getUserLoggedIn()

    }

}