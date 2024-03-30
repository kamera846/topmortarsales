package com.topmortar.topmortarsales.model

data class RencanaVisitModel(
    var id_rencana_visit: String = "",
    var id_contact: String = "",
    var id_surat_jalan: String = "",
    var is_visited: String = "",
    var visit_date: String? = null,
    var created_at: String = "",
    var created_at_store: String = "",
    var updated_at: String? = null,
    var type_rencana: String = "",
    var id_distributor: String = "",
    var id_invoice: String = "",
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
    var reputation: String = "",
    var payment_method: String = "",
    var date_invoice: String = ""
)
