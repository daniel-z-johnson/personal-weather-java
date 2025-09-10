package self.exercise.personal_weather.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClients {

    @Bean
    public RestClient openWeatherClient() {
        return RestClient.builder()
                .baseUrl("")
                .build();
    }

}
