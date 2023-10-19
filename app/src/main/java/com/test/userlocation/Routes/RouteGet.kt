package com.test.userlocation.Routes

import com.test.userlocation.Bird.BirdGet
import com.test.userlocation.Bird.BirdInterface
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RouteGet {

    private const val BASE_URL = "https://api.mapbox.com/"

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
            .create(RouteInterface::class.java)

    fun buildService(): RouteInterface {
        return retrofit
    }
}