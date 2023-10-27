package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.CountStore

data class ResponseCountStore(
    var status: String = "",
    var message: String = "",
    var results: ArrayList<CountStore> = arrayListOf(),
)
