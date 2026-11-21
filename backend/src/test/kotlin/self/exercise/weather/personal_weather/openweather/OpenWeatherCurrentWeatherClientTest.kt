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
import org.springframework.web.client.RestClientException
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

@RestClientTest(
	OpenWeatherCurrentWeatherClient::class,
	properties = ["open-weather.base-url=https://weather.test", "open-weather.api-key=secret-key"],
)
class OpenWeatherCurrentWeatherClientTest @Autowired constructor(
	private val client: OpenWeatherCurrentWeatherClient,
	private val server: MockRestServiceServer,
) {

	@Test
	fun `sends the coordinates, metric units, and key, and parses the temperature and reading time`() {
		server.expect(requestTo("https://weather.test/data/2.5/weather?lat=44.9497&lon=-93.0931&units=metric&appid=secret-key"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(
				withSuccess(
					"""
					{
						"coord": {"lon": -93.0931, "lat": 44.9497},
						"weather": [{"id": 800, "main": "Clear", "description": "clear sky", "icon": "01n"}],
						"main": {"temp": -3.21, "feels_like": -7.5, "humidity": 70},
						"dt": 1795214400,
						"name": "Saint Paul",
						"cod": 200
					}
					""",
					MediaType.APPLICATION_JSON,
				)
			)

		val weather = client.current(44.9497, -93.0931)

		assertEquals(CurrentWeather(-3.21, Instant.ofEpochSecond(1795214400)), weather)
		server.verify()
	}

	@Test
	fun `sends coordinates near zero in plain notation`() {
		server.expect(requestTo("https://weather.test/data/2.5/weather?lat=51.4769&lon=-0.0005&units=metric&appid=secret-key"))
			.andRespond(withSuccess("""{"main": {"temp": 8.0}, "dt": 1795214400}""", MediaType.APPLICATION_JSON))

		client.current(51.4769, -0.0005)

		server.verify()
	}

	@Test
	fun `an empty response is a rest client failure`() {
		server.expect(requestTo("https://weather.test/data/2.5/weather?lat=1.5&lon=2.5&units=metric&appid=secret-key"))
			.andRespond(withSuccess())

		assertFailsWith<RestClientException> { client.current(1.5, 2.5) }
	}

	@Test
	fun `error responses throw without exposing the api key`() {
		server.expect(requestTo("https://weather.test/data/2.5/weather?lat=1.5&lon=2.5&units=metric&appid=secret-key"))
			.andRespond(withStatus(HttpStatus.UNAUTHORIZED))

		val error = assertFailsWith<HttpClientErrorException> { client.current(1.5, 2.5) }

		assertFalse(error.message.orEmpty().contains("secret-key"), error.message)
	}
}
