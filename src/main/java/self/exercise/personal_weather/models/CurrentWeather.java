package self.exercise.personal_weather.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CurrentWeather {
    private BigDecimal temp;
}
