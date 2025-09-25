package self.exercise.personal_weather.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GeolocationRepo {

    private final JdbcTemplate jdbcTemplate;

    public GeolocationRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
