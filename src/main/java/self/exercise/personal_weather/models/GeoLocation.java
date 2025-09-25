package self.exercise.personal_weather.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GeoLocation {
    private long id;
    private String name;
    private String country;
    private String state;
    private String lat;
    private String lon;
}
