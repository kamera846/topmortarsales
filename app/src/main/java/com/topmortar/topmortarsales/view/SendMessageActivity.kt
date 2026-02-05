package com.topmortar.topmortarsales.view

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_ERROR
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.utils.CustomEtHandler.setMaxLength
import com.topmortar.topmortarsales.commons.utils.CustomEtHandler.updateTxtMaxLength
import com.topmortar.topmortarsales.commons.utils.EventBusUtils
import com.topmortar.topmortarsales.commons.utils.PhoneHandler.formatPhoneNumber
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.UriHandler.uriToMultiPart
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivitySendMessageBinding
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.ContactModel
import com.topmortar.topmortarsales.model.KontenModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class SendMessageActivity() : AppCompatActivity() {

    companion object {
        private const val GENERAL_MESSAGE = "GeneralMessage"
        private const val MEDIA_MESSAGE = "MediaMessage"
        private const val LINK_MESSAGE = "LinkMessage"
    }

    private lateinit var sessionManager: SessionManager
    private lateinit var binding: ActivitySendMessageBinding

    private var contact: ContactModel? = null

    private var selectedMsgType = GENERAL_MESSAGE

    private val msgMaxLines = 5
    private val msgMaxLength = 200

    private var limitFileMB = 16 // Size in MB
    private var exoPlayer: ExoPlayer? = null
    private var imagePart: MultipartBody.Part? = null

    private val imgMessagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            setUri(result.data?.data)
        }
    }

    private lateinit var searchModalKonten: SearchModal
    private var listKonten: ArrayList<KontenModel>? = null
    private var idKonten: String? = null

    private var listPhone: List<String> = mutableListOf("== Pilih Nomor Telpon")
    private var selectedPhone = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge(isPrimary = false)

        binding = ActivitySendMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        /*
        Set Dummy Item
        */
