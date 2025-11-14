package je.glitch.data.api.controllers;

import io.javalin.http.Context;
import je.glitch.data.api.models.*;
import je.glitch.data.api.services.ApiKeyService;
import je.glitch.data.api.services.UserService;
import je.glitch.data.api.utils.ErrorResponse;
import je.glitch.data.api.utils.ErrorType;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class MeController {
    private final ApiKeyService apiKeyService;
    private final UserService userService;

    public void handleGetSession(Context ctx) {
        Session session = ctx.sessionAttribute("session");
        User user = userService.getUserFromSession(session);

        if (session == null || user == null) {
            ctx.json(new Object());
            return;
        }

        ctx.json(new SessionResponse(session.getLoginTime(), user));
    }

    public void handleListApiKeys(Context ctx) {
        Session session = ctx.sessionAttribute("session");
        User user = userService.getUserFromSession(session);

        if (user == null) {
            ctx.status(401).json(new ErrorResponse(ErrorType.NOT_AUTHORIZED, "No active session"));
            return;
        }

        List<ApiToken> tokens = userService.listApiKeys(user);
        ctx.json(new ApiResponse<>(tokens));
    }

    public void handleDeleteApiKey(Context ctx) {
        Session session = ctx.sessionAttribute("session");
        User user = userService.getUserFromSession(session);

        if (user == null) {
            ctx.status(401).json(new ErrorResponse(ErrorType.NOT_AUTHORIZED, "No active session"));
            return;
        }

        String tokenId = ctx.pathParam("tokenId");

        try {
            boolean success = apiKeyService.deleteApiKey(user, tokenId);
            if (success) {
                ctx.status(200).result();
            } else {
                ctx.status(400).json(new ErrorResponse(ErrorType.INVALID_REQUEST, "Invalid API key ID"));
            }
        } catch (SecurityException ex) {
            ctx.status(403).json(new ErrorResponse(ErrorType.FORBIDDEN, ex.getMessage()));
        }
    }

    public void handleCreateApiKey(Context ctx) {
        Session session = ctx.sessionAttribute("session");
        User user = userService.getUserFromSession(session);

        if (user == null) {
            ctx.status(401).json(new ErrorResponse(ErrorType.NOT_AUTHORIZED, "No active session"));
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> body = ctx.bodyAsClass(Map.class);
        String summary = (String) body.get("summary");

        ApiToken token = apiKeyService.createApiKey(user, summary);

        if (token != null) {
            ctx.status(200).json(new ApiResponse<>(token));
        } else {
            ctx.status(500).json(new ErrorResponse(ErrorType.SERVER_ERROR, "Failed to create API key"));
        }
    }
}
