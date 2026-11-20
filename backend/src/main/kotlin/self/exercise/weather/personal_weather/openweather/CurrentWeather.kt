package self.exercise.weather.personal_weather.openweather

import java.time.Instant

/** Current conditions at a place. [observedAt] is when OpenWeather took the reading, not when we asked. */
data class CurrentWeather(
	val temperatureC: Double,
	val observedAt: Instant,
)

/** The parts of OpenWeather's current weather response we use; everything else is ignored. */
internal data class CurrentWeatherResponse(
	val main: Main,
	/** Reading time, in Unix seconds. */
	val dt: Long,
) {
	data class Main(val temp: Double)

	fun toCurrentWeather() = CurrentWeather(main.temp, Instant.ofEpochSecond(dt))
}
