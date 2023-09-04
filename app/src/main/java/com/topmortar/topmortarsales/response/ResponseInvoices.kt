package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.InvoiceModel

data class ResponseInvoices (
    var status: String = "",
    var message: String = "",
    var results: ArrayList<InvoiceModel> = arrayListOf()
)
