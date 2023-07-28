package com.example.weatherapp.service

import com.example.weatherapp.model.WeatherResponse
import com.example.weatherapp.util.Utils
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("weather?")
    suspend fun getCurrentWeather(
        @Query("lat")
        lat: String,
        @Query("lon")
        lon: String,
        @Query("appid")
        appid: String = Utils.API_KEY
    ): WeatherResponse

    @GET("weather?")
    suspend fun getWeatherByCity(
        @Query("q")
        city: String,
        @Query("appid")
        appid: String = Utils.API_KEY
    ): WeatherResponse
}