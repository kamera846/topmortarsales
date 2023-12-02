package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.DistributorModel

data class ResponseDistributor (
    val status: String = "",
    val message: String = "",
    val results: ArrayList<DistributorModel> = arrayListOf()
)