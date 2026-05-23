package com.topmortar.topmortarsales.response

data class WorldTimeResponse(
    val date_time: String,
    val date: String,
    val time: String,
    val day_of_week: String,
    val dst_active: String,
    val timezone: String,
    val utc_offset_seconds: String,
)