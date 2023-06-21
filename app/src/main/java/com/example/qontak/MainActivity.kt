package com.example.qontak

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qontak.adapter.ListContactRecyclerViewAdapter
import com.example.qontak.commons.RESPONSE_STATUS_OK
import com.example.qontak.commons.TAG_RESPONSE_CONTACT
import com.example.qontak.data.ApiService
import com.example.qontak.data.HttpClient
import com.example.qontak.model.ContactModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var scaleAnimation: Animation

    private lateinit var rlLoading: RelativeLayout
    private lateinit var rvListChat: RecyclerView
    private lateinit var btnFab: FloatingActionButton
    private lateinit var icSyncNow: ImageView
    private lateinit var icMore: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()
        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_anim)

        initVariable()
        initClickHandler()
        loadingState(true)
        setRecyclerView()

    }

    private fun setRecyclerView() {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getContacts()

                if (response.status == RESPONSE_STATUS_OK) {

                    val listItem: ArrayList<ContactModel> = response.results

                    rvListChat.layoutManager = LinearLayoutManager(this@MainActivity)
                    rvListChat.adapter = ListContactRecyclerViewAdapter(this@MainActivity, listItem)

                    loadingState(false)

                } else {

                    handleMessage(TAG_RESPONSE_CONTACT, "Failed get data")
                    loadingState(false)

                }


            } catch (e: Exception) {

                handleMessage(TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(false)

            }

        }

    }

    private fun loadingState(state: Boolean) {

        if (state) {

            rlLoading.visibility = View.VISIBLE
            rvListChat.visibility = View.GONE

            icSyncNow.startAnimation(scaleAnimation)

        } else {

            rlLoading.visibility = View.GONE
            rvListChat.visibility = View.VISIBLE

            Handler().postDelayed({

                icSyncNow.clearAnimation()

            }, 500)

        }

    }

    private fun handleMessage(tag: String, message: String) {

        Log.d(tag, message)
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()

    }

    private fun navigateAddNewRoom() {

        val intent = Intent(this, NewRoomChatFormActivity::class.java)
        startActivity(intent)

    }

    private fun initVariable() {

        rlLoading = findViewById(R.id.rl_loading)
        rvListChat = findViewById(R.id.rv_chat_list)
        btnFab = findViewById(R.id.btn_fab)
        icSyncNow = findViewById(R.id.ic_sync_now)
        icMore = findViewById(R.id.ic_more)

        // Set Title Bar
//        icSyncNow.visibility = View.VISIBLE
        icMore.visibility = View.VISIBLE

    }

    private fun initClickHandler() {

        btnFab.setOnClickListener { navigateAddNewRoom() }
//        icSyncNow.setOnClickListener { setRecyclerView() }
        icMore.setOnClickListener { showPopupMenu() }

    }

    private fun showPopupMenu() {
        val popupMenu = PopupMenu(this@MainActivity, icMore)
        popupMenu.inflate(R.menu.option_main_menu)
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.option_sync_now -> {
                    handleMessage(TAG_RESPONSE_CONTACT, "Option Sync Now Selected")
                    true
                }
                R.id.option_search -> {
                    handleMessage(TAG_RESPONSE_CONTACT, "Option Search Selected")
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    override fun onResume() {

        super.onResume()
        setRecyclerView()

    }

}