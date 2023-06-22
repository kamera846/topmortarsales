package com.example.qontak.commons.utils

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

fun createPartFromString(string: String): RequestBody {
    return string.toRequestBody("multipart/form-data".toMediaType())
}