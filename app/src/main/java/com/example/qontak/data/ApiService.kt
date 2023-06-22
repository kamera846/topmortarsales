package com.example.qontak.data

import com.example.qontak.commons.GET_CONTACT
import com.example.qontak.commons.SEARCH_CONTACT
import com.example.qontak.commons.SEND_MESSAGE
import com.example.qontak.model.MessageModel
import com.example.qontak.response.ResponseContactList
import com.example.qontak.response.ResponseMessage
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET(GET_CONTACT)
    suspend fun getContacts(): ResponseContactList

    @Multipart
    @POST(SEND_MESSAGE)
    suspend fun sendMessage(
        @Part("nama") name: RequestBody,
        @Part("nomorhp") phone: RequestBody,
        @Part("message_body") message: RequestBody
    ): Response<ResponseMessage>

    @Multipart
    @POST(SEARCH_CONTACT)
    suspend fun searchContact(
        @Part("key") key: RequestBody
    ): Response<ResponseContactList>

}
