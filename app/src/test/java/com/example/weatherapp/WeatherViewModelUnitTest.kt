package com.example.weatherapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherapp.model.Clouds
import com.example.weatherapp.model.Coord
import com.example.weatherapp.model.Main
import com.example.weatherapp.model.Sys
import com.example.weatherapp.model.Weather
import com.example.weatherapp.model.WeatherResponse
import com.example.weatherapp.model.Wind
import com.example.weatherapp.repository.WeatherRepository
import com.example.weatherapp.store.SharedPrefs
import com.example.weatherapp.ui.WeatherViewModel
import com.example.weatherapp.util.DataState
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi

class WeatherViewModelUnitTest {
    private val weatherRepository = mockk<WeatherRepository>()
    private val sharedPrefs = mockk<SharedPrefs>()

    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test dispatcher for coroutines
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `Verify that the weather data is fetched correctly when there is a last known location`() {
        // Define the latitude and longitude of the last known location
        val latitude = 37.7749
        val longitude = -122.4194
        val viewModel = createViewModel()

        // Mock the response from the repository
        val weatherResponse = WeatherResponse(/* Populate with response data */)
        coEvery { weatherRepository.getCurrentWeather(latitude.toString(), longitude.toString()) } returns weatherResponse

        // Mock the last known location in sharedPrefs
        coEvery { sharedPrefs.getLastLatitude() } returns latitude
        coEvery { sharedPrefs.getLastLongitude() } returns longitude

        // Call the fetchWeatherBasedOnLastLocation method
        viewModel.fetchWeatherBasedOnLastLocation()

        // Assert that the weather data is fetched correctly
        assert(viewModel.weatherData.value is DataState.Success)
        assert(viewModel.latitude.value == latitude)
        assert(viewModel.longitude.value == longitude)
    }

    @Test
    fun `Verify that the error state is set correctly when an exception occurs during data fetching`() {

        // Define the latitude and longitude of the last known location
        val latitude = 37.7749
        val longitude = -122.4194
        val viewModel = createViewModel()

        // Mock the exception to be thrown by the repository
        val exception = IOException("Network error")
        coEvery { weatherRepository.getCurrentWeather(latitude.toString(), longitude.toString()) } throws exception

        // Mock the last known location in sharedPrefs
        coEvery { sharedPrefs.getLastLatitude() } returns latitude
        coEvery { sharedPrefs.getLastLongitude() } returns longitude

        // Call the fetchWeatherBasedOnLastLocation method
        viewModel.fetchWeatherBasedOnLastLocation()

        // Assert that the weatherData contains the correct error state
        assert(viewModel.weatherData.value is DataState.Error)
        assert((viewModel.weatherData.value as DataState.Error).exception == exception)
    }

    @Test
    fun `Verify that the latitude and longitude values are correctly set in the ViewModel`()  {
        // Define the latitude and longitude of the last known location
        val latitude = 37.7749
        val longitude = -122.4194
        val viewModel = createViewModel()

        // Mock the last known location in sharedPrefs
        coEvery { sharedPrefs.getLastLatitude() } returns latitude
        coEvery { sharedPrefs.getLastLongitude() } returns longitude

        // Call the fetchWeatherBasedOnLastLocation method
        viewModel.fetchWeatherBasedOnLastLocation()

        // Assert that the latitude and longitude values are correctly set
        assert(viewModel.latitude.value == latitude)
        assert(viewModel.longitude.value == longitude)
    }

    @Test
    fun `Verify that the weather data is fetched correctly when providing a valid city name`() {

        // Mock the response from the repository
        val weatherResponse = createWeatherResponse()

        coEvery { weatherRepository.getWeatherByCity("New York") } returns weatherResponse

        // Mock the response for saveLastLocation
        coEvery { sharedPrefs.saveLastLocation(any(), any()) } returns Unit

        // Create the ViewModel
        val viewModel = WeatherViewModel(weatherRepository, sharedPrefs)

        // Call the searchWeather method
        viewModel.searchWeather("New York")

        // Assert that the weather data is fetched correctly
        assert(viewModel.weatherData.value is DataState.Success)
        assert(viewModel.latitude.value == weatherResponse.coord?.lat)
        assert(viewModel.longitude.value == weatherResponse.coord?.lon)
    }

