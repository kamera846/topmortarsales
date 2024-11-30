package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.ActiveStoreModel

data class ResponseActiveStore(
    val status: String = "",
    val results: ArrayList<ActiveStoreModel> = ArrayList()
)
