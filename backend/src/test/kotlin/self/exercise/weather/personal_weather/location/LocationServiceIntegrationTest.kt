package self.exercise.weather.personal_weather.location

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import self.exercise.weather.personal_weather.TestcontainersConfiguration
import self.exercise.weather.personal_weather.openweather.GeoLocation
import self.exercise.weather.personal_weather.openweather.OpenWeatherGeocodingClient
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Runs against real Postgres and Redis containers; only OpenWeather is mocked. */
@Import(TestcontainersConfiguration::class)
@SpringBootTest(properties = ["open-weather.api-key=test-key"])
class LocationServiceIntegrationTest @Autowired constructor(
	private val service: LocationService,
	private val repository: LocationRepository,
	private val redis: StringRedisTemplate,
) {

	@MockitoBean
	lateinit var geocodingClient: OpenWeatherGeocodingClient

	private val saintPaul = GeoLocation("Saint Paul", "Minnesota", "US", 44.9497, -93.0931)
	private val monaco = GeoLocation("Monaco", null, "MC", 43.73, 7.42)

	@BeforeEach
	fun cleanUp() {
		repository.deleteAll()
		redis.keys("location-search:*").takeIf { it.isNotEmpty() }?.let(redis::delete)
	}

	@Test
	fun `search stores the results in redis with the configured ttl`() {
		given(geocodingClient.findByName("Saint Paul", null, null)).willReturn(listOf(saintPaul, monaco))

		val results = service.search("Saint Paul", null, null)

		assertEquals(2, results.size)
		val searchId = results[0].id.substringBefore(":")
		assertEquals(listOf("$searchId:0", "$searchId:1"), results.map { it.id })

		val ttlSeconds = redis.getExpire(LocationSearchStore.key(UUID.fromString(searchId)), TimeUnit.SECONDS)
		assertTrue(ttlSeconds in 3500..3600, "ttl was $ttlSeconds seconds")
	}

	@Test
	fun `a search with no matches stores nothing`() {
		given(geocodingClient.findByName("Nowhere", null, null)).willReturn(emptyList())

		assertEquals(emptyList(), service.search("Nowhere", null, null))
		assertTrue(redis.keys("location-search:*").isEmpty())
	}

	@Test
	fun `selecting a result saves it to postgres`() {
		given(geocodingClient.findByName("Monaco", null, null)).willReturn(listOf(saintPaul, monaco))
		val pick = service.search("Monaco", null, null)[1]

		val selected = service.select(pick.id)

		assertTrue(selected.created)
		val saved = assertNotNull(repository.findByLatitudeAndLongitude(43.73, 7.42))
		assertEquals(selected.location.id, saved.id)
		assertEquals(listOf("Monaco", "", "MC"), listOf(saved.city, saved.state, saved.country))
	}

	@Test
	fun `selecting an already saved place returns the existing row`() {
		given(geocodingClient.findByName("Saint Paul", null, null)).willReturn(listOf(saintPaul))
		val first = service.select(service.search("Saint Paul", null, null)[0].id)
		val second = service.select(service.search("Saint Paul", null, null)[0].id)

		assertFalse(second.created)
		assertEquals(first.location.id, second.location.id)
		assertEquals(1, repository.count())
	}

	@Test
	fun `unknown, expired, out of range, or malformed ids are not found`() {
		given(geocodingClient.findByName("Saint Paul", null, null)).willReturn(listOf(saintPaul))
		val searchId = service.search("Saint Paul", null, null)[0].id.substringBefore(":")

		assertFailsWith<SearchSelectionNotFoundException> { service.select("${UUID.randomUUID()}:0") }
		assertFailsWith<SearchSelectionNotFoundException> { service.select("$searchId:1") }
		assertFailsWith<SearchSelectionNotFoundException> { service.select("garbage") }

		redis.delete(LocationSearchStore.key(UUID.fromString(searchId))) // same as the ttl running out
		assertFailsWith<SearchSelectionNotFoundException> { service.select("$searchId:0") }
		assertEquals(0, repository.count())
	}

	@Test
	fun `list returns saved locations oldest first`() {
		given(geocodingClient.findByName("Saint Paul", null, null)).willReturn(listOf(saintPaul, monaco))
		val results = service.search("Saint Paul", null, null)
		service.select(results[1].id)
		service.select(results[0].id)

		assertEquals(listOf("Monaco", "Saint Paul"), service.list().map { it.city })
	}

	@Test
	fun `delete removes only that location, and a second delete is not found`() {
		given(geocodingClient.findByName("Saint Paul", null, null)).willReturn(listOf(saintPaul, monaco))
		val results = service.search("Saint Paul", null, null)
		val kept = service.select(results[0].id).location
		val removed = service.select(results[1].id).location

		service.delete(checkNotNull(removed.id))

		assertEquals(listOf(kept.id), service.list().map { it.id })
		assertFailsWith<LocationNotFoundException> { service.delete(checkNotNull(removed.id)) }
	}
}
