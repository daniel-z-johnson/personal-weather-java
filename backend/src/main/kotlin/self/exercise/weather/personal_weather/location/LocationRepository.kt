package self.exercise.weather.personal_weather.location

import org.springframework.data.jpa.repository.JpaRepository

interface LocationRepository : JpaRepository<Location, Long> {
	fun findByLatitudeAndLongitude(latitude: Double, longitude: Double): Location?
}
