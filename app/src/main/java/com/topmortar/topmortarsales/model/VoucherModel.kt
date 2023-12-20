package com.topmortar.topmortarsales.model

data class VoucherModel(
    var id_voucher: String = "",
    var id_contact: String = "",
    var no_voucher: String = "",
    var no_fisik: String = "",
    var point_voucher: String = "",
    var date_voucher: String = "",
    var is_claimed: String = "",
)