package com.topmortar.topmortarsales.model

data class DistributorModel(
    var id_distributor: String = "",
    var nama_distributor: String = "",
    var nomorhp_distributor: String = "",
    var alamat_distributor: String = "",
    var jenis_distributor: String = "",
) {
    override fun toString(): String {
        return nama_distributor
    }
}
