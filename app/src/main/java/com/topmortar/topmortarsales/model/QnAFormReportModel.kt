package com.topmortar.topmortarsales.model

data class QnAFormReportModel(
    var id: String = "",
    var question: String = "",
    var is_required: String = "",
    var answer_type: String = "",
    var answer_option: ArrayList<String>? = null,
)
