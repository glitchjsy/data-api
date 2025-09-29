package je.glitch.data.api.utils;

import lombok.Getter;

@Getter
public class ErrorResponse {
    private final ErrorType error;
    private final int status;
    private final String message;

    public ErrorResponse(ErrorType type) {
        this(type, type.getDefaultMessage());
    }

    public ErrorResponse(ErrorType type, String error) {
        this.error = type;
        this.message = error;
        this.status = type.getStatusCode();
    }
}
