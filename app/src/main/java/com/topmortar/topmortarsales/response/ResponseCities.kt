package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.CityModel

data class ResponseCities (
    val status: String,
    val results: ArrayList<CityModel>
)
