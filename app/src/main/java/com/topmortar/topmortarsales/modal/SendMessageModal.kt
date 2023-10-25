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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.PING_NORMAL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.utils.CustomEtHandler.setMaxLength
import com.topmortar.topmortarsales.commons.utils.CustomEtHandler.updateTxtMaxLength
import com.topmortar.topmortarsales.commons.utils.PhoneHandler
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
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

    private lateinit var sessionManager: SessionManager

    private var pingStatus: Int? = null
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
        fun onSubmitMessage(status: Boolean)
    }

    fun setPingStatus(pingStatus: Int? = null) {
        this.pingStatus = pingStatus
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(context)

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
        tvTitleBar.text = "Kirim Pesan ke Toko"
        tvTitleBar.setPadding(convertDpToPx(16, context), 0, 0, 0)
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
            btnSend.text = context.getString(R.string.txt_loading)

        } else {

            btnSend.isEnabled = true
            btnSend.text = context.getString(R.string.submit)
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

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Koneksi Tidak Stabil!")
            .setMessage("Tunggu beberapa saat sampai indikasi sinyal berubah menjadi hijau.")
            .setPositiveButton("Oke") { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()

        if (pingStatus != PING_NORMAL) return dialog.show()

        loadingState(true)

        lifecycleScope.launch {
            try {
                val data = item!!
                val userId = sessionManager.userID().let { if (!it.isNullOrEmpty()) it else "" }
                val currentName = sessionManager.fullName().let { fullName -> if (!fullName.isNullOrEmpty()) fullName else sessionManager.userName().let { username -> if (!username.isNullOrEmpty()) username else "" } }

                val rbPhone = createPartFromString(PhoneHandler.formatPhoneNumber(data.nomorhp))
                val rbName = createPartFromString(data.nama)
                val rbLocation = createPartFromString(data.id_city)
                val rbBirthday = createPartFromString(data.tgl_lahir)
                val rbOwner = createPartFromString(data.store_owner)
                val rbMapsUrl = createPartFromString(data.maps_url)
                val rbMessage = createPartFromString("${ etMessage.text }")
                val rbUserId = createPartFromString(userId)
                val rbCurrentName = createPartFromString(currentName)
                val rbTermin = createPartFromString(data.termin_payment)

//                handleMessage(context, "SEND MESSAGE PARAM", "${ data.nomorhp } : ${ data.nama } : ${ data.id_city } : ${ data.tgl_lahir } : ${ data.store_owner } : ${ data.maps_url } : ${ etMessage.text }")

//                etMessage.setText("")
//                loadingState(false)
//                if (modalInterface != null) modalInterface!!.onSubmitMessage(true)
//                this@SendMessageModal.dismiss()
//                return@launch

                val apiService: ApiService = HttpClient.create()
                val response = apiService.sendMessage(name = rbName, phone = rbPhone, ownerName = rbOwner, birthday = rbBirthday, cityId = rbLocation, mapsUrl = rbMapsUrl, currentName = rbCurrentName, userId = rbUserId, termin = rbTermin, message = rbMessage)

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            etMessage.setText("")
                            loadingState(false)
                            handleMessage(context, TAG_RESPONSE_CONTACT, "Berhasil mengirim pesan!")

                            if (modalInterface != null) modalInterface!!.onSubmitMessage(true)
                            this@SendMessageModal.dismiss()

                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            handleMessage(context, TAG_RESPONSE_MESSAGE, "Gagal mengirim: ${ responseBody.message }")
                            loadingState(false)

                        }
                        else -> {

                            handleMessage(context, TAG_RESPONSE_CONTACT, "Gagal mengirim!")
                            loadingState(false)

                        }
                    }

                } else {

                    handleMessage(context, TAG_RESPONSE_CONTACT, "Gagal mengirim. Error: " + response.message())
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
            etMessage.error = "Pesan tidak boleh kosong!"
            etMessage.requestFocus()
            false
        } else {
            etMessage.error = null
            etMessage.clearFocus()
            true
        }
    }
}