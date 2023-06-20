package com.example.qontak

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qontak.adapter.ListChatRecyclerViewAdapter
import com.example.qontak.data.HttpClient
import com.example.qontak.model.ChatModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var rlLoading: RelativeLayout
    private lateinit var rvListChat: RecyclerView
    private lateinit var btnFab: FloatingActionButton

    private var isLoading = true

    private var LIST_CHAT_TAG = "FETCH LIST CHAT"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rlLoading = findViewById(R.id.rl_loading)
        rvListChat = findViewById(R.id.rv_chat_list)
        btnFab = findViewById(R.id.btn_fab)

        supportActionBar?.hide()

        btnFab.setOnClickListener {
            navigateAddNewRoom()
        }

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
                    rvListChat.adapter = ListChatRecyclerViewAdapter(listItem)

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

        isLoading = state

        if (isLoading) {

            rlLoading.visibility = View.VISIBLE
            rvListChat.visibility = View.GONE

        } else {

            rlLoading.visibility = View.GONE
            rvListChat.visibility = View.VISIBLE

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

    override fun onResume() {
        super.onResume()
        setRecyclerView()
    }

}