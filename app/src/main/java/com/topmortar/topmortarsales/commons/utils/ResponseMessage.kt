package com.topmortar.topmortarsales.commons.utils

object ResponseMessage {
    fun generateFailedRunServiceMessage(message: String): String {
        return if (message.contains("No address associated with hostname")) {
            "Terjadi kesalahan jaringan, coba berganti jaringan atau silahkan coba beberapa saat lagi."
        } else {
            "Failed run service. Exception $message"
        }
    }
}