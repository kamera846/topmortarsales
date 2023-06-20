package com.example.qontak

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qontak.adapter.ListChatRecyclerViewAdapter
import com.example.qontak.commons.LIST_CHAT_TAG
import com.example.qontak.data.HttpClient
import com.example.qontak.model.ChatModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var scaleAnimation: Animation

    private lateinit var rlLoading: RelativeLayout
    private lateinit var rvListChat: RecyclerView
    private lateinit var btnFab: FloatingActionButton
    private lateinit var icSyncNow: ImageView

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

                val response = HttpClient.apiService.getPosts()

                if (response.success) {

                    val listItem: ArrayList<ChatModel>
                    val data = response.data.data

                    listItem = data

                    rvListChat.layoutManager = LinearLayoutManager(this@MainActivity)
                    rvListChat.adapter = ListChatRecyclerViewAdapter(this@MainActivity, listItem)

                    loadingState(false)

                } else {

                    handleMessage(LIST_CHAT_TAG, "Failed get data: " + response.message)
                    loadingState(false)

                }

            } catch (e: Exception) {

                handleMessage(LIST_CHAT_TAG, "Failed run service: " + e.message)
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

        // Set Title Bar
        icSyncNow.visibility = View.VISIBLE

    }

    private fun initClickHandler() {

        btnFab.setOnClickListener { navigateAddNewRoom() }
        icSyncNow.setOnClickListener { setRecyclerView() }

    }

    override fun onResume() {

        super.onResume()
        setRecyclerView()

    }

}