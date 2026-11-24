package self.exercise.weather.personal_weather.weather

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("weather")
data class WeatherProperties(
	/** How long a location's current weather is reused before asking OpenWeather again. */
	val cacheTtl: Duration = Duration.ofMinutes(15),
	/** Most OpenWeather calls in flight at once, across all requests. */
	val fetchConcurrency: Int = 3,
	val retry: Retry = Retry(),
) {
	/**
	 * Retries of a failed OpenWeather call. The wait before retry n is
	 * `delay * multiplier^(n-1)`, capped at [maxDelay], plus or minus up to [jitter].
	 */
	data class Retry(
		/** Retries after the first attempt, so 2 means up to 3 calls. */
		val maxRetries: Long = 2,
		val delay: Duration = Duration.ofMillis(500),
		val multiplier: Double = 2.0,
		val jitter: Duration = Duration.ofMillis(250),
		val maxDelay: Duration = Duration.ofSeconds(4),
	)
}
