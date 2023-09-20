package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.SuratJalanModel

data class ResponseSuratJalan (
    var status: String = "",
    var message: String = "",
    var results: ArrayList<SuratJalanModel> = arrayListOf()
)
