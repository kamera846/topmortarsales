package com.topmortar.topmortarsales.view.hobby

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.topmortar.topmortarsales.adapter.HobbyAdapter
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityHobbyBinding
import com.topmortar.topmortarsales.model.HobbyModel
import kotlinx.coroutines.launch

class HobbyActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService

    private lateinit var binding: ActivityHobbyBinding

    private lateinit var adapter: HobbyAdapter

    private val hobbies = mutableListOf<HobbyModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge(isPrimary = false)

        binding = ActivityHobbyBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initialization()

        loadData()

        listenerAction()

        updateSelection()
    }

    private fun initialization() {
        apiService = HttpClient.apiService

        adapter = HobbyAdapter(hobbies) {
            updateSelection()
        }
    }

    private fun listenerAction() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadData()
        }

        binding.btnSave.setOnClickListener {
            confirmSubmit()
        }
    }

    private fun updateSelection() {

        binding.cgSelectedHobby.removeAllViews()

        val selection =
            hobbies.filter { it.isSelected }

        selectionStateHandler(selection)

        if (selection.isNotEmpty()) {
            selection.forEach { item ->
                val chip = Chip(this@HobbyActivity).apply {
                    text = item.name_hobi
                    isClickable = false
                    isCloseIconVisible = true
                    setEnsureMinTouchTargetSize(false)

                    setOnCloseIconClickListener {
                        item.isSelected = !item.isSelected
                        val itemPosition = hobbies.indexOfFirst { it.id_hobi == item.id_hobi }
                        if (itemPosition >= 0) {
                            adapter.notifyItemChanged(itemPosition)
                        }
                        binding.cgSelectedHobby.removeView(this)
                        selectionStateHandler()
                    }
                }
                binding.cgSelectedHobby.addView(chip)
            }
        }
    }

    private fun selectionStateHandler(data: List<HobbyModel>? = null) {
        val selection = data ?: hobbies.filter { it.isSelected }

        if (selection.isNotEmpty()) {
            binding.cgSelectedHobby.visibility = View.VISIBLE
            binding.selectedHobbiesLine.visibility = View.VISIBLE
            binding.btnSave.isEnabled = true
        } else {
            binding.cgSelectedHobby.visibility = View.GONE
            binding.selectedHobbiesLine.visibility = View.GONE
            binding.btnSave.isEnabled = false
        }
    }

    private fun loadData() {

        lifecycleScope.launch {
            try {
                loadingState(true)

                val response = apiService.listHobby(searchKey = "")

                if (!response.isSuccessful) {
                    throw IllegalStateException("Http Error: code ${response.code()}, message: ${response.message()}")
                }

                val body = response.body() ?: throw IllegalStateException("Response body kosong")

                if (body.status != RESPONSE_STATUS_OK) {
                    throw IllegalStateException("Error: status ${body.status}, message: ${body.message}")
                }

                hobbies.addAll(body.results)

                setRecyclerView()
            } catch (e: Exception) {
                val message = e.message ?: "Terjadi kesalahan memuat data"
                handleMessage(this@HobbyActivity, message = "[Daftar Hobi] $message")
            } finally {
                loadingState(false)
            }
        }
    }

    private fun setRecyclerView() {
        binding.rvHobby.apply {
            layoutManager = LinearLayoutManager(this@HobbyActivity)
            adapter = this@HobbyActivity.adapter
        }
    }

    private fun loadingState(isLoading: Boolean) {
        if (isLoading) {
            binding.rvHobby.visibility = View.GONE
            binding.btnSave.visibility = View.GONE
            binding.txtLoading.visibility = View.VISIBLE
            binding.etSearch.isEnabled = false
        } else {
            if (hobbies.isNotEmpty()) {
                binding.rvHobby.visibility = View.VISIBLE
                binding.btnSave.visibility = View.VISIBLE
                binding.txtLoading.visibility = View.GONE
            }

            binding.etSearch.isEnabled = true

            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun confirmSubmit() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi")
            .setMessage("Apakah Anda yakin akan menyimpan pilihan saat ini?")
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Ya") { dialog, _ ->
                dialog.dismiss()
                submit()
            }
            .create()
            .show()
    }

    private fun submit() {
        val selected =
            hobbies.filter { it.isSelected }

        val ids =
            selected.map { it.id_hobi }

        Toast.makeText(
            this,
            "Dipilih: $ids",
            Toast.LENGTH_LONG
        ).show()
    }
}