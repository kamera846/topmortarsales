package com.topmortar.topmortarsales.commons.utils

import android.content.Context

fun convertDpToPx (dp: Int, context: Context): Int {
    val scale = context.resources.displayMetrics.density
    return (dp * scale + 0.5f).toInt()
}