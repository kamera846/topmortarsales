package com.topmortar.topmortarsales.commons.utils

import android.os.AsyncTask
import android.util.Log
import android.view.View
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.PING_HOST
import com.topmortar.topmortarsales.commons.PING_MEDIUM
import com.topmortar.topmortarsales.commons.PING_NORMAL

class PingUtility(private val indicatorImageView: View) : AsyncTask<String, Void, Int>() {

    private lateinit var pingResultInterface: PingResultInterface
    fun setInterface(pingResultInterface: PingResultInterface) {
        this.pingResultInterface = pingResultInterface
    }
    interface PingResultInterface {
        fun onPingResult(pingResult: Int? = null)
    }

    override fun doInBackground(vararg params: String?): Int {
        val host = PING_HOST // Ganti dengan host yang ingin Anda ping
        val pingCommand = "/system/bin/ping -c 1 $host"
        try {
            val process = Runtime.getRuntime().exec(pingCommand)
            return process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1
    }

    override fun onPostExecute(result: Int) {
        super.onPostExecute(result)
        updateIndicator(result)
    }

    private fun updateIndicator(pingResult: Int) {
        val resource: Int = when (pingResult) {
            PING_NORMAL -> R.drawable.status_active // Ping normal atau stabil
            PING_MEDIUM -> R.drawable.status_data // Ping tidak terlalu tinggi
            else -> R.drawable.status_passive // Ping tinggi
        }
        indicatorImageView.setBackgroundResource(resource)
        pingResultInterface.onPingResult(pingResult)
    }
}
