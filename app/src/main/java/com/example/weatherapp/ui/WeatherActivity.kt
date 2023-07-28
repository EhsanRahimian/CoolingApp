package com.example.weatherapp.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ActivityWeatherBinding
import com.example.weatherapp.util.DataState
import com.example.weatherapp.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.HttpException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class WeatherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherBinding
    private val viewModel: WeatherViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)
        if (!checkLocationPermissions()) {
            requestLocationPermissions()
        }
        viewModel.fetchWeatherBasedOnLastLocation()
        binding.fetchLocationButton.setOnClickListener {
            handleFetchLocationButtonClick()
        }
        viewModel.weatherData.observe(this) { dataState ->
            when (dataState) {
                is DataState.Empty -> {
                    binding.progressBar.visibility = View.GONE
                    binding.cityNameTextView.text = getString(R.string.city_name)
                    binding.temperatureTextView.text = getString(R.string.dc)
                    binding.feelsLikeTextView.text = getString(R.string.feels_like_dc)
                    binding.minTempTextView.text = getString(R.string.min_temp_dc)
                    binding.maxTempTextView.text = getString(R.string.max_temp_dc)
                    binding.humidityTextView.text = getString(R.string.humidity_percentage)
                    binding.visibilityTextView.text = getString(R.string.visibility_m)
                    binding.windSpeedTextView.text = getString(R.string.wind_speed_m_s)
                    binding.latitudeTextView.text = getString(R.string.latitude)
                    binding.longitudeTextView.text = getString(R.string.longitude)
                    binding.description.text = getString(R.string.empty)
                    binding.countryTextView.text = getString(R.string.country)
                    binding.currentTimeTextView.text =
                        getString(R.string.last_update_in_local_00_00_pm)
                    binding.sunriseTimeTextView.text = getString(R.string.sunrise_00_00_am)
                    binding.sunsetTimeTextView.text = getString(R.string.sunset_00_00_pm)
                    binding.weatherImageView.setImageResource(R.drawable.ic_launcher_foreground)
                    binding.tempUnitSwitch.isEnabled = false
                }

                is DataState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is DataState.Success -> {
                    binding.tempUnitSwitch.isEnabled = true
                    // show weather data
                    binding.progressBar.visibility = View.GONE
                    val weather = dataState.data
                    binding.cityNameTextView.text = weather.name

                    viewModel.isFahrenheit.observe(this) { isFahrenheit ->
                        val temperature =
                            if (isFahrenheit) viewModel.temperatureInFahrenheit.value else viewModel.temperatureInCelsius.value
                        val feelsLike =
                            if (isFahrenheit) viewModel.feelsLikeInFahrenheit.value else viewModel.feelsLikeInCelsius.value
                        val minTemp =
                            if (isFahrenheit) viewModel.minTempInFahrenheit.value else viewModel.minTempInCelsius.value
                        val maxTemp =
                            if (isFahrenheit) viewModel.maxTempInFahrenheit.value else viewModel.maxTempInCelsius.value

                        temperature?.let {
                            if (isFahrenheit) {
                                binding.temperatureTextView.text =
                                    getString(R.string.temperature_f, it)
                            } else {
                                binding.temperatureTextView.text =
                                    getString(R.string.temperature_c, it)
                            }
                        }

                        feelsLike?.let {
                            if (isFahrenheit) {
                                binding.feelsLikeTextView.text =
                                    getString(R.string.feels_like_f, it)
                            } else {
                                binding.feelsLikeTextView.text =
                                    getString(R.string.feels_like_c, it)
                            }
                        }

                        minTemp?.let {
                            if (isFahrenheit) {
                                binding.minTempTextView.text = getString(R.string.min_temp_f, it)
                            } else {
                                binding.minTempTextView.text = getString(R.string.min_temp_c, it)
                            }
                        }

                        maxTemp?.let {
                            if (isFahrenheit) {
                                binding.maxTempTextView.text = getString(R.string.max_temp_f, it)
                            } else {
                                binding.maxTempTextView.text = getString(R.string.max_temp_c, it)
                            }
                        }
                    }

                    binding.tempUnitSwitch.setOnCheckedChangeListener { _, isChecked ->
                        viewModel.isFahrenheit.value = isChecked
                    }

                    val humidity = weather.main?.humidity
                    binding.humidityTextView.text = getString(R.string.humidity_title, humidity)

                    val visibility = weather.visibility
                    binding.visibilityTextView.text =
                        getString(R.string.visibility_title, visibility)

                    val windSpeed = weather.wind?.speed
                    binding.windSpeedTextView.text = getString(R.string.wind_speed_title, windSpeed)

                    binding.description.text =
                        weather.weather.getOrNull(0)?.description ?: "No description available"

                    val countryCode = weather.sys?.country
                    val local = Locale("", countryCode.orEmpty())
                    val countryName = local.displayCountry
                    binding.countryTextView.text = getString(R.string.country_title, countryName)

                    weather.dt?.let {
                        weather.timezone?.let {
                            binding.currentTimeTextView.text = getString(
                                R.string.last_update,
                                unixTimestampToDateTime(weather.dt, weather.timezone)
                            )
                        }
                    }


                    weather.sys?.sunrise?.let {
                        weather.timezone?.let {
                            binding.sunriseTimeTextView.text = getString(
                                R.string.sunrise,
                                unixTimestampToDateTime(weather.sys.sunrise, weather.timezone)
                            )

                        }
                    }

                    weather.sys?.sunset?.let {
                        weather.timezone?.let {
                            binding.sunsetTimeTextView.text = getString(
                                R.string.sunset,
                                unixTimestampToDateTime(weather.sys.sunset, weather.timezone)
                            )

                        }
                    }
                    val weatherIcon = weather.weather.getOrNull(0)?.icon
                    if (weatherIcon != null) {
                        val iconUrl = "https://openweathermap.org/img/wn/${weatherIcon}@2x.png"
                        Glide.with(this)
                            .load(iconUrl)
                            //.error(R.drawable.ic_error)
                            .into(binding.weatherImageView)
                    } else {
                        binding.weatherImageView.setImageResource(R.drawable.ic_error)
                    }

                    viewModel.latitude.observe(this) { latitude ->
                        // Update the UI with the latitude value
                        val formattedLatitude = viewModel.formatCoordinate(latitude ?: 0.0)
                        binding.latitudeTextView.text =
                            getString(R.string.latitude_title, formattedLatitude)
                    }

                    viewModel.longitude.observe(this) { longitude ->
                        // Update the UI with the longitude value
                        val formattedLongitude = viewModel.formatCoordinate(longitude ?: 0.0)
                        binding.longitudeTextView.text =
                            getString(R.string.longitude_title, formattedLongitude)
                    }
                }

                is DataState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    if (dataState.exception is HttpException && (dataState.exception).code() == 404) {
                        // Show a specific error message for 404s
                        AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("City not found. Please try another city.")
                            .setPositiveButton("OK", null)
                            .show()

                    } else {
                        // show a generic error dialog for other exceptions
                        AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage(dataState.exception.message)
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // handle search here
                if (!query.isNullOrEmpty()) {
                    viewModel.searchWeather(query)
                    searchView.setQuery("", false) // clear the search query
                    searchView.clearFocus() // close the keyboard
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun unixTimestampToDateTime(unixTimestamp: Long, timezoneOffset: Int): String {
        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(unixTimestamp),
            ZoneOffset.ofTotalSeconds(timezoneOffset)
        )
        return dateTime.format(DateTimeFormatter.ofPattern(getString(R.string.hh_mm_ss_dd_mm_yyyy)))
    }

    private fun checkLocationPermissions(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    // Function to request location permissions
    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            Utils.LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    // Handle the permission request result
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Utils.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                getCurrentLocationData()
                //viewModel.fetchWeatherBasedOnLastLocation()
            } else {
                // Permissions denied

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleFetchLocationButtonClick() {
        if (checkLocationPermissions()) {
            // Permissions are granted, proceed to get the current location and fetch weather
            getCurrentLocationData()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Location Access Permission")
                .setMessage(
                    getString(R.string.access_to_the_location_permission_message)
                )
                .setPositiveButton("OK", null)
                .show()
            requestLocationPermissions()
        }
    }

    // Function to get the current location
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentLocationData() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("Location Permissions", "Location permissions are granted")
            val location: Location? =
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                viewModel.setLocation(latitude, longitude)

                viewModel.saveLastLocation(latitude, longitude)
                viewModel.fetchWeatherBasedOnLocation(latitude.toString(), longitude.toString())

            } else {
                // Location is null, handle accordingly
                Log.d(
                    "Current Location",
                    "Location is null."
                )
                requestLocationPermissions()
            }
        } else {
            // Location permission not granted, handle accordingly
            Log.d(
                "Current Location",
                "Location permission not granted."
            )
            requestLocationPermissions()
        }
    }
}