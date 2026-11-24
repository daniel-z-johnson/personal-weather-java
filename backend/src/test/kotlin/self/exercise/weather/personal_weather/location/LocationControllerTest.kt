package self.exercise.weather.personal_weather.location

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willThrow
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@WebMvcTest(LocationController::class)
class LocationControllerTest @Autowired constructor(private val mockMvc: MockMvc) {

	@MockitoBean
	lateinit var service: LocationService

	private val selectionId = "3f2a9c1e-0b7d-4c55-9a51-2e8f6d4b1a00:0"

	@Test
	fun `search returns the matches with their ids`() {
		given(service.search("Saint Paul", "MN", "US"))
			.willReturn(listOf(SearchResult(selectionId, "Saint Paul", "Minnesota", "US", 44.9497, -93.0931)))

		mockMvc.get("/api/locations/search") {
			param("city", "Saint Paul")
			param("state", "MN")
			param("country", "US")
		}.andExpect {
			status { isOk() }
			content {
				json(
					"""[{"id":"$selectionId","city":"Saint Paul","state":"Minnesota","country":"US","latitude":44.9497,"longitude":-93.0931}]""",
					strict = true,
				)
			}
		}
	}

	@Test
	fun `search state and country are optional, and blank ones are dropped`() {
		given(service.search("Paris", null, null)).willReturn(emptyList())

		mockMvc.get("/api/locations/search") {
			param("city", "Paris")
			param("state", "")
			param("country", " ")
		}.andExpect {
			status { isOk() }
			content { json("[]", strict = true) }
		}

		verify(service).search("Paris", null, null)
	}

	@Test
	fun `search with a missing, blank, or too long city is a bad request`() {
		mockMvc.get("/api/locations/search").andExpect { status { isBadRequest() } }
		mockMvc.get("/api/locations/search") { param("city", " ") }.andExpect { status { isBadRequest() } }
		mockMvc.get("/api/locations/search") { param("city", "x".repeat(101)) }.andExpect { status { isBadRequest() } }

		verifyNoInteractions(service)
	}

	@Test
	fun `search when OpenWeather fails is a bad gateway`() {
		given(service.search("London", null, null))
			.willThrow(HttpClientErrorException(HttpStatus.UNAUTHORIZED))

		mockMvc.get("/api/locations/search") { param("city", "London") }
			.andExpect {
				status { isEqualTo(502) }
				jsonPath("$.detail") { value("The weather provider is unavailable right now. Please try again later.") }
			}
	}

	@Test
	@ExtendWith(OutputCaptureExtension::class)
	fun `an OpenWeather error response is logged by status, without its body`(output: CapturedOutput) {
		val body = """{"cod":401,"message":"Invalid API key secret-key"}""".toByteArray()
		given(service.search("London", null, null))
			.willThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", HttpHeaders(), body, null))

		mockMvc.get("/api/locations/search") { param("city", "London") }
			.andExpect { status { isEqualTo(502) } }

		assertTrue(output.contains("OpenWeather call failed with status 401"), output.toString())
		assertFalse(output.contains("secret-key"), output.toString())
	}

	@Test
	@ExtendWith(OutputCaptureExtension::class)
	fun `an OpenWeather timeout is a bad gateway and is logged with its message, without the api key`(output: CapturedOutput) {
		val message = """I/O error on GET request for "https://api.openweathermap.org/geo/1.0/direct?q=London&limit=5&appid=secret-key": Read timed out"""
		given(service.search("London", null, null)).willThrow(ResourceAccessException(message))

		mockMvc.get("/api/locations/search") { param("city", "London") }
			.andExpect {
				status { isEqualTo(502) }
				jsonPath("$.detail") { value("The weather provider is unavailable right now. Please try again later.") }
			}

		val redacted = message.replace("appid=secret-key", "appid=***")
		assertTrue(output.contains("OpenWeather call failed: $redacted"), output.toString())
		assertFalse(output.contains("secret-key"), output.toString())
	}

