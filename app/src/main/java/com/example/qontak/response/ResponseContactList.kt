package com.example.qontak.response

import com.example.qontak.model.ContactModel

data class ResponseContactList(
    val status: String,
    val results: ArrayList<ContactModel>
)