package com.example.qontak.data

import com.example.qontak.commons.BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HttpClient {
    companion object {

        fun create(): ApiService {
            val okHttpClient = OkHttpClient.Builder()
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ApiService::class.java)
        }

    }
}
