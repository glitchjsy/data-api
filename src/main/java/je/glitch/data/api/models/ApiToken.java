package je.glitch.data.api.models;

import lombok.Data;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.stream.IntStream;

@Data
public class ApiToken {
    private final String id;
    private final Timestamp createdAt;
    private final String userId;
    private final String userEmail;
    private final String token;
    private final String summary;
    private final int totalUses;

    public static ApiToken of(ResultSet result) throws SQLException {
        ResultSetMetaData m = result.getMetaData();
        boolean hasEmail = java.util.stream.IntStream.rangeClosed(1, m.getColumnCount())
                .anyMatch(i -> {
                    try {
                        return "userEmail".equalsIgnoreCase(m.getColumnLabel(i));
                    } catch (SQLException e) {
                        return false;
                    }
                });

        return new ApiToken(
                result.getString("id"),
                result.getTimestamp("createdAt"),
                result.getString("userId"),
                hasEmail ? result.getString("userEmail") : null,
                result.getString("token"),
                result.getString("summary"),
                result.getInt("totalUses")
        );
    }
}
