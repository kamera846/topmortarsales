package com.example.qontak

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qontak.adapter.ListChatRecyclerViewAdapter
import com.example.qontak.model.ChatModel
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

        setRecyclerView()

    }

    private fun setRecyclerView() {

        val listItem = listOf(
            ChatModel("Toko New Tunggal Jaya MLG", "Ok."),
            ChatModel("CV Fahri Putra Mandiri SBY", "Baik, nanti akan ada tim kami yang menghubungi ya! Terimakasih."),
            ChatModel("Pak Hartawan", "Siap pak."),
            ChatModel("abc", "Hai, Cimot. Order anda telah kami terima."),
            ChatModel("Greaty", "Ini no kamu al?"),
            ChatModel("TB Berkah Abadi MDN", "Baik, akan segera kami proses ya!. Terimakasih."),
            ChatModel("Sari Bumi Raya Rangkah SDA", "Iya baik, sama-sama."),
            ChatModel("Toko New Tunggal Jaya MLG", "Ok."),
            ChatModel("CV Fahri Putra Mandiri SBY", "Baik, nanti akan ada tim kami yang menghubungi ya! Terimakasih."),
            ChatModel("Pak Hartawan", "Siap pak."),
            ChatModel("abc", "Hai, Cimot. Order anda telah kami terima."),
            ChatModel("Greaty", "Ini no kamu al?"),
            ChatModel("TB Berkah Abadi MDN", "Baik, akan segera kami proses ya!. Terimakasih."),
            ChatModel("Sari Bumi Raya Rangkah SDA", "Iya baik, sama-sama.")
        )

        rvListChat.layoutManager = LinearLayoutManager(this)
        rvListChat.adapter = ListChatRecyclerViewAdapter(listItem)

    }

    private fun navigateAddNewRoom() {

        val intent = Intent(this, NewRoomChatFormActivity::class.java)
        startActivity(intent)

    }

}