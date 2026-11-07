package self.exercise.weather.personal_weather.openweather

/** A match from OpenWeather's geocoding API. Not every place has a [state]. */
data class GeoLocation(
	val name: String,
	val state: String? = null,
	val country: String,
	val lat: Double,
	val lon: Double,
)
