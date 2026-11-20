package self.exercise.weather.personal_weather.weather

import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import self.exercise.weather.personal_weather.location.Location
import self.exercise.weather.personal_weather.openweather.CurrentWeather
import java.time.Instant

@WebMvcTest(WeatherController::class)
class WeatherControllerTest @Autowired constructor(private val mockMvc: MockMvc) {

	@MockitoBean
	lateinit var service: WeatherService

	private val saintPaul = Location("Saint Paul", "Minnesota", "US", 44.9497, -93.0931, id = 7)
	private val monaco = Location("Monaco", "", "MC", 43.73, 7.42, id = 9)

	@Test
	fun `returns each location with its rounded temperatures and reading time`() {
		given(service.currentForAll()).willReturn(
			listOf(
				LocationWeather(saintPaul, CurrentWeather(-3.21, Instant.parse("2026-11-20T22:40:00Z"))),
				LocationWeather(monaco, CurrentWeather(21.47, Instant.parse("2026-11-20T22:30:00Z"))),
			),
		)

		mockMvc.get("/api/weather").andExpect {
			status { isOk() }
			content {
				json(
					"""[
						{"id":7,"city":"Saint Paul","state":"Minnesota","country":"US",
						 "temperatureC":-3.2,"temperatureF":26,"observedAt":"2026-11-20T22:40:00Z"},
						{"id":9,"city":"Monaco","state":null,"country":"MC",
						 "temperatureC":21.5,"temperatureF":71,"observedAt":"2026-11-20T22:30:00Z"}
					]""",
					strict = true,
				)
			}
		}
	}

	@Test
	fun `a location without weather is still listed, with null weather fields`() {
		given(service.currentForAll()).willReturn(
			listOf(
				LocationWeather(saintPaul, null),
				LocationWeather(monaco, CurrentWeather(14.5, Instant.parse("2026-11-20T22:30:00Z"))),
			),
		)

		mockMvc.get("/api/weather").andExpect {
			status { isOk() }
			content {
				json(
					"""[
						{"id":7,"city":"Saint Paul","state":"Minnesota","country":"US",
						 "temperatureC":null,"temperatureF":null,"observedAt":null},
						{"id":9,"city":"Monaco","state":null,"country":"MC",
						 "temperatureC":14.5,"temperatureF":58,"observedAt":"2026-11-20T22:30:00Z"}
					]""",
					strict = true,
				)
			}
		}
	}

	@Test
	fun `no saved locations is an empty array`() {
		given(service.currentForAll()).willReturn(emptyList())

		mockMvc.get("/api/weather").andExpect {
			status { isOk() }
			content { json("[]", strict = true) }
		}
	}

	@Test
	fun `redis being down is service unavailable`() {
		given(service.currentForAll()).willThrow(RedisConnectionFailureException("Unable to connect to Redis"))

		mockMvc.get("/api/weather").andExpect {
			status { isEqualTo(503) }
			jsonPath("$.detail") { value("Service is temporarily unavailable. Please try again.") }
		}
	}
}
