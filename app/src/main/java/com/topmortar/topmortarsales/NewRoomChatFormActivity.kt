package com.topmortar.topmortarsales

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.topmortar.topmortarsales.commons.CONST_MESSAGE
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.formatPhoneNumber
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
class NewRoomChatFormActivity : AppCompatActivity() {

    private lateinit var icBack: ImageView
    private lateinit var icSyncNow: ImageView
    private lateinit var tvTitleBar: TextView
    private lateinit var tvMaxMessage: TextView
    private lateinit var btnSubmit: Button
    private lateinit var etPhone: EditText
    private lateinit var etName: EditText
    private lateinit var etMessage: EditText

    private val msgMaxLines = 5
    private val msgMaxLength = 200

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        setContentView(R.layout.activity_new_room_chat_form)

        initVariable()
        initClickHandler()
        dataActivityValidation()
        etMessageListener()

    }

    private fun sendMessage() {

        val phone = "${ etPhone.text }"
        val name = "${etName.text}"
        val message = "${etMessage.text}"

        if (!formValidation(phone, name, message)) return

        loadingState(true)

        lifecycleScope.launch {
            try {

                val rbPhone = createPartFromString(formatPhoneNumber(phone))
                val rbName = createPartFromString(name)
                val rbMessage = createPartFromString(message)

                val apiService: ApiService = HttpClient.create()
                val response = apiService.sendMessage(rbName, rbPhone, rbMessage)

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    if (responseBody.status == RESPONSE_STATUS_OK) {

                        handleMessage(this@NewRoomChatFormActivity, TAG_RESPONSE_MESSAGE, "Successfully added transaction data!")
                        loadingState(false)

                        val resultIntent = Intent()
                        resultIntent.putExtra("$MAIN_ACTIVITY_REQUEST_CODE", SYNC_NOW)
                        setResult(RESULT_OK, resultIntent)

                        finish()

                    } else {

                        handleMessage(this@NewRoomChatFormActivity, TAG_RESPONSE_MESSAGE, "Failed to send message!")
                        loadingState(false)

                    }

                } else {

                    handleMessage(this@NewRoomChatFormActivity, TAG_RESPONSE_MESSAGE, "Failed to send message! Error: " + response.message())
                    loadingState(false)

                }


            } catch (e: Exception) {

                handleMessage(this@NewRoomChatFormActivity, TAG_RESPONSE_MESSAGE, "Failed run service. Exception " + e.message)
                loadingState(false)

            }

        }

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

        val iPhone = intent.getStringExtra(CONST_PHONE)
        val iName = intent.getStringExtra(CONST_NAME)
        val iMessage = intent.getStringExtra(CONST_MESSAGE)

        if (iPhone != "") etPhone.setText(iPhone)
        if (iName != "") etName.setText(iName)

        if (iMessage != "") {

            etMessage.setText(iMessage)
            updateTxtMaxLength(iMessage?.length ?: 0)

        }

    }

    private fun initVariable() {

        icBack = findViewById(R.id.ic_back)
        icSyncNow = findViewById(R.id.ic_sync_now)
        tvTitleBar = findViewById(R.id.tv_title_bar)
        tvMaxMessage = findViewById(R.id.tv_max_message)
        btnSubmit = findViewById(R.id.btn_submit)
        etPhone = findViewById(R.id.et_phone)
        etName = findViewById(R.id.et_name)
        etMessage = findViewById(R.id.et_message)

        // Set Title Bar
        icBack.visibility = View.VISIBLE
        icSyncNow.visibility = View.GONE
        tvTitleBar.text = getString(R.string.new_chat_room)

    }

    private fun initClickHandler() {

        icBack.setOnClickListener { finish() }
        btnSubmit.setOnClickListener { sendMessage() }

    }

    private fun loadingState(state: Boolean) {

        btnSubmit.setTextColor(ContextCompat.getColor(this, R.color.white))

        if (state) {

            btnSubmit.isEnabled = false
            btnSubmit.text = getString(R.string.txt_sending)

        } else {

            Handler().postDelayed({

                btnSubmit.isEnabled = true
                btnSubmit.text = getString(R.string.btn_submit_new_chat_room)

            }, 500)

        }

    }

    private fun updateTxtMaxLength(length: Int) {
        tvMaxMessage.text = "$length/$msgMaxLength"
    }

    private fun EditText.setMaxLength(maxLength: Int) {

        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))

    }

    private fun formValidation(phone: String, name: String, message: String): Boolean {
        return if (TextUtils.isEmpty(phone)) {
            etPhone.error = "Phone number cannot be empty!"
            false
        } else if (!phoneValidation(phone)) {
            false
        } else if (TextUtils.isEmpty(name)) {
            etPhone.error = null
            etName.error = "Name cannot be empty!"
            false
        } else if (TextUtils.isEmpty(message)) {
            etPhone.error = null
            etName.error = null
            etMessage.error = "Message cannot be empty!"
            false
        } else {
            etPhone.error = null
            etName.error = null
            etMessage.error = null
            true
        }
    }

    private fun phoneValidation(input: String): Boolean {
        val pattern = Regex("^\\d{10,16}$")
        val trimmedInput = input.trim()

        return if (!trimmedInput.startsWith("0") && !trimmedInput.startsWith("8") && !trimmedInput.startsWith("62")) {
            etPhone.error = "Phone number must consist of starting with: 0, 8, 62"
            false
        } else if (!pattern.matches(input)) {
            etPhone.error = "Phone number must be 10 to 16 digits long"
            false
        } else true
    }
}