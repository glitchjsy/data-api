package je.glitch.data.api.services;

import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.models.LoginBody;
import je.glitch.data.api.models.Session;
import je.glitch.data.api.models.User;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Date;
import java.util.Optional;

@RequiredArgsConstructor
public class AuthService {
    private final MySQLConnection connection;

    public Session login(LoginBody loginBody) throws IllegalArgumentException {
        if (loginBody.getEmail() == null || loginBody.getPassword() == null) {
            throw new IllegalArgumentException("Email and password must be provided");
        }

        Optional<User> userOptional = connection.getUserTable().getUsers().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(loginBody.getEmail()))
                .findFirst();

        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();

        if (!BCrypt.checkpw(loginBody.getPassword(), user.getPassword())) {
            return null;
        }
        return new Session(new Date(), user.getId());
    }

    public User getUserByEmail(String email) {
        return connection.getUserTable().getUsers().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }
}