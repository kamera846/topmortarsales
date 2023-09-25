package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.InvoicePaymentModel

data class ResponsePayment(
    var status: String = "",
    var message: String = "",
    var results: ArrayList<InvoicePaymentModel> = arrayListOf()
)