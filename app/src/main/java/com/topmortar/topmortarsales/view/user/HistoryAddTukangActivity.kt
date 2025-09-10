package com.topmortar.topmortarsales.view.user

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.CONST_ADDRESS
import com.topmortar.topmortarsales.commons.CONST_BIRTHDAY
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_DATE
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
import com.topmortar.topmortarsales.commons.CONST_USER_CITY
import com.topmortar.topmortarsales.commons.CONST_USER_ID
import com.topmortar.topmortarsales.commons.CONST_WEEKLY_VISIT_STATUS
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.USER_KIND_PENAGIHAN
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.EventBusUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.databinding.ActivityHistoryAddTukangBinding
import com.topmortar.topmortarsales.model.ContactModel
import com.topmortar.topmortarsales.view.contact.DetailContactActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

@SuppressLint("SetTextI18n")
class HistoryAddTukangActivity : AppCompatActivity() {

    private var _binding: ActivityHistoryAddTukangBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val userId get() = sessionManager.userID()
    private val userKind get() = sessionManager.userKind()
    private val userDistributorId get() = sessionManager.userDistributor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge()

        sessionManager = SessionManager(this)
        _binding = ActivityHistoryAddTukangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBarDark.tvTitleBar.text = "Riwayat Input Tukang"
        binding.titleBarDark.icSyncNow.visibility = View.VISIBLE
        binding.titleBarDark.icBack.setOnClickListener { finish() }

        val iUserID = intent.getStringExtra(CONST_USER_ID)
        val iUserCity = intent.getStringExtra(CONST_USER_CITY)

        if (userKind == USER_KIND_COURIER || userKind == USER_KIND_SALES || userKind == USER_KIND_PENAGIHAN) {
            CustomUtility(this).setUserStatusOnline(true, userDistributorId ?: "-custom-018", userId ?: "")
        }

        /*
        Call Fragment
         */
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val myFragment = UserHistoryAddTukangFragment()
        myFragment.setUserCityParam(iUserCity)
        myFragment.setUserIdParam(iUserID)
        myFragment.setCounterItem(object : UserHistoryAddTukangFragment.CounterItem{
            override fun counterItem(count: Int) {
                // Do something..
            }

        })
        fragmentTransaction.replace(R.id.historyVisitedFragmentContainer, myFragment)
        fragmentTransaction.addToBackStack(null)

        binding.titleBarDark.icSyncNow.setOnClickListener { myFragment.syncNow() }

        fragmentTransaction.commit()
        /*
        End Call Fragment
         */

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                myOnBackPressed()
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null

        if (sessionManager.isLoggedIn()) {
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    sessionManager.userDistributor() ?: "-custom-018",
                    sessionManager.userID().toString()
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

        Handler(Looper.getMainLooper()).postDelayed({
            if (sessionManager.isLoggedIn()) {
                if (CustomUtility(this).isUserWithOnlineStatus()) {
                    CustomUtility(this).setUserStatusOnline(
                        true,
                        sessionManager.userDistributor() ?: "-custom-018",
                        sessionManager.userID().toString()
                    )
                }
            }
        }, 1000)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)

        if (sessionManager.isLoggedIn()) {
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    sessionManager.userDistributor() ?: "-custom-018",
                    sessionManager.userID().toString()
                )
            }
        }
    }

    @Subscribe
    fun onEventBus(event: EventBusUtils.MessageEvent) {
//        navigateDetailContact(event.data)
        AlertDialog.Builder(this)
            .setTitle("Peringatan!")
            .setMessage("Untuk sementara ini anda tidak bisa mengakses toko melalui halaman ini.")
            .setPositiveButton("Oke") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

    }

    private fun navigateDetailContact(data: ContactModel? = null) {

        val intent = Intent(this@HistoryAddTukangActivity, DetailContactActivity::class.java)

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

        startActivity(intent)

    }

    private fun myOnBackPressed() {
        finish()
    }
}