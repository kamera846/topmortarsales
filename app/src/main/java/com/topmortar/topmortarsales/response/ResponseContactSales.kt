package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.ContactSales

data class ResponseContactSales (
    val status: String = "",
    val message: String = "",
    val results: ContactSales? = null
)