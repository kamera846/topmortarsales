package com.topmortar.topmortarsales.commons.utils

import android.text.InputFilter
import android.widget.EditText
import android.widget.TextView

object CustomEtHandler {

    fun EditText.setMaxLength(maxLength: Int) {

        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))

    }

    fun updateTxtMaxLength(tv: TextView, maxLength: Int, length: Int) {
        tv.text = "$length/$maxLength"
    }

}