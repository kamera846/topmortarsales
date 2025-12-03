package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.TotalVisitSalesModel

object ResponseObject {
    data class TotalVisitSales(
        var response: Int = -1,
        var status: String = "",
        var message: String = "",
        var results: TotalVisitSalesModel? = null,
    )
}