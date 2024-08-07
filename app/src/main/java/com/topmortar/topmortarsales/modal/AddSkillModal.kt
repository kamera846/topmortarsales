package com.topmortar.topmortarsales.modal

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAIL
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_FAILED
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_MESSAGE
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.model.SkillModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AddSkillModal(private val context: Context, private val lifecycleScope: CoroutineScope) : Dialog(context) {

    private lateinit var sessionManager: SessionManager
    private val userDistributorId get() = sessionManager.userDistributor().toString()

    private lateinit var titleBar: LinearLayout
    private lateinit var icBack: ImageView
    private lateinit var icClose: ImageView
    private lateinit var tvTitleBar: TextView
    private lateinit var tvSkillName: TextView
    private lateinit var tvSkillCode: TextView
    private lateinit var etSkillName: EditText
    private lateinit var etSkillCode: EditText
    private lateinit var btnSubmit: Button

    private var skillID: String? = "0"
    private var item: SkillModel? = null
    private var isEdit: Boolean = false
    fun setItem(data: SkillModel) {
        this.item = data
        skillID = data.id_skill
        etSkillName.setText(data.nama_skill)
        etSkillCode.setText(data.kode_skill)
    }
    fun setTitle(title: String) {
        tvTitleBar.text = title
    }
    fun setEditCase(isEdit: Boolean) {
        this.isEdit = isEdit
        btnSubmit.text = context.getString(R.string.save)
    }

    private var modalInterface : AddSkillModalInterface? = null
    fun initializeInterface(data : AddSkillModalInterface) {
        this.modalInterface = data
    }
    interface AddSkillModalInterface {
        fun onSubmit(status: Boolean)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.modal_add_city)
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

        tvSkillName = findViewById(R.id.tv_city_name)
        tvSkillCode = findViewById(R.id.tv_city_code)
        etSkillName = findViewById(R.id.et_city_name)
        etSkillCode = findViewById(R.id.et_city_code)
        btnSubmit = findViewById(R.id.btn_submit)

        // Set Title Bar
        icBack.visibility = View.GONE
        icClose.visibility = View.VISIBLE
        tvTitleBar.text = "Tambah Keahlian Baru"
        tvSkillName.text = "Nama"
        tvSkillCode.text = "Kode"
        etSkillName.hint = "e.g Tukang Batu"
        etSkillCode.hint = "e.g TB"

        tvTitleBar.setPadding(convertDpToPx(16, context),0, convertDpToPx(16, context), 0)
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

        if (!formValidation("${ etSkillName.text }", "${ etSkillCode.text }")) return

        loadingState(true)

        lifecycleScope.launch {
            try {

                val skillName = createPartFromString("${ etSkillName.text }")
                val skillCode = createPartFromString("${ etSkillCode.text }")
                val distributorId = createPartFromString(userDistributorId)

//                handleMessage(context, "SUBMIT SKILL", "Name: $skillName, Code: $skillCode")
//                return@launch

                val apiService: ApiService = HttpClient.create()
                val response = isEdit.let {
                    if (it) apiService.editSkill(id = createPartFromString("$skillID"), name = skillName, code = skillCode, distributorID = distributorId)
                    else apiService.addSkill(name = skillName, code = skillCode, distributorID = distributorId)
                }

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            handleMessage(context, TAG_RESPONSE_CONTACT, if (isEdit) "Berhasil mengubah data" else "Berhasil menambahkan data!")

                            tvTitleBar.text = "Tambah Keahlian Baru"
                            etSkillName.setText("")
                            etSkillCode.setText("")
                            isEdit = false
                            loadingState(false)

                            modalInterface!!.onSubmit(true)
                            this@AddSkillModal.dismiss()

                        }
                        RESPONSE_STATUS_FAIL, RESPONSE_STATUS_FAILED -> {

                            handleMessage(context, TAG_RESPONSE_MESSAGE, "Gagal menambahkan! Message: ${ responseBody.message }")
                            loadingState(false)

                        }
                        else -> {

                            handleMessage(context, TAG_RESPONSE_CONTACT, "Gagal menambahkan data!")
                            loadingState(false)

                        }
                    }

                } else {

                    handleMessage(context, TAG_RESPONSE_CONTACT, "Gagal menambahkan data! Message: " + response.message())
                    loadingState(false)

                }


            } catch (e: Exception) {

                handleMessage(context, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(false)

            }

        }

    }

    private fun formValidation(name: String, code: String): Boolean {
        return if (name.isEmpty()) {
            etSkillName.error = "Nama keahlian wajib diisi!"
            etSkillName.requestFocus()
            false
        } else if (code.isEmpty()) {
            etSkillName.error = null
            etSkillName.clearFocus()
            etSkillCode.error = "Kode wajib diisi!"
            etSkillCode.requestFocus()
            false
        } else {
            etSkillName.error = null
            etSkillName.clearFocus()
            etSkillCode.error = null
            etSkillCode.clearFocus()
            true
        }
    }
}