package com.example.weatherapp.store

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefs @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "WeatherAppPrefs"
        private const val KEY_LONGITUDE = "longitude"
        private const val KEY_LATITUDE = "latitude"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveLastLocation(latitude: Double, longitude: Double) {
        sharedPreferences.edit()
            .putFloat(KEY_LATITUDE, latitude.toFloat())
            .putFloat(KEY_LONGITUDE, longitude.toFloat())
            .apply()
    }

    fun getLastLatitude(): Double {
        return sharedPreferences.getFloat(KEY_LATITUDE, 0.0f).toDouble()
    }

    fun getLastLongitude(): Double {
        return sharedPreferences.getFloat(KEY_LONGITUDE, 0.0f).toDouble()
    }

}