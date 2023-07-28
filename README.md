# WeatherApp
This is a minimalistic weather app that provides real-time weather information for locations in the United States. With this app, you can effortlessly check the weather in any US city by either searching for the city or granting location permission to fetch weather data based on your current location.

To use the app, you need to get an API key from the OpenWeatherMap website (https://openweathermap.org/) and then follow these steps:

Open the app and navigate to the gradle.properties file (Project Properties).
Look for the line: OPENWEATHERMAP_API_KEY="ENTER_YOUR_API_KEY_HERE".
Replace ENTER_YOUR_API_KEY_HERE with your API key.

I have spent approximately total of 5 hours to create this app, which includes the following breakdown:

1)Reading the documentation and planning work: 35 minutes.
2)Implementing service, Hilt, repository, and data classes: 1 hour and 20 minutes.
3)Working on the user interface, including ViewModel and Activity: 1 hour and 45 minutes.
4)Writing unit tests: 1:15 hour.

In this app, I used the following technologies:

Kotlin
MVVM Architecture
LiveData
Coroutines 
Retrofit 
Hilt 
JUnit4 
