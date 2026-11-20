package self.exercise.weather.personal_weather.weather

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import self.exercise.weather.personal_weather.location.Location
import self.exercise.weather.personal_weather.location.LocationService
import self.exercise.weather.personal_weather.openweather.CurrentWeather
import self.exercise.weather.personal_weather.openweather.OpenWeatherCurrentWeatherClient

/** A saved location and its current weather; [weather] is null when OpenWeather couldn't provide it. */
data class LocationWeather(val location: Location, val weather: CurrentWeather?)

@Service
class WeatherService(
	private val locationService: LocationService,
	private val weatherClient: OpenWeatherCurrentWeatherClient,
	private val cache: WeatherCache,
) {

	private val log = LoggerFactory.getLogger(javaClass)

	/**
	 * Current weather for every saved location, oldest location first. Cached weather is used when
	 * there is some; the rest is fetched from OpenWeather and cached. A location whose fetch fails
	 * gets null weather rather than failing the whole list.
	 */
	fun currentForAll(): List<LocationWeather> {
		val locations = locationService.list()
		val cached = cache.getAll(locations.map(::idOf))
		return locations.map { location ->
			LocationWeather(location, cached[idOf(location)] ?: fetch(location))
		}
	}

	/** Fetches and caches one location's weather, or null if OpenWeather failed. Failures aren't cached, so the next request retries. */
	private fun fetch(location: Location): CurrentWeather? {
		val weather = try {
			weatherClient.current(location.latitude, location.longitude)
		} catch (e: RestClientException) {
			if (e is RestClientResponseException) {
				// Status only: the message of an error response includes the upstream body, which we don't control.
				log.warn("Current weather for location {} failed with status {}", location.id, e.statusCode.value())
			} else {
				log.warn("Current weather for location {} failed: {}", location.id, e.message)
			}
			return null
		}
		cache.put(idOf(location), weather)
		return weather
	}

	private fun idOf(location: Location): Long = checkNotNull(location.id) { "Location was never saved" }
}
