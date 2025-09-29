package je.glitch.data.api.models;

import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Data
public class User {
    private final String id;
    private final Timestamp createdAt;
    private final Timestamp updatedAt;
    private final String email;
    private final String password;
    private final boolean siteAdmin;

    public static User of(ResultSet result) throws SQLException {
        return new User(
                result.getString("id"),
                result.getTimestamp("createdAt"),
                result.getTimestamp("updatedAt"),
                result.getString("email"),
                result.getString("password"),
                result.getBoolean("siteAdmin")
        );
    }
}
