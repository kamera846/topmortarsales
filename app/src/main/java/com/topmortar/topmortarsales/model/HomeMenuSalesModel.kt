package com.topmortar.topmortarsales.model

data class HomeMenuSalesModel (
    var title: String = "",
    var icon: Int? = null,
    var bgColor: Int? = null,
    var target: Class<*>? = null,
    var action: (() -> Unit)? = null,
    var isLocked: Boolean = true
)