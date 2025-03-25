package je.glitch.data.api.models;

import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;

@Data
public class BusStop {
    private final String id;
    private final String createdAt;
    private final String name;
    private final String code;
    private final long latitude;
    private final long longitude;
    private final boolean shelter;

    public static BusStop of(ResultSet result) throws SQLException {
        return new BusStop(
                result.getString("id"),
                result.getString("createdAt"),
                result.getString("name"),
                result.getString("code"),
                result.getLong("latitude"),
                result.getLong("longitude"),
                result.getBoolean("shelter")
        );
    }
}
