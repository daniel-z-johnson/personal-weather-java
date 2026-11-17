package self.exercise.weather.personal_weather.location

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/** A place the user chose to keep. [state] is empty when the place has none. */
@Entity
@Table(name = "locations")
class Location(
	val city: String,
	val state: String,
	val country: String,
	val latitude: Double,
	val longitude: Double,
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null,
)
