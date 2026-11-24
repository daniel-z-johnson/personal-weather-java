package self.exercise.weather.personal_weather.weather

import org.slf4j.LoggerFactory
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.retry.RetryTemplate
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import self.exercise.weather.personal_weather.location.Location
import self.exercise.weather.personal_weather.location.LocationService
import self.exercise.weather.personal_weather.openweather.CurrentWeather
import self.exercise.weather.personal_weather.openweather.OpenWeatherCurrentWeatherClient
import self.exercise.weather.personal_weather.openweather.redactApiKey
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.function.Supplier

/** A saved location and its current weather; [weather] is null when OpenWeather couldn't provide it. */
data class LocationWeather(val location: Location, val weather: CurrentWeather?)

@Service
class WeatherService(
	private val locationService: LocationService,
	private val weatherClient: OpenWeatherCurrentWeatherClient,
	private val cache: WeatherCache,
	properties: WeatherProperties,
) {

	private val log = LoggerFactory.getLogger(javaClass)

	/** Shared by every request, so the limit holds however many requests are fetching at once. */
	private val openWeatherPermits = Semaphore(properties.fetchConcurrency)

	// TODO: On a 429, wait for OpenWeather's Retry-After header instead of the exponential backoff
	//  below, falling back to the backoff when the header is missing. For now every retry uses the backoff.
	private val retryTemplate = RetryTemplate(
		RetryPolicy.builder()
			.maxRetries(properties.retry.maxRetries)
			.delay(properties.retry.delay)
			.multiplier(properties.retry.multiplier)
			.jitter(properties.retry.jitter)
			.maxDelay(properties.retry.maxDelay)
			.predicate(::isTransient)
			.build()
	)

	/**
	 * Current weather for every saved location, oldest location first. Cached weather is used when
	 * there is some; the rest is fetched from OpenWeather in parallel and cached. A location whose
	 * fetch fails gets null weather rather than failing the whole list.
	 */
	fun currentForAll(): List<LocationWeather> {
		val locations = locationService.list()
		val cached = cache.getAll(locations.map(::idOf))
		val fetched = fetchAll(locations.filter { idOf(it) !in cached })
		return locations.map { location ->
			val id = idOf(location)
			LocationWeather(location, cached[id] ?: fetched[id])
		}
	}

	/** Fetches [locations] concurrently, each on its own virtual thread; [openWeatherPermits] caps the calls actually in flight. */
	private fun fetchAll(locations: List<Location>): Map<Long, CurrentWeather?> {
		if (locations.isEmpty()) return emptyMap()
		Executors.newVirtualThreadPerTaskExecutor().use { executor ->
			val futures = locations.associate { idOf(it) to executor.submit(Callable { fetch(it) }) }
			return futures.mapValues { (_, future) ->
				try {
					future.get()
				} catch (e: ExecutionException) {
					// Rethrow what fetch threw (a Redis failure, say), so the exception handler sees the real cause.
					throw e.cause ?: e
				}
			}
		}
	}

	/** Fetches and caches one location's weather, or null if OpenWeather failed. Failures aren't cached, so the next request retries. */
	private fun fetch(location: Location): CurrentWeather? {
		val weather = try {
			retryTemplate.invoke(Supplier { callOpenWeather(location) })
		} catch (e: RestClientException) {
			if (e is RestClientResponseException) {
				// Status only: the message of an error response includes the upstream body, which we don't control.
				log.warn("Current weather for location {} failed with status {}", location.id, e.statusCode.value())
			} else {
				log.warn("Current weather for location {} failed: {}", location.id, redactApiKey(e.message))
			}
			return null
		}
		cache.put(idOf(location), weather)
		return weather
	}

	/** One attempt. The permit covers only the call itself, so a location waiting to retry doesn't hold one. */
	private fun callOpenWeather(location: Location): CurrentWeather {
		openWeatherPermits.acquire()
		try {
			return weatherClient.current(location.latitude, location.longitude)
		} finally {
			openWeatherPermits.release()
		}
	}

	private fun idOf(location: Location): Long = checkNotNull(location.id) { "Location was never saved" }

	private companion object {
		/**
		 * Failures that may pass on their own: timeouts and I/O errors, OpenWeather's own errors, and rate limiting.
		 * Anything else (a bad key, a malformed response) would fail the same way again.
		 */
		fun isTransient(e: Throwable): Boolean =
			e is ResourceAccessException ||
				e is HttpServerErrorException ||
				e is HttpClientErrorException.TooManyRequests
	}
}
