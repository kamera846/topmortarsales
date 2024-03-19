package com.topmortar.topmortarsales.commons.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
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

    fun now(): String {
        val waktuSekarang = Date()

        // Format tampilan waktu (opsional)
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return formatter.format(waktuSekarang)
    }

    fun dateAfterNow(dateString: String): Boolean {
        val calendarNow = Calendar.getInstance()

        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val targetDate = formatter.parse(dateString)

        return calendarNow.time.after(targetDate)
    }

    fun dateTimeAfterNow(dateTimeString: String): Boolean {
        val calendarNow = Calendar.getInstance()

        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val targetDate = formatter.parse(dateTimeString)

        return calendarNow.time.after(targetDate)
    }

    fun differenceDateNowDesc(dateString: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateTime = dateFormat.parse(dateString)

        val calendar = Calendar.getInstance()
        val today = calendar.time
        calendar.time = dateTime

        val difference = ((today.time - calendar.time.time) / (1000 * 60 * 60 * 24)).toInt()

        return when {
            difference == 0 -> "hari ini"
            difference == 1 -> "kemarin"
            difference > 1 -> "pada $difference hari yang lalu"
            calendar.after(today) -> "pada $difference hari mendatang"
            else -> ""
        }
    }

}