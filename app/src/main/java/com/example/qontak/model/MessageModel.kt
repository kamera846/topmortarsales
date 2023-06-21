package com.example.qontak.model

class MessageModel {
    data class ResponseMessageObject(
        val response: Int,
        val status: String,
        val message: String,
    )

    data class ResponseMessageList(
        val status: String,
        val results: ArrayList<MessageObject>
    )

    data class MessageObject(
        val id_contact: String,
        val nama: String,
        val nomorhp: String
    )
}