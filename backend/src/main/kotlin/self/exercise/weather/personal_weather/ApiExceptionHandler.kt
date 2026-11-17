package self.exercise.weather.personal_weather

import org.slf4j.LoggerFactory
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import self.exercise.weather.personal_weather.location.LocationNotFoundException
import self.exercise.weather.personal_weather.location.SearchSelectionNotFoundException

/** Turns failures from any controller into problem-detail responses. */
@RestControllerAdvice
class ApiExceptionHandler {

	private val log = LoggerFactory.getLogger(javaClass)

	@ExceptionHandler(SearchSelectionNotFoundException::class)
	fun selectionNotFound(): ProblemDetail =
		ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Search result not found or expired. Please search again.")

	@ExceptionHandler(LocationNotFoundException::class)
	fun locationNotFound(): ProblemDetail =
		ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Location not found.")

	/** OpenWeather failed, timed out, or was unreachable: an upstream problem, not ours, so 502 rather than 500. */
	@ExceptionHandler(RestClientException::class)
	fun openWeatherFailed(e: RestClientException): ProblemDetail {
		if (e is RestClientResponseException) {
			// Status only: the message of an error response includes the upstream body, which we don't control.
			log.warn("OpenWeather call failed with status {}", e.statusCode.value())
		} else {
			log.warn("OpenWeather call failed: {}", e.message)
		}
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "Location lookup is unavailable right now.")
	}

	/** Redis is down: nothing is wrong with the request, and retrying later will work. */
	@ExceptionHandler(RedisConnectionFailureException::class)
	fun redisUnavailable(e: RedisConnectionFailureException): ProblemDetail {
		log.warn("Redis is unreachable: {}", e.message)
		return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "Service is temporarily unavailable. Please try again.")
	}
}
