package je.glitch.data.api.models;

import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Data
public class ApiToken {
    private final String id;
    private final Timestamp createdAt;
    private final String userId;
    private final String userEmail;
    private final String token;
    private final String summary;

    public static ApiToken of(ResultSet result) throws SQLException {
        return new ApiToken(
                result.getString("id"),
                result.getTimestamp("createdAt"),
                result.getString("userId"),
                result.getString("userEmail"),
                result.getString("token"),
                result.getString("summary")
        );
    }
}
