package self.exercise.personal_weather.repository;

public class LocationSQL {

    private LocationSQL() {
        // this class should never be initialized, is it just for SQL strings
    }

    public static final String INSERT_LOCATION = """
                INSERT INTO locations (city, state, country, latitude, longitude) VALUES 
                                        ( ?,     ?,       ?,        ?,         ?) RETURNING id;
                """;

    public static final String ALL_LOCATIONS = """
                SELECT id, city, state, country, latitude, longitude FROM locations;
    """;
}
