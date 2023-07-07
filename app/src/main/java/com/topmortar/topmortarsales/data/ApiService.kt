package com.topmortar.topmortarsales.data

import com.topmortar.topmortarsales.commons.EDIT_CONTACT
import com.topmortar.topmortarsales.commons.GET_CONTACT
import com.topmortar.topmortarsales.commons.SEARCH_CONTACT
import com.topmortar.topmortarsales.commons.SEND_MESSAGE
import com.topmortar.topmortarsales.response.ResponseContactList
import com.topmortar.topmortarsales.response.ResponseMessage
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET(GET_CONTACT)
    suspend fun getContacts(): ResponseContactList

    @Multipart
    @POST(EDIT_CONTACT)
    suspend fun editContact(
        @Part("id") id: RequestBody,
        @Part("nama") nama: RequestBody,
        @Part("owner_name") ownerName: RequestBody,
        @Part("tgl_lahir") tglLahir: RequestBody
    ): Response<ResponseMessage>

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
