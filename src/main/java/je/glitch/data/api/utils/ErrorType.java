package je.glitch.data.api.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorType {
    MISSING_USER_AGENT("You are missing a valid User-Agent header", 400),
    INVALID_REQUEST("Invalid request", 400),
    NOT_FOUND("The requested resource does not exist", 404),
    NOT_AUTHORIZED("Missing authentication", 401),
    FORBIDDEN("You are not authorised to perform that action", 403),
    SERVER_ERROR("An unknown error has occurred", 500),
    RATE_LIMITED("You have exceeded the rate limit", 429),
    ALREADY_EXISTS("The resource you are creating already exists", 409);

    private final String defaultMessage;
    private final int statusCode;
}
