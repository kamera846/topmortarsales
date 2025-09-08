package com.topmortar.topmortarsales.modal

import android.app.Dialog
import android.content.Context
import android.net.Uri
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.BID_VISITED
import com.topmortar.topmortarsales.commons.PING_NORMAL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_ERROR
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.commons.utils.CompressImageUtil
import com.topmortar.topmortarsales.commons.utils.CustomEtHandler.setMaxLength
import com.topmortar.topmortarsales.commons.utils.CustomEtHandler.updateTxtMaxLength
import com.topmortar.topmortarsales.commons.utils.PhoneHandler.formatPhoneNumber
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ModalSendMessageBinding
import com.topmortar.topmortarsales.model.ContactModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class SendMessageModal(private val context: Context, private val lifecycleScope: CoroutineScope) : Dialog(context) {

    private lateinit var titleBar: LinearLayout
    private lateinit var icBack: ImageView
    private lateinit var icClose: ImageView
    private lateinit var tvTitleBar: TextView
    private lateinit var tvMaxMessage: TextView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button

    private lateinit var sessionManager: SessionManager
    private val userKind get() = sessionManager.userKind().toString()
    private val userID get() = sessionManager.userID().toString()
    private lateinit var binding: ModalSendMessageBinding

    private var pingStatus: Int? = null
    private val msgMaxLines = 5
    private val msgMaxLength = 200

    // Bidding Notice
    private var isBiddingAvailable = true
    private var isOnBidorVisited = false
    private var currentBidding = 0
    private val limitBidding get() = sessionManager.userBidLimit().toString().toInt()

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
        fun onPickImage()
    }

    fun setPingStatus(pingStatus: Int? = null) {
        this.pingStatus = pingStatus
    }

    private var imagePart: MultipartBody.Part? = null
    fun setUri(uri: Uri?) {
        if (uri != null) {

            val imgUri = CompressImageUtil.compressImageOptimized(context, uri)

            Glide.with(context)
                .load(uri)
                .transform(CenterCrop(), RoundedCorners(convertDpToPx(8, context)))
                .into(binding.imgMessage)

            binding.txtBtnPickImg.text = "Klik untuk mengganti gambar"
            binding.btnClearImg.visibility = View.VISIBLE
            binding.imgMessage.visibility = View.VISIBLE

            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(imgUri)
            val byteArray = inputStream?.readBytes()

            if (byteArray != null) {
                val requestFile: RequestBody =
                    byteArray.toRequestBody("image/*".toMediaTypeOrNull())
                imagePart = MultipartBody.Part.createFormData("img_message", "image.jpg", requestFile)
            } else {
                clearImg()
                handleMessage(context, TAG_RESPONSE_CONTACT, "Gagal memproses gambar")
            }

        } else {
            clearImg()
        }
    }

    private fun clearImg() {
        binding.txtBtnPickImg.text = "Klik untuk mimilih gambar"
        binding.btnClearImg.visibility = View.GONE
        binding.imgMessage.visibility = View.GONE
        imagePart = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(context)

        binding = ModalSendMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setLayout()
        initVariable()
        initClickHandler()
        etMessageListener()

//        if (userKind == USER_KIND_SALES) getCurrentBid()
//        else setupBiddingNotice()
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
        binding.inputImgContainer.visibility = View.VISIBLE
        tvTitleBar.text = "Kirim Pesan ke Toko"
        tvTitleBar.setPadding(convertDpToPx(16, context), 0, 0, 0)
    }

    private fun initClickHandler() {
        icClose.setOnClickListener { this.dismiss() }
        btnSend.setOnClickListener { submitHandler() }
        binding.btnPickImg.setOnClickListener {
            this.dismiss()
            modalInterface?.onPickImage()
        }
        binding.btnClearImg.setOnClickListener {
            clearImg()
        }
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

    private fun setupBiddingNotice() {

        if (userKind == USER_KIND_SALES) {

            binding.contaierCountBidding.visibility = View.VISIBLE
            binding.noticeBidding.visibility = View.VISIBLE
            binding.textTotalBidding.text = "$currentBidding/$limitBidding"

            if (isBiddingAvailable) {

                binding.etMessage.isEnabled = true
                binding.etMessage.setBackgroundResource(R.drawable.et_background)
                binding.btnSend.isEnabled = true
                binding.btnSend.setBackgroundColor(context.getColor(R.color.primary))

                if (isOnBidorVisited) {
                    binding.contaierCountBidding.visibility = View.GONE
                    binding.textNotice.text = context.getString(R.string.text_bid_on_going)
                } else {
                    binding.textTotalBidding.setTextColor(context.getColor(R.color.status_bid))
                    binding.textNotice.text = context.getString(R.string.text_bid_available)
                }
            } else {
                binding.etMessage.isEnabled = false
                binding.etMessage.setBackgroundResource(R.drawable.et_background_disabled)
                binding.btnSend.isEnabled = false
                binding.btnSend.setBackgroundColor(context.getColor(R.color.mixed_300))
                binding.textTotalBidding.setTextColor(context.getColor(R.color.primary))
                binding.textNotice.text = context.getString(R.string.text_bid_not_available)
            }

        }

    }

    private fun getCurrentBid() {
        loadingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getContactsUserBid(userId = userID)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        loadingState(false)

                        currentBidding = response.results.size
                        isBiddingAvailable = currentBidding < limitBidding
                        for (data in response.results.listIterator()) {
                            if (data.id_contact == item!!.id_contact) {
                                isBiddingAvailable = true
                                isOnBidorVisited = true
                            }
                        }
                        getCurrentVisited()

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(false)
                        currentBidding = 0
                        isBiddingAvailable = true
                        getCurrentVisited()

                    }
                    else -> {

                        handleMessage(context, TAG_RESPONSE_CONTACT, context.getString(R.string.failed_get_data))
                        loadingState(true)

                    }
                }


            } catch (e: Exception) {

                handleMessage(context, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true)

            }

        }
    }

    private fun getCurrentVisited() {
        loadingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getContactsUserBid(userId = userID, visit = BID_VISITED)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        loadingState(false)

                        for (data in response.results.listIterator()) {
                            if (data.id_contact == item!!.id_contact) {
                                isBiddingAvailable = true
                                isOnBidorVisited = true
                            }
                        }
                        setupBiddingNotice()

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(false)
                        setupBiddingNotice()

                    }
                    else -> {

                        handleMessage(context, TAG_RESPONSE_CONTACT, context.getString(R.string.failed_get_data))
                        loadingState(true)

                    }
                }


            } catch (e: Exception) {

                handleMessage(context, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true)

            }

        }
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

                val rbPhone = createPartFromString(formatPhoneNumber(data.nomorhp))
                val rbPhoneCategory = createPartFromString(formatPhoneNumber(data.nomor_cat_1))
