package com.topmortar.topmortarsales.data

import com.topmortar.topmortarsales.response.WorldTimeResponse
import retrofit2.Call
import retrofit2.http.GET

interface WorldTimeApi {

    @GET("api/v1/time/current/zone?timezone=Asia%2FJakarta")
    fun getJakartaTime(): Call<WorldTimeResponse>
}