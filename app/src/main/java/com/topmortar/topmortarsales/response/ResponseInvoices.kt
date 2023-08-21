package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.InvoiceModel

data class ResponseInvoices (
    val status: String,
    val results: ArrayList<InvoiceModel>
)
