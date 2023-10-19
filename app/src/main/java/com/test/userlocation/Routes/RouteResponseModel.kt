package com.example.example

import com.google.gson.annotations.SerializedName


data class RouteResponseModel (

  @SerializedName("routes"    ) var routes    : ArrayList<RoutesModel>    = arrayListOf(),
  @SerializedName("waypoints" ) var waypoints : ArrayList<WaypointsModel> = arrayListOf(),
  @SerializedName("code"      ) var code      : String?              = null,
  @SerializedName("uuid"      ) var uuid      : String?              = null

)