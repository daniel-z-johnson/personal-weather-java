package self.exercise.weather.personal_weather.location

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import self.exercise.weather.personal_weather.openweather.OpenWeatherGeocodingClient
import java.util.UUID

/** A search match the user can pick by [id]. */
data class SearchResult(
	val id: String,
	val city: String,
	val state: String?,
	val country: String,
	val latitude: Double,
	val longitude: Double,
)

data class SelectedLocation(val location: Location, val created: Boolean)

class SearchSelectionNotFoundException : RuntimeException("Search result not found or expired")

class LocationNotFoundException(id: Long) : RuntimeException("Location $id not found")

@Service
class LocationService(
	private val geocodingClient: OpenWeatherGeocodingClient,
	private val searchStore: LocationSearchStore,
	private val repository: LocationRepository,
) {

	fun search(city: String, state: String?, country: String?): List<SearchResult> {
		val matches = geocodingClient.findByName(city, state, country)
		if (matches.isEmpty()) return emptyList()

		val searchId = searchStore.save(matches)
		return matches.mapIndexed { index, match ->
			SearchResult("$searchId:$index", match.city, match.state, match.country, match.lat, match.lon)
		}
	}

	/**
	 * Saves the search result [id] refers to. Picking an already saved place returns the existing row,
	 * so repeating the request is harmless.
	 *
	 * @throws SearchSelectionNotFoundException if [id] is malformed, expired, or out of range
	 */
	fun select(id: String): SelectedLocation {
		val (searchId, index) = parseSelectionId(id) ?: throw SearchSelectionNotFoundException()
		val match = searchStore.find(searchId, index) ?: throw SearchSelectionNotFoundException()

		repository.findByLatitudeAndLongitude(match.lat, match.lon)?.let { return SelectedLocation(it, created = false) }

		val location = Location(match.city, match.state.orEmpty(), match.country, match.lat, match.lon)
		return try {
			SelectedLocation(repository.save(location), created = true)
		} catch (e: DataIntegrityViolationException) {
			// Another request saved the same place between our lookup and insert.
			val existing = repository.findByLatitudeAndLongitude(match.lat, match.lon) ?: throw e
			SelectedLocation(existing, created = false)
		}
	}

	/** Every saved location, oldest first. */
	fun list(): List<Location> = repository.findAll(Sort.by("id"))

	/** @throws LocationNotFoundException if there is no saved location with [id] */
	fun delete(id: Long) {
		if (!repository.existsById(id)) throw LocationNotFoundException(id)
		repository.deleteById(id)
	}

	companion object {
		const val SELECTION_ID_PATTERN = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}:[0-9]{1,2}$"
		private val selectionIdRegex = Regex(SELECTION_ID_PATTERN)

		fun parseSelectionId(id: String): Pair<UUID, Int>? {
			if (!selectionIdRegex.matches(id)) return null
			val (uuid, index) = id.split(":")
			return UUID.fromString(uuid) to index.toInt()
		}
	}
}
