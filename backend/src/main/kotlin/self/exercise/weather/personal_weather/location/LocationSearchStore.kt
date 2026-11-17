package self.exercise.weather.personal_weather.location

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import self.exercise.weather.personal_weather.openweather.GeoLocation
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.time.Duration
import java.util.UUID

@ConfigurationProperties("locations")
data class LocationSearchProperties(
	/** How long search results stay pickable. */
	val searchTtl: Duration = Duration.ofHours(1),
)

/**
 * Holds search results in Redis for a short time, so a later pick can be made by ID instead of
 * trusting coordinates sent by the client. Losing these (TTL, Redis restart) only means searching again.
 */
@Component
@EnableConfigurationProperties(LocationSearchProperties::class)
class LocationSearchStore(
	private val redis: StringRedisTemplate,
	private val jsonMapper: ObjectMapper,
	private val properties: LocationSearchProperties,
) {

	/** Stores [results] and returns the search ID; results are then addressed as `<searchId>:<index>`. */
	fun save(results: List<GeoLocation>): UUID {
		val searchId = UUID.randomUUID()
		redis.opsForValue().set(key(searchId), jsonMapper.writeValueAsString(results), properties.searchTtl)
		return searchId
	}

	/** The result at [index] of search [searchId], or null if the search expired or the index is out of range. */
	fun find(searchId: UUID, index: Int): GeoLocation? {
		val json = redis.opsForValue().get(key(searchId)) ?: return null
		return jsonMapper.readValue<List<GeoLocation>>(json).getOrNull(index)
	}

	companion object {
		fun key(searchId: UUID) = "location-search:$searchId"
	}
}
