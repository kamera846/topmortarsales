package com.example.qontak

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView

class NewRoomChatFormActivity : AppCompatActivity() {

    private lateinit var icBack: ImageView
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_room_chat_form)

        supportActionBar?.hide()

        icBack = findViewById(R.id.ic_back)
        btnSubmit = findViewById(R.id.btn_submit)

        icBack.setOnClickListener { finish() }
        btnSubmit.setOnClickListener { finish() }
    }
}