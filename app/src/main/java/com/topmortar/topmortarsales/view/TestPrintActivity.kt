package com.topmortar.topmortarsales.view

import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.topmortar.topmortarsales.R


class TestPrintActivity : AppCompatActivity() {

    private lateinit var btnPrintText: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_print)

        btnPrintText = findViewById(R.id.btn_print)

        btnPrintText.setOnClickListener {
            executePrinter()
        }

    }

    private fun executePrinter() {
        val printer =
            EscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), 203, 48f, 32)
        printer
            .printFormattedText(
                """
                [C]<img>${
                            PrinterTextParserImg.bitmapToHexadecimalString(
                                printer,
                                this.applicationContext.resources.getDrawableForDensity(
                                    R.drawable.logo_top_mortar,
                                    DisplayMetrics.DENSITY_MEDIUM
                                )
                            )
                        }</img>
                [L]
                [C]<u><font size='big'>ORDER N°045</font></u>
                [L]
                [C]================================
                [L]
                [L]<b>BEAUTIFUL SHIRT</b>[R]9.99e
                [L]  + Size : S
                [L]
                [L]<b>AWESOME HAT</b>[R]24.99e
                [L]  + Size : 57/58
                [L]
                [C]--------------------------------
                [R]TOTAL PRICE :[R]34.98e
                [R]TAX :[R]4.23e
                [L]
                [C]================================
                [L]
                [L]<font size='tall'>Customer :</font>
                [L]Raymond DUPONT
                [L]5 rue des girafes
                [L]31547 PERPETES
                [L]Tel : +33801201456
                [L]
                [C]<barcode type='ean13' height='10'>831254784551</barcode>
                [C]<qrcode size='20'>https://dantsu.com/</qrcode>
                """.trimIndent())
    }

}