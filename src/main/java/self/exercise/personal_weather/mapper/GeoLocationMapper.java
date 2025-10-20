package self.exercise.personal_weather.mapper;

import org.springframework.jdbc.core.RowMapper;
import self.exercise.personal_weather.models.GeoLocation;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GeoLocationMapper implements RowMapper<GeoLocation> {
    @Override
    public GeoLocation mapRow(ResultSet rs, int rowNum) throws SQLException {
        GeoLocation geo = new GeoLocation();

        Object idObj = rs.getObject("id");
        if (idObj != null) {
            geo.setId(rs.getLong("id"));
        }

        geo.setCity(rs.getString("city"));
        geo.setState(rs.getString("state"));
        geo.setCountry(rs.getString("country"));
        geo.setLat(rs.getString("latitude"));
        geo.setLon(rs.getString("longitude"));

        return geo;
    }
}