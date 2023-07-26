package com.topmortar.topmortarsales.data

import com.topmortar.topmortarsales.commons.AUTH
import com.topmortar.topmortarsales.commons.EDIT_CONTACT
import com.topmortar.topmortarsales.commons.GET_CITY
import com.topmortar.topmortarsales.commons.GET_CONTACT
import com.topmortar.topmortarsales.commons.GET_USERS
import com.topmortar.topmortarsales.commons.SEARCH_CONTACT
import com.topmortar.topmortarsales.commons.SEND_MESSAGE
import com.topmortar.topmortarsales.response.ResponseAuth
import com.topmortar.topmortarsales.response.ResponseCities
import com.topmortar.topmortarsales.response.ResponseContactList
import com.topmortar.topmortarsales.response.ResponseMessage
import com.topmortar.topmortarsales.response.ResponseUsers
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET(GET_CONTACT)
    suspend fun getContacts(@Query("c") cityId: String): ResponseContactList
    @GET(GET_CONTACT)
    suspend fun getContacts(): ResponseContactList

    @Multipart
    @POST(EDIT_CONTACT)
    suspend fun editContact(
        @Part("id") id: RequestBody,
        @Part("nama") name: RequestBody,
        @Part("owner_name") ownerName: RequestBody,
        @Part("tgl_lahir") birthday: RequestBody,
        @Part("id_city") cityId: RequestBody,
        @Part("maps_url") mapsUrl: RequestBody,
    ): Response<ResponseMessage>

    @Multipart
    @POST(SEND_MESSAGE)
    suspend fun sendMessage(
        @Part("nama") name: RequestBody,
        @Part("nomorhp") phone: RequestBody,
        @Part("owner_name") ownerName: RequestBody,
        @Part("tgl_lahir") birthday: RequestBody,
        @Part("id_city") cityId: RequestBody,
        @Part("maps_url") mapsUrl: RequestBody,
        @Part("message_body") message: RequestBody,
    ): Response<ResponseMessage>

    @Multipart
    @POST(SEARCH_CONTACT)
    suspend fun searchContact(
        @Part("key") key: RequestBody
    ): Response<ResponseContactList>

    @Multipart
    @POST(SEARCH_CONTACT)
    suspend fun searchContact(
        @Part("id_city") cityId: RequestBody,
        @Part("key") key: RequestBody
    ): Response<ResponseContactList>

    @GET(GET_CITY)
    suspend fun getCities(): ResponseCities

    @Multipart
    @POST(AUTH)
    suspend fun auth(
        @Part("username") username: RequestBody,
        @Part("password") password: RequestBody
    ): ResponseAuth

    @GET(GET_USERS)
    suspend fun getUsers(): ResponseUsers
}
