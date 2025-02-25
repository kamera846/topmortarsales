package com.topmortar.topmortarsales.commons.utils

import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import com.topmortar.topmortarsales.R
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.applyMyEdgeToEdge(
    isPrimary: Boolean = true
) {

    if (isPrimary) {
        if (CustomUtility(this).isDarkMode()) {
            enableEdgeToEdge(SystemBarStyle.dark(getColor(R.color.black_300)))
        } else {
            enableEdgeToEdge(SystemBarStyle.dark(getColor(R.color.primary)))
        }
    } else {
        enableEdgeToEdge()
    }

    // Set padding top sesuai dengan system bar inset
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
        val systemBars = insets.getInsets(Type.systemBars())
        val imeInsets = insets.getInsets(Type.ime())
        v.setPadding(0, systemBars.top, 0, imeInsets.bottom)
        insets
    }
}
