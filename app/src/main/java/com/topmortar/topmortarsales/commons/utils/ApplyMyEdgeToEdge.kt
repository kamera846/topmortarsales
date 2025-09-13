package com.topmortar.topmortarsales.commons.utils

import android.os.Build
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type
import com.topmortar.topmortarsales.R

fun AppCompatActivity.applyMyEdgeToEdge(
    isPrimary: Boolean = true
) {

    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    } else {
        if (isPrimary) {
            if (CustomUtility(this).isDarkMode()) {
//                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
//                    enableEdgeToEdge(SystemBarStyle.dark(getColor(R.color.black_300)))
//                    ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
//                        val systemBars = insets.getInsets(Type.systemBars())
//                        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//                        insets
//                    }
//                } else {
                    window.statusBarColor = getColor(R.color.black_300)
//                }
            } else {
//                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
//                    enableEdgeToEdge(SystemBarStyle.dark(getColor(R.color.primary)))
//                    ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
//                        val systemBars = insets.getInsets(Type.systemBars())
//                        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//                        insets
//                    }
//                } else {
                    window.statusBarColor = getColor(R.color.primary)
//                }
            }
        } else {
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
//                enableEdgeToEdge()
//
//                ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
//                    val systemBars = insets.getInsets(Type.systemBars())
//                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//                    insets
//                }
//            } else {
                if (CustomUtility(this).isDarkMode()) {
                    window.statusBarColor = getColor(R.color.black_200)
                } else {
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    window.statusBarColor = getColor(R.color.white)
                }
//            }
        }
    }
}
