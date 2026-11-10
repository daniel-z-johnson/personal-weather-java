package self.exercise.weather.personal_weather.location

import jakarta.validation.constraints.NotBlank
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClientException
import self.exercise.weather.personal_weather.openweather.GeoLocation
import self.exercise.weather.personal_weather.openweather.OpenWeatherGeocodingClient

@RestController
@RequestMapping("/api/locations")
class LocationController(private val geocodingClient: OpenWeatherGeocodingClient) {

	private val log = LoggerFactory.getLogger(javaClass)

	/** Finds places matching the name using OpenWeather. Blank state or country is treated as absent. */
	@GetMapping("/search")
	fun search(
		@RequestParam @NotBlank city: String,
		@RequestParam(required = false) state: String?,
		@RequestParam(required = false) country: String?,
	): List<GeoLocation> =
		geocodingClient.findByName(
			city.trim(),
			state?.trim()?.ifBlank { null },
			country?.trim()?.ifBlank { null },
		)

	/** OpenWeather failed or was unreachable: that's an upstream problem, not ours, so 502 rather than 500. */
	@ExceptionHandler(RestClientException::class)
	fun openWeatherFailed(e: RestClientException): ProblemDetail {
		log.warn("OpenWeather geocoding call failed: {}", e.message)
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "Location lookup is unavailable right now.")
	}
}
