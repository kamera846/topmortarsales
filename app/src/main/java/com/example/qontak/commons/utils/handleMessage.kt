package com.example.qontak.commons.utils

import android.content.Context
import android.util.Log
import com.example.qontak.commons.TOAST_LONG

fun handleMessage(context: Context, tag: String, message: String, duration: Int = TOAST_LONG) {

    Log.d(tag, message)
    showToast(context, message, duration)

}