package self.exercise.weather.personal_weather.location

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClientException

data class SelectLocationRequest(
	@NotBlank
	@Pattern(regexp = LocationService.SELECTION_ID_PATTERN, message = "must be an id from a search result")
	val id: String,
)

data class LocationResponse(
	val id: Long,
	val city: String,
	val state: String?,
	val country: String,
	val latitude: Double,
	val longitude: Double,
) {
	constructor(location: Location) : this(
		checkNotNull(location.id),
		location.city,
		location.state.ifEmpty { null },
		location.country,
		location.latitude,
		location.longitude,
	)
}

@RestController
@RequestMapping("/api/locations")
class LocationController(private val locationService: LocationService) {

	private val log = LoggerFactory.getLogger(javaClass)

	/** Finds places matching the name using OpenWeather. Blank state or country is treated as absent. */
	@GetMapping("/search")
	fun search(
		@RequestParam @NotBlank @Size(max = MAX_QUERY_LENGTH) city: String,
		@RequestParam(required = false) @Size(max = MAX_QUERY_LENGTH) state: String?,
		@RequestParam(required = false) @Size(max = MAX_QUERY_LENGTH) country: String?,
	): List<SearchResult> =
		locationService.search(
			city.trim(),
			state?.trim()?.ifBlank { null },
			country?.trim()?.ifBlank { null },
		)

	/** Saves the chosen search result: 201 if new, 200 if it was already saved. */
	@PostMapping
	fun select(@Valid @RequestBody request: SelectLocationRequest): ResponseEntity<LocationResponse> {
		val selected = locationService.select(request.id)
		val status = if (selected.created) HttpStatus.CREATED else HttpStatus.OK
		return ResponseEntity.status(status).body(LocationResponse(selected.location))
	}

	@ExceptionHandler(SearchSelectionNotFoundException::class)
	fun selectionNotFound(): ProblemDetail =
		ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Search result not found or expired. Please search again.")

	/** OpenWeather failed or was unreachable: that's an upstream problem, not ours, so 502 rather than 500. */
	@ExceptionHandler(RestClientException::class)
	fun openWeatherFailed(e: RestClientException): ProblemDetail {
		log.warn("OpenWeather geocoding call failed: {}", e.message)
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "Location lookup is unavailable right now.")
	}

	companion object {
		const val MAX_QUERY_LENGTH = 100
	}
}
