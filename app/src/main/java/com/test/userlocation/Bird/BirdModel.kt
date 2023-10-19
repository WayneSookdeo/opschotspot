package com.test.userlocation.Bird

import com.google.gson.annotations.SerializedName

data class BirdModel(

        @SerializedName("locId") val locId: String?,
        @SerializedName("locName") val name: String?, // Change to "locName"
        @SerializedName("lat") val latitude: Double?, // Change to "lat"
        @SerializedName("lng") val longitude: Double?, // Change to "lng"
        @SerializedName("countryCode") val countryCode: String?,
        @SerializedName ("subnationalCode") val subnationallCode: String?,
        @SerializedName("latestObsDt") val latestobsDt: String?,
        @SerializedName("numSpeciesAllTime") val numSpeciesAllTime: Int?

)
