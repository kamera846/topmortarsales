package com.topmortar.topmortarsales.commons.utils

import org.apache.commons.net.ntp.NTPUDPClient
import java.net.InetAddress

object SntpClient {
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
}
