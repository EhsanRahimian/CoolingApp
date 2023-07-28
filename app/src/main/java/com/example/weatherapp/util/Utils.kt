package com.example.weatherapp.util

import com.example.weatherapp.BuildConfig

class Utils {
    companion object{
        var BASE_URL : String = "https://api.openweathermap.org/data/2.5/"
        var API_KEY = BuildConfig.OPENWEATHERMAP_API_KEY
        const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }
}