package com.topmortar.topmortarsales.data

import com.topmortar.topmortarsales.commons.ADD_CITY
import com.topmortar.topmortarsales.commons.ADD_USERS
import com.topmortar.topmortarsales.commons.AUTH
import com.topmortar.topmortarsales.commons.EDIT_CONTACT
import com.topmortar.topmortarsales.commons.GET_CITY
import com.topmortar.topmortarsales.commons.GET_CONTACT
import com.topmortar.topmortarsales.commons.SURAT_JALAN
import com.topmortar.topmortarsales.commons.GET_USERS
import com.topmortar.topmortarsales.commons.INVOICE
import com.topmortar.topmortarsales.commons.REQUEST_OTP
import com.topmortar.topmortarsales.commons.SEARCH_CONTACT
import com.topmortar.topmortarsales.commons.SEND_MESSAGE
import com.topmortar.topmortarsales.commons.SKILL
import com.topmortar.topmortarsales.commons.TUKANG
import com.topmortar.topmortarsales.commons.TUKANG_MESSAGE
import com.topmortar.topmortarsales.commons.UPDATE_PASSWORD
import com.topmortar.topmortarsales.commons.VERIFY_OTP
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.response.ResponseAuth
import com.topmortar.topmortarsales.response.ResponseCities
import com.topmortar.topmortarsales.response.ResponseContactList
import com.topmortar.topmortarsales.response.ResponseInvoices
import com.topmortar.topmortarsales.response.ResponseMessage
import com.topmortar.topmortarsales.response.ResponseSkills
import com.topmortar.topmortarsales.response.ResponseTukangList
import com.topmortar.topmortarsales.response.ResponseUsers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET(GET_CONTACT)
    suspend fun getContacts(@Query("c") cityId: String): ResponseContactList

    @GET(GET_CONTACT)
    suspend fun getContacts(): ResponseContactList

    @GET(GET_CONTACT)
    suspend fun getContactDetail(@Query("id") contactId: String): Response<ResponseContactList>

    @Multipart
    @POST(EDIT_CONTACT)
    suspend fun editContact(
        @Part("id") id: RequestBody,
        @Part("nomorhp") phone: RequestBody,
        @Part("nama") name: RequestBody,
        @Part("owner_name") ownerName: RequestBody,
        @Part("tgl_lahir") birthday: RequestBody,
        @Part("id_city") cityId: RequestBody,
        @Part("mapsUrl") mapsUrl: RequestBody,
        @Part("address") address: RequestBody,
        @Part("status") status: RequestBody,
        @Part("termin_payment") termin: RequestBody,
        @Part ktp: MultipartBody.Part? = null,
    ): Response<ResponseMessage>

    @Multipart
    @POST(SEND_MESSAGE)
    suspend fun sendMessage(
        @Part("nama") name: RequestBody,
        @Part("nomorhp") phone: RequestBody,
        @Part("owner_name") ownerName: RequestBody,
        @Part("tgl_lahir") birthday: RequestBody,
        @Part("id_city") cityId: RequestBody,
        @Part("mapsUrl") mapsUrl: RequestBody,
        @Part("id_user") userId: RequestBody,
        @Part("full_name") currentName: RequestBody,
        @Part("termin_payment") termin: RequestBody,
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
    @POST(ADD_CITY)
    suspend fun addCity(
        @Part("nama_city") name: RequestBody,
        @Part("kode_city") code: RequestBody
    ): Response<ResponseMessage>

    @Multipart
    @POST(AUTH)
    suspend fun auth(
        @Part("username") username: RequestBody,
        @Part("password") password: RequestBody
    ): ResponseAuth

    @GET(GET_USERS)
    suspend fun getUsers(): ResponseUsers

    @GET(GET_USERS)
    suspend fun detailUser(@Query("id") userId: String): ResponseUsers

    @Multipart
    @POST(ADD_USERS)
    suspend fun addUser(
        @Part("level_user") level: RequestBody,
        @Part("id_city") cityId: RequestBody,
        @Part("phone_user") phone: RequestBody,
        @Part("username") username: RequestBody,
        @Part("full_name") fullName: RequestBody,
        @Part("password") password: RequestBody
    ): Response<ResponseMessage>

    @Multipart
    @POST(ADD_USERS)
    suspend fun editUser(
        @Part("id") ID: RequestBody,
        @Part("level_user") level: RequestBody,
        @Part("id_city") cityId: RequestBody,
        @Part("phone_user") phone: RequestBody,
        @Part("username") username: RequestBody,
        @Part("full_name") fullName: RequestBody
    ): Response<ResponseMessage>

    @Multipart
    @POST(REQUEST_OTP)
    suspend fun requestOtp(
        @Part("username") username: RequestBody
    ): Response<ResponseMessage>

    @Multipart
    @POST(VERIFY_OTP)
    suspend fun verifyOtp(
        @Part("otp") otp: RequestBody
    ): Response<ResponseMessage>

    @Multipart
    @POST(UPDATE_PASSWORD)
    suspend fun updatePassword(
        @Part("id_user") userID: RequestBody,
        @Part("password") password: RequestBody,
    ): Response<ResponseMessage>

    @GET(SURAT_JALAN)
    suspend fun getCourierStore(
        @Query("p") processNumber: String,
        @Query("cr") courierId: String
    ): ResponseContactList

    @GET(SURAT_JALAN)
    suspend fun getInvoices(
        @Query("p") processNumber: String,
        @Query("str") contactId: String
    ): ResponseInvoices

    @GET(SURAT_JALAN)
    suspend fun getInvoicesDetail(
        @Query("p") processNumber: String,
        @Query("sj") invoiceId: String
    ): ResponseInvoices

    @Multipart
    @POST(SURAT_JALAN)
    suspend fun printInvoice(
        @Part("command") command: RequestBody = createPartFromString("print"),
        @Part("id_surat_jalan") invoiceId: RequestBody
    ): ResponseInvoices

    @Multipart
    @POST(SURAT_JALAN)
    suspend fun closingInvoice(
        @Part("command") command: RequestBody = createPartFromString("closing"),
        @Part("id_surat_jalan") invoiceId: RequestBody,
        @Part image: MultipartBody.Part,
    ): ResponseInvoices

    @Multipart
    @POST(INVOICE)
    suspend fun addInvoice(
        @Part("id_surat_jalan") invoiceId: RequestBody
     ): ResponseInvoices

    @GET(SKILL)
    suspend fun getSkills(): ResponseSkills

    @Multipart
    @POST(SKILL)
    suspend fun addSkill(
        @Part("nama_skill") name: RequestBody,
        @Part("kode_skill") code: RequestBody
    ): Response<ResponseMessage>

    @Multipart
    @POST(TUKANG)
    suspend fun editTukang(
        @Part("id") id: RequestBody,
        @Part("nomorhp") phone: RequestBody,
        @Part("nama") name: RequestBody,
        @Part("nama_lengkap") namaLengkap: RequestBody,
        @Part("tgl_lahir") birthday: RequestBody,
        @Part("id_city") cityId: RequestBody,
        @Part("mapsUrl") mapsUrl: RequestBody,
        @Part("address") address: RequestBody,
        @Part("status") status: RequestBody,
        @Part("id_skill") skillId: RequestBody,
    ): Response<ResponseMessage>

    @GET(TUKANG)
    suspend fun getTukang(): ResponseTukangList

    @GET(TUKANG)
    suspend fun getTukang(@Query("c") cityId: String): ResponseTukangList

    @Multipart
    @POST(TUKANG_MESSAGE)
    suspend fun sendMessageTukang(
        @Part("nama") name: RequestBody,
        @Part("nomorhp") phone: RequestBody,
        @Part("nama_lengkap") namaLengkap: RequestBody,
        @Part("tgl_lahir") birthday: RequestBody,
        @Part("id_city") cityId: RequestBody,
        @Part("id_skill") skillId: RequestBody,
        @Part("mapsUrl") mapsUrl: RequestBody,
//        @Part("id_user") userId: RequestBody,
        @Part("full_name") currentName: RequestBody,
        @Part("message_body") message: RequestBody,
    ): Response<ResponseMessage>
}