//                val rbPhone2 = createPartFromString(data.nomorhp_2.let{ if (it == "0") it else formatPhoneNumber(it) })
                val rbName = createPartFromString(data.nama)
                val rbLocation = createPartFromString(data.id_city)
                val rbBirthday = createPartFromString(data.tgl_lahir)
                val rbOwner = createPartFromString(data.store_owner)
                val rbMapsUrl = createPartFromString(data.maps_url)
                val rbMessage = createPartFromString("${ etMessage.text }")
                val rbUserId = createPartFromString(userId)
                val rbContactId = createPartFromString(data.id_contact)
                val rbCurrentName = createPartFromString(currentName)
                val rbTermin = createPartFromString(data.termin_payment)

//                handleMessage(context, "SEND MESSAGE PARAM", "${ data.nomorhp } : ${ data.nama } : ${ data.id_city } : ${ data.tgl_lahir } : ${ data.store_owner } : ${ data.maps_url } : ${ etMessage.text }")

//                etMessage.setText("")
//                loadingState(false)
//                if (modalInterface != null) modalInterface!!.onSubmitMessage(true)
//                this@SendMessageModal.dismiss()
//                return@launch

                val apiService: ApiService = HttpClient.create()
                val response = imagePart.let {
                    if (it != null) {
                        apiService.sendImgMessage(
                            contactId = rbContactId,
                            userId = rbUserId,
                            phone = rbPhone,
                            message = rbMessage,
                            imageMessage = it,
                        )
                    } else {
                        apiService.sendMessage(
                            name = rbName,
                            phoneCategory = rbPhoneCategory,
                            phone = rbPhone,
//                    phone2 = rbPhone2,
                            ownerName = rbOwner,
                            birthday = rbBirthday,
                            cityId = rbLocation,
                            mapsUrl = rbMapsUrl,
                            currentName = rbCurrentName,
                            userId = rbUserId,
                            termin = rbTermin,
                            message = rbMessage
                        )
                    }
                }

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
                        RESPONSE_STATUS_ERROR-> {

                            val errorMessages = responseBody.error?.messages.let { if (!it.isNullOrEmpty()) it[0] else "" }
                            handleMessage(context, TAG_RESPONSE_MESSAGE, "Error Code ${ responseBody.error?.code } $errorMessages")
                            loadingState(false)

                        }
                        else -> {

                            handleMessage(context, TAG_RESPONSE_CONTACT, "Gagal mengirim!")
                            loadingState(false)

                        }
                    }

                } else {

                    handleMessage(context, TAG_RESPONSE_CONTACT, "Gagal mengirim. Error: Code ${response.code()}, Message: ${response.message()}")
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