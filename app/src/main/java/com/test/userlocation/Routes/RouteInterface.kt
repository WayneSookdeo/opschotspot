package com.test.userlocation.Routes

import com.example.example.RouteResponseModel
import com.test.userlocation.Bird.BirdModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RouteInterface {

    @GET("directions/v5/mapbox/{profile}/{coordinates}")
    fun getRoute(
        @Path("profile", encoded = true) profile: String,
        @Path(value = "coordinates", encoded = true) coordinates: String,
        @Query("alternatives") alternatives: Boolean,
        @Query("language") language: String,
        @Query("geometries") geometries: String,
        @Query("overview") overview: String,
        @Query("steps") steps: Boolean,
        @Query("access_Token") access_Token: String,
    ): Call<List<RouteResponseModel>>
}