package je.glitch.data.api.database.tables;

import com.zaxxer.hikari.HikariDataSource;
import je.glitch.data.api.models.ApiToken;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ApiKeyTable implements ITable {
    private final HikariDataSource dataSource;

    /**
     * Returns all API tokens in the database.
     * @return a list of tokens
     */
    public List<ApiToken> getAllTokens() {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("""
            SELECT
                apiTokens.*,
                users.email AS userEmail
            FROM
                apiTokens
            LEFT JOIN users ON users.id = apiTokens.userId
            GROUP BY apiTokens.id, users.id
            """);
            try (ResultSet result = stmt.executeQuery()) {
                List<ApiToken> tokens = new ArrayList<>();
                while (result.next()) {
                    tokens.add(ApiToken.of(result));
                }
                return tokens;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Creates a new API token in the database.
     * @param token the token to create
     * @return true if creation was successful
     */
    public boolean createToken(ApiToken token) {
        String sql = "INSERT INTO apiTokens (id, userId, token, summary) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, token.getId());
            stmt.setString(2, token.getUserId());
            stmt.setString(3, token.getToken());
            stmt.setString(4, token.getSummary());

            return stmt.executeUpdate() > 0;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    public String getIdFromKey(String key) {
        String sql = "SELECT id FROM apiTokens WHERE token = ?";

        try (Connection connection = dataSource.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, key);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return result.getString("id");
                }
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    /**
     * Deletes an API token from the database by ID.
     * @param tokenId the ID of the token to delete
     * @return true if deletion was successful
     */
    public boolean deleteToken(String tokenId) {
        String sql = "DELETE FROM apiTokens WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, tokenId);
            return stmt.executeUpdate() > 0;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    /**
     * Returns all API tokens for a specific user.
     * @param userId the ID of the user
     * @return a list of tokens for the user
     */
    public List<ApiToken> getTokensForUser(String userId) {
        String sql = "SELECT * FROM apiTokens WHERE userId = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, userId);
            try (ResultSet result = stmt.executeQuery()) {
                List<ApiToken> tokens = new ArrayList<>();
                while (result.next()) {
                    tokens.add(ApiToken.of(result));
                }
                return tokens;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new ArrayList<>();
        }
    }
}
