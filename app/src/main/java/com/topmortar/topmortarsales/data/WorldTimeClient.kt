package com.topmortar.topmortarsales.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WorldTimeClient {

    private const val BASE_URL = "https://timeapi.io/"

    val api: WorldTimeApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WorldTimeApi::class.java)
    }
}