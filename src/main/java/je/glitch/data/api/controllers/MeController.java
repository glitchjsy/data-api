package je.glitch.data.api.controllers;

import com.google.gson.JsonObject;
import io.javalin.http.Context;
import je.glitch.data.api.database.MySQLConnection;
import je.glitch.data.api.models.Session;
import je.glitch.data.api.models.SessionResponse;
import je.glitch.data.api.models.User;
import lombok.RequiredArgsConstructor;

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
}
