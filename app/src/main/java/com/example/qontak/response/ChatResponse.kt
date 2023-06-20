package com.example.qontak.response

import com.example.qontak.model.ChatModelPagination

data class ChatResponse (
    val success: Boolean,
    val message: String,
    val data: ChatModelPagination
)