package com.topmortar.topmortarsales.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.topmortar.topmortarsales.R
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
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivitySendMessageBinding
import com.topmortar.topmortarsales.model.ContactModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.io.FileOutputStream

class SendMessageActivity() : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var binding: ActivitySendMessageBinding

    private var item: ContactModel? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge(isPrimary = false)

        binding = ActivitySendMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Initialize Click Handler
        binding.titleBar.icBack.setOnClickListener { finish() }
        binding.btnSend.setOnClickListener { submitHandler() }
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
        item = event.data

        binding.titleBar.tvTitleBar.text = item?.nama

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

            if (mimeType?.startsWith("image") == true) {
                imagePart = uriToMultiPart(uri)

                binding.btnClearImg.visibility = View.VISIBLE
                binding.imgMessage.visibility = View.VISIBLE
                binding.videoMessage.visibility = View.GONE

                Glide.with(this)
                    .load(uri)
                    .into(binding.imgMessage)

            } else if (mimeType?.startsWith("video") == true) {
                imagePart = uriToMultiPart(uri)

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

    private fun uriToMultiPart(uri: Uri?): MultipartBody.Part? {
        if (uri == null) return null

        val inputStream = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "upload_temp")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        val requestFile = file.asRequestBody(contentResolver.getType(uri)?.toMediaTypeOrNull())
        val (name, ext) = getFileNameAndExtension(uri)
        return MultipartBody.Part.createFormData("img_message", "$name.$ext", requestFile)
    }

    private fun getFileNameAndExtension(uri: Uri): Pair<String, String> {
        var name = "image"
        var extension = "jpg" // default

        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex != -1) {
                val fullName = it.getString(nameIndex)
                val dotIndex = fullName.lastIndexOf('.')
                if (dotIndex != -1) {
                    name = fullName.substring(0, dotIndex)
                    extension = fullName.substring(dotIndex + 1)
                } else {
                    name = fullName
                }
            }
        }
        val sanitizedName = sanitizeFileName(name)
        return Pair(sanitizedName, extension)
    }

    private fun sanitizeFileName(filename: String): String {
        // Karakter yang tidak diizinkan dalam nama file
        val regex = Regex("[!&\$@=;/:+,?%\\[\\]<>\\\\~^*#|()\\s]")
        return filename.replace(regex, "_")
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

        if (!formValidation( "${ binding.etMessage.text }")) return

        loadingState(true)

        lifecycleScope.launch {
            try {
                val data = item!!
                val userId = sessionManager.userID().let { if (!it.isNullOrEmpty()) it else "" }
                val currentName = sessionManager.fullName().let { fullName -> if (!fullName.isNullOrEmpty()) fullName else sessionManager.userName().let { username -> if (!username.isNullOrEmpty()) username else "" } }

                val rbPhone = createPartFromString(formatPhoneNumber(data.nomorhp))
                val rbPhoneCategory = createPartFromString(formatPhoneNumber(data.nomor_cat_1))
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
                            RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                                handleMessage(this@SendMessageActivity, TAG_RESPONSE_MESSAGE, "Gagal mengirim: ${ responseBody.message }")
                                loadingState(false)

                            }
                            RESPONSE_STATUS_ERROR-> {

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

                handleMessage(this@SendMessageActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(false)

            }

        }

    }

    private fun formValidation(message: String): Boolean {
        return if (message.isEmpty()) {
            binding.etMessage.error = "Pesan tidak boleh kosong!"
            binding.etMessage.requestFocus()
            false
        } else {
            binding.etMessage.error = null
            binding.etMessage.clearFocus()
            true
        }
    }
}