package com.topmortar.topmortarsales.model

data class QnAFormReportModel(
    var id_visit_question: String = "",
    var text_question: String = "",
    var is_required: String = "",
    var answer_type: String = "",
    var created_at: String = "",
    var updated_at: String? = "",
    var answer_option: ArrayList<String>? = null,
    var selected_answer: ArrayList<String>? = null,
    var text_answer: String = "",
)
