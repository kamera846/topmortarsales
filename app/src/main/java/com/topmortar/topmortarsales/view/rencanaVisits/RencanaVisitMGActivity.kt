package com.topmortar.topmortarsales.view.rencanaVisits

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_CITY_ID
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_NAME
import com.topmortar.topmortarsales.commons.CONST_LIST_COORDINATE_STATUS
import com.topmortar.topmortarsales.commons.CONST_NEAREST_STORE
import com.topmortar.topmortarsales.commons.CONST_NEAREST_STORE_HIDE_FILTER
import com.topmortar.topmortarsales.commons.CONST_NEAREST_STORE_WITH_DEFAULT_RANGE
import com.topmortar.topmortarsales.commons.utils.CustomProgressBar
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.EventBusUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.databinding.ActivityRencanaVisitMgBinding
import com.topmortar.topmortarsales.model.RencanaVisitModel
import com.topmortar.topmortarsales.view.MapsActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class RencanaVisitMGActivity : AppCompatActivity() {

    private var _binding: ActivityRencanaVisitMgBinding? = null
    private lateinit var sessionManager: SessionManager
    private val userId get() = sessionManager.userID()
    private val userDistributorId get() = sessionManager.userDistributor()
    private val binding get() = _binding!!

    private lateinit var myFragment: MGFragment
    private lateinit var progressBar: CustomProgressBar

    private var itemCount = 0
    private var selectedItemCount = 0
    private var isSelectBarActive = false
    private var totalProcess = 0
    private var processed = 0
    private var percentage = 0

    private lateinit var listCoordinate: ArrayList<String>
    private lateinit var listCoordinateName: ArrayList<String>
    private lateinit var listCoordinateStatus: ArrayList<String>
    private lateinit var listCoordinateCityID: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge()

        sessionManager = SessionManager(this)
        _binding = ActivityRencanaVisitMgBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBar = CustomProgressBar(this)
        progressBar.setMessage(getString(R.string.txt_loading))

        binding.titleBarDark.tvTitleBar.text = "Rencana Visit MG"
        binding.titleBarDark.icRoadMap.visibility = View.VISIBLE
        binding.titleBarDark.icRoadMap.setOnClickListener { showMapsOption() }
        binding.titleBarDark.icBack.setOnClickListener { finish() }

        if (CustomUtility(this).isUserWithOnlineStatus()) {
            CustomUtility(this).setUserStatusOnline(
                true,
                userDistributorId ?: "-custom-011",
                userId ?: ""
            )
        }

        /*
        Call Fragment
         */
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        myFragment = MGFragment()
        fragmentTransaction.replace(R.id.renviMGFragmentContainer, myFragment)
        fragmentTransaction.addToBackStack(null)

        fragmentTransaction.commit()
        /*
        End Call Fragment
         */

        binding.selectTitleBarDark.componentSelectTitleBarDark.visibility = View.GONE
        binding.selectTitleBarDark.icCloseSelect.setOnClickListener { toggleSelectBar() }
        binding.selectTitleBarDark.icConfirmSelect.alpha =
            if (CustomUtility(this).isDarkMode()) 0.2f
            else 0.5f
        binding.selectTitleBarDark.icConfirmSelect.setOnClickListener {
            if (selectedItemCount > 0) {
                progressBar.show()
                myFragment.onConfirmSelected()
            }
        }

        myFragment.setCounterItem(object: MGFragment.CounterItem{
            override fun counterItem(count: Int) {
                itemCount = count
                binding.titleBarDark.tvTitleBar.text = "Rencana Visit MG" + "${if (count != 0) " ($count)" else ""}"
            }

        })

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                myOnBackPressed()
            }

        })

    }

    private fun showMapsOption() {
        val popupMenu = PopupMenu(this, binding.titleBarDark.icRoadMap)
        popupMenu.inflate(R.menu.option_maps_menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.option_choices -> {
                    if (itemCount != 0) toggleSelectBar()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()

    }

    private fun toggleSelectBar() {
        isSelectBarActive = !isSelectBarActive
        myFragment.isSelectBarActive(isSelectBarActive)

        val titleBar = findViewById<LinearLayout>(R.id.titleBarDark)

        if (isSelectBarActive) {
            binding.selectTitleBarDark.componentSelectTitleBarDark.visibility = View.VISIBLE
            titleBar.visibility = View.GONE
        } else {
            binding.selectTitleBarDark.componentSelectTitleBarDark.visibility = View.GONE
            titleBar.visibility = View.VISIBLE
        }
    }

    fun onSelectedItems(items: ArrayList<RencanaVisitModel>) {

        listCoordinate = arrayListOf()
        listCoordinateName = arrayListOf()
        listCoordinateStatus = arrayListOf()
        listCoordinateCityID = arrayListOf()

        totalProcess = items.size
        LoopingTask(items).execute()
    }

    private inner class LoopingTask(private var items: ArrayList<RencanaVisitModel>) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            for (item in items.listIterator()) {
                listCoordinate.add(item.maps_url)
                listCoordinateName.add(item.nama)
                listCoordinateStatus.add(item.store_status)
                listCoordinateCityID.add(item.id_city)

                processed ++
                percentage = (processed * 100) / totalProcess
                runOnUiThread {
                    progressBar.setMessage(getString(R.string.txt_loading) + "($percentage%)")
                }
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            val intent = Intent(this@RencanaVisitMGActivity, MapsActivity::class.java)
            intent.putExtra(CONST_NEAREST_STORE, true)
            intent.putExtra(CONST_NEAREST_STORE_HIDE_FILTER, true)
            intent.putExtra(CONST_NEAREST_STORE_WITH_DEFAULT_RANGE, -1)
            intent.putStringArrayListExtra(CONST_LIST_COORDINATE, listCoordinate)
            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_NAME, listCoordinateName)
            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_STATUS, listCoordinateStatus)
            intent.putStringArrayListExtra(CONST_LIST_COORDINATE_CITY_ID, listCoordinateCityID)

            progressBar.dismiss()
            progressBar.setMessage(getString(R.string.txt_loading))
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        Handler(Looper.getMainLooper()).postDelayed({
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    true,
                    userDistributorId ?: "-custom-011",
                    userId ?: ""
                )
            }
        }, 1000)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        if (sessionManager.isLoggedIn()) {
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    userDistributorId ?: "-custom-011",
                    userId ?: ""
                )
            }
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        if (::progressBar.isInitialized && progressBar.isShowing()) {
            progressBar.dismiss()
        }
        if (sessionManager.isLoggedIn()) {
            if (CustomUtility(this).isUserWithOnlineStatus()) {
                CustomUtility(this).setUserStatusOnline(
                    false,
                    userDistributorId ?: "-custom-011",
                    userId ?: ""
                )
            }
        }
    }

    private fun myOnBackPressed() {
        if (isSelectBarActive) toggleSelectBar()
        else finish()
    }

    @Subscribe
    fun onEventBus(event: EventBusUtils.IntEvent) {
        selectedItemCount = event.data!!
        binding.selectTitleBarDark.selectionTitle.text = "$selectedItemCount Item Terpilih"
        if (selectedItemCount > 0)
            binding.selectTitleBarDark.icConfirmSelect.alpha = 1f
        else
            binding.selectTitleBarDark.icConfirmSelect.alpha =
                if (CustomUtility(this).isDarkMode()) 0.2f
                else 0.5f
    }

}