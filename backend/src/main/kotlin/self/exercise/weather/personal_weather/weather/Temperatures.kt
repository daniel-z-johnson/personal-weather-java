package self.exercise.weather.personal_weather.weather

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Display rounding for temperatures. Both work in decimal rather than binary arithmetic, so a reading
 * like 2.25 is exactly a half and rounds as one, instead of becoming 2.2499999… first.
 * Halves round away from zero, the same on both sides of freezing.
 */
object Temperatures {

	private val NINE_FIFTHS = BigDecimal("1.8")
	private val THIRTY_TWO = BigDecimal(32)

	/** Celsius to one decimal place. */
	fun roundCelsius(celsius: Double): Double =
		BigDecimal.valueOf(celsius).setScale(1, RoundingMode.HALF_UP).toDouble()

	/** Fahrenheit to a whole degree, converted from the unrounded [celsius]. */
	fun toRoundedFahrenheit(celsius: Double): Int =
		BigDecimal.valueOf(celsius)
			.multiply(NINE_FIFTHS)
			.add(THIRTY_TWO)
			.setScale(0, RoundingMode.HALF_UP)
			.intValueExact()
}