//        contact = ContactModel(
//            id_contact = "2726",
//            nama = "Trial Toko Bangunan Rafli",
//            nomorhp = "62895636998639",
//            tgl_lahir = "2024-12-18",
//            store_owner = "M Rafli Ramadani",
//            id_city = "1",
//            maps_url = "-7.95244741393831,112.69275560975075,-7.9477740,112.6103210",
//            address = "Jl Melati Blok 001 No 001 Kota Istimewa",
//            store_status = "active",
//            ktp_owner = "min-2025-09-08-18-15-16image.jpg",
//            termin_payment = "30",
//            id_promo = "1",
//            reputation = "good",
//            created_at = "2024-07-25 18:18:15",
//            payment_method = "transfer",
//            tagih_mingguan = "1",
//            nomorhp_2 = "0",
//            nomor_cat_1 = "Owner",
//            nomor_cat_2 = "Konsultan",
//            pass_contact = "ad52f32fb5c55f5ff52cc89cb8ab92f4",
//            hari_bayar = "sabtu",
//        )
//        binding.titleBar.tvTitleBar.text = contact?.nama

        // Initialize Message Type
        toggleMessageType(GENERAL_MESSAGE)

        // Initialize Radio Button
        binding.radioButtonGeneral.setOnClickListener { toggleMessageType(GENERAL_MESSAGE) }
        binding.radioButtonMedia.setOnClickListener { toggleMessageType(MEDIA_MESSAGE) }
        binding.radioButtonKonten.setOnClickListener { toggleMessageType(LINK_MESSAGE) }

        // Initialize Click Handler
        binding.titleBar.icBack.setOnClickListener { finish() }
        binding.btnSend.setOnClickListener { submitHandler() }
        binding.iconSelectableKonten.setOnClickListener { showModalSelectKonten() }
        binding.selectableKonten.setOnClickListener { showModalSelectKonten() }
        binding.tvSelectableKonten.setOnClickListener { showModalSelectKonten() }
        binding.btnPickImg.setOnClickListener {
            chooseFileSendMessage()
        }
        binding.btnClearImg.setOnClickListener {
            clearImg()
        }

        // Initialize TextView
        binding.tvMaxFileSize.text = "Maksimal ukuran file $limitFileMB MB"

        etMessageListener()
        EventBus.getDefault().register(this)
    }

    @Subscribe(sticky = true, threadMode = org.greenrobot.eventbus.ThreadMode.MAIN)
    fun onEvent(event: EventBusUtils.ContactModelEvent) {
        contact = event.data

        binding.titleBar.tvTitleBar.text = contact?.nama
        setupPhoneSpinner()

        EventBus.getDefault().removeStickyEvent(event)
    }

    override fun onStop() {
        super.onStop()
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun setupPhoneSpinner() {
        val phone1 = contact?.nomorhp
        val phone2 = contact?.nomorhp_2
        val phones = mutableListOf<String>()

        if (!phone1.isNullOrEmpty() && phone1 != "0") {
            phones.add(phone1)
            selectedPhone = phone1
        }

        if (!phone2.isNullOrEmpty() && phone2 != "0") {
            phones.add(phone2)
            if (phone1.isNullOrEmpty() || phone1 == "0") {
                selectedPhone = phone2
            }
        }

        if (phones.isNotEmpty()) {
            listPhone = phones
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listPhone)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinPhone.adapter = adapter
        binding.spinPhone.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedPhone = listPhone[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do something
            }
        }
    }

    private fun toggleMessageType(messageType: String) {
        selectedMsgType = messageType
        binding.etMessage.setText("")
        binding.etMessage.error = null
        binding.etMessage.clearFocus()
        clearImg()
        setSelectedKonten()
        when (messageType) {
            MEDIA_MESSAGE -> {
                binding.radioButtonGeneral.isChecked = false
                binding.radioButtonMedia.isChecked = true
                binding.radioButtonKonten.isChecked = false
                binding.inputContainer.visibility = View.VISIBLE
                binding.inputMediaContainer.visibility = View.VISIBLE
                binding.inputKontenContainer.visibility = View.GONE
            } LINK_MESSAGE -> {
                // Get List Konten
                getListKonten()
                binding.radioButtonGeneral.isChecked = false
                binding.radioButtonMedia.isChecked = false
                binding.radioButtonKonten.isChecked = true
                binding.inputContainer.visibility = View.GONE
                binding.inputMediaContainer.visibility = View.GONE
                binding.inputKontenContainer.visibility = View.VISIBLE
            } else -> {
                binding.radioButtonGeneral.isChecked = true
                binding.radioButtonMedia.isChecked = false
                binding.radioButtonKonten.isChecked = false
                binding.inputContainer.visibility = View.VISIBLE
                binding.inputMediaContainer.visibility = View.GONE
                binding.inputKontenContainer.visibility = View.GONE
            }
        }
    }

    private fun setSelectedKonten(konten: KontenModel? = null) {
        if (konten != null) {
            binding.selectedKontenLayout.visibility = View.VISIBLE
            binding.tvNameSelectedKonten.text = konten.name_kontenmsg
            binding.tvBodySelectedKonten.text = konten.body_kontenmsg
            binding.tvLinkSelectedKonten.text = konten.link_kontenmsg
            binding.tvLinkSelectedKonten.paintFlags = binding.tvLinkSelectedKonten.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            binding.tvLinkSelectedKonten.setOnClickListener {
                navigateToWebview(konten.link_kontenmsg)
            }
            binding.btnPreviewSelectedKonten.isEnabled = true
            binding.btnPreviewSelectedKonten.setOnClickListener {
                navigateToWebview(konten.link_kontenmsg)
            }
            Glide.with(this)
                .load(konten.link_thumbnail.toUri())
                .transform(FitCenter(), RoundedCorners(convertDpToPx(8, this)))
                .into(binding.imgSelectedKonten)
            idKonten = konten.id_kontenmsg
        } else {
            binding.selectedKontenLayout.visibility = View.GONE
            binding.tvNameSelectedKonten.text = ""
            binding.tvBodySelectedKonten.text = ""
            binding.tvLinkSelectedKonten.text = ""
            binding.btnPreviewSelectedKonten.isEnabled = false
            Glide.with(this)
                .load(R.drawable.background_rounded_8)
                .into(binding.imgSelectedKonten)
            idKonten = null
        }
    }

    private fun navigateToWebview(url: String) {
        val intent = Intent(this, WebviewActivity::class.java)
        intent.putExtra("URL", url)
        startActivity(intent)
    }

    private fun loadingState(state: Boolean) {

        binding.btnSend.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.btnSend.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_200))

        if (state) {

            binding.btnSend.isEnabled = false
            binding.btnSend.text = this.getString(R.string.txt_loading)

        } else {

            binding.btnSend.isEnabled = true
            binding.btnSend.text = this.getString(R.string.submit)
            binding.btnSend.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))

        }

    }

    private fun etMessageListener() {

        binding.etMessage.maxLines = msgMaxLines
        binding.etMessage.setMaxLength(msgMaxLength)

        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateTxtMaxLength(binding.tvMaxMessage, msgMaxLength, binding.etMessage.text.length)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

    }

    private fun chooseFileSendMessage() {
        val options = arrayOf("Pilih Gambar", "Pilih Video")
        AlertDialog.Builder(this)
            .setTitle("Pilih dari Galeri")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "image/*"
                            addCategory(Intent.CATEGORY_OPENABLE)
                        }
                        imgMessagePickerLauncher.launch(Intent.createChooser(intent, "Pilih Gambar"))
                    }
                    1 -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "video/*"
                            addCategory(Intent.CATEGORY_OPENABLE)
                        }
                        imgMessagePickerLauncher.launch(Intent.createChooser(intent, "Pilih Video"))
                    }
                }
            }
            .show()
    }

    private fun setUri(uri: Uri?) {
        if (uri != null) {

            if (!isFileSizeAllowed(uri)) {
                clearImg()
                handleMessage(this, "TAG SEND MESSAGE ACTIVITY", "Ukuran file melebihi batas $limitFileMB MB")
                return
            }

            val mimeType = contentResolver.getType(uri)
            val partName = "img_message"

            if (mimeType?.startsWith("image") == true) {
                imagePart = uriToMultiPart(this, uri, partName)

                binding.btnClearImg.visibility = View.VISIBLE
                binding.imgMessage.visibility = View.VISIBLE
                binding.videoMessage.visibility = View.GONE

                Glide.with(this)
                    .load(uri)
                    .into(binding.imgMessage)

            } else if (mimeType?.startsWith("video") == true) {
                imagePart = uriToMultiPart(this, uri, partName)

                binding.btnClearImg.visibility = View.VISIBLE
                binding.imgMessage.visibility = View.GONE
                binding.videoMessage.visibility = View.VISIBLE

                // Initialize Exo Player
                exoPlayer = ExoPlayer.Builder(this).build()
                exoPlayer?.addListener(object : Player.Listener {
                    override fun onVideoSizeChanged(videoSize: VideoSize) {
                        val width = videoSize.width
                        val height = videoSize.height

                        val aspectRatio = height.toFloat() / width.toFloat()
                        val newHeight = (binding.videoMessage.width * aspectRatio).toInt()

                        binding.videoMessage.updateLayoutParams {
                            this.height = newHeight
                        }
                    }
                })

                binding.videoMessage.player = exoPlayer

                val mediaItem = MediaItem.fromUri(uri)
                exoPlayer?.setMediaItem(mediaItem)
                exoPlayer?.prepare()

            } else {
                clearImg()
                handleMessage(this@SendMessageActivity, TAG_RESPONSE_CONTACT, "Gagal memproses media")
            }

        } else {
            clearImg()
        }
    }

    private fun isFileSizeAllowed(uri: Uri): Boolean {
        val fileSizeInBytes = contentResolver.openFileDescriptor(uri, "r")?.statSize ?: return false
        val fileSizeInMB = fileSizeInBytes / (1024 * 1024)
        return fileSizeInMB <= limitFileMB
    }

    private fun clearImg() {
        binding.btnClearImg.visibility = View.GONE
        binding.imgMessage.visibility = View.GONE
        binding.videoMessage.visibility = View.GONE
        exoPlayer?.release()
        exoPlayer = null
        imagePart = null
    }

    private fun submitHandler() {
        if (!formValidation()) return

        AlertDialog.Builder(this)
            .setTitle("Konfirmasi")
            .setMessage("Pastikan pesan yang ingin anda kirim sudah sesuai.\n\nApakah anda yakin akan mengirim pesan sekarang?")
            .setPositiveButton("Ya") { dialog, _ ->

                dialog.dismiss()
                loadingState(true)

                when (selectedMsgType) {
                    LINK_MESSAGE -> {
                        submitMessageKonten()
                    } else -> {
                        submitMessageGeneralAndMedia()
                    }
                }
            }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun submitMessageGeneralAndMedia() {

        lifecycleScope.launch {
            try {
                val data = contact!!
                val userId = sessionManager.userID().let { if (!it.isNullOrEmpty()) it else "" }
                val currentName = sessionManager.fullName().let { fullName -> if (!fullName.isNullOrEmpty()) fullName else sessionManager.userName().let { username -> if (!username.isNullOrEmpty()) username else "" } }

                var phoneCat = data.nomor_cat_1

                if (selectedPhone == data.nomorhp_2) {
                    phoneCat = data.nomor_cat_2
                }

                val rbPhone = createPartFromString(formatPhoneNumber(selectedPhone))
                val rbPhoneCategory = createPartFromString(formatPhoneNumber(phoneCat))
                val rbName = createPartFromString(data.nama)
                val rbLocation = createPartFromString(data.id_city)
                val rbBirthday = createPartFromString(data.tgl_lahir)
                val rbOwner = createPartFromString(data.store_owner)
                val rbMapsUrl = createPartFromString(data.maps_url)
                val rbMessage = createPartFromString("${ binding.etMessage.text }")
                val rbUserId = createPartFromString(userId)
                val rbContactId = createPartFromString(data.id_contact)
                val rbCurrentName = createPartFromString(currentName)
                val rbTermin = createPartFromString(data.termin_payment)

                val apiService: ApiService = HttpClient.create()
                val response = when (selectedMsgType) {
                    MEDIA_MESSAGE -> {
                        apiService.sendImgMessage(
                            contactId = rbContactId,
                            userId = rbUserId,
                            phone = rbPhone,
                            message = rbMessage,
                            imageMessage = imagePart,
                        )
                    } else -> {
                        apiService.sendMessage(
                            name = rbName,
                            phoneCategory = rbPhoneCategory,
                            phone = rbPhone,
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

                    val responseBody = response.body()

                    if (responseBody != null) {
                        when (responseBody.status) {
                            RESPONSE_STATUS_OK -> {

                                loadingState(false)

                                val responseQontak = responseBody.qontak

                                if (responseQontak == null) {
                                    handleMessage(this@SendMessageActivity, TAG_RESPONSE_MESSAGE, responseBody.message)
                                    setResult(RESULT_OK)
                                    finish()
                                } else {
                                    if (responseQontak.status == "success") {
                                        handleMessage(this@SendMessageActivity, TAG_RESPONSE_MESSAGE, responseBody.message)
                                        setResult(RESULT_OK)
                                        finish()
                                    } else {
                                        val qontakError = responseQontak.error
                                        if (qontakError == null) {
                                            handleMessage(
                                                this@SendMessageActivity,
                                                TAG_RESPONSE_MESSAGE,
                                                "Status Qontak: ${responseQontak.status}"
                                            )
                                        } else {
                                            handleMessage(
                                                this@SendMessageActivity,
                                                TAG_RESPONSE_MESSAGE,
                                                "Code: ${qontakError.code}, Message: ${qontakError.messages}"
                                            )
                                        }
                                    }
                                }

                            }
                            RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                                handleMessage(this@SendMessageActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim: ${ responseBody.message }")
                                loadingState(false)

                            }
                            RESPONSE_STATUS_ERROR -> {

                                val errorMessages = responseBody.error?.messages.let { if (!it.isNullOrEmpty()) it[0] else "" }
                                handleMessage(this@SendMessageActivity, TAG_RESPONSE_MESSAGE, "Error Code ${ responseBody.error?.code } $errorMessages")
                                loadingState(false)

                            }
                            else -> {

                                handleMessage(this@SendMessageActivity, TAG_RESPONSE_CONTACT, "Gagal mengirim!")
                                loadingState(false)

                            }
                        }
                    }

                } else {

                    handleMessage(this@SendMessageActivity, TAG_RESPONSE_CONTACT, "Gagal mengirim. Error: Code ${response?.code()}, Message: ${response?.message()}")
                    loadingState(false)

                }

            } catch (e: Exception) {

                handleMessage(this@SendMessageActivity, TAG_RESPONSE_CONTACT, "Failed run service send message. Exception " + e.message)
                loadingState(false)

            }

        }

    }

    private fun submitMessageKonten() {

        lifecycleScope.launch {
            try {
                val data = contact!!

                val rbContactId = createPartFromString(data.id_contact)
                val rbKontenId = createPartFromString(idKonten ?: "-1")

                val apiService: ApiService = HttpClient.create()
                val response = apiService.sendMessageKonten(
                        idKonten = rbKontenId,
                        idContact = rbContactId
                    )

                if (response.isSuccessful) {

                    val responseBody = response.body()

                    if (responseBody != null) {
                        when (responseBody.status) {
                            RESPONSE_STATUS_OK -> {

                                loadingState(false)

                                val responseQontak = responseBody.qontak

                                if (responseQontak == null) {
                                    handleMessage(this@SendMessageActivity, TAG_RESPONSE_MESSAGE, responseBody.message)
                                    setResult(RESULT_OK)
                                    finish()
                                } else {
                                    if (responseQontak.status == "success") {
                                        handleMessage(this@SendMessageActivity, TAG_RESPONSE_MESSAGE, responseBody.message)
                                        setResult(RESULT_OK)
                                        finish()
                                    } else {
                                        val qontakError = responseQontak.error
                                        if (qontakError == null) {
                                            handleMessage(
                                                this@SendMessageActivity,
                                                TAG_RESPONSE_MESSAGE,
                                                "Status Qontak: ${responseQontak.status}"
                                            )
                                        } else {
                                            handleMessage(
                                                this@SendMessageActivity,
                                                TAG_RESPONSE_MESSAGE,
                                                "Code: ${ qontakError.code }, Message: ${qontakError.messages}"
                                            )
                                        }
                                    }
                                }

                            }
                            RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                                handleMessage(this@SendMessageActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim: ${ responseBody.message }")
                                loadingState(false)

                            }
                            RESPONSE_STATUS_ERROR -> {

                                val errorMessages = responseBody.error?.messages.let { if (!it.isNullOrEmpty()) it[0] else "" }
                                handleMessage(this@SendMessageActivity, TAG_RESPONSE_MESSAGE, "Error Code ${ responseBody.error?.code } $errorMessages")
                                loadingState(false)

                            }
                            else -> {

                                handleMessage(this@SendMessageActivity, TAG_RESPONSE_CONTACT, "Gagal mengirim!")
                                loadingState(false)

                            }
                        }
                    }

                } else {

                    handleMessage(this@SendMessageActivity, TAG_RESPONSE_CONTACT, "Gagal mengirim. Error: Code ${response?.code()}, Message: ${response?.message()}")
                    loadingState(false)

                }

            } catch (e: Exception) {

                handleMessage(this@SendMessageActivity, TAG_RESPONSE_CONTACT, "Failed run service send message. Exception " + e.message)
                loadingState(false)

            }

        }

    }

    private fun formValidation(): Boolean {
        if (selectedPhone.isEmpty()) {
            handleMessage(this, message = "Anda belum memilih nomor")
            return false
        }

        return when (selectedMsgType) {
            GENERAL_MESSAGE -> {
                val message = "${ binding.etMessage.text }"
                if (message.isEmpty()) {
                    handleMessage(this, message = "Text pesan tidak boleh kosong")
                    false
                } else true
            } MEDIA_MESSAGE -> {
                val message = "${ binding.etMessage.text }"
                if (message.isEmpty()) {
                    handleMessage(this, message = "Text pesan tidak boleh kosong")
                    false
                } else if (imagePart == null) {
                    handleMessage(this, message = "Tidak ada media yang dipilih")
                    false
                } else true
            } LINK_MESSAGE -> {
                if (idKonten == null) {
                    handleMessage(this, message = "Tidak ada konten yang dipilih")
                    false
                } else true
            } else -> {
                handleMessage(this, message = "Anda belum memilih opsi pesan")
                false
            }
        }
    }

    private fun getListKonten() {
        val apiService: ApiService = HttpClient.create()

        try {
            lifecycleScope.launch {
                val response = apiService.getKonten()
                when (response.status) {
                    RESPONSE_STATUS_OK -> {
                        listKonten = response.results
                        val listItem = listKonten
                        val items: ArrayList<ModalSearchModel> = ArrayList()

                        if (listItem != null) {
                            for (i in 0 until listItem.size) {
                                val data = listItem[i]
                                items.add(ModalSearchModel(data.id_kontenmsg, data.name_kontenmsg, data.body_kontenmsg))
                            }
                            setupModalSearchKonten(items)
                        }
                    } RESPONSE_STATUS_EMPTY -> {
                        listKonten = arrayListOf()
                        val items: ArrayList<ModalSearchModel> = ArrayList()
                        setupModalSearchKonten(items)
                        handleMessage(this@SendMessageActivity, message = "List konten kosong.")
                    } RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED, RESPONSE_STATUS_ERROR -> {
                        handleMessage(this@SendMessageActivity, message = "Gagal memuat konten. Error: ${response.message}")
                    } else -> {
                        handleMessage(this@SendMessageActivity, message = "Gagal memuat konten. Status: ${response.status}")
                    }
                }
            }
        } catch (e: Exception) {
            handleMessage(this, message = "Failed run service konten. Exception " + e.message)
        }
    }

    private fun setupModalSearchKonten(items: ArrayList<ModalSearchModel>) {

        searchModalKonten = SearchModal(this, items)
        searchModalKonten.setCustomDialogListener(object : SearchModal.SearchModalListener {
            override fun onDataReceived(data: ModalSearchModel) {
                val findItem = listKonten?.find { it.id_kontenmsg == data.id }
                setSelectedKonten(findItem)
            }

        })
        searchModalKonten.searchHint = "Masukkan judul konten…"
    }

    private fun showModalSelectKonten() {
        if (listKonten != null)
            searchModalKonten.show()
    }
}