    @Test
    fun `Verify that the error state is set correctly when the city is not found (HTTP 404)`() {
        // Define the city name that will trigger a 404 response
        val city = "NonExistentCity"

        // Mock the response from the repository to return an HTTP 404 error
        val errorResponseBody = ResponseBody.create(null, "{}")
        val exception = HttpException(Response.error<Any>(404, errorResponseBody))
        coEvery { weatherRepository.getWeatherByCity(city) } throws exception

        // Create the ViewModel
        val viewModel = WeatherViewModel(weatherRepository, sharedPrefs)

        // Call the searchWeather method
        runBlocking { viewModel.searchWeather(city) }

        // Assert that the weather data is in the error state
        assert(viewModel.weatherData.value is DataState.Error)
    }

    @Test
    fun `Verify that the latitude and longitude values are correctly set in the ViewModel when calling setLocation`() {
        // Define the latitude and longitude
        val latitude = 37.7749
        val longitude = -122.4194

        // Create the ViewModel
        val viewModel = createViewModel()

        // Call the setLocation method
        viewModel.setLocation(latitude, longitude)

        // Assert that the latitude and longitude are correctly set
        TestCase.assertEquals(latitude, viewModel.latitude.value)
        TestCase.assertEquals(longitude, viewModel.longitude.value)
    }

    @Test
    fun `Verify that kelvinToCelsius correctly converts Kelvin to Celsius for various temperature values`() {
        // Define test cases with expected results
        val viewModel = createViewModel()
        val testCases = listOf(
            300.0 to 26.85, // 300 Kelvin should be 26.85 Celsius
            273.15 to 0.0,    // 273.15 Kelvin should be 0 Celsius
            310.0 to 36.85,   // 310 Kelvin should be 36.85 Celsius
            0.0 to -273.15    // 0 Kelvin should be -273.15 Celsius
        )

        // Test each case
        testCases.forEach { (kelvin, expectedCelsius) ->
            val actualCelsius = viewModel.kelvinToCelsius(kelvin)
            actualCelsius?.let { TestCase.assertEquals(expectedCelsius, it, 0.01) } // Using delta for floating-point comparison
        }
    }

    @Test
    fun `Verify that kelvinToCelsius returns null when the input temperature is null`() {
        val viewModel = createViewModel()
        val temperatureInKelvin: Double? = null
        val actualCelsius = viewModel.kelvinToCelsius(temperatureInKelvin)
        TestCase.assertEquals(null, actualCelsius)
    }

    @Test
    fun `Verify that celsiusToFahrenheit correctly converts Celsius to Fahrenheit for various temperature values`() {
        // Define test cases with expected results
        val viewModel = createViewModel()
        val testCases = listOf(
            0.0 to 32.0,    // 0 Celsius should be 32 Fahrenheit
            25.0 to 77.0,   // 25 Celsius should be 77 Fahrenheit
            -10.0 to 14.0,  // -10 Celsius should be 14 Fahrenheit
            100.0 to 212.0  // 100 Celsius should be 212 Fahrenheit
        )

        // Test each case
        testCases.forEach { (celsius, expectedFahrenheit) ->
            val actualFahrenheit = viewModel.celsiusToFahrenheit(celsius)
            actualFahrenheit?.let { TestCase.assertEquals(expectedFahrenheit, it, 0.01) }
        }
    }

    @Test
    fun `Verify that celsiusToFahrenheit returns null when the input temperature is null`() {
        val temperatureInCelsius: Double? = null
        val viewModel = createViewModel()
        val actualFahrenheit = viewModel.celsiusToFahrenheit(temperatureInCelsius)
        TestCase.assertEquals(null, actualFahrenheit)
    }

    private fun createWeatherResponse(): WeatherResponse {
        return WeatherResponse(
            coord = Coord(lon = 40.7128, lat = -74.0060),
            weather = mutableListOf(
                Weather(id = 800, main = "Clear", description = "clear sky", icon = "01d"),
                Weather(id = 801, main = "Clouds", description = "few clouds", icon = "02d")
            ) as ArrayList<Weather>,
            base = "base",
            main = Main(temp = 25.0, feelsLike = 24.0),
            visibility = 10000,
            wind = Wind(speed = 10.0, deg = null),
            clouds = Clouds(all = 10),
            dt = 1627000000,
            sys = Sys(type = 1, id = 1234, country = "US", sunrise = 1627000000, sunset = 1627000000),
            timezone = 3600,
            id = 5128581,
            name = "New York",
            cod = 200
        )
    }

    private fun createViewModel(): WeatherViewModel {
        return WeatherViewModel(weatherRepository, sharedPrefs)
    }
}