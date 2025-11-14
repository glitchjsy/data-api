package je.glitch.data.api.services;

import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.models.ApiToken;
import je.glitch.data.api.models.Session;
import je.glitch.data.api.models.User;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class UserService {
    private final MySQLConnection connection;

    public User getUserFromSession(Session session) {
        if (session == null) {
            return null;
        }
        return connection.getUserTable().getUser(session.getUserId());
    }

    public List<ApiToken> listApiKeys(User user) {
        return connection.getApiKeyTable().getTokensForUser(user.getId());
    }
}
