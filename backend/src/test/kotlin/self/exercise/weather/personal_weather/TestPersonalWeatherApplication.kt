package self.exercise.weather.personal_weather

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<PersonalWeatherApplication>().with(TestcontainersConfiguration::class).run(*args)
}
