package com.topmortar.topmortarsales.commons.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.Window
import android.view.WindowManager

fun changeStatusBarColor(context: Context ,color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val window: Window = (context as Activity).window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = context.getColor(color)
    }
}