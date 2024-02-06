package com.topmortar.topmortarsales.commons.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.LocalDate
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun dateAfterNow(dateString: String): Boolean {
        val dateNow = LocalDate.now()

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val targetDate = LocalDate.parse(dateString, formatter)

        return dateNow.isAfter(targetDate)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun dateTimeAfterNow(dateTimeString: String): Boolean {
        val dateNow = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val targetDate = LocalDateTime.parse(dateTimeString, formatter)

        return dateNow.isAfter(targetDate)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun differenceDateNowDesc(dateString: String): String {

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDateTime.parse(dateString, formatter)

        val localDate = dateTime.toLocalDate()
        val today = LocalDate.now()

        val difference = today.toEpochDay() - localDate.toEpochDay()

        return when {
            difference == 0L -> "hari ini"
            difference == 1L -> "kemarin"
            difference > 1L -> "pada $difference hari yang lalu"
            localDate.isAfter(today) -> "pada $difference hari mendatang"
            else -> ""
        }

    }

}