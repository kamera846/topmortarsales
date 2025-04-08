package com.topmortar.topmortarsales.commons.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.topmortar.topmortarsales.commons.TOAST_LONG

fun handleMessage(context: Context, tag: String, message: String, duration: Int = TOAST_LONG) {

    Log.d(tag, message)

    if (context is Activity) {
        context.runOnUiThread {
            showToast(context, message, duration)
        }
    } else {
        showToast(context, message, duration)
    }

}