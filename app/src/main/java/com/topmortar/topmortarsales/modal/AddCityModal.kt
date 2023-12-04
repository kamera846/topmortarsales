package com.topmortar.topmortarsales.modal

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
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
import com.topmortar.topmortarsales.model.CityModel
import com.topmortar.topmortarsales.model.DistributorModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AddCityModal(private val context: Context, private val lifecycleScope: CoroutineScope) : Dialog(context) {

    private lateinit var sessionManager: SessionManager
    private val userDistributorId get() = sessionManager.userDistributor().toString()

    private lateinit var titleBar: LinearLayout
    private lateinit var icBack: ImageView
    private lateinit var icClose: ImageView
    private lateinit var tvTitleBar: TextView
    private lateinit var etCityName: EditText
    private lateinit var etCityCode: EditText
    private lateinit var btnSubmit: Button

    private var item: CityModel? = null
    fun setItem(data: CityModel) {
        this.item = data
    }

    private var modalInterface : AddCityModalInterface? = null
    fun initializeInterface(data : AddCityModalInterface) {
        this.modalInterface = data
    }
    interface AddCityModalInterface {
        fun onSubmit(status: Boolean)
    }
    private var distributorOptions = arrayListOf(DistributorModel(id_distributor = "-1", nama_distributor = "Memuat..."))
    private var selectedDistributor = DistributorModel(id_distributor = "-1", nama_distributor = "Memuat...")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.modal_add_city)

        sessionManager = SessionManager(context)

        setLayout()
        initVariable()
        initClickHandler()
        setSpinner()
//        getListDistributor()
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

        etCityName = findViewById(R.id.et_city_name)
        etCityCode = findViewById(R.id.et_city_code)
        btnSubmit = findViewById(R.id.btn_submit)

        // Set Title Bar
        icBack.visibility = View.GONE
        icClose.visibility = View.VISIBLE
        tvTitleBar.text = "Add New City"

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

        if (!formValidation("${ etCityName.text }", "${ etCityCode.text }")) return

        loadingState(true)

        lifecycleScope.launch {
            try {

                val cityName = createPartFromString("${ etCityName.text }")
                val cityCode = createPartFromString("${ etCityCode.text }")
                val distributorID = createPartFromString(userDistributorId)

                val apiService: ApiService = HttpClient.create()
                val response = apiService.addCity(name = cityName, code = cityCode, distributorID = distributorID)

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            etCityName.setText("")
                            etCityCode.setText("")
                            loadingState(false)
                            handleMessage(context, TAG_RESPONSE_CONTACT, "Berhasil menambahkan data!")

                            modalInterface!!.onSubmit(true)
                            this@AddCityModal.dismiss()

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
            etCityName.error = "Nama kota wajib diisi!"
            etCityName.requestFocus()
            false
        } else if (code.isEmpty()) {
            etCityName.error = null
            etCityName.clearFocus()
            etCityCode.error = "Kode wajib diisi!"
            etCityCode.requestFocus()
            false
//        } else if (selectedDistributor.id_distributor == "-1") {
//            etCityCode.error = null
//            etCityCode.clearFocus()
//            handleMessage(context, "Error Form", "Distributor wajib dipilih!")
//            false
        } else {
            etCityName.error = null
            etCityName.clearFocus()
            etCityCode.error = null
            etCityCode.clearFocus()
            true
        }
    }

    private fun setSpinner() {

        val spinner: Spinner = findViewById(R.id.spin_distributor)
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, distributorOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                // Get the selected item value (e.g., "admin" or "sales")
                selectedDistributor = distributorOptions[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

    }

    private fun getListDistributor() {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getListDistributor()

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        distributorOptions = response.results
                        distributorOptions.add(0, DistributorModel(id_distributor = "-1", nama_distributor = "Pilih Distributor"))
                        setSpinner()
                        loadingState(false)

                    }
                    RESPONSE_STATUS_EMPTY -> {
                        handleMessage(context, TAG_RESPONSE_CONTACT, context.getString(R.string.failed_get_data))
                        distributorOptions.add(0, DistributorModel(id_distributor = "-1", nama_distributor = "Pilih Distributor"))
                        setSpinner()
                        loadingState(false)
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
}