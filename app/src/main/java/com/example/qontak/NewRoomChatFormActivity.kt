package com.example.qontak

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.example.qontak.commons.ET_MESSAGE
import com.example.qontak.commons.ET_NAME
import com.example.qontak.commons.ET_PHONE

@SuppressLint("SetTextI18n")
class NewRoomChatFormActivity : AppCompatActivity() {

    private lateinit var icBack: ImageView
    private lateinit var btnSubmit: Button
    private lateinit var tvMaxMessage: TextView
    private lateinit var etPhone: EditText
    private lateinit var etName: EditText
    private lateinit var etMessage: EditText

    private val msgMaxLines = 5
    private val msgMaxLength = 50

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_room_chat_form)

        supportActionBar?.hide()

        initVariable()

        icBack.setOnClickListener { finish() }
        btnSubmit.setOnClickListener { finish() }

        dataActivityValidation()
        etMessageListener()

    }

    private fun etMessageListener() {

        etMessage.maxLines = msgMaxLines
        etMessage.setMaxLength(msgMaxLength)

        etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateTxtMaxLength(etMessage.text.length)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

    }

    private fun dataActivityValidation() {

        val intent = intent

        val iPhone = intent.getStringExtra(ET_PHONE)
        val iName = intent.getStringExtra(ET_NAME)
        val iMessage = intent.getStringExtra(ET_MESSAGE)

        if (iPhone != "") etPhone.setText(iPhone)
        if (iName != "") etName.setText(iName)

        if (iMessage != "") {

            etMessage.setText(iMessage)
            updateTxtMaxLength(iMessage?.length ?: 0)

        }

    }

    private fun initVariable() {

        icBack = findViewById(R.id.ic_back)
        btnSubmit = findViewById(R.id.btn_submit)
        tvMaxMessage = findViewById(R.id.tv_max_message)
        etPhone = findViewById(R.id.et_phone)
        etName = findViewById(R.id.et_name)
        etMessage = findViewById(R.id.et_message)

    }

    private fun updateTxtMaxLength(length: Int) {
        tvMaxMessage.text = "$length/$msgMaxLength"
    }

    private fun EditText.setMaxLength(maxLength: Int) {

        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))

    }
}