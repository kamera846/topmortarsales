package com.topmortar.topmortarsales.modal

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.utils.CustomEtHandler.setMaxLength
import com.topmortar.topmortarsales.commons.utils.CustomEtHandler.updateTxtMaxLength
import com.topmortar.topmortarsales.commons.utils.PhoneHandler
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.model.ContactModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SendMessageModal(private val context: Context, private val lifecycleScope: CoroutineScope) : Dialog(context) {

    private lateinit var titleBar: LinearLayout
    private lateinit var icBack: ImageView
    private lateinit var icClose: ImageView
    private lateinit var tvTitleBar: TextView
    private lateinit var tvMaxMessage: TextView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button

    private val msgMaxLines = 5
    private val msgMaxLength = 200

    private var item: ContactModel? = null
    fun setItem(data: ContactModel) {
        this.item = data
    }

    private var modalInterface : SendMessageModalInterface? = null
    fun initializeInterface(data : SendMessageModalInterface) {
        this.modalInterface = data
    }
    interface SendMessageModalInterface {
        fun onSubmit(status: Boolean)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.modal_send_message)

        setLayout()
        initVariable()
        initClickHandler()
        etMessageListener()
    }

    private fun setLayout() {
        val displayMetrics = DisplayMetrics()
        window?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

        val layoutParams = window?.attributes
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams?.height = WindowManager.LayoutParams.WRAP_CONTENT

        layoutParams?.gravity = Gravity.BOTTOM // Set position to bottom

        window?.attributes = layoutParams as WindowManager.LayoutParams
    }

    private fun initVariable() {
        titleBar = findViewById(R.id.title_bar)

        icBack = titleBar.findViewById(R.id.ic_back)
        icClose = titleBar.findViewById(R.id.ic_close)
        tvTitleBar = titleBar.findViewById(R.id.tv_title_bar)

        tvMaxMessage = findViewById(R.id.tv_max_message)
        etMessage = findViewById(R.id.et_message)
        btnSend = findViewById(R.id.btn_send)

        // Set Title Bar
        icBack.visibility = View.GONE
        icClose.visibility = View.VISIBLE
        tvTitleBar.text = "Send Message to Customer"
    }

    private fun initClickHandler() {
        icClose.setOnClickListener { this.dismiss() }
        btnSend.setOnClickListener { submitHandler() }
    }

    private fun loadingState(state: Boolean) {

        btnSend.setTextColor(ContextCompat.getColor(context, R.color.white))
        btnSend.setBackgroundColor(ContextCompat.getColor(context, R.color.primary_200))

        if (state) {

            btnSend.isEnabled = false
            btnSend.text = "LOADING..."

        } else {

            btnSend.isEnabled = true
            btnSend.text = "SUBMIT"
            btnSend.setBackgroundColor(ContextCompat.getColor(context, R.color.primary))

        }

    }

    private fun etMessageListener() {

        etMessage.maxLines = msgMaxLines
        etMessage.setMaxLength(msgMaxLength)

        etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateTxtMaxLength(tvMaxMessage, msgMaxLength, etMessage.text.length)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

    }

    private fun submitHandler() {

        if (!formValidation( "${ etMessage.text }")) return

        loadingState(true)

        lifecycleScope.launch {
            try {
                val data = item!!

                val rbPhone = createPartFromString(PhoneHandler.formatPhoneNumber(data.nomorhp))
                val rbName = createPartFromString(data.nama)
                val rbLocation = createPartFromString(data.id_city)
                val rbBirthday = createPartFromString(data.tgl_lahir)
                val rbOwner = createPartFromString(data.store_owner)
                val rbMapsUrl = createPartFromString(data.maps_url)
                val rbMessage = createPartFromString("${ etMessage.text }")

//                handleMessage(context, "SEND MESSAGE PARAM", "${ data.nomorhp } : ${ data.nama } : ${ data.id_city } : ${ data.tgl_lahir } : ${ data.store_owner } : ${ data.maps_url } : ${ etMessage.text }")

//                etMessage.setText("")
//                loadingState(false)
//                if (modalInterface != null) modalInterface!!.onSubmit(true)
//                this@SendMessageModal.dismiss()
//                return@launch

                val apiService: ApiService = HttpClient.create()
                val response = apiService.sendMessage(name = rbName, phone = rbPhone, ownerName = rbOwner, birthday = rbBirthday, cityId = rbLocation, mapsUrl = rbMapsUrl, message = rbMessage)

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    if (responseBody.status == RESPONSE_STATUS_OK) {

                        etMessage.setText("")
                        loadingState(false)
                        handleMessage(context, TAG_RESPONSE_CONTACT, "Successfully send message!")

                        if (modalInterface != null) modalInterface!!.onSubmit(true)
                        this@SendMessageModal.dismiss()

                    } else {

                        handleMessage(context, TAG_RESPONSE_CONTACT, "Failed to send!")
                        loadingState(false)

                    }

                } else {

                    handleMessage(context, TAG_RESPONSE_CONTACT, "Failed to send! Message: " + response.message())
                    loadingState(false)

                }


            } catch (e: Exception) {

                handleMessage(context, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(false)

            }

        }

    }

    private fun formValidation(message: String): Boolean {
        return if (message.isEmpty()) {
            etMessage.error = "Message cannot be empty!"
            etMessage.requestFocus()
            false
        } else {
            etMessage.error = null
            etMessage.clearFocus()
            true
        }
    }
}