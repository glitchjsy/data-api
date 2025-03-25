package je.glitch.data.api.models;

import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;

@Data
public class LiveParkingSpace {
    private final String id;
    private final String createdAt;
    private final String name;
    private final String code;
    private final int spaces;
    private final String status;
    private final boolean open;

    public static LiveParkingSpace of(ResultSet result) throws SQLException {
        return new LiveParkingSpace(
                result.getString("id"),
                result.getString("createdAt"),
                result.getString("name"),
                result.getString("code"),
                result.getInt("spaces"),
                result.getString("status"),
                result.getBoolean("open")
        );
    }
}
