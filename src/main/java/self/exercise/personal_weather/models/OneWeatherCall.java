package self.exercise.personal_weather.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OneWeatherCall {
    private CurrentWeather current;
}
