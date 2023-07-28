package com.example.weatherapp.model

import com.google.gson.annotations.SerializedName

data class Coord(
    @SerializedName("lon") val lon: Double? = null,
    @SerializedName("lat") val lat: Double? = null,
)
