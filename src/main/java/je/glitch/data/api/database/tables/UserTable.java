package je.glitch.data.api.database.tables;

import com.zaxxer.hikari.HikariDataSource;
import je.glitch.data.api.models.Carpark;
import je.glitch.data.api.models.LiveParkingSpace;
import je.glitch.data.api.models.User;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class UserTable implements ITable {
    private final HikariDataSource dataSource;

    /**
     * Returns a user from the database.
     * @return information about a user
     */
    public User getUser(String id) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE id = ?");
            stmt.setString(1, id);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return User.of(result);
                }
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    public boolean checkUserExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (Connection connection = dataSource.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    /**
     * Returns all users in the database.
     * @return a list of users
     */
    public List<User> getUsers() {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users");

            try (ResultSet result = stmt.executeQuery()) {
                List<User> users = new ArrayList<>();

                while (result.next()) {
                    users.add(User.of(result));
                }
                return users;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Creates a new user in the database.
     * @param user the user to create
     * @return true if creation was successful
     */
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (id, email, password, siteAdmin) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, user.getId());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setBoolean(4, user.isSiteAdmin());

            return stmt.executeUpdate() > 0;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    /**
     * Updates an existing user in the database.
     * @param user the user to update
     * @return true if update was successful
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET email = ?, siteAdmin = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, user.getEmail());
            stmt.setBoolean(2, user.isSiteAdmin());
            stmt.setString(3, user.getId());

            return stmt.executeUpdate() > 0;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    /**
     * Deletes a user from the database by ID.
     * @param userId the ID of the user to delete
     * @return true if deletion was successful
     */
    public boolean deleteUser(String userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }
}
