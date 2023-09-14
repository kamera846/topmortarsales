package com.topmortar.topmortarsales.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.topmortar.topmortarsales.R

//
//import com.topmortar.topmortarsales.commons.utils.BluetoothPrinterManager
//import android.Manifest
//import android.app.Activity
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.graphics.drawable.Drawable
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.widget.Button
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.mazenrashed.printooth.Printooth
//import com.mazenrashed.printooth.data.printable.ImagePrintable
//import com.mazenrashed.printooth.data.printable.Printable
//import com.mazenrashed.printooth.data.printable.RawPrintable
//import com.mazenrashed.printooth.data.printable.TextPrintable
//import com.mazenrashed.printooth.data.printer.DefaultPrinter
//import com.mazenrashed.printooth.ui.ScanningActivity
//import com.mazenrashed.printooth.utilities.Printing
//import com.mazenrashed.printooth.utilities.PrintingCallback
//import com.squareup.picasso.Picasso
//import com.squareup.picasso.Target
//import com.topmortar.topmortarsales.R
//import com.topmortar.topmortarsales.commons.REQUEST_BLUETOOTH_PERMISSIONS
//import com.topmortar.topmortarsales.commons.REQUEST_ENABLE_BLUETOOTH
//import com.topmortar.topmortarsales.commons.TOAST_SHORT
//import java.lang.Exception
//
class TestPrintooth : AppCompatActivity() {
    //class TestPrintActivity : AppCompatActivity(), PrintingCallback {
//
//    private lateinit var btnPrint: Button
//    private lateinit var btnPrintImage : Button
//    private lateinit var btnPiarUnpair: Button
//
//    internal var printing: Printing? = null
//
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_print)
//
//        btnPrint = findViewById(R.id.btn_print)
//        btnPrintImage = findViewById(R.id.btn_print_image)
//        btnPiarUnpair = findViewById(R.id.btn_piar_unpair)
//
//        initView()
//
    }
//
//    private fun initView() {
//
//        if (printing != null) printing!!.printingCallback = this
//
//        btnPiarUnpair.setOnClickListener {
//            if(Printooth.hasPairedPrinter()) Printooth.removeCurrentPrinter()
//            else {
//                startActivityForResult(Intent(this, ScanningActivity::class.java), ScanningActivity.SCANNING_FOR_PRINTER)
//                changePairUnpair()
//            }
//        }
//
//        btnPrintImage.setOnClickListener {
//            if(Printooth.hasPairedPrinter()) Printooth.removeCurrentPrinter()
//            else {
//                startActivityForResult(Intent(this, ScanningActivity::class.java), ScanningActivity.SCANNING_FOR_PRINTER)
//                printImage()
//            }
//        }
//
//        btnPrint.setOnClickListener {
//            if(Printooth.hasPairedPrinter()) Printooth.removeCurrentPrinter()
//            else {
//                startActivityForResult(Intent(this, ScanningActivity::class.java), ScanningActivity.SCANNING_FOR_PRINTER)
//                printText()
//            }
//        }
//
//    }
//
//    private fun printText() {
//        val printables = ArrayList<Printable>()
//        printables.add(RawPrintable.Builder(byteArrayOf(27,100,4)).build())
//
//        printables.add(TextPrintable.Builder()
//            .setText("Hello World")
//            .setCharacterCode(DefaultPrinter.CHARCODE_PC1252)
//            .setNewLinesAfter(1)
//            .build()
//        )
//
//        printables.add(TextPrintable.Builder()
//            .setText("Hello World")
//            .setLineSpacing(DefaultPrinter.LINE_SPACING_60)
//            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
//            .setEmphasizedMode(DefaultPrinter.EMPHASIZED_MODE_BOLD)
//            .setUnderlined(DefaultPrinter.UNDERLINED_MODE_ON)
//            .setCharacterCode(DefaultPrinter.CHARCODE_PC1252)
//            .setNewLinesAfter(1)
//            .build()
//        )
//
//        printing!!.print(printables)
//    }
//
//    private fun printImage() {
//        val printables = ArrayList<Printable>()
//
//        Picasso.get().load("https://cdn3.vectorstock.com/i/1000x1000/29/72/urban-logo-template-city-skyline-silhouette-vector-27622972.jpg")
//            .into(object:Target{
//                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
//                    printables.add(ImagePrintable.Builder(bitmap!!).build())
//                    printing!!.print(printables)
//                }
//
//                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
//
//                }
//
//                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
//
//                }
//
//            })
//    }
//
//    private fun changePairUnpair() {
//
//        if (Printooth.hasPairedPrinter()) btnPiarUnpair.text = "Unpair ${Printooth.getPairedPrinter()!!.name}"
//        else btnPiarUnpair.text = "Pair With Printer!"
//
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == ScanningActivity.SCANNING_FOR_PRINTER && resultCode == Activity.RESULT_OK) initPrinting()
//        changePairUnpair()
//    }
//
//    private fun initPrinting() {
//        if (Printooth.hasPairedPrinter()) printing = Printooth.printer()
//        else printing!!.printingCallback = this
//    }
//
//    override fun connectingWithPrinter() { Toast.makeText(this, "Connecting to printer", TOAST_SHORT).show() }
//
//    override fun connectionFailed(error: String) { Toast.makeText(this, "Failed: $error", TOAST_SHORT).show() }
//
//    override fun disconnected() { Toast.makeText(this, "Disconnected", TOAST_SHORT).show() }
//
//    override fun onError(error: String) { Toast.makeText(this, "Error: $error", TOAST_SHORT).show() }
//
//    override fun onMessage(message: String) { Toast.makeText(this, "Message: $message", TOAST_SHORT).show() }
//
//    override fun printingOrderSentSuccessfully() { Toast.makeText(this, "Order sent to printer", TOAST_SHORT).show() }
//
}