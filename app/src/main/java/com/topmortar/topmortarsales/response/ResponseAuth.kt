package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.CityModel
import com.topmortar.topmortarsales.model.UserModel

data class ResponseAuth (
    val status: String,
    val results: ArrayList<UserModel>
)
