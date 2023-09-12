package com.topmortar.topmortarsales.view.invoice

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.InvoiceOrderRecyclerViewAdapter
import com.topmortar.topmortarsales.commons.CONST_INVOICE_ID
import com.topmortar.topmortarsales.commons.CONST_URI
import com.topmortar.topmortarsales.commons.IMG_PREVIEW_STATE
import com.topmortar.topmortarsales.commons.REQUEST_BLUETOOTH_PERMISSIONS
import com.topmortar.topmortarsales.commons.REQUEST_ENABLE_BLUETOOTH
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.utils.BluetoothPrinterManager
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.model.DetailInvoiceModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailInvoiceActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    private lateinit var icBack: ImageView
    private lateinit var icSyncNow: ImageView
    private lateinit var tvTitleBar: TextView

    private lateinit var container: RelativeLayout

    private lateinit var previewInvoiceLayout: ScrollView
    private lateinit var tvReferenceNumber: TextView
    private lateinit var tvDeliveryDate: TextView
    private lateinit var tvShipToName: TextView
    private lateinit var tvShipToAddress: TextView
    private lateinit var tvShipToPhone: TextView
    private lateinit var tvDeliveryOrderDate: TextView
    private lateinit var tvDeliveryOrderNumber: TextView
    private lateinit var rvOrderList: RecyclerView
    private lateinit var tvCourier: TextView
    private lateinit var tvVehicle: TextView
    private lateinit var tvVehicleNumber: TextView
    private lateinit var tvReceivedBy: TextView
    private lateinit var tvReceivedDate: TextView

    private lateinit var btnPrint: Button
    private lateinit var btnClosing: Button
    private lateinit var btnBottomAction: LinearLayout
    private lateinit var lnrClosing: LinearLayout
    private lateinit var txtLoading: TextView

    private var invoiceId: String? = null
    private var isClosing: Boolean = false

    private var cameraPermissionLauncher: ActivityResultLauncher<String>? = null
    private var imagePicker: ActivityResultLauncher<Intent>? = null
    private var selectedUri: Uri? = null
    private var currentPhotoUri: Uri? = null
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var printerManager = BluetoothPrinterManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)

        setContentView(R.layout.activity_detail_invoice)

        initVariable()
        initClickHandler()
        dataActivityValidation()

    }

    private fun initVariable() {

        icBack = findViewById(R.id.ic_back)
        icSyncNow = findViewById(R.id.ic_sync_now)
        tvTitleBar = findViewById(R.id.tv_title_bar)

        container = findViewById(R.id.container)

        previewInvoiceLayout = findViewById(R.id.preview_invoice_layout)
        tvReferenceNumber = previewInvoiceLayout.findViewById(R.id.tv_reference_number)
        tvDeliveryDate = previewInvoiceLayout.findViewById(R.id.tv_delivery_date)
        tvShipToName = previewInvoiceLayout.findViewById(R.id.tv_ship_to_name)
        tvShipToAddress = previewInvoiceLayout.findViewById(R.id.tv_ship_to_address)
        tvShipToPhone = previewInvoiceLayout.findViewById(R.id.tv_ship_to_phone)
        tvDeliveryOrderDate = previewInvoiceLayout.findViewById(R.id.tv_delivery_order_date)
        tvDeliveryOrderNumber = previewInvoiceLayout.findViewById(R.id.tv_delivery_order_number)
        rvOrderList = previewInvoiceLayout.findViewById(R.id.rv_order_list)
        tvCourier = previewInvoiceLayout.findViewById(R.id.tv_courier)
        tvVehicle = previewInvoiceLayout.findViewById(R.id.tv_vehicle)
        tvVehicleNumber = previewInvoiceLayout.findViewById(R.id.tv_vehicle_number)
        tvReceivedBy = previewInvoiceLayout.findViewById(R.id.tv_received_by)
        tvReceivedDate = previewInvoiceLayout.findViewById(R.id.tv_received_date)

        btnPrint = findViewById(R.id.btn_print_invoice)
        btnClosing = findViewById(R.id.btn_closing)
        btnBottomAction = findViewById(R.id.bottom_action)
        lnrClosing = findViewById(R.id.lnr_closing)
        txtLoading = findViewById(R.id.txt_loading)

        // Setup Title Bar
        icSyncNow.visibility = View.VISIBLE
        tvTitleBar.text = "Detail Surat Jalan"
        tvTitleBar.setPadding(0, 0, convertDpToPx(16, this), 0)

        // Setup Printer
        if (sessionManager.userKind() == USER_KIND_COURIER) {
            btnBottomAction.visibility = View.VISIBLE
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            printerManager.setContext(this)
        }

        // Setup Image Picker
        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                chooseFile()
            } else {
                handleMessage(this@DetailInvoiceActivity, "CAMERA ACCESS DENIED", "Permission camera denied")
            }
        }
        imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                selectedUri = if (data == null || data.data == null) currentPhotoUri else data.data
                navigateToPreviewClosing()
            }
        }

    }

    private fun navigateToPreviewClosing() {
        val uriList = ArrayList<Uri>()
        selectedUri?.let { uriList.add(it) }

        val intent = Intent(this, PreviewClosingActivity::class.java)
        intent.putExtra(CONST_INVOICE_ID, invoiceId)
        intent.putParcelableArrayListExtra(CONST_URI, uriList)
        startActivityForResult(intent, IMG_PREVIEW_STATE)

    }

    private fun initClickHandler() {
        icBack.setOnClickListener { backHandler() }
        icSyncNow.setOnClickListener { getDetail() }
        btnPrint.setOnClickListener { printNow() }
        btnClosing.setOnClickListener { chooseFile() }
        lnrClosing.setOnClickListener { chooseFile() }
    }

    private fun dataActivityValidation() {

        val iInvoiceId = intent.getStringExtra(CONST_INVOICE_ID)

        if (!iInvoiceId.isNullOrEmpty() ) {
            invoiceId = iInvoiceId.toString()
        }

        getDetail()

    }

    private fun getDetail() {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getInvoicesDetail(processNumber = "3", invoiceId = invoiceId!!)

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        val data = response.results[0]
                        isClosing = data.is_closing == "1"

                        tvReferenceNumber.text = "${ data.no_surat_jalan }"
//                        tvDeliveryDate.text = "${ data.dalivery_date }"
                        tvShipToName.text = "${ data.ship_to_name }"
                        tvShipToAddress.text = "${ data.ship_to_address }"
                        tvShipToPhone.text = "${ data.ship_to_phone }"
                        tvDeliveryOrderDate.text = "Delivery Date: ${ data.dalivery_date }"
                        tvDeliveryOrderNumber.text = "Printed Date: " + isClosing.let { if (it) data.date_printed else "-" }
                        tvCourier.text = "Kurir: ${ data.courier_name }"
                        tvVehicle.text = "Kendaraan: ${ data.nama_kendaraan }"
                        tvVehicleNumber.text = "No. Polisi: ${ data.nopol_kendaraan }"
                        if (isClosing) {
                            tvReceivedBy.visibility = View.VISIBLE
                            tvReceivedDate.visibility = View.VISIBLE
                            tvReceivedBy.text = "${ data.ship_to_name }"
                            tvReceivedDate.text = "Already closed at ${ data.date_closing }"
                            btnBottomAction.visibility = View.GONE
                        }

                        setRecyclerView(response.results[0].details.let { if (it.isNullOrEmpty()) arrayListOf() else it })
                        loadingState(false)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Detail surat jalan kosong!")

                    }
                    else -> {

                        handleMessage(this@DetailInvoiceActivity, TAG_RESPONSE_CONTACT, "Failed get data")
                        loadingState(true, getString(R.string.failed_request))

                    }
                }

            } catch (e: Exception) {

                handleMessage(this@DetailInvoiceActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))

            }

        }

    }


    private fun setRecyclerView(listItem: ArrayList<DetailInvoiceModel>) {
        val rvAdapter = InvoiceOrderRecyclerViewAdapter()
        rvAdapter.setListItem(listItem)

        rvOrderList.apply {
            layoutManager = LinearLayoutManager(this@DetailInvoiceActivity)
            adapter = rvAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                private var lastScrollPosition = 0

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy < 0) {
                        // Scrolled up
                        val firstVisibleItemPosition =
                            (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        if (lastScrollPosition != firstVisibleItemPosition) {
                            recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition)?.itemView?.startAnimation(
                                AnimationUtils.loadAnimation(
                                    recyclerView.context,
                                    R.anim.rv_item_fade_slide_down
                                )
                            )
                            lastScrollPosition = firstVisibleItemPosition
                        }
                    } else lastScrollPosition = -1
                }
            })
        }
    }

    private fun loadingState(state: Boolean, message: String = getString(R.string.txt_loading)) {

        txtLoading.text = message

        if (state) {

            txtLoading.visibility = View.VISIBLE
            container.visibility = View.GONE

        } else {

            txtLoading.visibility = View.GONE
            container.visibility = View.VISIBLE

        }

    }

    private fun printingState(state: Boolean) {

        if (state) {

            btnPrint.text = "Printing..."
            btnPrint.isEnabled = false

        } else {

            btnPrint.text = "Print"
            btnPrint.isEnabled = true

        }

    }

    private fun chooseFile() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            // Create a file to store the captured image
            val photoFile: File? = createImageFile()

            if (photoFile != null) {
                val photoUri: Uri = FileProvider.getUriForFile(this, "com.topmortar.topmortarsales.fileprovider", photoFile)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                currentPhotoUri = photoUri
            }

            val chooserIntent = Intent.createChooser(galleryIntent, "Select Image")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

            imagePicker!!.launch(chooserIntent)
        } else {
            cameraPermissionLauncher!!.launch(Manifest.permission.CAMERA)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir("Invoices")
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun backHandler() {
        finish()
    }

    private fun printNow() {

        if (hasBluetoothPermissions()) {
            if (bluetoothAdapter.isEnabled) {
                if (checkPermission()) {
                    val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
                    showPrinterSelectionDialog(pairedDevices)
                }
            } else {
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
            }
        } else requestBluetoothPermissions()

    }

    private fun showPrinterSelectionDialog(devices: Set<BluetoothDevice>?) {
        if (checkPermission()) {
            val deviceList = devices?.toList() ?: emptyList()
            val deviceNames = deviceList.map { it.name }.toTypedArray()

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select printer device")
            builder.setItems(deviceNames) { _, which ->
                val selectedDevice = deviceList[which]
                executePrinter(selectedDevice)
//                printEscPos()
                Toast.makeText(this, "Try to connect with: ${ selectedDevice.name }", TOAST_SHORT).show()
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

        // Enter new line in the beginning
        val gap = 1
        val starterBytes = ArrayList<ByteArray>()
        starterBytes.add(printerManager.resetFormat())
        starterBytes.add(printerManager.textEnter(gap))
        printerManager.connectToDevice(device, starterBytes)

        printingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.printInvoice(invoiceId = createPartFromString(invoiceId!!))

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        val data = response.results[0]
                        val orders = data.details.let { if (it.isNullOrEmpty()) arrayListOf() else it }

                        val txtReferenceNumber = "${ data.no_surat_jalan }"
//                        val txtDeliveryDate = "${ data.dalivery_date }"
                        val txtShipToName = "${ data.ship_to_name }"
                        val txtShipToAddress = "${ data.ship_to_address }"
                        val txtShipToPhone = "${ data.ship_to_phone }"
                        val txtDeliveryOrderDate = "Delivery Date: ${ data.dalivery_date }"
                        val txtPrintedDate = "Printed Date: ${ data.date_printed }"
                        val txtCourier = "Kurir: ${ data.courier_name }"
                        val txtVehicle = "Kendaraan: ${ data.nama_kendaraan }"
                        val txtVehicleNumber = "No. Polisi: ${ data.nopol_kendaraan }"

                        val bytes = ArrayList<ByteArray>()
                        bytes.add(printerManager.textLeft("~"))
                        bytes.add(printerManager.textEnter(gap*8))
                        bytes.add(printerManager.textCenter(txtReferenceNumber))
                        bytes.add(printerManager.textEnter(gap))
                        bytes.add(printerManager.textCenter("Distributor Indonesia"))
                        bytes.add(printerManager.textCenter("PT. TOP MORTAR INDONESIA"))
//                        bytes.add(printerManager.textCenter(txtDeliveryDate))
                        bytes.add(printerManager.textEnter(gap))
                        bytes.add(printerManager.textLeft("Shipped to:"))
                        bytes.add(printerManager.textLeft(txtShipToName))
                        bytes.add(printerManager.textLeft(txtShipToAddress))
                        bytes.add(printerManager.textLeft(txtShipToPhone))
                        bytes.add(printerManager.textEnter(gap))
                        bytes.add(printerManager.textLeft("Delivery Order"))
                        bytes.add(printerManager.textLeft(txtDeliveryOrderDate))
                        bytes.add(printerManager.textLeft(txtPrintedDate))
                        bytes.add(printerManager.textEnter(gap))
                        bytes.add(printerManager.textBetween("Daftar Pesanan", "Qty"))
                        orders.forEach {
                            bytes.add(printerManager.textBetween(it.nama_produk, it.qty_produk))
                            if (it.is_bonus == "1") {
                                bytes.add(printerManager.textLeft("Free"))
                            }
                            bytes.add(printerManager.textEnter(gap))
                        }
                        bytes.add(printerManager.textLeft("Description"))
                        bytes.add(printerManager.textLeft(txtCourier))
                        bytes.add(printerManager.textLeft(txtVehicle))
                        bytes.add(printerManager.textLeft(txtVehicleNumber))
                        bytes.add(printerManager.textEnter(gap))
                        bytes.add(printerManager.textLeft("Received By:"))
                        bytes.add(printerManager.textEnter(gap*8))
                        bytes.add(printerManager.textCenterBoldUnderline(txtShipToName))
                        bytes.add(printerManager.textEnter(gap*2))
                        bytes.add(printerManager.textLeft("~"))
                        bytes.add(printerManager.textEnter(gap*2))

                        Handler().postDelayed({
                            printerManager.connectToDevice(device, bytes)
                            Handler().postDelayed({
                                printingState(false)
                            }, 1000)
                        }, 1000)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        handleMessage(this@DetailInvoiceActivity, TAG_RESPONSE_CONTACT, "Failed to print: Detail surat jalan kosong!")
                        printingState(false)

                    }
                    else -> {

                        handleMessage(this@DetailInvoiceActivity, TAG_RESPONSE_CONTACT, "Failed to print")
                        printingState(false)

                    }
                }

            } catch (e: Exception) {

                handleMessage(this@DetailInvoiceActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                printingState(false)

            }

        }
    }

    // Testing
    private fun printEscPos() {

        val printersConnections = BluetoothPrintersConnections.selectFirstPaired()
        val printer = EscPosPrinter(printersConnections, 203, 70f, 48)

        // Change the desired width and height for your image (in pixels)
        val desiredWidth = 200*3 // Adjust this to your desired width
        val desiredHeight = 103*3 // Adjust this to your desired height

        val drawable = this.applicationContext.resources.getDrawableForDensity(
            R.drawable.logo_top_mortar,
            DisplayMetrics.DENSITY_MEDIUM
        )

        // Scale the drawable to the desired dimensions
        val scaledDrawable = scaleDrawable(drawable!!, desiredWidth, desiredHeight)

        // Convert the scaled drawable to hexadecimal string and print it
        val imageHexadecimal = PrinterTextParserImg.bitmapToHexadecimalString(printer, scaledDrawable)

        printer.printFormattedText(
            "[C]<img>$imageHexadecimal</img>\n" +
            "[L]\n" +
            "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
            "[L]\n" +
            "[C]" + "=".repeat(48) + "\n" +
            "[L]\n" +
            "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99e\n" +
            "[L]  + Size : S\n" +
            "[L]\n"
//            "[L]<b>AWESOME HAT</b>[R]24.99e\n" +
//            "[L]  + Size : 57/58\n" +
//            "[L]\n" +
//            "[C]--------------------------------\n" +
//            "[R]TOTAL PRICE :[R]34.98e\n" +
//            "[R]TAX :[R]4.23e\n" +
//            "[L]\n" +
//            "[C]================================\n" +
//            "[L]\n" +
//            "[L]<font size='tall'>Customer :</font>\n" +
//            "[L]Raymond DUPONT\n" +
//            "[L]5 rue des girafes\n" +
//            "[L]31547 PERPETES\n" +
//            "[L]Tel : +33801201456\n" +
//            "[L]\n" +
//            "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
//            "[C]<qrcode size='20'>https://dantsu.com/</qrcode>"
        )
    }

    private fun scaleDrawable(drawable: Drawable, width: Int, height: Int): Drawable {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return BitmapDrawable(this.applicationContext.resources, bitmap)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                printNow()
                return
            } else {
                if (bluetoothAdapter.isEnabled) {
                    if (checkPermission()) {
                        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
                        showPrinterSelectionDialog(pairedDevices)
                    }
                } else {
                    val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
                }
            }
//            Toast.makeText(this, "Request permission denied", TOAST_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) printNow()
            else Toast.makeText(this, "Bluetooth still inactive", TOAST_SHORT).show()
        } else if (requestCode == IMG_PREVIEW_STATE || "$requestCode" == "$IMG_PREVIEW_STATE") {
            val resultData = data?.getIntExtra("$IMG_PREVIEW_STATE", 0)
            if (resultData == RESULT_OK ) {
                getDetail()
            }
            // Remove image temp
            currentPhotoUri?.let {
                val contentResolver = contentResolver
                contentResolver.delete(it, null, null)
            }
        }
    }

    override fun onBackPressed() {
        backHandler()
    }


}