package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.ReportVisitModel

data class ResponseReportVisit(
    var response: Int = 0,
    var status: String = "",
    var message: String = "",
    var results: ArrayList<ReportVisitModel> = arrayListOf(),
)
