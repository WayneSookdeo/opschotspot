package com.example.example

import com.google.gson.annotations.SerializedName


data class WaypointsModel (

  @SerializedName("distance" ) var distance : Double?           = null,
  @SerializedName("name"     ) var name     : String?           = null,
  @SerializedName("location" ) var location : ArrayList<Double> = arrayListOf()

)