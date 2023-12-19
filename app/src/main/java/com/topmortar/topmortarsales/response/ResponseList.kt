package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.VoucherModel

object ResponseList {
    data class ResponseVoucher (
        var status: String = "",
        var message: String = "",
        var results: ArrayList<VoucherModel> = arrayListOf(),
    )
}