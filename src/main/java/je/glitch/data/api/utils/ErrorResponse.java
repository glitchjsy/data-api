package je.glitch.data.api.utils;

import lombok.Getter;

@Getter
public class ErrorResponse {
    private final ErrorType type;
    private final String error;

    public ErrorResponse(ErrorType type) {
        this(type, type.getDefaultMessage());
    }

    public ErrorResponse(ErrorType type, String error) {
        this.type = type;
        this.error = error;
    }
}
