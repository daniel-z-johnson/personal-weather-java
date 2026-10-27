package self.exercise.weather.personal_weather

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class PersonalWeatherApplicationTests {

	@Test
	fun contextLoads() {
	}

}
