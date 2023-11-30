package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.BaseCampModel

data class ResponseBaseCamp(
    val status: String = "",
    val message: String = "",
    val results: ArrayList<BaseCampModel> = arrayListOf()
)
