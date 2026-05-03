package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.TotalVisitSalesModel
import java.util.Objects

object ResponseObject {
    data class TotalVisitSales(
        var response: Int = -1,
        var status: String = "",
        var message: String = "",
        var results: TotalVisitSalesModel? = null,
    )

    data class PrintInvoice(
        var response: Int = -1,
        var status: String = "",
        var message: String = "",
        var data: DataPrintInvoice
    )

    data class DataPrintInvoice(
        var date_printed_inv: String
    )
}