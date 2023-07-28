package com.example.weatherapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.model.WeatherResponse
import com.example.weatherapp.repository.WeatherRepository
import com.example.weatherapp.store.SharedPrefs
import com.example.weatherapp.util.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.DecimalFormat
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject
constructor(
    private val repository: WeatherRepository,
    private val sharedPrefs: SharedPrefs,
) : ViewModel() {

    private val _weatherData = MutableLiveData<DataState<WeatherResponse>>()
    val weatherData: LiveData<DataState<WeatherResponse>> get() = _weatherData

    // Temperature LiveData
    private val _temperatureInCelsius = MutableLiveData<Double?>()
    val temperatureInCelsius: LiveData<Double?> get() = _temperatureInCelsius

    private val _temperatureInFahrenheit = MutableLiveData<Double?>()
    val temperatureInFahrenheit: LiveData<Double?> get() = _temperatureInFahrenheit

    private val _feelsLikeInCelsius = MutableLiveData<Double?>()
    val feelsLikeInCelsius: LiveData<Double?> get() = _feelsLikeInCelsius

    private val _feelsLikeInFahrenheit = MutableLiveData<Double?>()
    val feelsLikeInFahrenheit: LiveData<Double?> get() = _feelsLikeInFahrenheit

    private val _minTempInCelsius = MutableLiveData<Double?>()
    val minTempInCelsius: LiveData<Double?> get() = _minTempInCelsius

    private val _minTempInFahrenheit = MutableLiveData<Double?>()
    val minTempInFahrenheit: LiveData<Double?> get() = _minTempInFahrenheit

    private val _maxTempInCelsius = MutableLiveData<Double?>()
    val maxTempInCelsius: LiveData<Double?> get() = _maxTempInCelsius

    private val _maxTempInFahrenheit = MutableLiveData<Double?>()
    val maxTempInFahrenheit: LiveData<Double?> get() = _maxTempInFahrenheit

    // Location LiveData
    private val _latitude = MutableLiveData<Double?>()
    val latitude: LiveData<Double?> get() = _latitude

    private val _longitude = MutableLiveData<Double?>()
    val longitude: LiveData<Double?> get() = _longitude


    val isFahrenheit: MutableLiveData<Boolean> = MutableLiveData(false)

    // Function to set latitude and longitude values
    fun setLocation(latitude: Double?, longitude: Double?) {
        _latitude.value = latitude
        _longitude.value = longitude
    }

    private fun updateTemperatures(weather: WeatherResponse) {
        val temperatureInKelvin = weather.main?.temp
        val feelsLikeInKelvin = weather.main?.feelsLike
        val minTempInKelvin = weather.main?.tempMin
        val maxTempInKelvin = weather.main?.tempMax

        if (isFahrenheit.value == true) {
            _temperatureInCelsius.value = kelvinToCelsius(temperatureInKelvin)
            _feelsLikeInCelsius.value = kelvinToCelsius(feelsLikeInKelvin)
            _minTempInCelsius.value = kelvinToCelsius(minTempInKelvin)
            _maxTempInCelsius.value = kelvinToCelsius(maxTempInKelvin)

            _temperatureInFahrenheit.value = celsiusToFahrenheit(_temperatureInCelsius.value)
            _feelsLikeInFahrenheit.value = celsiusToFahrenheit(_feelsLikeInCelsius.value)
            _minTempInFahrenheit.value = celsiusToFahrenheit(_minTempInCelsius.value)
            _maxTempInFahrenheit.value = celsiusToFahrenheit(_maxTempInCelsius.value)
        } else {
            _temperatureInCelsius.value = kelvinToCelsius(temperatureInKelvin)
            _feelsLikeInCelsius.value = kelvinToCelsius(feelsLikeInKelvin)
            _minTempInCelsius.value = kelvinToCelsius(minTempInKelvin)
            _maxTempInCelsius.value = kelvinToCelsius(maxTempInKelvin)

            _temperatureInFahrenheit.value = celsiusToFahrenheit(_temperatureInCelsius.value)
            _feelsLikeInFahrenheit.value = celsiusToFahrenheit(_feelsLikeInCelsius.value)
            _minTempInFahrenheit.value = celsiusToFahrenheit(_minTempInCelsius.value)
            _maxTempInFahrenheit.value = celsiusToFahrenheit(_maxTempInCelsius.value)
        }
    }

    fun kelvinToCelsius(temperatureInKelvin: Double?): Double? {
        return if (temperatureInKelvin != null) temperatureInKelvin - 273.15 else null
    }

    fun celsiusToFahrenheit(temperatureInCelsius: Double?): Double? {
        return if (temperatureInCelsius != null) (temperatureInCelsius * 9 / 5) + 32 else null
    }

    // Method to fetch weather based on the last known location
    fun fetchWeatherBasedOnLastLocation() {
        //Log.d("BasedOnLastLocation", " BasedOnLastLocation called ")
        val latitude = sharedPrefs.getLastLatitude()
        val longitude = sharedPrefs.getLastLongitude()
        setLocation(latitude, longitude)
//        Log.d("BasedOnLastLocation", " ${sharedPrefs.getLastLatitude()} ")
//        Log.d("BasedOnLastLocation", " ${sharedPrefs.getLastLongitude()} ")
        if (latitude != 0.0 && longitude != 0.0) {
            viewModelScope.launch {
                _weatherData.value = DataState.Loading
                try {
                    val response = repository.getCurrentWeather(
                        latitude.toString(),
                        longitude.toString()
                    )
                    updateTemperatures(response)
                    _weatherData.value = DataState.Success(response)
                } catch (e: Exception) {
                    _weatherData.value = DataState.Error(e)
                }
            }
        }
    }

    // Method to fetch weather based on the current location
    fun fetchWeatherBasedOnLocation(lat: String, lon: String) {
        viewModelScope.launch {
            _weatherData.value = DataState.Empty
            _weatherData.value = DataState.Loading
            try {
                val response = repository.getCurrentWeather(lat, lon)
                updateTemperatures(response)
                _weatherData.value = DataState.Success(response)
                // Save the searched location to SharedPrefs
                val latitude = lat.toDouble()
                val longitude = lon.toDouble()
                sharedPrefs.saveLastLocation(latitude, longitude)
                setLocation(lat.toDouble(), lon.toDouble())
            } catch (e: Exception) {
                _weatherData.value = DataState.Error(e)
            }
        }
    }

    // Method to fetch weather based on usa city search
    fun searchWeather(city: String) {
        if (city.trim().isNotEmpty()) {
            viewModelScope.launch {
                _weatherData.value = DataState.Empty
                _weatherData.value = DataState.Loading

                try {
                    // Fetch weather data for the searched city
                    val weatherResponse = repository.getWeatherByCity(city)
                    val latitude = weatherResponse.coord?.lat ?: 0.0
                    val longitude = weatherResponse.coord?.lon ?: 0.0
                    // Update latitude and longitude in ViewModel
                    _latitude.value = latitude
                    _longitude.value = longitude
                    // Save the last searched location to SharedPrefs
                    sharedPrefs.saveLastLocation(latitude, longitude)
                    // Update temperatures and weather data in ViewModel
                    updateTemperatures(weatherResponse)
                    _weatherData.value = DataState.Success(weatherResponse)
                    setLocation(latitude, longitude)
                } catch (e: HttpException) {
                    // Handle HTTP exceptions separately
                    if (e.code() == 404) {
                        _weatherData.value =
                            DataState.Error(Exception("City not found. Please try another city."))
                    } else {
                        _weatherData.value = DataState.Error(e)
                    }
                } catch (e: Exception) {
                    _weatherData.value = DataState.Error(e)
                }
            }
        } else {
            _weatherData.value = DataState.Error(Exception("Invalid city name."))
        }
    }

    fun saveLastLocation(latitude: Double, longitude: Double) {
        sharedPrefs.saveLastLocation(latitude, longitude)
    }

    fun formatCoordinate(value: Double): String {
        val decimalFormat = DecimalFormat("#.####")
        return decimalFormat.format(value)
    }
}