package com.topmortar.topmortarsales.modal

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_SUCCESS
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ModalAddVoucherBinding
import com.topmortar.topmortarsales.model.VoucherModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AddVoucherModal(private val context: Context, private val lifecycleScope: CoroutineScope) : Dialog(context) {

    private var _binding: ModalAddVoucherBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val userDistributorId get() = sessionManager.userDistributor().toString()

    private lateinit var titleBar: LinearLayout
    private lateinit var icBack: ImageView
    private lateinit var icClose: ImageView
    private lateinit var tvTitleBar: TextView
    private lateinit var btnSubmit: Button

    private var isEdit: Boolean = false
    private var data: VoucherModel? = null
    fun setEditCase(isEdit: Boolean, data: VoucherModel? = null) {
        this.isEdit = isEdit
        this.data = data
    }
    private var contactId: String = "-1"
    fun setVoucherId(id: String) {
        this.contactId = id
    }

    // Interface
    private var modalInterface : AddVoucherModalInterface? = null
    fun initializeInterface(data : AddVoucherModalInterface) {
        this.modalInterface = data
    }
    interface AddVoucherModalInterface {
        fun onSubmit(status: Boolean)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ModalAddVoucherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(context)

        setLayout()
        initVariable()
        initClickHandler()
    }

    private fun setLayout() {
        val displayMetrics = DisplayMetrics()
        window?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels

        val widthPercentage = 0.8f // Set the width percentage (e.g., 80%)

        val width = (screenWidth * widthPercentage).toInt()

        val layoutParams = window?.attributes
        layoutParams?.width = width
        layoutParams?.height = WindowManager.LayoutParams.WRAP_CONTENT // Set height to wrap content
        window?.attributes = layoutParams as WindowManager.LayoutParams
    }

    private fun initVariable() {
        titleBar = findViewById(R.id.title_bar)

        icBack = titleBar.findViewById(R.id.ic_back)
        icClose = titleBar.findViewById(R.id.ic_close)
        tvTitleBar = titleBar.findViewById(R.id.tv_title_bar)

        btnSubmit = findViewById(R.id.btn_submit)

        // Set Title Bar
        icBack.visibility = View.GONE
        icClose.visibility = View.VISIBLE
        tvTitleBar.text = (if (isEdit) "Edit" else "Tambah") + " Voucher"

        tvTitleBar.setPadding(convertDpToPx(16, context),0, convertDpToPx(16, context), 0)

        if (isEdit && data != null) binding.etVoucher1.setText(data!!.no_fisik)
//        handleMessage(context, TAG_RESPONSE_MESSAGE, "$isEdit : $data")
    }

    private fun initClickHandler() {
        icClose.setOnClickListener { this.dismiss() }
        btnSubmit.setOnClickListener { submitHandler() }
    }

    private fun loadingState(state: Boolean) {

        btnSubmit.setTextColor(ContextCompat.getColor(context, R.color.white))
        btnSubmit.setBackgroundColor(ContextCompat.getColor(context, R.color.primary_200))

        if (state) {

            btnSubmit.isEnabled = false
            btnSubmit.text = context.getString(R.string.txt_loading)

        } else {

            btnSubmit.isEnabled = true
            btnSubmit.text = context.getString(R.string.submit)
            btnSubmit.setBackgroundColor(ContextCompat.getColor(context, R.color.primary))

        }

    }

    private fun submitHandler() {

        if (!formValidation()) return

        loadingState(true)
        val vouchers = arrayListOf<String>()

        val voucher1 = binding.etVoucher1.text.trim()
//        val voucher2 = binding.etVoucher2.text.trim()
//        val voucher3 = binding.etVoucher3.text.trim()
//        val voucher4 = binding.etVoucher4.text.trim()
//        val voucher5 = binding.etVoucher5.text.trim()

        if (voucher1.isNotEmpty()) vouchers.add(voucher1.toString())
//        if (voucher2.isNotEmpty()) vouchers.add(voucher2.toString())
//        if (voucher3.isNotEmpty()) vouchers.add(voucher3.toString())
//        if (voucher4.isNotEmpty()) vouchers.add(voucher4.toString())
//        if (voucher5.isNotEmpty()) vouchers.add(voucher5.toString())

        lifecycleScope.launch {
            try {

//                val idContact = createPartFromString(contactId)
//                val voucher = createPartFromString(vouchers.joinToString(","))

                val idVoucher = createPartFromString(contactId)
                val noFisik = createPartFromString(voucher1.toString())


                val apiService: ApiService = HttpClient.create()
                val response = apiService.editNoFisikVoucher(idVoucher = idVoucher, noFisik = noFisik)

                when (response.status) {
                    RESPONSE_STATUS_OK, RESPONSE_STATUS_SUCCESS -> {

                        handleMessage(context, TAG_RESPONSE_CONTACT, response.message)

                        isEdit = false
                        loadingState(false)
                        modalInterface!!.onSubmit(true)

                        this@AddVoucherModal.dismiss()

                    }
                    RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                        handleMessage(context, TAG_RESPONSE_MESSAGE, "Gagal menambahkan! Message: ${ response.message }")
                        loadingState(false)

                    }
                    else -> {

                        handleMessage(context, TAG_RESPONSE_CONTACT, "Gagal menambahkan data!")
                        loadingState(false)

                    }
                }


            } catch (e: Exception) {

                handleMessage(context, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(false)

            }

        }

    }

    private fun formValidation(): Boolean {
        if (
            binding.etVoucher1.text.trim().isEmpty()
//            binding.etVoucher1.text.trim().isEmpty() &&
//            binding.etVoucher2.text.trim().isEmpty() &&
//            binding.etVoucher3.text.trim().isEmpty() &&
//            binding.etVoucher4.text.trim().isEmpty() &&
//            binding.etVoucher5.text.trim().isEmpty()
        ) {
            val textError = "Kode voucher harus diisi!"
            binding.etVoucher1.error = textError
            binding.etVoucher1.requestFocus()
            handleMessage(context, "Voucher Validation", textError)
//            handleMessage(context, "Voucher Validation", "Isi minimal satu form voucher untuk menambahkan")
            return false
        }

        if (
            binding.etVoucher1.text.length < 5 ||
            binding.etVoucher1.text.length > 5
//            binding.etVoucher1.text.trim().isEmpty() &&
//            binding.etVoucher2.text.trim().isEmpty() &&
//            binding.etVoucher3.text.trim().isEmpty() &&
//            binding.etVoucher4.text.trim().isEmpty() &&
//            binding.etVoucher5.text.trim().isEmpty()
        ) {
            val textError = "Kode voucher harus memiliki 5 karakter huruf!"
            binding.etVoucher1.error = textError
            binding.etVoucher1.requestFocus()
            handleMessage(context, "Voucher Validation", textError)
//            handleMessage(context, "Voucher Validation", "Isi minimal satu form voucher untuk menambahkan")
            return false
        }

        binding.etVoucher1.error = null
        binding.etVoucher1.clearFocus()
        return true
    }

    override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        super.setOnDismissListener(listener)
        _binding = null
    }
}