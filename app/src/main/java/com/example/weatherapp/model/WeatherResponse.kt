package com.example.weatherapp.model

import com.google.gson.annotations.SerializedName


data class WeatherResponse(
    @SerializedName("coord") val coord: Coord? = Coord(),
    @SerializedName("weather") val weather: ArrayList<Weather> = arrayListOf(),
    @SerializedName("base") val base: String? = null,
    @SerializedName("main") val main: Main? = Main(),
    @SerializedName("visibility") val visibility: Int? = null,
    @SerializedName("wind") val wind: Wind? = Wind(),
    @SerializedName("clouds") val clouds: Clouds? = Clouds(),
    @SerializedName("dt") val dt: Long? = null,
    @SerializedName("sys") val sys: Sys? = Sys(),
    @SerializedName("timezone") val timezone: Int? = null,
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("cod") val cod: Int? = null
)