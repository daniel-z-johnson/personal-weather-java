package self.exercise.weather.personal_weather.location

import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.web.client.HttpClientErrorException
import self.exercise.weather.personal_weather.openweather.GeoLocation
import self.exercise.weather.personal_weather.openweather.OpenWeatherGeocodingClient

@WebMvcTest(LocationController::class)
class LocationControllerTest @Autowired constructor(private val mockMvc: MockMvc) {

	@MockitoBean
	lateinit var client: OpenWeatherGeocodingClient

	@Test
	fun `returns the matches from OpenWeather`() {
		given(client.findByName("Saint Paul", "MN", "US"))
			.willReturn(listOf(GeoLocation("Saint Paul", "Minnesota", "US", 44.9497, -93.0931)))

		mockMvc.get("/api/locations/search") {
			param("city", "Saint Paul")
			param("state", "MN")
			param("country", "US")
		}.andExpect {
			status { isOk() }
			content {
				json(
					"""[{"name":"Saint Paul","state":"Minnesota","country":"US","lat":44.9497,"lon":-93.0931}]""",
					strict = true,
				)
			}
		}
	}

	@Test
	fun `state and country are optional, and blank ones are dropped`() {
		given(client.findByName("Paris", null, null)).willReturn(emptyList())

		mockMvc.get("/api/locations/search") {
			param("city", "Paris")
			param("state", "")
			param("country", " ")
		}.andExpect {
			status { isOk() }
			content { json("[]", strict = true) }
		}

		verify(client).findByName("Paris", null, null)
	}

	@Test
	fun `missing or blank city is a bad request`() {
		mockMvc.get("/api/locations/search").andExpect { status { isBadRequest() } }
		mockMvc.get("/api/locations/search") { param("city", " ") }.andExpect { status { isBadRequest() } }

		verifyNoInteractions(client)
	}

	@Test
	fun `an OpenWeather failure is a bad gateway`() {
		given(client.findByName("London", null, null))
			.willThrow(HttpClientErrorException(HttpStatus.UNAUTHORIZED))

		mockMvc.get("/api/locations/search?city=London")
			.andExpect {
				status { isEqualTo(502) }
				jsonPath("$.detail") { value("Location lookup is unavailable right now.") }
			}
	}
}
