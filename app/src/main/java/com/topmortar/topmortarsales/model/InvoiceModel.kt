package com.topmortar.topmortarsales.model

data class InvoiceModel(
    var id_surat_jalan : String = "",
    var no_surat_jalan : String = "",
    var id_contact : String = "",
    var dalivery_date : String = "",
    var order_number : String = "",
    var ship_to_name : String = "",
    var ship_to_address : String = "",
    var ship_to_phone : String = "",
    var id_courier : String = "",
    var courier_name : String = "",
    var nama_kendaraan : String = "",
    var nopol_kendaraan : String = "",
    var details : ArrayList<DetailInvoiceModel> = ArrayList(),
)
