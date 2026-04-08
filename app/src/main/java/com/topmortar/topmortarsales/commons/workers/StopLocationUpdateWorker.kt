package com.topmortar.topmortarsales.commons.workers

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.topmortar.topmortarsales.commons.services.TrackingService

class StopLocationUpdateWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {

        if (!TrackingService.isRunning) return Result.success()

        val intent = Intent(applicationContext, TrackingService::class.java).apply {
            action = TrackingService.ACTION_STOP
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(intent)
        } else {
            applicationContext.startService(intent)
        }

        return Result.success()
    }
}