package self.exercise.weather.personal_weather.weather

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import self.exercise.weather.personal_weather.openweather.CurrentWeather
import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue

/**
 * Current weather per saved location, kept in Redis for [WeatherProperties.cacheTtl] so repeated
 * requests don't each call OpenWeather. Losing entries (TTL, Redis restart) only means fetching again.
 */
@Component
@EnableConfigurationProperties(WeatherProperties::class)
class WeatherCache(
	private val redis: StringRedisTemplate,
	private val jsonMapper: ObjectMapper,
	private val properties: WeatherProperties,
) {

	private val log = LoggerFactory.getLogger(javaClass)

	/**
	 * The cached weather for each of [locationIds] that has some, in one Redis round trip.
	 * Ids with nothing cached, or with an entry we can't read, are left out: callers treat them as misses.
	 */
	fun getAll(locationIds: Collection<Long>): Map<Long, CurrentWeather> {
		if (locationIds.isEmpty()) return emptyMap()

		val ids = locationIds.distinct()
		val values = redis.opsForValue().multiGet(ids.map(::key)).orEmpty()
		return ids.zip(values)
			.mapNotNull { (id, json) -> json?.let(::read)?.let { id to it } }
			.toMap()
	}

	fun put(locationId: Long, weather: CurrentWeather) {
		redis.opsForValue().set(key(locationId), jsonMapper.writeValueAsString(weather), properties.cacheTtl)
	}

	/** Null for an entry written in an older format; it's replaced on the next fetch. */
	private fun read(json: String): CurrentWeather? =
		try {
			jsonMapper.readValue<CurrentWeather>(json)
		} catch (e: JacksonException) {
			log.warn("Ignoring unreadable cached weather: {}", e.originalMessage)
			null
		}

	companion object {
		fun key(locationId: Long) = "weather:$locationId"
	}
}
