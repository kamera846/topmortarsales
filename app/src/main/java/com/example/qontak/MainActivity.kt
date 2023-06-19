package com.example.qontak

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var rvListChat: RecyclerView
    private lateinit var btnFab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvListChat = findViewById(R.id.rv_chat_list)
        btnFab = findViewById(R.id.btn_fab)

        supportActionBar?.hide()

        btnFab.setOnClickListener {
            navigateAddNewRoom()
        }
    }

    private fun navigateAddNewRoom() {
        val intent = Intent(this, NewRoomChatFormActivity::class.java)
        startActivity(intent)
    }

}