	@Test
	fun `search when redis is down is service unavailable`() {
		given(service.search("London", null, null))
			.willThrow(RedisConnectionFailureException("Unable to connect to Redis"))

		mockMvc.get("/api/locations/search") { param("city", "London") }
			.andExpect {
				status { isEqualTo(503) }
				jsonPath("$.detail") { value("Service is temporarily unavailable. Please try again.") }
			}
	}

	@Test
	fun `selecting a new location is created`() {
		given(service.select(selectionId))
			.willReturn(SelectedLocation(Location("Saint Paul", "Minnesota", "US", 44.9497, -93.0931, id = 7), created = true))

		postSelection(selectionId).andExpect {
			status { isCreated() }
			content {
				json(
					"""{"id":7,"city":"Saint Paul","state":"Minnesota","country":"US","latitude":44.9497,"longitude":-93.0931}""",
					strict = true,
				)
			}
		}
	}

	@Test
	fun `selecting an already saved location is ok and has no state when it has none`() {
		given(service.select(selectionId))
			.willReturn(SelectedLocation(Location("Monaco", "", "MC", 43.73, 7.42, id = 3), created = false))

		postSelection(selectionId).andExpect {
			status { isOk() }
			jsonPath("$.id") { value(3) }
			jsonPath("$.state") { value(null) }
		}
	}

	@Test
	fun `selecting an expired or unknown id is not found`() {
		given(service.select(selectionId)).willThrow(SearchSelectionNotFoundException())

		postSelection(selectionId).andExpect {
			status { isNotFound() }
			jsonPath("$.detail") { value("Search result not found or expired. Please search again.") }
		}
	}

	@Test
	fun `selecting with a malformed or missing id is a bad request`() {
		postSelection("not-an-id").andExpect { status { isBadRequest() } }
		postSelection("3f2a9c1e-0b7d-4c55-9a51-2e8f6d4b1a00").andExpect { status { isBadRequest() } }
		mockMvc.post("/api/locations") {
			contentType = MediaType.APPLICATION_JSON
			content = "{}"
		}.andExpect { status { isBadRequest() } }

		verifyNoInteractions(service)
	}

	@Test
	fun `list returns the saved locations`() {
		given(service.list()).willReturn(
			listOf(
				Location("Saint Paul", "Minnesota", "US", 44.9497, -93.0931, id = 7),
				Location("Monaco", "", "MC", 43.73, 7.42, id = 9),
			),
		)

		mockMvc.get("/api/locations").andExpect {
			status { isOk() }
			content {
				json(
					"""[
						{"id":7,"city":"Saint Paul","state":"Minnesota","country":"US","latitude":44.9497,"longitude":-93.0931},
						{"id":9,"city":"Monaco","state":null,"country":"MC","latitude":43.73,"longitude":7.42}
					]""",
					strict = true,
				)
			}
		}
	}

	@Test
	fun `list with nothing saved is an empty array`() {
		given(service.list()).willReturn(emptyList())

		mockMvc.get("/api/locations").andExpect {
			status { isOk() }
			content { json("[]", strict = true) }
		}
	}

	@Test
	fun `deleting a saved location is no content`() {
		mockMvc.delete("/api/locations/7").andExpect {
			status { isNoContent() }
			content { string("") }
		}

		verify(service).delete(7)
	}

	@Test
	fun `deleting an unknown location is not found`() {
		willThrow(LocationNotFoundException(7)).given(service).delete(7)

		mockMvc.delete("/api/locations/7").andExpect {
			status { isNotFound() }
			jsonPath("$.detail") { value("Location not found.") }
		}
	}

	@Test
	fun `deleting with a non-numeric id is a bad request`() {
		mockMvc.delete("/api/locations/abc").andExpect { status { isBadRequest() } }

		verifyNoInteractions(service)
	}

	private fun postSelection(id: String) = mockMvc.post("/api/locations") {
		contentType = MediaType.APPLICATION_JSON
		content = """{"id":"$id"}"""
	}
}
