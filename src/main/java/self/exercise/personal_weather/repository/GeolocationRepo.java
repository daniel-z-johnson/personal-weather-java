package self.exercise.personal_weather.repository;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import self.exercise.personal_weather.mapper.GeoLocationMapper;
import self.exercise.personal_weather.models.GeoLocation;

import java.util.List;

@Repository
public class GeolocationRepo {

    private final JdbcTemplate jdbcTemplate;

    public GeolocationRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public GeoLocation insertLocation(String city, String state, String country, String lat, String lon) {
        Long id = jdbcTemplate.queryForObject(LocationSQL.INSERT_LOCATION, Long.class, city, state, country, lat, lon);
        var loc = new GeoLocation();
        if(id != null) {
            loc.setId(id);
        }
        loc.setLat(lat);
        loc.setLon(lon);
        loc.setCountry(country);
        loc.setState(state);
        loc.setCity(city);
        return loc;
    }

    public List<GeoLocation> getLocations() {
        return jdbcTemplate.query(LocationSQL.ALL_LOCATIONS, new GeoLocationMapper());
    }
}
