package com.example.qontak.data

import com.example.qontak.commons.GET_CONTACT
import com.example.qontak.response.ResponseContactList
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET(GET_CONTACT)
    suspend fun getContacts(): ResponseContactList

}
