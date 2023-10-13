package com.topmortar.topmortarsales.model

data class InvoicePaymentModel(
    var id_payment: String = "",
    var amount_payment: String = "",
    var date_payment: String = "",
    var remark_payment: String = "",
    var id_invoice: String = "",
    var is_removed: String = "",
    var potongan_payment: String = "",
    var adjustment_payment: String = ""
)
