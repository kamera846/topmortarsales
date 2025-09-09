package com.topmortar.topmortarsales.response

data class ResponseMessage (
    val response: Int = 0,
    val status: String = "",
    val message: String = "",
    val user_id: String = "",
    val error: ErrorObject? = null,
    val qontak: Qontak? = null,
) {
    class ErrorObject (
        val code: Int = 0,
        val messages: List<String> = listOf(),
    )
}
data class Qontak (
    val status: String = "",
    val error: QontakError? = null,
)
data class QontakError (
    val code: Int = 0,
    val messages: List<String> = listOf(),
)
