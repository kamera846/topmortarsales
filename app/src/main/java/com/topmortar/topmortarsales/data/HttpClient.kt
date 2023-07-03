package com.topmortar.topmortarsales.data

import com.topmortar.topmortarsales.commons.BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class HttpClient {
    companion object {

        fun create(): ApiService {
            val timeOutValue = 10L

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(timeOutValue, TimeUnit.SECONDS)
                .readTimeout(timeOutValue, TimeUnit.SECONDS)
                .callTimeout(timeOutValue, TimeUnit.SECONDS)
                .writeTimeout(timeOutValue, TimeUnit.SECONDS)
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
