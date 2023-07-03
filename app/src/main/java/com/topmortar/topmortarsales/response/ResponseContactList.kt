package com.topmortar.topmortarsales.response

import com.topmortar.topmortarsales.model.ContactModel

data class ResponseContactList(
    val status: String,
    val results: ArrayList<ContactModel>
)