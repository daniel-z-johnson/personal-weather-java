package self.exercise.weather.personal_weather.openweather

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.HttpClientErrorException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

@RestClientTest(
	OpenWeatherGeocodingClient::class,
	properties = ["open-weather.base-url=https://geo.test", "open-weather.api-key=secret-key"],
)
class OpenWeatherGeocodingClientTest @Autowired constructor(
	private val client: OpenWeatherGeocodingClient,
	private val server: MockRestServiceServer,
) {

	@Test
	fun `sends the query and key, and parses matches ignoring unknown fields`() {
		server.expect(requestTo("https://geo.test/geo/1.0/direct?q=Saint%20Paul%2CMN%2CUS&limit=5&appid=secret-key"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(
				withSuccess(
					"""
					[{
						"name": "Saint Paul",
						"local_names": {"en": "Saint Paul"},
						"lat": 44.9497,
						"lon": -93.0931,
						"country": "US",
						"state": "Minnesota"
					}]
					""",
					MediaType.APPLICATION_JSON,
				)
			)

		val matches = client.findByName("Saint Paul", "MN", "US")

		assertEquals(listOf(GeoLocation("Saint Paul", "Minnesota", "US", 44.9497, -93.0931)), matches)
		server.verify()
	}

	@Test
	fun `omits missing parts of the query and allows a match with no state`() {
		server.expect(requestTo("https://geo.test/geo/1.0/direct?q=Monaco&limit=1&appid=secret-key"))
			.andRespond(
				withSuccess(
					"""[{"name": "Monaco", "lat": 43.73, "lon": 7.42, "country": "MC"}]""",
					MediaType.APPLICATION_JSON,
				)
			)

		val matches = client.findByName("Monaco", limit = 1)

		assertEquals(listOf(GeoLocation("Monaco", null, "MC", 43.73, 7.42)), matches)
		server.verify()
	}

	@Test
	fun `returns an empty list when nothing matches`() {
		server.expect(requestTo("https://geo.test/geo/1.0/direct?q=Nowhere&limit=5&appid=secret-key"))
			.andRespond(withSuccess("[]", MediaType.APPLICATION_JSON))

		assertEquals(emptyList(), client.findByName("Nowhere"))
	}

	@Test
	fun `error responses throw without exposing the api key`() {
		server.expect(requestTo("https://geo.test/geo/1.0/direct?q=London&limit=5&appid=secret-key"))
			.andRespond(withStatus(HttpStatus.UNAUTHORIZED))

		val error = assertFailsWith<HttpClientErrorException> { client.findByName("London") }

		assertFalse(error.message.orEmpty().contains("secret-key"), error.message)
	}
}
