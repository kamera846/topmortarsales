package com.topmortar.topmortarsales.data

import com.topmortar.topmortarsales.commons.ADD_CITY
import com.topmortar.topmortarsales.commons.ADD_USERS
import com.topmortar.topmortarsales.commons.AUTH
import com.topmortar.topmortarsales.commons.BASECAMP
import com.topmortar.topmortarsales.commons.BID
import com.topmortar.topmortarsales.commons.BID_ON_GOING
import com.topmortar.topmortarsales.commons.CONTACT
import com.topmortar.topmortarsales.commons.DELIVERY
import com.topmortar.topmortarsales.commons.DISTRIBUTOR
import com.topmortar.topmortarsales.commons.EDIT_CONTACT
import com.topmortar.topmortarsales.commons.GET_CITY
import com.topmortar.topmortarsales.commons.GET_USERS
import com.topmortar.topmortarsales.commons.INVOICE
import com.topmortar.topmortarsales.commons.PAYMENT
import com.topmortar.topmortarsales.commons.PROMO
import com.topmortar.topmortarsales.commons.RENCANA_VISIT
import com.topmortar.topmortarsales.commons.REQUEST_OTP
import com.topmortar.topmortarsales.commons.SEARCH_CONTACT
import com.topmortar.topmortarsales.commons.SEND_MESSAGE
import com.topmortar.topmortarsales.commons.SKILL
import com.topmortar.topmortarsales.commons.STORE_STATUS
import com.topmortar.topmortarsales.commons.SURAT_JALAN
import com.topmortar.topmortarsales.commons.SURAT_JALAN_NOT_CLOSING
import com.topmortar.topmortarsales.commons.TUKANG
import com.topmortar.topmortarsales.commons.TUKANG_MESSAGE
import com.topmortar.topmortarsales.commons.UPDATE_PASSWORD
import com.topmortar.topmortarsales.commons.VERIFY_OTP
import com.topmortar.topmortarsales.commons.VISIT
import com.topmortar.topmortarsales.commons.VOUCHER
import com.topmortar.topmortarsales.commons.WAREHOUSE
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.response.ResponseAuth
import com.topmortar.topmortarsales.response.ResponseBaseCamp
import com.topmortar.topmortarsales.response.ResponseCities
import com.topmortar.topmortarsales.response.ResponseContactList
import com.topmortar.topmortarsales.response.ResponseCountStore
import com.topmortar.topmortarsales.response.ResponseDelivery
import com.topmortar.topmortarsales.response.ResponseDistributor
import com.topmortar.topmortarsales.response.ResponseGudang
import com.topmortar.topmortarsales.response.ResponseInvoice
import com.topmortar.topmortarsales.response.ResponseList
import com.topmortar.topmortarsales.response.ResponseMessage
import com.topmortar.topmortarsales.response.ResponsePayment
import com.topmortar.topmortarsales.response.ResponsePromo
import com.topmortar.topmortarsales.response.ResponseRencanaVisit
import com.topmortar.topmortarsales.response.ResponseReportVisit
import com.topmortar.topmortarsales.response.ResponseSkills
import com.topmortar.topmortarsales.response.ResponseSuratJalan
import com.topmortar.topmortarsales.response.ResponseSuratJalanNotClosing
import com.topmortar.topmortarsales.response.ResponseTukangList
import com.topmortar.topmortarsales.response.ResponseUsers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import java.util.Calendar

interface ApiService {

    val currenMonth: Int get() = Calendar.getInstance().get(Calendar.MONTH) + 1

    @GET(CONTACT)
    suspend fun getContacts(@Query("c") cityId: String, @Query("dst") distributorID: String): ResponseContactList
    @GET(CONTACT)
    suspend fun getContacts(@Query("c") cityId: String, @Query("status") status: String, @Query("dst") distributorID: String): ResponseContactList

//    @GET(CONTACT)
//    suspend fun getContacts(): ResponseContactList
    @GET(CONTACT)
    suspend fun getContactsByDistributor(@Query("dst") distributorID: String): ResponseContactList
    @GET(CONTACT)
    suspend fun getContactsByStatus(@Query("status") status: String, @Query("dst") distributorID: String): ResponseContactList

