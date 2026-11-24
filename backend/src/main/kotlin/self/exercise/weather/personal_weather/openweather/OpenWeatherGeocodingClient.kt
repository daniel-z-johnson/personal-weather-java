package self.exercise.weather.personal_weather.openweather

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
@EnableConfigurationProperties(OpenWeatherProperties::class)
class OpenWeatherGeocodingClient(
	builder: RestClient.Builder,
	private val properties: OpenWeatherProperties,
) {

	private val restClient = builder.baseUrl(properties.baseUrl).build()

	/**
	 * Looks up places by name, best match first. Returns an empty list when nothing matches.
	 *
	 * @param state state code; OpenWeather only supports this for the US
	 * @param country ISO 3166 country code; defaults to `US` when [state] is given, since OpenWeather
	 *   would otherwise read the state as the country (`Saint Paul,MN` is Mongolia)
	 * @param limit maximum number of matches; OpenWeather returns at most 5
	 */
	fun findByName(
		city: String,
		state: String? = null,
		country: String? = null,
		limit: Int = 5,
	): List<GeoLocation> {
		val effectiveCountry = country ?: state?.let { DEFAULT_STATE_COUNTRY }
		val query = listOfNotNull(city, state, effectiveCountry).joinToString(",")
		return restClient.get()
			.uri { uri ->
				uri.path("/geo/1.0/direct")
					.queryParam("q", "{q}")
					.queryParam("limit", limit)
					.queryParam("appid", "{appid}")
					.build(query, properties.apiKey)
			}
			.retrieve()
			.body<List<GeoLocation>>()
			.orEmpty()
	}

	private companion object {
		const val DEFAULT_STATE_COUNTRY = "US"
	}
}
