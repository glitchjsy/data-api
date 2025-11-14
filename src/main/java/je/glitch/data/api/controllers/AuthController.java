package je.glitch.data.api.controllers;

import io.javalin.http.Context;
import je.glitch.data.api.models.LoginBody;
import je.glitch.data.api.models.Session;
import je.glitch.data.api.models.SessionResponse;
import je.glitch.data.api.models.User;
import je.glitch.data.api.services.AuthService;
import je.glitch.data.api.utils.ErrorResponse;
import je.glitch.data.api.utils.ErrorType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    public void handleLogin(Context ctx) {
        Session existingSession = ctx.sessionAttribute("session");
        if (existingSession != null) {
            ctx.status(400).json(new ErrorResponse(ErrorType.INVALID_REQUEST, "Already logged in"));
            return;
        }

        LoginBody loginBody = ctx.bodyAsClass(LoginBody.class);

        try {
            Session session = service.login(loginBody);

            if (session == null) {
                ctx.status(401).json(new ErrorResponse(ErrorType.NOT_AUTHORIZED, "Invalid email or password"));
                return;
            }

            User user = service.getUserByEmail(loginBody.getEmail());
            ctx.sessionAttribute("session", session);

            ctx.status(200).json(new SessionResponse(session.getLoginTime(), user));
        } catch (IllegalArgumentException ex) {
            ctx.status(400).json(new ErrorResponse(ErrorType.INVALID_REQUEST, ex.getMessage()));
        }
    }

    public void handleRegister(Context ctx) {
    }
}