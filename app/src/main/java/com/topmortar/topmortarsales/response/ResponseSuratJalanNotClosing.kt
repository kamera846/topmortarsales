package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.SuratJalanNotClosingModel

data class ResponseSuratJalanNotClosing (
    var status: String = "",
    var message: String = "",
    var results: ArrayList<SuratJalanNotClosingModel> = arrayListOf()
)