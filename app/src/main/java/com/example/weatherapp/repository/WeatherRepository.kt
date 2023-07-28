package com.example.weatherapp.repository

import com.example.weatherapp.model.WeatherResponse
import com.example.weatherapp.service.WeatherService
import javax.inject.Inject

class WeatherRepository @Inject constructor(private val weatherService: WeatherService) {

    suspend fun getCurrentWeather(lat: String, lon: String): WeatherResponse {
        return weatherService.getCurrentWeather(lat, lon)
    }

    suspend fun getWeatherByCity(cityName: String): WeatherResponse {
        val city = "$cityName,us"
        return weatherService.getWeatherByCity(city)
    }
}