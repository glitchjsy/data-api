package je.glitch.data.api.services;

import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.models.ApiToken;
import je.glitch.data.api.models.User;
import je.glitch.data.api.models.Session;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class ApiKeyService {
    private final MySQLConnection connection;

    public boolean deleteApiKey(User user, String tokenId) {
        ApiToken token = connection.getApiKeyTable().getToken(tokenId);

        if (token == null) {
            return false;
        }
        if (!token.getUserId().equals(user.getId())) {
            throw new SecurityException("Cannot delete another user's token");
        }
        return connection.getApiKeyTable().deleteToken(tokenId);
    }

    public ApiToken createApiKey(User user, String summary) {
        String id = UUID.randomUUID().toString();
        String tokenString = UUID.randomUUID().toString();

        ApiToken token = new ApiToken(
                id,
                null,
                user.getId(),
                null,
                tokenString,
                summary,
                0
        );

        boolean success = connection.getApiKeyTable().createToken(token);
        return success ? token : null;
    }
}
