package com.topmortar.topmortarsales.modal

import android.app.Dialog
import android.content.Context
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
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.model.CityModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AddCityModal(private val context: Context, private val lifecycleScope: CoroutineScope) : Dialog(context) {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.modal_add_city)

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

        etCityName = findViewById(R.id.et_city_name)
        etCityCode = findViewById(R.id.et_city_code)
        btnSubmit = findViewById(R.id.btn_submit)

        // Set Title Bar
        icBack.visibility = View.GONE
        icClose.visibility = View.VISIBLE
        tvTitleBar.text = "Add New City"
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
            btnSubmit.text = "LOADING..."

        } else {

            btnSubmit.isEnabled = true
            btnSubmit.text = "SUBMIT"
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

                val apiService: ApiService = HttpClient.create()
                val response = apiService.addCity(name = cityName, code = cityCode)

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    if (responseBody.status == RESPONSE_STATUS_OK) {

                        etCityName.setText("")
                        etCityCode.setText("")
                        loadingState(false)
                        handleMessage(context, TAG_RESPONSE_CONTACT, "Successfully added data!")

                        modalInterface!!.onSubmit(true)
                        this@AddCityModal.dismiss()

                    } else {

                        handleMessage(context, TAG_RESPONSE_CONTACT, "Failed added data!")
                        loadingState(false)

                    }

                } else {

                    handleMessage(context, TAG_RESPONSE_CONTACT, "Failed added data! Message: " + response.message())
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
            etCityName.error = "City name cannot be empty!"
            etCityName.requestFocus()
            false
        } else if (code.isEmpty()) {
            etCityName.error = null
            etCityName.clearFocus()
            etCityCode.error = "Code cannot be empty!"
            etCityCode.requestFocus()
            false
        } else {
            etCityName.error = null
            etCityName.clearFocus()
            etCityCode.error = null
            etCityCode.clearFocus()
            true
        }
    }
}