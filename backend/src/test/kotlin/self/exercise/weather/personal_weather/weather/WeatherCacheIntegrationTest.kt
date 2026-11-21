package self.exercise.weather.personal_weather.weather

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.StringRedisTemplate
import self.exercise.weather.personal_weather.TestcontainersConfiguration
import self.exercise.weather.personal_weather.openweather.CurrentWeather
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Runs against a real Redis container. */
@Import(TestcontainersConfiguration::class)
@SpringBootTest(properties = ["open-weather.api-key=test-key"])
class WeatherCacheIntegrationTest @Autowired constructor(
	private val cache: WeatherCache,
	private val redis: StringRedisTemplate,
) {

	private val saintPaul = CurrentWeather(-3.21, Instant.parse("2026-11-20T22:40:00Z"))
	private val monaco = CurrentWeather(14.5, Instant.parse("2026-11-20T22:30:00Z"))

	@BeforeEach
	fun cleanUp() {
		redis.keys("weather:*").takeIf { it.isNotEmpty() }?.let(redis::delete)
	}

	@Test
	fun `stored weather is read back with the configured ttl`() {
		cache.put(7, saintPaul)

		assertEquals(mapOf(7L to saintPaul), cache.getAll(listOf(7)))
		val ttlSeconds = redis.getExpire(WeatherCache.key(7), TimeUnit.SECONDS)
		assertTrue(ttlSeconds in 890..900, "ttl was $ttlSeconds seconds")
	}

	@Test
	fun `only ids with cached weather are returned`() {
		cache.put(7, saintPaul)
		cache.put(9, monaco)

		assertEquals(mapOf(7L to saintPaul, 9L to monaco), cache.getAll(listOf(7, 8, 9, 7)))
	}

	@Test
	fun `no ids is an empty map`() {
		assertEquals(emptyMap(), cache.getAll(emptyList()))
	}

	@Test
	fun `an unreadable entry is treated as a miss`() {
		redis.opsForValue().set(WeatherCache.key(7), """{"tempK":270.0}""")
		cache.put(9, monaco)

		assertEquals(mapOf(9L to monaco), cache.getAll(listOf(7, 9)))
	}

	@Test
	fun `putting again replaces the entry`() {
		cache.put(7, saintPaul)
		val newer = CurrentWeather(-2.0, Instant.parse("2026-11-20T22:50:00Z"))
		cache.put(7, newer)

		assertEquals(mapOf(7L to newer), cache.getAll(listOf(7)))
	}
}
