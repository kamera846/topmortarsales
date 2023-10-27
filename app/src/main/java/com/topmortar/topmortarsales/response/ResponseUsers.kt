package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.UserModel

data class ResponseUsers (
    val status: String = "",
    val message: String = "",
    val results: ArrayList<UserModel> = arrayListOf()
)
