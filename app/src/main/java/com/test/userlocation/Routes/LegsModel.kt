package com.example.example

import com.google.gson.annotations.SerializedName


data class LegsModel (

  @SerializedName("steps"    ) var steps    : ArrayList<String> = arrayListOf(),
  @SerializedName("summary"  ) var summary  : String?           = null,
  @SerializedName("weight"   ) var weight   : Double?           = null,
  @SerializedName("duration" ) var duration : Double?           = null,
  @SerializedName("distance" ) var distance : Double?           = null

)