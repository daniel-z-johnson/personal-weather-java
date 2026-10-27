package self.exercise.weather.personal_weather

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PersonalWeatherApplication

fun main(args: Array<String>) {
	runApplication<PersonalWeatherApplication>(*args)
}
