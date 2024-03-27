package com.topmortar.topmortarsales.commons.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormat {

    fun format(calendar: Calendar, format: String = "dd MMMM yyyy"): String {
        val formatResult = SimpleDateFormat(format, Locale.getDefault())
        return formatResult.format(calendar.time)
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
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(waktuSekarang)
    }

    fun dateAfterNow(dateString: String): Boolean {
        val dateCalendarNow = Calendar.getInstance()
        dateCalendarNow.set(Calendar.HOUR_OF_DAY, 0)
        dateCalendarNow.set(Calendar.MINUTE, 0)
        dateCalendarNow.set(Calendar.SECOND, 0)
        dateCalendarNow.set(Calendar.MILLISECOND, 0)
        val dateNow = dateCalendarNow.time

        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        formatter.isLenient = false // Set lenient to false to ensure strict parsing
        val targetDate = formatter.parse(dateString)

        // Reset waktu pada targetDate menjadi 00:00:00 untuk membandingkan hanya tanggal
        val calendarNow = Calendar.getInstance()
        calendarNow.time = targetDate!!
        calendarNow.set(Calendar.HOUR_OF_DAY, 0)
        calendarNow.set(Calendar.MINUTE, 0)
        calendarNow.set(Calendar.SECOND, 0)
        calendarNow.set(Calendar.MILLISECOND, 0)

        return dateNow.after(calendarNow.time)
    }

//    fun dateTimeAfterNow(dateTimeString: String): Boolean {
//        val calendarNow = Calendar.getInstance()
//
//        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//        val targetDate = formatter.parse(dateTimeString)
//
//        return calendarNow.time.after(targetDate)
//    }

    fun differenceDateNowDesc(dateString: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateTime = dateFormat.parse(dateString)!!

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

    fun differenceDateNowDescCustom(dateString: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateTime = dateFormat.parse(dateString)!!

        val calendar = Calendar.getInstance()
        val today = calendar.time
        calendar.time = dateTime

        val difference = ((today.time - calendar.time.time) / (1000 * 60 * 60 * 24)).toInt()

        return when {
            difference == 0 -> "hari ini"
            difference == 1 -> "kemarin"
            difference > 1 -> "$difference hari"
            calendar.after(today) -> "$difference hari"
            else -> ""
        }
    }

    fun changeDateToDaysBeforeOrAfter(dateString: String, totalDays: Int, inputDateFormat: String = "yyyy-MM-dd HH:mm:ss", outputDateFormat: String = "dd MMMM yyyy, HH.mm"): String {
        val dateFormat = SimpleDateFormat(inputDateFormat, Locale.getDefault())

        return try {
            val date = dateFormat.parse(dateString)

            val calendar = Calendar.getInstance()
            calendar.time = date!!

            calendar.add(Calendar.DAY_OF_YEAR, totalDays)

            SimpleDateFormat(outputDateFormat, Locale.getDefault()).format(calendar.time)
        } catch (e: Exception) {
            println("Terjadi kesalahan: ${e.message}")
            dateString
        }
    }

}