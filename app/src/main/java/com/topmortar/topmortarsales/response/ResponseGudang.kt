package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.GudangModel

data class ResponseGudang(
    val status: String = "",
    val message: String = "",
    val results: ArrayList<GudangModel> = arrayListOf()
)
