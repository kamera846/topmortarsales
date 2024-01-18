package com.topmortar.topmortarsales.model

object DeliveryModel {
    data class Delivery (
        var id: String = "",
        var end_datetime: String = "",
        var end_lat: Double = 0.0,
        var end_lng: Double = 0.0,
        var lat: Double = 0.0,
        var lng: Double = 0.0,
        var start_datetime: String = "",
        var start_lat: Double = 0.0,
        var start_lng: Double = 0.0,
        var courier: Courier? = null,
        var store: Store? = null,
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
        var name: String = "",
        var lat: Double = 0.0,
        var lng: Double = 0.0,
    ) {
//        constructor() : this("")
    }
}