package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.HobbyModel

data class ResponseHobby(
    var status: String = "",
    val message: String = "",
    var results: ArrayList<HobbyModel> = arrayListOf()
)
