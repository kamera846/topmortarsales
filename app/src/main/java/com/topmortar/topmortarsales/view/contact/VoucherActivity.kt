package com.topmortar.topmortarsales.view.contact

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.recyclerview.VoucherRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityVoucherBinding
import com.topmortar.topmortarsales.modal.AddVoucherModal
import com.topmortar.topmortarsales.model.VoucherModel
import kotlinx.coroutines.launch

class VoucherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoucherBinding
    private lateinit var apiService: ApiService
    private var idContact = ""
    private var contactName = ""
    private var voucherModal: AddVoucherModal? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        binding = ActivityVoucherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = HttpClient.create()

        idContact = intent.getStringExtra(CONST_CONTACT_ID).toString()
        contactName = intent.getStringExtra(CONST_NAME).toString()

        binding.titleBarDark.icBack.setOnClickListener { finish() }
        binding.titleBarDark.tvTitleBar.text = "Daftar Voucher"
        binding.titleBarDark.tvTitleBarDescription.text = "Toko $contactName"
        binding.titleBarDark.tvTitleBarDescription.visibility = if (!contactName.isNullOrEmpty()) View.VISIBLE else View.GONE

//        voucherModal = AddVoucherModal(this, lifecycleScope)
//        voucherModal.setEditCase(true)
//        voucherModal.initializeInterface(object: AddVoucherModal.AddVoucherModalInterface {
//            override fun onSubmit(status: Boolean) {
//                // Do Something
//                if (status) getList()
//            }
//
//        })

        getList()

    }

    private fun getList() {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val response = apiService.listVoucher(idContact = idContact)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        setRecyclerView(response.results)
                        loadingState(false)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Belum ada voucher!")

                    }
                    else -> {

                        handleMessage(this@VoucherActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        loadingState(true, getString(R.string.failed_request))

                    }
                }

            } catch (e: Exception) {

                handleMessage(this@VoucherActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))

            }

        }

    }

    private fun setRecyclerView(listItem: ArrayList<VoucherModel>) {
        val rvAdapter = VoucherRecyclerViewAdapter(listItem, object: VoucherRecyclerViewAdapter.ItemClickListener {
            override fun onItemClick(data: VoucherModel?) {
                // Do Something
                if (data?.is_claimed == "0") {
                    voucherModal = null
                    voucherModal = AddVoucherModal(this@VoucherActivity, lifecycleScope)
                    voucherModal!!.setEditCase(true, data)
                    voucherModal!!.setVoucherId(data?.id_voucher ?: "")
                    voucherModal!!.initializeInterface(object: AddVoucherModal.AddVoucherModalInterface {
                        override fun onSubmit(status: Boolean) {
                            // Do Something
                            if (status) getList()
                        }

                    })
                    voucherModal!!.show()
                }
            }

        })

        binding.rvList.apply {
            layoutManager = LinearLayoutManager(this@VoucherActivity)
            adapter = rvAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                private var lastScrollPosition = 0

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy < 0) {
                        // Scrolled up
                        val firstVisibleItemPosition =
                            (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        if (lastScrollPosition != firstVisibleItemPosition) {
                            recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition)?.itemView?.startAnimation(
                                AnimationUtils.loadAnimation(
                                    recyclerView.context,
                                    R.anim.rv_item_fade_slide_down
                                )
                            )
                            lastScrollPosition = firstVisibleItemPosition
                        }
                    } else lastScrollPosition = -1
                }
            })
        }
    }

    private fun loadingState(state: Boolean, message: String = getString(R.string.txt_loading)) {

        binding.txtLoading.text = message

        if (state) {

            binding.txtLoading.visibility = View.VISIBLE
            binding.rvList.visibility = View.GONE

        } else {

            binding.txtLoading.visibility = View.GONE
            binding.rvList.visibility = View.VISIBLE

        }

    }
}