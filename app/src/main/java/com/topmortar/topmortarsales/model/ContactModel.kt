package com.topmortar.topmortarsales.model

data class ContactModel(
    val id_contact: String = "",
    val nama: String = "",
    val nomorhp: String = "",
    val tgl_lahir: String = "0000-00-00",
    val store_owner: String = "",
    val id_city: String = "0",
    val maps_url: String = "",
    val address: String = "",
    var store_status: String = "",
    var ktp_owner: String = "",
    var termin_payment: String = "",
    var id_promo: String = "",
)