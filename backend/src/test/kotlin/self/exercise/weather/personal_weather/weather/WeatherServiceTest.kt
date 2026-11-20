package self.exercise.weather.personal_weather.weather

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verifyNoMoreInteractions
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import self.exercise.weather.personal_weather.location.Location
import self.exercise.weather.personal_weather.location.LocationService
import self.exercise.weather.personal_weather.openweather.CurrentWeather
import self.exercise.weather.personal_weather.openweather.OpenWeatherCurrentWeatherClient
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WeatherServiceTest {

	private val locationService = mock(LocationService::class.java)
	private val client = mock(OpenWeatherCurrentWeatherClient::class.java)
	private val cache = mock(WeatherCache::class.java)
	private val service = WeatherService(locationService, client, cache)

	private val saintPaul = Location("Saint Paul", "Minnesota", "US", 44.9497, -93.0931, id = 7)
	private val monaco = Location("Monaco", "", "MC", 43.73, 7.42, id = 9)

	private val saintPaulWeather = CurrentWeather(-3.21, Instant.parse("2026-11-20T22:40:00Z"))
	private val monacoWeather = CurrentWeather(14.5, Instant.parse("2026-11-20T22:30:00Z"))

	@Test
	fun `everything cached is returned without calling OpenWeather`() {
		given(locationService.list()).willReturn(listOf(saintPaul, monaco))
		given(cache.getAll(listOf(7L, 9L))).willReturn(mapOf(7L to saintPaulWeather, 9L to monacoWeather))

		assertEquals(
			listOf(LocationWeather(saintPaul, saintPaulWeather), LocationWeather(monaco, monacoWeather)),
			service.currentForAll(),
		)
		verifyNoInteractions(client)
		verify(cache, never()).put(7L, saintPaulWeather)
		verify(cache, never()).put(9L, monacoWeather)
	}

	@Test
	fun `nothing cached is fetched and cached`() {
		given(locationService.list()).willReturn(listOf(saintPaul, monaco))
		given(cache.getAll(listOf(7L, 9L))).willReturn(emptyMap())
		given(client.current(44.9497, -93.0931)).willReturn(saintPaulWeather)
		given(client.current(43.73, 7.42)).willReturn(monacoWeather)

		assertEquals(
			listOf(LocationWeather(saintPaul, saintPaulWeather), LocationWeather(monaco, monacoWeather)),
			service.currentForAll(),
		)
		verify(cache).put(7L, saintPaulWeather)
		verify(cache).put(9L, monacoWeather)
	}

	@Test
	fun `only uncached locations are fetched, and order is kept`() {
		given(locationService.list()).willReturn(listOf(saintPaul, monaco))
		given(cache.getAll(listOf(7L, 9L))).willReturn(mapOf(9L to monacoWeather))
		given(client.current(44.9497, -93.0931)).willReturn(saintPaulWeather)

		assertEquals(
			listOf(LocationWeather(saintPaul, saintPaulWeather), LocationWeather(monaco, monacoWeather)),
			service.currentForAll(),
		)
		verify(client).current(44.9497, -93.0931)
		verifyNoMoreInteractions(client)
		verify(cache).put(7L, saintPaulWeather)
		verify(cache, never()).put(9L, monacoWeather)
	}

	@Test
	@ExtendWith(OutputCaptureExtension::class)
	fun `a failed fetch gives that location no weather, isn't cached, and doesn't stop the others`(output: CapturedOutput) {
		given(locationService.list()).willReturn(listOf(saintPaul, monaco))
		given(cache.getAll(listOf(7L, 9L))).willReturn(emptyMap())
		given(client.current(44.9497, -93.0931)).willThrow(ResourceAccessException("Read timed out"))
		given(client.current(43.73, 7.42)).willReturn(monacoWeather)

		assertEquals(
			listOf(LocationWeather(saintPaul, null), LocationWeather(monaco, monacoWeather)),
			service.currentForAll(),
		)
		verify(cache).put(9L, monacoWeather)
		verify(cache, never()).put(7L, saintPaulWeather)
		assertTrue(output.contains("Current weather for location 7 failed: Read timed out"), output.toString())
	}

	@Test
	@ExtendWith(OutputCaptureExtension::class)
	fun `an OpenWeather error response is logged by status, without its body`(output: CapturedOutput) {
		val body = """{"cod":500,"message":"internal error secret-key"}""".toByteArray()
		given(locationService.list()).willReturn(listOf(saintPaul))
		given(cache.getAll(listOf(7L))).willReturn(emptyMap())
		given(client.current(44.9497, -93.0931)).willThrow(
			HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", HttpHeaders(), body, null),
		)

		assertEquals(listOf(LocationWeather(saintPaul, null)), service.currentForAll())
		assertTrue(output.contains("Current weather for location 7 failed with status 500"), output.toString())
		assertFalse(output.contains("secret-key"), output.toString())
	}

	@Test
	fun `no saved locations is an empty list without calling OpenWeather`() {
		given(locationService.list()).willReturn(emptyList())
		given(cache.getAll(emptyList())).willReturn(emptyMap())

		assertEquals(emptyList(), service.currentForAll())
		verifyNoInteractions(client)
	}
}
