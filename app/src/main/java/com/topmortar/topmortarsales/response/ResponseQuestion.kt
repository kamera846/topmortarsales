package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.QnAFormReportModel

data class ResponseQuestion (
    val status: String = "",
    val message: String = "",
    val results: ArrayList<QnAFormReportModel> = ArrayList()
)
