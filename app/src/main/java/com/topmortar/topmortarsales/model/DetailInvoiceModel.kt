package com.topmortar.topmortarsales.model

data class DetailInvoiceModel(
    var id_detail_surat_jalan : String = "",
    var id_surat_jalan : String = "",
    var id_produk : String = "",
    var qty_produk : String = "",
    var is_bonus : String = "",
    var nama_produk : String = "",
    var id_city : String = "",
    var harga_produk : String = "",
)
