package je.glitch.data.api.controllers.admin;

import io.javalin.http.Context;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.models.ApiResponse;
import je.glitch.data.api.models.ApiToken;
import je.glitch.data.api.utils.ErrorResponse;
import je.glitch.data.api.utils.ErrorType;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class AdminTokensController {
    private final MySQLConnection connection;

    public void handleListTokens(Context ctx) {
        List<ApiToken> tokens = connection.getApiKeyTable().getAllTokens();
        ctx.json(new ApiResponse<>(tokens));
    }

    public void handleCreateToken(Context ctx) {
        ApiToken bodyToken = ctx.bodyAsClass(ApiToken.class);

        String id = UUID.randomUUID().toString();
        String tokenString = UUID.randomUUID().toString();

        ApiToken token = new ApiToken(
                id,
                null,
                bodyToken.getUserId(),
                null,
                tokenString,
                bodyToken.getSummary()
        );

        boolean success = connection.getApiKeyTable().createToken(token);

        if (success) {
            ctx.status(200).json(new ApiResponse<>(token));
        } else {
            ctx.status(500).json(new ErrorResponse(ErrorType.SERVER_ERROR, "Failed to create token"));
        }
    }

    public void handleDeleteToken(Context ctx) {
        String tokenId = ctx.pathParam("tokenId");
        boolean success = connection.getApiKeyTable().deleteToken(tokenId);

        if (success) {
            ctx.status(200).result();
        } else {
            ctx.status(404).json(new ErrorResponse(ErrorType.NOT_FOUND, "Token not found"));
        }
    }

    public void handleListTokensForUser(Context ctx) {
        String userId = ctx.pathParam("userId");
        List<ApiToken> tokens = connection.getApiKeyTable().getTokensForUser(userId);
        ctx.json(new ApiResponse<>(tokens));
    }
}
