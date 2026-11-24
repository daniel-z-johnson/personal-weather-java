package self.exercise.weather.personal_weather.openweather

private val apiKeyParam = Regex("(appid=)[^&\"\\s]*", RegexOption.IGNORE_CASE)

/**
 * [message] with the api key masked. Spring puts the full request URI, `appid` included, in the
 * message of I/O failures such as timeouts, so anything logged from an OpenWeather call goes through here.
 */
fun redactApiKey(message: String?): String = apiKeyParam.replace(message.orEmpty(), "\$1***")
