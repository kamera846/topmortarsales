package com.topmortar.topmortarsales.response

data class ResponseMessage (
    val response: Int = 0,
    val status: String = "",
    val message: String = "",
    val user_id: String = "",
    val error: ErrorObject? = null,
) {
    class ErrorObject (
        val code: Int = 0,
        val messages: Array<String> = arrayOf(),
    )
}
