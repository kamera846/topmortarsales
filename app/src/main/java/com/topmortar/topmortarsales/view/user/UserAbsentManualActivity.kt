package com.topmortar.topmortarsales.view.user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.*
import com.topmortar.topmortarsales.commons.utils.*
import com.topmortar.topmortarsales.commons.utils.ResponseMessage.generateFailedRunServiceMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityUserAbsentManualBinding
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.BaseCampModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import com.topmortar.topmortarsales.model.UserModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserAbsentManualActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserAbsentManualBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var progressBar: CustomProgressBar

    private lateinit var userSearchModal: SearchModal
    private lateinit var basecampSearchModal: SearchModal

    private var selectedUserItem: ModalSearchModel? = null
    private var selectedBasecampItem: ModalSearchModel? = null

    private val userList = arrayListOf<UserModel>()
    private val basecampList = arrayListOf<BaseCampModel>()

    private val distributorId: String
        get() = sessionManager.userDistributor() ?: "-1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge(isPrimary = false)

        binding = ActivityUserAbsentManualBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        progressBar = CustomProgressBar(this)

        initView()
        setupUserField()
        setupBasecampField()

        loadInitialData()
    }

    private fun initView() {
        binding.titleBar.tvTitleBar.text = "Absen Manual"

        binding.titleBar.icBack.setOnClickListener {
            finish()
        }

        binding.btnSubmit.setOnClickListener {
            submitManualAbsent()
        }
    }

    private fun loadInitialData() {
        progressBar.show()
        progressBar.setMessage("Memuat data...")

        lifecycleScope.launch {

            try {

                val userDeferred = async { fetchUsers() }
                val basecampDeferred = async { fetchBasecamps() }

                userDeferred.await()
                basecampDeferred.await()

            } catch (e: Exception) {

                FirebaseUtils.logErr(
                    this@UserAbsentManualActivity,
                    "User Absent Manual Activity. Failed on loadInitialData: ${e.message}"
                )
                handleMessage(this@UserAbsentManualActivity, message = "Failed loadInitialData: ${e.message}")

            } finally {
                progressBar.dismiss()
            }
        }
    }

    private suspend fun fetchUsers() {
        val apiService: ApiService = HttpClient.create()
        val response = apiService.getUsers(distributorID = distributorId)

        when (response.status) {

            RESPONSE_STATUS_OK -> {

                userList.clear()

                userList.addAll(
                    response.results.filter {
                        it.level_user in listOf(
                            "sales",
                            "penagihan",
                            "courier"
                        )
                    }
                )

                val items = ArrayList<ModalSearchModel>()

                userList.forEach {
                    items.add(
                        ModalSearchModel(
                            it.id_user,
                            "${it.full_name} - ${it.kode_city}"
                        )
                    )
                }

                setupUserSearchModal(items)
            }

            RESPONSE_STATUS_EMPTY -> {
                handleMessage(
                    this,
                    "LIST USER",
                    "Daftar pengguna kosong!"
                )
            }

            else -> {
                handleMessage(
                    this,
                    TAG_RESPONSE_CONTACT,
                    getString(R.string.failed_get_data)
                )
            }
        }
    }

    private suspend fun fetchBasecamps() {
        val apiService: ApiService = HttpClient.create()
        val response = apiService.getListBaseCamp(distributorID = distributorId)

        when (response.status) {

            RESPONSE_STATUS_OK -> {

                basecampList.clear()
                basecampList.addAll(response.results)

                val items = ArrayList<ModalSearchModel>()

                basecampList.forEach {
                    items.add(
                        ModalSearchModel(
                            it.id_gudang,
                            "${it.nama_gudang} - ${it.kode_city}"
                        )
                    )
                }

                setupBasecampSearchModal(items)
            }

            RESPONSE_STATUS_EMPTY -> {
                handleMessage(
                    this,
                    "LIST BASECAMP",
                    "Daftar basecamp kosong!"
                )
            }

            else -> {
                handleMessage(
                    this,
                    TAG_RESPONSE_CONTACT,
                    getString(R.string.failed_get_data)
                )
            }
        }
    }

    private fun setupUserField() {

        binding.etUser.setOnClickListener {
            showUserSearch()
        }

        binding.etUser.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showUserSearch()
        }

        setupUserSearchModal()
    }

    private fun setupBasecampField() {

        binding.etBasecamp.setOnClickListener {
            showBasecampSearch()
        }

        binding.etBasecamp.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showBasecampSearch()
        }

        setupBasecampSearchModal()
    }

    private fun setupUserSearchModal(
        items: ArrayList<ModalSearchModel> = arrayListOf()
    ) {

        userSearchModal = SearchModal(this, items)

        userSearchModal.label = "Pilih Pengguna"
        userSearchModal.searchHint = "Cari sales, kurir, atau penagihan"

        userSearchModal.setCustomDialogListener(
            object : SearchModal.SearchModalListener {
                override fun onDataReceived(data: ModalSearchModel) {

                    selectedUserItem = data
                    binding.etUser.setText(data.title)
                }
            }
        )

        userSearchModal.setOnDismissListener {
            binding.etUser.clearFocus()
        }
    }

    private fun setupBasecampSearchModal(
        items: ArrayList<ModalSearchModel> = arrayListOf()
    ) {

        basecampSearchModal = SearchModal(this, items)

        basecampSearchModal.label = "Pilih Basecamp"
        basecampSearchModal.searchHint = "Cari basecamp"

        basecampSearchModal.setCustomDialogListener(
            object : SearchModal.SearchModalListener {
                override fun onDataReceived(data: ModalSearchModel) {

                    selectedBasecampItem = data
                    binding.etBasecamp.setText(data.title)
                }
            }
        )

        basecampSearchModal.setOnDismissListener {
            binding.etBasecamp.clearFocus()
        }
    }

    private fun showUserSearch() {

        val keyword = binding.etUser.text.toString()

        if (keyword.isNotBlank()) {
            userSearchModal.setSearchKey(keyword)
        }

        userSearchModal.show()
    }

    private fun showBasecampSearch() {

        val keyword = binding.etBasecamp.text.toString()

        if (keyword.isNotBlank()) {
            basecampSearchModal.setSearchKey(keyword)
        }

        basecampSearchModal.show()
    }

    private fun submitManualAbsent() {

        if (!validateForm()) return

        progressBar.show()
        progressBar.setMessage(getString(R.string.txt_sending))

        lifecycleScope.launch {

            try {

                val selectedUser =
                    userList.find { it.id_user == selectedUserItem?.id }
                val apiService: ApiService = HttpClient.create()
                val response = apiService.absentManualInBasecamp(
                        idGudang = createPartFromString(
                            selectedBasecampItem?.id ?: "-1"
                        ),
                        idUser = createPartFromString(
                            selectedUser?.id_user ?: "-1"
                        ),
                        levelUser = createPartFromString(
                            selectedUser?.level_user ?: "-"
                        )
                    )

                if (!response.isSuccessful) {
                    handleMessage(
                        this@UserAbsentManualActivity,
                        TAG_RESPONSE_MESSAGE,
                        "Gagal absen: ${response.message()}"
                    )
                    return@launch
                }

                val body = response.body() ?: return@launch

                when (body.status) {

                    RESPONSE_STATUS_OK -> {

                        updateFirebaseAttendance(
                            selectedUser?.id_user ?: "0"
                        )

                        handleMessage(
                            this@UserAbsentManualActivity,
                            TAG_RESPONSE_MESSAGE,
                            "Berhasil absen"
                        )

                        clearInput()
                    }

                    RESPONSE_STATUS_FAIL,
                    RESPONSE_STATUS_FAILED -> {

                        handleMessage(
                            this@UserAbsentManualActivity,
                            TAG_RESPONSE_MESSAGE,
                            body.message
                        )
                    }

                    else -> {
                        handleMessage(
                            this@UserAbsentManualActivity,
                            TAG_RESPONSE_MESSAGE,
                            "Gagal fetch absen: ${body.message}"
                        )
                    }
                }

            } catch (e: CancellationException) {

            } catch (e: Exception) {

                FirebaseUtils.logErr(
                    this@UserAbsentManualActivity,
                    "submitManualAbsent: ${e.message}"
                )

                handleMessage(
                    this@UserAbsentManualActivity,
                    TAG_RESPONSE_MESSAGE,
                    generateFailedRunServiceMessage(
                        e.message.orEmpty()
                    )
                )

            } finally {
                progressBar.dismiss()
            }
        }
    }

    private fun updateFirebaseAttendance(userId: String) {

        val firebaseReference =
            FirebaseUtils.getReference(
                distributorId = distributorId
            )

        val currentDateTime =
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss z",
                Locale.getDefault()
            ).format(Date())

        firebaseReference
            .child(FIREBASE_CHILD_ABSENT)
            .child(userId)
            .child("morningDateTime")
            .setValue(currentDateTime)
    }

    private fun validateForm(): Boolean {

        binding.tilUser.error = null
        binding.tilBasecamp.error = null

        var isValid = true

        if (selectedUserItem == null) {
            binding.tilUser.error = "Pilih pengguna terlebih dahulu"
            isValid = false
        }

        if (selectedBasecampItem == null) {
            binding.tilBasecamp.error = "Pilih basecamp terlebih dahulu"
            isValid = false
        }

        return isValid
    }

    private fun clearInput() {
        binding.etUser.text?.clear()
        binding.etBasecamp.text?.clear()
        selectedUserItem = null
        selectedBasecampItem = null
    }
}