package com.topmortar.topmortarsales.view.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.topmortar.topmortarsales.adapter.viewpager.UserProfileViewPagerAdapter
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
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.utils.EventBusUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityUserProfileBinding
import com.topmortar.topmortarsales.model.UserModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class UserProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var binding: ActivityUserProfileBinding

    private var iUserName: String? = null; private var iFullName: String? = null; private var iUserLevel: String? = null
    private var iUserID: String? = null; private var iPhone: String? = null; private var iLocation: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initClickHandler()

        iUserID = intent.getStringExtra(CONST_USER_ID)
        iPhone = intent.getStringExtra(CONST_PHONE)
        iUserName = intent.getStringExtra(CONST_NAME)
        iFullName = intent.getStringExtra(CONST_FULL_NAME)
        iUserLevel = intent.getStringExtra(CONST_USER_LEVEL)
        iLocation = intent.getStringExtra(CONST_LOCATION)

        dataActivityValidation()

    }

    private fun getUserDetail() {
        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                sessionManager.userID()?.let {
                    val response = apiService.detailUser(userId = it)
                    when (response.status) {
                        RESPONSE_STATUS_OK -> {

                            val data = response.results[0]

                            iUserID = data.id_user
                            iPhone = data.phone_user
                            iUserName = data.username
                            iFullName = data.full_name
                            iUserLevel = data.level_user
                            iLocation = data.id_city

                            sessionManager.setUserID(data.id_user)
                            sessionManager.setUserName(data.username)
                            sessionManager.setFullName(data.full_name)
                            sessionManager.setUserCityID(data.id_city)

                            dataActivityValidation()

                        } RESPONSE_STATUS_EMPTY -> handleMessage(this@UserProfileActivity, "GET USER DETAIL", "Failed get detail user: ${response.message}")
                        else -> handleMessage(this@UserProfileActivity, "GET USER DETAIL", "Failed get data. Error: ${response.message}")
                    }
                }

            } catch (e: Exception) {
                handleMessage(this@UserProfileActivity, "GET USER DETAIL", "Failed run service. Error: ${e.message}")
            }

        }
    }

    private fun navigateEditUser() {

        val intent = Intent(this, AddUserActivity::class.java)

        intent.putExtra(CONST_USER_ID, iUserID)
        intent.putExtra(CONST_PHONE, iPhone)
        intent.putExtra(CONST_NAME, iUserName)
        intent.putExtra(CONST_USER_LEVEL, iUserLevel)
        intent.putExtra(CONST_LOCATION, iLocation)
        intent.putExtra(CONST_FULL_NAME, iFullName)

        startActivityForResult(intent, MANAGE_USER_ACTIVITY_REQUEST_CODE)

    }

    private fun initClickHandler() {

        binding.titleBarLight.icEdit.visibility = View.VISIBLE
        binding.titleBarLight.icEdit.setOnClickListener {
            navigateEditUser()
        }

    }

    private fun dataActivityValidation() {

        if (iUserID.isNullOrEmpty()) {
            return getUserDetail()
        }

        if (iUserName!!.isNotEmpty()) {
            binding.titleBarLight.tvTitleBar.text = iUserName
            binding.titleBarLight.tvTitleBarDescription.visibility = View.VISIBLE
            binding.titleBarLight.tvTitleBarDescription.text = iUserLevel
            binding.titleBarLight.tvTitleBar.setPadding(0, 0, convertDpToPx(16, this), 0)
        }
        if (iFullName!!.isNotEmpty()) binding.tvFullName.text = iFullName
        if (iUserLevel!!.isNotEmpty()) binding.tvLevel.text = iUserLevel

        if (iUserLevel == AUTH_LEVEL_SALES) {

            binding.titleBarLight.icEdit.visibility = View.VISIBLE
            binding.priceContainer.visibility = View.VISIBLE
            binding.tabContainer.visibility = View.VISIBLE
//            binding.counterContainer.visibility = View.VISIBLE

            val tabLayout: TabLayout = binding.tabLayout
            val viewPager: ViewPager = binding.viewPager // If using ViewPager

            val pagerAdapter = UserProfileViewPagerAdapter(supportFragmentManager) // Create your PagerAdapter
            viewPager.adapter = pagerAdapter

            // Connect TabLayout and ViewPager
            tabLayout.setupWithViewPager(viewPager)

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
        // Handle the event here
        val message = event.message
        Toast.makeText(this, message, TOAST_SHORT).show()
        // Update UI or perform other actions
    }

}