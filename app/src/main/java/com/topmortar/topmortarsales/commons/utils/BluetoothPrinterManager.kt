package com.topmortar.topmortarsales.commons.utils

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList

class BluetoothPrinterManager {

    private var context: Context? = null

    fun setContext(context: Context) {
        this.context = context
    }
    fun textLeft(text: String): ByteArray {
        return (text + "\n").toByteArray()
    }
    fun textRight(text: String): ByteArray {
        val spacesBeforeRight = 48 - text.length
        return (" ".repeat(spacesBeforeRight) + text + "\n").toByteArray()
    }
    fun textCenter(text: String): ByteArray {
        val spacesBeforeCenter = (48 - text.length) / 2
        return (" ".repeat(spacesBeforeCenter) + text + "\n").toByteArray()
    }
    fun textCenterBoldUnderline(text: String): ByteArray {
        val spacesBeforeCenter = (48 - text.length) / 2
        val boldCommand = byteArrayOf(0x1B, 0x45, 0x01) // ESC E n (n = 1: Bold)
        val resetBoldCommand = byteArrayOf(0x1B, 0x45, 0x00) // ESC E n (n = 0: Reset Bold)
        val underlineCommand = byteArrayOf(0x1B, 0x2D, 0x01) // ESC - n (n = 1: Underline)
        val resetUnderlineCommand = byteArrayOf(0x1B, 0x2D, 0x00) // ESC - n (n = 0: Underline)
        val text = (" ".repeat(spacesBeforeCenter) + text + "\n").toByteArray()
        return boldCommand + underlineCommand + text + resetBoldCommand + resetUnderlineCommand
    }
    fun textBetween(textLeft: String, textRight: String): ByteArray {
        val spacesBeforeRightAligned = 48 - textLeft.length - textRight.length
        return (textLeft + " ".repeat(spacesBeforeRightAligned) + textRight + "\n").toByteArray()
    }
    fun textEnter(repeat: Int): ByteArray {
        return ("\n".repeat(repeat)).toByteArray()
    }

    fun connectToDevice(device: BluetoothDevice, data: ArrayList<ByteArray>) {
        GlobalScope.launch(Dispatchers.IO) {
            if (ActivityCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val socket: BluetoothSocket? =
                    device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                socket?.use { bluetoothSocket ->
                    try {
                        bluetoothSocket.connect()

                        val outputStream: OutputStream = bluetoothSocket.outputStream
                        for (i in 0 until data.size) {
                            outputStream.write(data[i])
                        }
                        outputStream.flush()

                    } catch (e: Exception) {
//                        handleMessage(context!!, "FAILED CONNECT PRINTER", "Failed to connect ${ device.name }. Please try again!")
//                        Log.e("FAILED CONNECT PRINTER", "Stacktrace: ${ e.stackTrace }")
//                        Log.e("FAILED CONNECT PRINTER", "Error Message: ${ e.message }")

                    }

                }

            }

        }

    }

}
