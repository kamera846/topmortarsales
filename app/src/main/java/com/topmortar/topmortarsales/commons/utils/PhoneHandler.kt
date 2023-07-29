package com.topmortar.topmortarsales.commons.utils

import android.widget.EditText

object PhoneHandler {

    fun formatPhoneNumber(input: String): String {
        val trimmedInput = input.trim()

        return if (trimmedInput.startsWith("0") || trimmedInput.startsWith("8")) {
            "62${trimmedInput.substring(1)}"
        } else {
            trimmedInput
        }
    }

    fun phoneValidation(input: String, etPhone: EditText): Boolean {
        val pattern = Regex("^\\d{10,16}$")
        val trimmedInput = input.trim()

        return if (!trimmedInput.startsWith("0") && !trimmedInput.startsWith("8") && !trimmedInput.startsWith("62")) {
            etPhone.error = "Phone number must consist of starting with: 0, 8, 62"
            false
        } else if (!pattern.matches(input)) {
            etPhone.error = "Phone number must be 10 to 16 digits long"
            false
        } else true
    }

}