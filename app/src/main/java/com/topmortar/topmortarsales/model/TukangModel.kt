package com.topmortar.topmortarsales.model

data class TukangModel(
    val id_tukang: String = "",
    val nama: String = "",
    val nomorhp: String = "",
    val tgl_lahir: String = "0000-00-00",
    val nama_lengkap: String = "",
    val id_city: String = "0",
    val maps_url: String = "",
    val address: String = "",
    var tukang_status: String = "",
    var id_skill: String = "",
    var kode_skill: String = "",
    var ktp_tukang: String = "",
    var nama_skill: String = "",
    var id_catcus: String = "",
    var is_demo: String = "",
    var created_at: String = "",
    var is_valid: String = ""
)
