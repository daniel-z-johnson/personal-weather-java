package self.exercise.weather.personal_weather.openweather

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.body

@Component
@EnableConfigurationProperties(OpenWeatherProperties::class)
class OpenWeatherCurrentWeatherClient(
	builder: RestClient.Builder,
	private val properties: OpenWeatherProperties,
) {

	private val restClient = builder.baseUrl(properties.baseUrl).build()

	/**
	 * The current weather at the given coordinates, in Celsius.
	 *
	 * @throws RestClientException if OpenWeather fails, times out, or sends no body
	 */
	fun current(latitude: Double, longitude: Double): CurrentWeather {
		val response = restClient.get()
			.uri { uri ->
				uri.path("/data/2.5/weather")
					.queryParam("lat", "{lat}")
					.queryParam("lon", "{lon}")
					.queryParam("units", "metric")
					.queryParam("appid", "{appid}")
					.build(plain(latitude), plain(longitude), properties.apiKey)
			}
			.retrieve()
			.body<CurrentWeatherResponse>()
			?: throw EmptyResponseException()
		return response.toCurrentWeather()
	}

	/** An empty 200 from OpenWeather; a [RestClientException] so it's handled like other upstream failures. */
	class EmptyResponseException : RestClientException("OpenWeather returned no current weather")

	private companion object {
		/** Double.toString switches to scientific notation near zero (1.0E-4), which OpenWeather may not accept. */
		fun plain(value: Double): String = value.toBigDecimal().stripTrailingZeros().toPlainString()
	}
}
