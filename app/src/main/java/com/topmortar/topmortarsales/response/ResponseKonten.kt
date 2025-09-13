package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.KontenModel

data class ResponseKonten(
    val status: String = "",
    val message: String = "",
    val results: ArrayList<KontenModel> = ArrayList(),
    val error: ResponseErrorObject? = null,
    val qontak: ResponseQontak? = null,
)