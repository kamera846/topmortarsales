package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.CityModel
import com.topmortar.topmortarsales.model.PromoModel

data class ResponsePromo (
    var status: String = "",
    var message: String = "",
    var results: ArrayList<PromoModel> = arrayListOf()
)