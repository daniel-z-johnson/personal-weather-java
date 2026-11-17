package self.exercise.weather.personal_weather.openweather

import com.fasterxml.jackson.annotation.JsonAlias

/**
 * A match from OpenWeather's geocoding API. Not every place has a [state].
 * OpenWeather calls the city `name`; the alias reads that, while our own stored copies use `city`.
 */
data class GeoLocation(
	@JsonAlias("name") val city: String,
	val state: String? = null,
	val country: String,
	val lat: Double,
	val lon: Double,
)
