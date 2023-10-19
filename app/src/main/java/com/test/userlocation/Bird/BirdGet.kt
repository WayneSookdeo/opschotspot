package com.test.userlocation.Bird

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object BirdGet {

    private const val BASE_URL = "https://api.ebird.org/v2/"
    private val apiKey = "cgg84aonm2ti"

    private val client = OkHttpClient()
        .newBuilder()
        .build()

    private val retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(client)
            .build()
            .create(BirdInterface::class.java)

    fun buildService(): BirdInterface {
        return retrofit
    }
}

