package je.glitch.data.api.controllers;

import com.google.gson.JsonObject;
import io.javalin.http.Context;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.models.*;
import je.glitch.data.api.utils.ErrorResponse;
import je.glitch.data.api.utils.ErrorType;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class MeController {
    private final MySQLConnection connection;

    public void handleGetSession(Context ctx) {
        Session session = ctx.sessionAttribute("session");

        if (session == null) {
            ctx.json(new Object());
            return;
        }

        User user = connection.getUserTable().getUser(session.getUserId());

        if (user == null) {
            ctx.json(new Object());
            return;
        }
        ctx.json(new SessionResponse(session.getLoginTime(), user));
    }

    public void handleListApiKeys(Context ctx) {
        Session session = ctx.sessionAttribute("session");
        if (session == null) {
            ctx.status(401).json(new ErrorResponse(ErrorType.NOT_AUTHORIZED, "No active session"));
            return;
        }

        User user = connection.getUserTable().getUser(session.getUserId());
        if (user == null) {
            ctx.status(401).json(new ErrorResponse(ErrorType.NOT_AUTHORIZED, "User not found"));
            return;
        }

        List<ApiToken> tokens = connection.getApiKeyTable().getTokensForUser(user.getId());
        ctx.json(new ApiResponse<>(tokens));
    }

    public void handleDeleteApiKey(Context ctx) {
        Session session = ctx.sessionAttribute("session");
        if (session == null) {
            ctx.status(401).json(new ErrorResponse(ErrorType.NOT_AUTHORIZED, "No active session"));
            return;
        }

        User user = connection.getUserTable().getUser(session.getUserId());
        if (user == null) {
            ctx.status(401).json(new ErrorResponse(ErrorType.NOT_AUTHORIZED, "User not found"));
            return;
        }

        String tokenId = ctx.pathParam("tokenId");

        ApiToken apiToken = connection.getApiKeyTable().getToken(tokenId);
        if (apiToken == null) {
            ctx.status(400).json(new ErrorResponse(ErrorType.INVALID_REQUEST, "Invalid API key ID"));
            return;
        }
        if (!apiToken.getUserId().equals(user.getId())) {
            ctx.status(403).json(new ErrorResponse(ErrorType.FORBIDDEN, "You cannot delete the API key of another user"));
            return;
        }
        boolean success = connection.getApiKeyTable().deleteToken(tokenId);

        if (success) {
            ctx.status(200).result();
        } else {
            ctx.status(404).json(new ErrorResponse(ErrorType.NOT_FOUND, "API key not found"));
        }
    }

    public void handleCreateApiKey(Context ctx) {
        Session session = ctx.sessionAttribute("session");
        if (session == null) {
            ctx.status(401).json(new ErrorResponse(ErrorType.NOT_AUTHORIZED, "No active session"));
            return;
        }

        User user = connection.getUserTable().getUser(session.getUserId());
        if (user == null) {
            ctx.status(401).json(new ErrorResponse(ErrorType.NOT_AUTHORIZED, "User not found"));
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> body = ctx.bodyAsClass(Map.class);
        String summary = (String) body.get("summary");

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

        if (success) {
            ctx.status(200).json(new ApiResponse<>(token));
        } else {
            ctx.status(500).json(new ErrorResponse(ErrorType.SERVER_ERROR, "Failed to create API key"));
        }
    }

}
