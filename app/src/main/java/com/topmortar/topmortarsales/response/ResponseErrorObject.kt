package com.topmortar.topmortarsales.response

data class ResponseErrorObject (
    val code: Int = 0,
    val messages: List<String> = listOf(),
)
