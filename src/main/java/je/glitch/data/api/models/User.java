package je.glitch.data.api.models;

import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;

@Data
public class User {
    private final String id;
    private final String createdAt;
    private final String updatedAt;
    private final String email;
    private final String password;
    private final boolean siteAdmin;

    public static User of(ResultSet result) throws SQLException {
        return new User(
                result.getString("id"),
                result.getString("createdAt"),
                result.getString("updatedAt"),
                result.getString("email"),
                result.getString("password"),
                result.getBoolean("siteAdmin")
        );
    }
}
