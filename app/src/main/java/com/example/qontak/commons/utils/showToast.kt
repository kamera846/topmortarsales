package com.example.qontak.commons.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.example.qontak.R

fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val layout = inflater.inflate(R.layout.toast_layout, null)

    val toastMessage = layout.findViewById<TextView>(R.id.toast_message)
    toastMessage.text = message

    val toast = Toast(context)
    toast.duration = duration
    toast.view = layout
    toast.show()
}
