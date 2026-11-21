package self.exercise.weather.personal_weather.weather

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

class TemperaturesTest {

	@ParameterizedTest(name = "{0}°C shows as {1}°C and {2}°F")
	@CsvSource(
		"-3.21,  -3.2,  26",  // typical winter reading
		"26.6,   26.6,  80",  // rounding °C to 27 first would give 81°F
		"21.47,  21.5,  71",  // truncating would give 21.4 and 70
		"2.25,   2.3,   36",  // an exact half rounds away from zero
		"-2.25,  -2.3,  28",  // and the same below zero
		"-0.04,  0.0,   32",
		"-17.78, -17.8, 0",
		"-40.0,  -40.0, -40", // where the scales meet
		"0.0,    0.0,   32",
		"100.0,  100.0, 212",
	)
	fun `rounds celsius to a tenth and fahrenheit to a whole degree`(raw: Double, celsius: Double, fahrenheit: Int) {
		assertEquals(celsius, Temperatures.roundCelsius(raw))
		assertEquals(fahrenheit, Temperatures.toRoundedFahrenheit(raw))
	}

	@ParameterizedTest
	@CsvSource("-0.04", "-0.01", "-0.0")
	fun `a reading just below zero is not negative zero`(raw: Double) {
		// assertEquals on Doubles tells 0.0 and -0.0 apart, so this catches a "-0.0" in the JSON.
		assertEquals(0.0, Temperatures.roundCelsius(raw))
	}
}
