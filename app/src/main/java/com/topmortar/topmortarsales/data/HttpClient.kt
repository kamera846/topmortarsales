package com.topmortar.topmortarsales.data

import com.topmortar.topmortarsales.commons.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object HttpClient {

    private const val TIMEOUT = 30L

    private val trustManager = object : X509TrustManager {

        override fun checkClientTrusted(
            chain: Array<out X509Certificate>?,
            authType: String?
        ) {
        }

        override fun checkServerTrusted(
            chain: Array<out X509Certificate>?,
            authType: String?
        ) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return emptyArray()
        }
    }

    private val sslSocketFactory: SSLSocketFactory by lazy {

        val sslContext = SSLContext.getInstance("TLS")

        sslContext.init(
            null,
            arrayOf<TrustManager>(trustManager),
            null
        )

        sslContext.socketFactory
    }

    private val okHttpClient: OkHttpClient by lazy {

        OkHttpClient.Builder()
            .sslSocketFactory(
                sslSocketFactory,
                trustManager
            )
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .callTimeout(TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}