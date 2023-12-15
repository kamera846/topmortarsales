package com.topmortar.topmortarsales.commons.utils

import android.os.AsyncTask
import android.view.View
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.PING_HOST
import com.topmortar.topmortarsales.commons.PING_MEDIUM
import com.topmortar.topmortarsales.commons.PING_NORMAL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class PingUtility(private val indicatorImageView: View? = null) : AsyncTask<String, Void, Int>() {

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
        indicatorImageView!!.setBackgroundResource(resource)
        pingResultInterface.onPingResult(pingResult)
    }

    private var job: Job? = null
    private val pingInterval = 1000L // Waktu interval ping dalam milidetik

    interface PingResultListener {
        fun onPingResult(result: Long)
    }

    fun startPingMonitoring(host: String = PING_HOST, listener: PingResultListener) {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val pingResult = performPingAsync(host)
                withContext(Dispatchers.Main) {
                    listener.onPingResult(pingResult)
                }
                delay(pingInterval)
            }
        }
    }

    fun stopPingMonitoring() {
        job?.cancel()
    }

    private suspend fun performPingAsync(host: String): Long = withContext(Dispatchers.IO) {
        val command = "ping -c 4 $host" // Ganti -c 4 dengan jumlah ping yang diinginkan

        try {
            val process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))

            var line: String?
            var pingResult: Long = 0

            while (reader.readLine().also { line = it } != null) {
                if (line!!.contains("time=")) {
                    val startIndex = line!!.indexOf("time=") + 5
                    val endIndex = line!!.indexOf(" ms", startIndex)
                    val timeString = line!!.substring(startIndex, endIndex)
                    pingResult = timeString.toDouble().toLong()
                    break
                }
            }

            reader.close()
            process.waitFor()

            pingResult
        } catch (e: Exception) {
            e.printStackTrace()
            -1 // Mengembalikan -1 jika terjadi kesalahan
        }
    }
}
