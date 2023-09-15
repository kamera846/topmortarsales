package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.TukangModel

data class ResponseTukangList(
    val status: String,
    val results: ArrayList<TukangModel>
)
