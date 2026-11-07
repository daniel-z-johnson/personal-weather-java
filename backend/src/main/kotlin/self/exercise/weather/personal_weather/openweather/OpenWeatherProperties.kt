package self.exercise.weather.personal_weather.openweather

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties("open-weather")
data class OpenWeatherProperties(
	@NotBlank val baseUrl: String,
	@NotBlank val apiKey: String,
) {
	// Keeps the key out of logs and failure reports; a data class would print it otherwise.
	override fun toString() = "OpenWeatherProperties(baseUrl=$baseUrl, apiKey=***)"
}
