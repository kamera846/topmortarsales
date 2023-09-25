package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.InvoiceModel

data class ResponseInvoice(
    var status: String = "",
    var message: String = "",
    var results: ArrayList<InvoiceModel> = arrayListOf()
)
