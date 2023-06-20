package com.example.qontak

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

class NewRoomChatFormActivity : AppCompatActivity() {

    private lateinit var icBack: ImageView
    private lateinit var btnSubmit: Button
    private lateinit var tvMaxMessage: TextView
    private lateinit var etMessage: EditText

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_room_chat_form)

        supportActionBar?.hide()

        icBack = findViewById(R.id.ic_back)
        btnSubmit = findViewById(R.id.btn_submit)
        tvMaxMessage = findViewById(R.id.tv_max_message)
        etMessage = findViewById(R.id.et_message)

        icBack.setOnClickListener { finish() }
        btnSubmit.setOnClickListener { finish() }

        etMessageListener()

    }

    private fun etMessageListener() {

        val maxLines = 5
        val maxLength = 50

        etMessage.maxLines = maxLines
        etMessage.setMaxLength(maxLength)

        etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val txtLength = etMessage.text.length
                tvMaxMessage.text = "$txtLength/$maxLength"
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

    }

    private fun EditText.setMaxLength(maxLength: Int) {
        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
    }
}