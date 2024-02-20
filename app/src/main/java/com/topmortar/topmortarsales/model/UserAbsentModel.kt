package com.topmortar.topmortarsales.model

data class UserAbsentModel(
    var eveningDateTime:String = "",
    var fullname:String = "",
    var id:String = "",
    var isOnline:Boolean = false,
    var lastSeen:String = "",
    var lastTracking:String = "",
    var lat:Double = 0.0,
    var lng:Double = 0.0,
    var morningDateTime:String = "",
    var username:String = "",
)
