package com.test.userlocation.Bird

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface BirdInterface  {

    @GET("ref/hotspot/{regionCode}")
    fun getHotspotsInRegion(
        @Path("regionCode") regionCode: String,
        @Query("back") back: Int = 30,
        @Query("fmt") format: String = "json",
        @Query("key") apiKey: String
    ): Call<List<BirdModel>>// Change the return type to Response<List<Hotspot>>



    @GET("ref/hotspot/geo")
    fun getNearbyHotspots(
        @Header("x-ebirdapitoken") apiKey: String, // Add API key as a header
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("back") back: Int = 30, // Default to fetching hotspots up to 30 days back
        @Query("dist") distance: Int = 25, // Default search radius of 25 kilometers
        @Query("fmt") format: String = "json" // Default to JSON format
    ): Call<List<BirdModel>>
}