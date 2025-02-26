package com.topmortar.topmortarsales.commons.utils

import android.os.Build
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type
import com.topmortar.topmortarsales.R

fun AppCompatActivity.applyMyEdgeToEdge(
    isPrimary: Boolean = true
) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        enableEdgeToEdge()
    } else {
        if (isPrimary) {
            if (CustomUtility(this).isDarkMode()) {
                enableEdgeToEdge(SystemBarStyle.dark(getColor(R.color.black_300)))
            } else {
                enableEdgeToEdge(SystemBarStyle.dark(getColor(R.color.primary)))
            }
        } else {
            enableEdgeToEdge()
        }
    }

    // Set padding top sesuai dengan system bar inset
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
        val systemBars = insets.getInsets(Type.systemBars())
        val imeInsets = insets.getInsets(Type.ime())
        v.setPadding(0, systemBars.top, 0, imeInsets.bottom)
        insets
    }
}
