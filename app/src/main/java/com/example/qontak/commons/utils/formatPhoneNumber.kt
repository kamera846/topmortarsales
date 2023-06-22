package com.example.qontak.commons.utils

fun formatPhoneNumber(input: String): String {
    val trimmedInput = input.trim()

    return if (trimmedInput.startsWith("0") || trimmedInput.startsWith("8")) {
        "62${trimmedInput.substring(1)}"
    } else {
        trimmedInput
    }
}