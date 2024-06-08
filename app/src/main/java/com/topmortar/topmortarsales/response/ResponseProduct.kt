package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.ProductModel

data class ResponseProduct (
    val status: String = "",
    val message: String = "",
    val results: ArrayList<ProductModel> = ArrayList()
)
