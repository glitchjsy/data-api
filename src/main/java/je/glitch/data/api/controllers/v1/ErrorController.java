package je.glitch.data.api.controllers.v1;

import io.javalin.http.Context;
import je.glitch.data.api.utils.ErrorResponse;
import je.glitch.data.api.utils.ErrorType;

import java.sql.SQLException;

public class ErrorController {

    public void handleException(Throwable ex, Context ctx) {
        System.err.println(ex);

        if (ex instanceof IllegalArgumentException) {
            ctx.status(400).json(new ErrorResponse(ErrorType.INVALID_REQUEST, ex.getMessage()));
            return;
        }
        if (ex instanceof SQLException) {
            ctx.status(500).json(new ErrorResponse(ErrorType.SERVER_ERROR, "An sql error has occurred"));
            return;
        }
        ctx.status(500).json(new ErrorResponse(ErrorType.SERVER_ERROR, ex.getMessage()));
    }

    public void handleNotFound(Context ctx) {
        ctx.status(404).json(new ErrorResponse(ErrorType.NOT_FOUND, "Route not found"));
    }

    public void handleServerError(Context ctx) {
        ctx.status(500).json(new ErrorResponse(ErrorType.SERVER_ERROR, "An unknown error has occurred"));
    }
}
