package je.glitch.data.api.controllers;

import io.javalin.http.Context;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.models.LoginBody;
import je.glitch.data.api.models.Session;
import je.glitch.data.api.models.SessionResponse;
import je.glitch.data.api.models.User;
import je.glitch.data.api.utils.ErrorResponse;
import je.glitch.data.api.utils.ErrorType;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Date;
import java.util.Optional;

@RequiredArgsConstructor
public class AuthController {
    private final MySQLConnection connection;

    public void handleLogin(Context ctx) {
        Session existingSession = ctx.sessionAttribute("session");
        if (existingSession != null) {
            ctx.status(400).json(new ErrorResponse(ErrorType.INVALID_REQUEST, "Already logged in"));
            return;
        }

        LoginBody loginBody = ctx.bodyAsClass(LoginBody.class);

        if (loginBody.getEmail() == null || loginBody.getPassword() == null) {
            ctx.status(400).json(new ErrorResponse(ErrorType.INVALID_REQUEST, "Email and password must be provided"));
            return;
        }

        Optional<User> userOptional = connection.getUserTable().getUsers().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(loginBody.getEmail()))
                .findFirst();

        if (userOptional.isEmpty()) {
            ctx.status(401).json(new ErrorResponse(ErrorType.NOT_AUTHORIZED, "Invalid email or password"));
            return;
        }

        User user = userOptional.get();

        if (!BCrypt.checkpw(loginBody.getPassword(), user.getPassword())) {
            ctx.status(401).json(new ErrorResponse(ErrorType.NOT_AUTHORIZED, "Invalid email or password"));
            return;
        }

        Session session = new Session(new Date(), user.getId());
        ctx.sessionAttribute("session", session);

        ctx.status(200).json(new SessionResponse(session.getLoginTime(), user));
    }

    public void handleRegister(Context ctx) {

    }
}