    @GET(CONTACT)
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
        @Part("payment_method") paymentMethod: RequestBody,
        @Part("termin_payment") termin: RequestBody,
        @Part("reputation") reputation: RequestBody,
        @Part("id_promo") promoId: RequestBody,
        @Part ktp: MultipartBody.Part? = null,
    ): Response<ResponseMessage>

    @Multipart
    @POST(SEND_MESSAGE)
    suspend fun insertContact(
        @Part("nama") name: RequestBody,
        @Part("nomorhp") phone: RequestBody,
        @Part("owner_name") ownerName: RequestBody,
        @Part("tgl_lahir") birthday: RequestBody,
        @Part("id_city") cityId: RequestBody,
        @Part("mapsUrl") mapsUrl: RequestBody,
        @Part("id_user") userId: RequestBody,
        @Part("full_name") currentName: RequestBody,
        @Part("termin_payment") termin: RequestBody
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
        @Part("key") key: RequestBody,
        @Part("dst") distributorID: RequestBody
    ): Response<ResponseContactList>

    @Multipart
    @POST(SEARCH_CONTACT)
    suspend fun searchContact(
        @Part("id_city") cityId: RequestBody,
        @Part("key") key: RequestBody,
        @Part("dst") distributorID: RequestBody
    ): Response<ResponseContactList>

    @Multipart
    @POST(SEARCH_CONTACT)
    suspend fun searchContactByStatus(
        @Part("status") status: RequestBody,
        @Part("key") key: RequestBody,
        @Part("dst") distributorID: RequestBody
    ): Response<ResponseContactList>

    @Multipart
    @POST(SEARCH_CONTACT)
    suspend fun searchContact(
        @Part("status") status: RequestBody,
        @Part("id_city") cityId: RequestBody,
        @Part("key") key: RequestBody,
        @Part("dst") distributorID: RequestBody
    ): Response<ResponseContactList>

    @GET(GET_CITY)
    suspend fun getCities(@Query("dst") distributorID: String): ResponseCities

    @Multipart
    @POST(ADD_CITY)
    suspend fun addCity(
        @Part("nama_city") name: RequestBody,
        @Part("id_distributor") distributorID: RequestBody,
        @Part("kode_city") code: RequestBody
    ): Response<ResponseMessage>

    @Multipart
    @POST(AUTH)
    suspend fun auth(
        @Part("username") username: RequestBody,
        @Part("password") password: RequestBody
    ): ResponseAuth

    @GET(GET_USERS)
    suspend fun getUsers(@Query("dst") distributorID: String): ResponseUsers

    @GET(GET_USERS)
    suspend fun getUsers(@Query("c") cityId: String, @Query("dst") distributorID: String): ResponseUsers

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
        @Part("id_distributor") distributorID: RequestBody,
        @Part("password") password: RequestBody,
        @Part("is_notify") isNotify: RequestBody
    ): Response<ResponseMessage>

    @Multipart
    @POST(ADD_USERS)
    suspend fun editUser(
        @Part("id") ID: RequestBody,
        @Part("level_user") level: RequestBody,
        @Part("id_city") cityId: RequestBody,
        @Part("phone_user") phone: RequestBody,
        @Part("username") username: RequestBody,
        @Part("id_distributor") distributorID: RequestBody,
        @Part("full_name") fullName: RequestBody,
        @Part("is_notify") isNotify: RequestBody
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
    suspend fun getSuratJalan(
        @Query("p") processNumber: String,
        @Query("str") contactId: String
    ): ResponseSuratJalan

    @GET(INVOICE)
    suspend fun getInvoices(
        @Query("id_contact") contactId: String
    ): ResponseInvoice

    @GET(INVOICE)
    suspend fun getInvoices(
        @Query("id_contact") contactId: String,
        @Query("status") status: String
    ): ResponseInvoice

    @GET(PAYMENT)
    suspend fun getPayment(
        @Query("id_invoice") idInvoice: String
    ): ResponsePayment

    @GET(SURAT_JALAN)
    suspend fun getSuratJalanDetail(
        @Query("p") processNumber: String,
        @Query("sj") invoiceId: String
    ): ResponseSuratJalan

    @Multipart
    @POST(SURAT_JALAN)
    suspend fun printInvoice(
        @Part("command") command: RequestBody = createPartFromString("print"),
        @Part("id_surat_jalan") invoiceId: RequestBody
    ): ResponseSuratJalan

    @Multipart
    @POST(SURAT_JALAN)
    suspend fun closingInvoice(
        @Part("command") command: RequestBody = createPartFromString("closing"),
        @Part("distance") distance: RequestBody,
        @Part("id_surat_jalan") invoiceId: RequestBody,
        @Part image: MultipartBody.Part,
    ): ResponseSuratJalan

    @Multipart
    @POST(INVOICE)
    suspend fun addInvoice(
        @Part("id_surat_jalan") invoiceId: RequestBody
     ): ResponseInvoice

    @GET(SKILL)
    suspend fun getSkills(@Query("dst") distributorID: String): ResponseSkills

    @Multipart
    @POST(SKILL)
    suspend fun addSkill(
        @Part("nama_skill") name: RequestBody,
        @Part("id_distributor") distributorID: RequestBody,
        @Part("kode_skill") code: RequestBody
    ): Response<ResponseMessage>

    @Multipart
    @POST(SKILL)
    suspend fun editSkill(
        @Part("id") id: RequestBody,
        @Part("nama_skill") name: RequestBody,
        @Part("id_distributor") distributorID: RequestBody,
        @Part("kode_skill") code: RequestBody
    ): Response<ResponseMessage>

    @Multipart
    @POST(TUKANG)
    suspend fun editTukang(
        @Part("id") id: RequestBody,
        @Part("nomorhp") phone: RequestBody,
        @Part("nama") name: RequestBody,
//        @Part("nama_lengkap") namaLengkap: RequestBody,
        @Part("tgl_lahir") birthday: RequestBody,
        @Part("id_city") cityId: RequestBody,
        @Part("mapsUrl") mapsUrl: RequestBody,
        @Part("address") address: RequestBody,
        @Part("status") status: RequestBody,
        @Part("id_skill") skillId: RequestBody,
        @Part ktp: MultipartBody.Part? = null,
    ): Response<ResponseMessage>

    @GET(TUKANG)
    suspend fun getTukang(@Query("c") cityId: String): ResponseTukangList

    @GET(TUKANG)
    suspend fun getDetailTukang(@Query("id") tukangId: String): Response<ResponseTukangList>

    @Multipart
    @POST(TUKANG)
    suspend fun insertTukang(
        @Part("nama") name: RequestBody,
        @Part("nomorhp") phone: RequestBody,
//        @Part("nama_lengkap") namaLengkap: RequestBody,
        @Part("tgl_lahir") birthday: RequestBody,
        @Part("id_city") cityId: RequestBody,
        @Part("id_skill") skillId: RequestBody,
        @Part("mapsUrl") mapsUrl: RequestBody,
        @Part("full_name") currentName: RequestBody,
        @Part("id_user") userId: RequestBody,
    ): Response<ResponseMessage>

    @Multipart
    @POST(TUKANG_MESSAGE)
    suspend fun sendMessageTukang(
        @Part("nama") name: RequestBody,
        @Part("nomorhp") phone: RequestBody,
//        @Part("nama_lengkap") namaLengkap: RequestBody,
        @Part("tgl_lahir") birthday: RequestBody,
        @Part("id_city") cityId: RequestBody,
        @Part("id_skill") skillId: RequestBody,
        @Part("mapsUrl") mapsUrl: RequestBody,
        @Part("full_name") currentName: RequestBody,
        @Part("id_user") userId: RequestBody,
        @Part("message_body") message: RequestBody,
    ): Response<ResponseMessage>

    @GET(PROMO)
    suspend fun getPromo(): ResponsePromo

    @GET(STORE_STATUS)
    suspend fun getStoreCount(): ResponseCountStore

    @GET(STORE_STATUS)
    suspend fun getStoreCount(
        @Query("c") cityId: String
    ): ResponseCountStore

    @GET(BID)
    suspend fun getContactsUserBid(
        @Query("u") userId: String,
        @Query("m") month: String = currenMonth.toString(),
        @Query("visit") visit: String = BID_ON_GOING
    ): ResponseContactList

    @Multipart
    @POST(VISIT)
    suspend fun makeVisitCourierReport(
        @Part("id_gudang") idGudang: RequestBody,
        @Part("id_user") idUser: RequestBody,
        @Part("distance_visit") distanceVisit: RequestBody,
        @Part("laporan_visit") laporanVisit: RequestBody
    ): Response<ResponseReportVisit>

    @GET(VISIT)
    suspend fun listCourierReport(
        @Query("u") idUser: String,
        @Query("g") idGudang: String
    ): Response<ResponseReportVisit>

    @GET(VISIT)
    suspend fun listAllCourierReport(
        @Query("u") idUser: String,
        @Query("cat") category: String = "courier"
    ): Response<ResponseReportVisit>

    @Multipart
    @POST(VISIT)
    suspend fun makeVisitReport(
        @Part("id_contact") idContact: RequestBody,
        @Part("id_user") idUser: RequestBody,
        @Part("distance_visit") distanceVisit: RequestBody,
        @Part("laporan_visit") laporanVisit: RequestBody
    ): Response<ResponseReportVisit>

    @GET(VISIT)
    suspend fun listReport(
        @Query("u") idUser: String,
        @Query("s") idContact: String
    ): Response<ResponseReportVisit>

    @GET(VISIT)
    suspend fun listAllReport(
        @Query("u") idUser: String,
        @Query("cat") category: String = "sales"
    ): Response<ResponseReportVisit>

    @GET(VISIT)
    suspend fun listUsersReport(
        @Query("a") idUser: String,
        @Query("s") idContact: String
    ): ResponseUsers

    @GET(BASECAMP)
    suspend fun getListBaseCamp(@Query("dst") distributorID: String): ResponseBaseCamp

    @GET(BASECAMP)
    suspend fun getListBaseCamp(
        @Query("c") cityId: String,
        @Query("dst") distributorID: String
    ): ResponseBaseCamp

    @Multipart
    @POST(BASECAMP)
    suspend fun addBaseCamp(
        @Part("nama_gudang") name: RequestBody,
        @Part("location_gudang") mapsUrl: RequestBody,
        @Part("nomorhp_gudang") phone: RequestBody,
        @Part("id_city") cityId: RequestBody,
        @Part("id_distributor") distributorID: RequestBody,
    ): ResponseBaseCamp

    @Multipart
    @POST(BASECAMP)
    suspend fun addBaseCamp(
        @Part("nama_gudang") name: RequestBody,
        @Part("location_gudang") mapsUrl: RequestBody,
        @Part("id_city") cityId: RequestBody,
        @Part("id_distributor") distributorID: RequestBody,
    ): ResponseBaseCamp

    @Multipart
    @POST(BASECAMP)
    suspend fun editBaseCamp(
        @Part("nama_gudang") name: RequestBody,
        @Part("location_gudang") mapsUrl: RequestBody,
        @Part("nomorhp_gudang") phone: RequestBody,
        @Part("id_city") cityId: RequestBody,
        @Part("id") idBasecamp: RequestBody,
        @Part("id_distributor") distributorID: RequestBody,
    ): ResponseBaseCamp

    @Multipart
    @POST(BASECAMP)
    suspend fun deleteBaseCamp(
        @Part("id") idBasecamp: RequestBody,
    ): ResponseBaseCamp

    @GET(WAREHOUSE)
    suspend fun getListGudang(
        @Query("dst") distributorID: String
    ): ResponseGudang

    @GET(WAREHOUSE)
    suspend fun getListGudang(
        @Query("c") cityId: String,
        @Query("dst") distributorID: String
    ): ResponseGudang

    @Multipart
    @POST(WAREHOUSE)
    suspend fun addGudang(
        @Part("nama_warehouse") name: RequestBody,
        @Part("location_warehouse") mapsUrl: RequestBody,
        @Part("nomorhp_warehouse") phone: RequestBody,
        @Part("id_city") cityId: RequestBody,
    ): ResponseGudang

    @Multipart
    @POST(WAREHOUSE)
    suspend fun addGudang(
        @Part("nama_warehouse") name: RequestBody,
        @Part("location_warehouse") mapsUrl: RequestBody,
        @Part("id_city") cityId: RequestBody,
    ): ResponseGudang

    @Multipart
    @POST(WAREHOUSE)
    suspend fun editGudang(
        @Part("nama_warehouse") name: RequestBody,
        @Part("location_warehouse") mapsUrl: RequestBody,
        @Part("nomorhp_warehouse") phone: RequestBody,
        @Part("id_city") cityId: RequestBody,
        @Part("id") idGudang: RequestBody,
    ): ResponseGudang

    @Multipart
    @POST(WAREHOUSE)
    suspend fun editGudang(
        @Part("nama_warehouse") name: RequestBody,
        @Part("location_warehouse") mapsUrl: RequestBody,
        @Part("id_city") cityId: RequestBody,
        @Part("id") idGudang: RequestBody,
    ): ResponseGudang

    @Multipart
    @POST(WAREHOUSE)
    suspend fun deleteGudang(
        @Part("id") idGudang: RequestBody,
    ): ResponseGudang

    @GET(DISTRIBUTOR)
    suspend fun getListDistributor(): ResponseDistributor

    @Multipart
    @POST(VOUCHER)
    suspend fun addVoucher(
        @Part("id_contact") idContact: RequestBody,
        @Part("no_voucher") noVoucher: RequestBody,
    ): ResponseMessage

    @Multipart
    @POST(VOUCHER)
    suspend fun editNoFisikVoucher(
        @Part("id_voucher") idVoucher: RequestBody,
        @Part("no_fisik") noFisik: RequestBody,
    ): ResponseMessage

    @GET(VOUCHER)
    suspend fun listVoucher(
        @Query("c") idContact: String
    ): ResponseList.ResponseVoucher

    @Multipart
    @POST(DELIVERY)
    suspend fun saveDelivery(
        @Part("lat") lat: RequestBody,
        @Part("lng") lng: RequestBody,
        @Part("endDateTime") endDateTime: RequestBody,
        @Part("endLat") endLat: RequestBody,
        @Part("endLng") endLng: RequestBody,
        @Part("startDateTime") startDateTime: RequestBody,
        @Part("startLat") startLat: RequestBody,
        @Part("startLng") startLng: RequestBody,
        @Part("id_courier") idCourier: RequestBody,
        @Part("id_contact") idContact: RequestBody,
    ): ResponseMessage

    @GET(DELIVERY)
    suspend fun getDelivery(@Query("dst") distributorID: String): ResponseDelivery

    @GET(DELIVERY)
    suspend fun getDelivery(
        @Query("id_courier") idCourier: String,
        @Query("dst") distributorID: String
    ): ResponseDelivery

    @GET(DELIVERY)
    suspend fun getDeliveryByCity(
        @Query("c") cityId: String,
        @Query("dst") distributorID: String
    ): ResponseDelivery

    @GET(DELIVERY)
    suspend fun getDetailDelivery(
        @Query("id") idDelivery: String,
        @Query("dst") distributorID: String
    ): ResponseDelivery

    @GET(SURAT_JALAN_NOT_CLOSING)
    suspend fun sjNotClosing(@Query("dst") distributorID: String): ResponseSuratJalanNotClosing

    @GET(SURAT_JALAN_NOT_CLOSING)
    suspend fun sjNotClosing(
        @Query("c") idCity: String,
        @Query("dst") distributorID: String
    ): ResponseSuratJalanNotClosing

    @GET(RENCANA_VISIT)
    suspend fun targetJatem(
        @Query("type") type: String = "jatem",
        @Query("c") idCity: String,
    ): ResponseRencanaVisit

    @GET(RENCANA_VISIT)
    suspend fun targetVoucher(
        @Query("type") type: String = "voucher",
        @Query("c") idCity: String,
    ): ResponseRencanaVisit

    @GET(RENCANA_VISIT)
    suspend fun targetPasif(
        @Query("type") type: String = "pasif",
        @Query("c") idCity: String,
    ): ResponseRencanaVisit
}
