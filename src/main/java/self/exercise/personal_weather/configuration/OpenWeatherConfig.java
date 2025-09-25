package self.exercise.personal_weather.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:secrets.properties")
public class OpenWeatherConfig {
    @Value("${openweather.api.key}")
    private String apiKey;
    @Value("${openweather.api.location}")
    private String locationUrl;
    @Value("${openweather.api.temperature}")
    private String temperatureUrl;

    public String getApiKey() {
        return apiKey;
    }

    public String getLocationUrl() {
        return locationUrl;
    }

    public String getTemperatureUrl() {
        return temperatureUrl;
    }
}
