package com.example.qontak.data

import com.example.qontak.model.ChatModel
import com.example.qontak.response.ChatResponse
import retrofit2.http.*

interface ApiService {

    @GET("posts")
    suspend fun getPosts(): ChatResponse

}
