package self.exercise.personal_weather.services;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import self.exercise.personal_weather.configuration.OpenWeatherConfig;
import self.exercise.personal_weather.models.GeoLocation;
import self.exercise.personal_weather.models.OneWeatherCall;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

@Service
public class OpenWeatherService {
    private final RestClient restClient;
    private final OpenWeatherConfig openWeatherConfig;
    public OpenWeatherService(RestClient restClient, OpenWeatherConfig openWeatherConfig) {
        this.restClient = restClient;
        this.openWeatherConfig = openWeatherConfig;
    }

    private URI buildLocationUri(String locationQuery) {
        return UriComponentsBuilder.fromUriString(openWeatherConfig.getLocationUrl())
                .queryParam("appid", openWeatherConfig.getApiKey())
                .queryParam("q", locationQuery)
                .build()
                .toUri();
    }

    private URI buildCurrentWeatherUri(String lat, String lon) {
        return UriComponentsBuilder.fromUriString(openWeatherConfig.getTemperatureUrl())
                .queryParam("appid", openWeatherConfig.getApiKey())
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("exclude", "hourly,daily,minutely")
                .queryParam("units", "metric")
                .build()
                .toUri();
    }


    public List<GeoLocation> getGeolocations(String city, Optional<String> state, Optional<String> country) {
        if(!StringUtils.hasText(city)) {
            throw new IllegalArgumentException("city must be provided");
        }
        StringJoiner joiner = new StringJoiner(",");
        joiner.add(city);
        state.ifPresent(joiner::add);
        country.ifPresent(joiner::add);
        return restClient.get()
                .uri(buildLocationUri(joiner.toString()))
                .retrieve()
                .body(new ParameterizedTypeReference<List<GeoLocation>>() {});
    }

    public OneWeatherCall getCurrentWeather(String lat, String lon) {
        return restClient.get()
                .uri(buildCurrentWeatherUri(lat, lon))
                .retrieve()
                .body(OneWeatherCall.class);
    }



}
