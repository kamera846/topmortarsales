package com.topmortar.topmortarsales.commons.utils

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
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

                        // Tambahkan kode untuk menangani respons atau tindakan setelah mencetak
                    } catch (e: Exception) {
                        // Tangani jika terjadi kesalahan pada saat menghubungkan atau mencetak
                    }
                }
            }
        }
    }
}
