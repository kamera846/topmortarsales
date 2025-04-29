package com.topmortar.topmortarsales.commons.utils

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.topmortar.topmortarsales.R

class CustomProgressBar(context: Context) {

    private val dialog: Dialog = Dialog(context)
    private val messageView: TextView

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_custom_progress_bar, null)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(view)
        dialog.setCancelable(false) // default
        dialog.setCanceledOnTouchOutside(false)

        // Optional: buat dialog tidak terlalu besar
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        messageView = view.findViewById(R.id.loadingStateMessage)
    }

    fun setCancelable(cancelable: Boolean) {
        dialog.setCancelable(cancelable)
        dialog.setCanceledOnTouchOutside(cancelable)
    }

    fun setMessage(message: String) {
        messageView.text = message
    }

    fun show() {
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    fun isShowing(): Boolean = dialog.isShowing
}
