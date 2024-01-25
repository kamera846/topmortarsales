package com.topmortar.topmortarsales.commons.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

object DateFormat {

    fun format(calendar: Calendar, format: String = "dd MMMM yyyy"): String {
        val format = SimpleDateFormat(format, Locale.getDefault())
        return format.format(calendar.time)
    }

    fun format(dateString: String, input: String = "yyyy-MM-dd", format: String = "dd MMMM yyyy"): String {
        val inputFormat = SimpleDateFormat(input, Locale.getDefault())
        val outputFormat = SimpleDateFormat(format, Locale.getDefault())

        val date = inputFormat.parse(dateString)
        return outputFormat.format(date!!)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun now(): String {
        val waktuSekarang = LocalDateTime.now()

        // Format tampilan waktu (opsional)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return waktuSekarang.format(formatter)
    }

}