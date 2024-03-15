package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.RencanaVisitModel

data class ResponseRencanaVisit (
    val status: String = "",
    val message: String = "",
    val results: ArrayList<RencanaVisitModel> = ArrayList()
)