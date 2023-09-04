package com.topmortar.topmortarsales.view

import com.topmortar.topmortarsales.commons.utils.BluetoothPrinterManager
import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.REQUEST_BLUETOOTH_PERMISSIONS
import com.topmortar.topmortarsales.commons.REQUEST_ENABLE_BLUETOOTH
import com.topmortar.topmortarsales.commons.TOAST_SHORT

class TestPrintActivity : AppCompatActivity() {

    private lateinit var btnPrint: Button

    private lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_print)

        btnPrint = findViewById(R.id.btn_print)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        btnPrint.setOnClickListener { printNow() }
    }

    private fun printNow() {

        if (bluetoothAdapter.isEnabled) {
            if (hasBluetoothPermissions()) {
                if (checkPermission()) {
                    val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
                    showPrinterSelectionDialog(pairedDevices)
                }
            } else requestBluetoothPermissions()
        } else Toast.makeText(this, "Bluetooth inactive", TOAST_SHORT).show()

    }

    private fun showPrinterSelectionDialog(devices: Set<BluetoothDevice>?) {
        if (checkPermission()) {
            val deviceList = devices?.toList() ?: emptyList()
            val deviceNames = deviceList.map { it.name }.toTypedArray()

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pilih Perangkat Printer")
            builder.setItems(deviceNames) { _, which ->
                val selectedDevice = deviceList[which]
                executePrinter(selectedDevice)
                Toast.makeText(this, "Terhubung dengan: ${ selectedDevice.name }", TOAST_SHORT).show()
            }
            builder.show()
        }
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasBluetoothPermissions(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestBluetoothPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            ), REQUEST_BLUETOOTH_PERMISSIONS
        )
    }

    private fun executePrinter(device: BluetoothDevice) {
        val printerManager = BluetoothPrinterManager()
        printerManager.setContext(this)
        val textLeft = "Selamat datang"
        val textCenter = "di"
        val textRight = "Top Mortar"
        val textLeftAligned = "Welcome"
        val textRightAligned = "Friend"

        val data = ArrayList<ByteArray>()

        // Format teks "Welcome" dengan rata kiri
        data.add(printerManager.textLeft(textLeft))
        data.add(printerManager.textCenter(textCenter))
        data.add(printerManager.textRight(textRight))
        data.add(printerManager.textBetween(textLeftAligned, textRightAligned))
        data.add(printerManager.textLeft(textLeft))
        data.add(printerManager.textCenter(textCenter))
        data.add(printerManager.textRight(textRight))
        data.add(printerManager.textBetween(textLeftAligned, textRightAligned))
        data.add(printerManager.textLeft(textLeft))
        data.add(printerManager.textCenter(textCenter))
        data.add(printerManager.textRight(textRight))
        data.add(printerManager.textBetween(textLeftAligned, textRightAligned))
        data.add(printerManager.textBetween(textLeftAligned, textRightAligned))
        data.add(printerManager.textEnter(5))

        printerManager.connectToDevice(device, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                if (checkPermission()) {
                    val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
                    showPrinterSelectionDialog(pairedDevices)
                    return
                }
            }
        }
        Toast.makeText(this, "Request permission denied", TOAST_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) Toast.makeText(this, "PRINT NOW", TOAST_SHORT).show()
            else Toast.makeText(this, "Bluetooth still inactive", TOAST_SHORT).show()
        }
    }

}