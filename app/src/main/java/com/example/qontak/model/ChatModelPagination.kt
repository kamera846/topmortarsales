package com.example.qontak.model

data class ChatModelPagination (
    val current_page: Int,
    val per_page: Int,
    val last_page: Int,
    val data: ArrayList<ChatModel>
)