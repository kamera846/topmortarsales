package com.topmortar.topmortarsales.commons.utils

import android.widget.EditText

object PhoneHandler {

    fun formatPhoneNumber(input: String): String {
        val trimmedInput = input.trim()

        return if (trimmedInput.startsWith("0")) {
            "62${trimmedInput.substring(1)}"
        } else if (trimmedInput.startsWith("8")) {
            "62${trimmedInput.substring(0)}"
        } else {
            trimmedInput
        }
    }

    fun formatPhoneNumber62(input: String): String {
        val trimmedInput = input.trim()

        return if (trimmedInput.startsWith("0")) {
            "62${trimmedInput.substring(1)}"
        } else if (trimmedInput.startsWith("8")) {
            "62${trimmedInput.substring(0)}"
        } else if (trimmedInput.startsWith("+")) {
            "${trimmedInput.substring(1)}"
        } else {
            trimmedInput
        }
    }

    fun phoneValidation(input: String, etPhone: EditText): Boolean {
        val pattern = Regex("^\\d{10,16}$")
        val trimmedInput = input.trim()

        return if (!trimmedInput.startsWith("0") && !trimmedInput.startsWith("8") && !trimmedInput.startsWith("62")) {
            etPhone.error = "Nomor telpon harus diawali dengan format: 08XXXX, 8XXXX, 628XXXX"
            false
        } else if (!pattern.matches(input)) {
            etPhone.error = "Jumlah nomor telpon umumnya sekitar 10 - 16 digit"
            false
        } else true
    }

}