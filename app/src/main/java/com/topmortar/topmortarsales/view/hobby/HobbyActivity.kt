package com.topmortar.topmortarsales.view.hobby

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.HobbyAdapter
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_USER_ID
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_SUCCESS
import com.topmortar.topmortarsales.commons.utils.CustomProgressBar
import com.topmortar.topmortarsales.commons.utils.EventBusUtils
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityHobbyBinding
import com.topmortar.topmortarsales.model.HobbyModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import kotlin.time.Duration.Companion.milliseconds

class HobbyActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var binding: ActivityHobbyBinding
    private lateinit var adapter: HobbyAdapter
    private lateinit var contactId: String
    private lateinit var userId: String
    private lateinit var progressBar: CustomProgressBar

    // Source of truth untuk selection — tidak duplikat state di HobbyModel
    private val selectedHobbies = mutableMapOf<String, HobbyModel>()

    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        applyMyEdgeToEdge(isPrimary = false)

        binding = ActivityHobbyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupIntentData()
        initialization()

        lifecycleScope.launch {
            loadData()
        }
        setupListeners()

        EventBus.getDefault().register(this)
    }

    @Subscribe(sticky = true, threadMode = org.greenrobot.eventbus.ThreadMode.MAIN)
    fun onEvent(event: EventBusUtils.ListHobbyEvent) {
        event.data.forEach { item ->
            selectedHobbies[item.id_hobi] = item
            addChip(item)
        }
        EventBus.getDefault().removeStickyEvent(event)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    private fun setupIntentData() {
        contactId = intent.getStringExtra(CONST_CONTACT_ID) ?: ""
        userId = intent.getStringExtra(CONST_USER_ID) ?: ""
    }

    private fun initialization() {
        apiService = HttpClient.apiService

        adapter = HobbyAdapter { item, isSelected ->
            onHobbyToggled(item, isSelected)
        }

        progressBar = CustomProgressBar(this).apply {
            title = "Memproses"
            setMessage("Tunggu sebentar...")
            setCancelable(false)
        }

        // RecyclerView cukup di-setup sekali
        binding.rvHobby.apply {
            layoutManager = LinearLayoutManager(this@HobbyActivity)
            adapter = this@HobbyActivity.adapter
        }

        binding.titleBar.tvTitleBar.text = "Edit Hobi"
    }

    private fun setupListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                loadData(binding.etSearch.text.toString())
            }
        }

        binding.btnSave.setOnClickListener {
            confirmSubmit()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(500.milliseconds)
                    loadData(s?.toString().orEmpty())
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun onHobbyToggled(item: HobbyModel, isSelected: Boolean) {
        if (isSelected) {
            selectedHobbies[item.id_hobi] = item
            addChip(item)
        } else {
            selectedHobbies.remove(item.id_hobi)
            removeChip(item.id_hobi)
        }
        // Sync visual state ke adapter tanpa reload dari API
        val updated = adapter.currentList.map { hobby ->
            if (hobby.id_hobi == item.id_hobi) hobby.copy(isSelected = isSelected) else hobby
        }
        adapter.submitList(updated)

        updateSelectionVisibility()
    }

    private fun addChip(item: HobbyModel) {
        // Hindari duplikat chip
        if (binding.cgSelectedHobby.findViewWithTag<Chip>(item.id_hobi) != null) return

        val chip = Chip(this).apply {
            tag = item.id_hobi        // Gunakan tag string, bukan .toInt()
            text = item.name_hobi
            isClickable = false
            isCloseIconVisible = true
            setEnsureMinTouchTargetSize(false)
            setOnCloseIconClickListener {
                onHobbyToggled(item.copy(isSelected = true), isSelected = false)
            }
        }
        binding.cgSelectedHobby.addView(chip)
    }

    private fun removeChip(id: String) {
        binding.cgSelectedHobby.findViewWithTag<Chip>(id)?.let {
            binding.cgSelectedHobby.removeView(it)
        }
    }

    private fun updateSelectionVisibility() {
        val hasSelection = selectedHobbies.isNotEmpty()
        binding.cgSelectedHobby.isVisible = hasSelection
        binding.selectedHobbiesLine.isVisible = hasSelection
        binding.btnSave.isEnabled = hasSelection
    }

    private suspend fun loadData(searchKey: String = "") {
        try {
            setLoadingState(true)

            val response = apiService.hobi(searchKey = searchKey)

            if (!response.isSuccessful) {
                throw IllegalStateException("Http Error ${response.code()}: ${response.message()}")
            }

            val body = response.body()
                ?: throw IllegalStateException("Response body kosong")

            if (body.status != RESPONSE_STATUS_OK) {
                throw IllegalStateException("Status ${body.status}: ${body.message}")
            }

            // Terapkan selection state dari selectedHobbies ke hasil baru
            val results = body.results.map { hobby ->
                hobby.copy(isSelected = hobby.id_hobi in selectedHobbies)
            }

            adapter.submitList(results)
            setLoadingState(false, isEmpty = results.isEmpty())

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val message = e.message ?: "Terjadi kesalahan memuat data"
            handleMessage(this@HobbyActivity, message = "[Daftar Hobi] $message")
            setLoadingState(false, isEmpty = true)
        } finally {
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setLoadingState(isLoading: Boolean, isEmpty: Boolean = false) {
        binding.txtLoading.isVisible = isLoading || isEmpty
        binding.txtLoading.text = if (isEmpty && !isLoading) {
            "Data tidak ditemukan"
        } else {
            getString(R.string.txt_loading)
        }
        binding.rvHobby.isVisible = !isLoading && !isEmpty
        binding.btnSave.isVisible = !isLoading
    }

    private fun confirmSubmit() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi")
            .setMessage("Apakah Anda yakin akan menyimpan pilihan saat ini?")
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Ya") { dialog, _ ->   // FIX: was setNegativeButton
                dialog.dismiss()
                submit()
            }
            .show()
    }

    private fun submit() {
        val ids = selectedHobbies.keys.toList().joinToString { it }
        var isSuccess = false

        lifecycleScope.launch {
            try {
                progressBar.show()

                val response = apiService.saveHobiToko(
                    createPartFromString(contactId),
                    createPartFromString(ids),
                    createPartFromString(userId),
                )

                if (!response.isSuccessful) {
                    throw IllegalStateException("Http Error ${response.code()}: ${response.message()}")
                }

                val body = response.body()
                    ?: throw IllegalStateException("Response body kosong")

                if (body.status != RESPONSE_STATUS_SUCCESS) {
                    throw IllegalStateException("Status ${body.status}: ${body.message}")
                }

                isSuccess = true
                handleMessage(this@HobbyActivity, message = "Berhasil menyimpan hobi")

            } catch (e: Exception) {
                val message = e.message ?: "Terjadi kesalahan menyimpan data"
                handleMessage(this@HobbyActivity, message = "[Simpan Hobi] $message")
                setLoadingState(false, isEmpty = true)
            } finally {
                progressBar.dismiss()
                if (isSuccess) {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }
}