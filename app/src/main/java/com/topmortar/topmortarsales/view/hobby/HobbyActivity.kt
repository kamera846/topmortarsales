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
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityHobbyBinding
import com.topmortar.topmortarsales.model.HobbyModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class HobbyActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var binding: ActivityHobbyBinding
    private lateinit var adapter: HobbyAdapter

    // Source of truth untuk selection — tidak duplikat state di HobbyModel
    private val selectedHobbies = mutableMapOf<String, HobbyModel>()

    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        applyMyEdgeToEdge(isPrimary = false)

        binding = ActivityHobbyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialization()
        loadData()
        setupListeners()
    }

    private fun initialization() {
        apiService = HttpClient.apiService

        adapter = HobbyAdapter { item, isSelected ->
            onHobbyToggled(item, isSelected)
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
            loadData(binding.etSearch.text.toString())
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

    private fun loadData(searchKey: String = "") {
        lifecycleScope.launch {
            try {
                setLoadingState(true)

                val response = apiService.listHobby(searchKey = searchKey)

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

            } catch (e: Exception) {
                val message = e.message ?: "Terjadi kesalahan memuat data"
                handleMessage(this@HobbyActivity, message = "[Daftar Hobi] $message")
                setLoadingState(false, isEmpty = true)
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
            }
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
        binding.etSearch.isEnabled = !isLoading
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
        val ids = selectedHobbies.keys.toList()
        handleMessage(this, message = "Id yang dipilih $ids")
    }
}