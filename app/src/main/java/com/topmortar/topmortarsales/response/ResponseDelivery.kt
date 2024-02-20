package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.DeliveryModel

data class ResponseDelivery(
    val status: String = "",
    val message: String = "",
    val results: ArrayList<DeliveryModel.History> = arrayListOf()
)
