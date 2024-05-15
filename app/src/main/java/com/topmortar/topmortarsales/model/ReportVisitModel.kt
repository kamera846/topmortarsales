package com.topmortar.topmortarsales.model

data class ReportVisitModel(
    var id_visit: String = "",
    var id_contact: String = "",
    var distance_visit: String = "",
    var laporan_visit: String = "",
    var id_user: String = "",
    var date_visit: String = "",
    var nama: String = "",
    var nomorhp: String = "",
    var tgl_lahir: String = "",
    var store_owner: String = "",
    var id_city: String = "",
    var maps_url: String = "",
    var address: String = "",
    var store_status: String = "",
    var ktp_owner: String = "",
    var termin_payment: String = "",
    var id_promo: String = "",
    var is_approved: String = "",
    var is_deleted: String = "",
    var id_gudang: String = "",
    var nama_gudang: String = "",
    var location_gudang: String = "",
    var nomorhp_gudang: String = "",
    var source_visit: String = "",
    var approve_message: String? = null
)
