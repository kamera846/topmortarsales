package com.topmortar.topmortarsales.model

object DeliveryModel {
    data class Delivery (
        var id: String = "",
        var lat: Double = 0.0,
        var lng: Double = 0.0,
        var courier: Courier? = null,
//        var stores: ArrayList<Store>? = null,
    ) {
//        constructor() : this("")
    }
    data class Courier (
        var id: String = "",
        var name: String = ""
    ) {
//        constructor() : this("")
    }
    data class Store (
        var id: String = "",
        var deliveryId: String = "",
        var name: String = "",
        var lat: Double = 0.0,
        var lng: Double = 0.0,
        var endDatetime: String = "",
        var endLat: Double = 0.0,
        var endLng: Double = 0.0,
        var startDatetime: String = "",
        var startLat: Double = 0.0,
        var startLng: Double = 0.0,
        var courier: Courier? = null
    ) {
//        constructor() : this("")
    }

    data class History (
        var id_delivery: String = "",
        var endDatetime: String = "",
        var endLat: String = "",
        var endLng: String = "",
        var lat: String = "",
        var lng: String = "",
        var id_courier: String = "",
        var id_contact: String = "",
        var startDatetime: String = "",
        var startLat: String = "",
        var startLng: String = "",
        var id_user: String = "",
        var full_name: String = "",
        var username: String = "",
        var password: String = "",
        var level_user: String = "",
        var id_city: String = "",
        var phone_user: String = "",
        var bid_limit: String = "",
        var id_distributor: String = "",
        var is_sales: String = "",
        var is_notify: String = "",
    )
}