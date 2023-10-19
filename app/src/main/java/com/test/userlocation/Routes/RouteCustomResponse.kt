package com.test.userlocation.Routes
import com.google.gson.annotations.SerializedName

data class RouteCustomResponse (
    @SerializedName("routes") val routes: List<CustomGeometry>
        )

data class CustomRoute (
    @SerializedName("geometry") val geometry: CustomGeometry
)

data class CustomGeometry (
    @SerializedName("coordinates") val coordinates: List<List<Double>>,
    @SerializedName("type") val type: String
)