package com.topmortar.topmortarsales.commons.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.apache.commons.net.ntp.NTPUDPClient
import java.net.InetAddress
import java.util.Calendar

object MySntpClient {
    fun getNetworkTime(): Long? {
        return try {
            val address = InetAddress.getByName("time.google.com")
            val client = NTPUDPClient()
            client.defaultTimeout = 10000
            val info = client.getTime(address)
            info.message.transmitTimeStamp.time
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun checkTimeFromInternet(mContext: Context): Calendar? {
        return withContext(Dispatchers.IO) {
            var attempt = 0
            while (attempt < 5) {
                FirebaseUtils.firebaseLogging(mContext, "Absent", "Trying to get time from internet, with attempt $attempt")
                val networkTimeMillis = getNetworkTime()
                if (networkTimeMillis != null) {
                    return@withContext Calendar.getInstance().apply {
                        timeInMillis = networkTimeMillis
                    }
                }
                attempt++
                delay(500)
            }
            return@withContext null
        }
    }
}
