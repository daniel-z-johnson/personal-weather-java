package self.exercise.weather.personal_weather.weather

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

/**
 * A saved location with its current temperature, rounded for display: Celsius to a tenth, Fahrenheit
 * to a whole degree. The weather fields are all null when OpenWeather couldn't provide it.
 */
data class LocationWeatherResponse(
	val id: Long,
	val city: String,
	val state: String?,
	val country: String,
	val temperatureC: Double?,
	val temperatureF: Int?,
	val observedAt: Instant?,
) {
	constructor(locationWeather: LocationWeather) : this(
		checkNotNull(locationWeather.location.id),
		locationWeather.location.city,
		locationWeather.location.state.ifEmpty { null },
		locationWeather.location.country,
		locationWeather.weather?.let { Temperatures.roundCelsius(it.temperatureC) },
		locationWeather.weather?.let { Temperatures.toRoundedFahrenheit(it.temperatureC) },
		locationWeather.weather?.observedAt,
	)
}

@RestController
@RequestMapping("/api/weather")
class WeatherController(private val weatherService: WeatherService) {

	/** Current weather for every saved location, oldest location first. */
	@GetMapping
	fun currentForAll(): List<LocationWeatherResponse> =
		weatherService.currentForAll().map(::LocationWeatherResponse)
}
