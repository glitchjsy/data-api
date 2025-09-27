package je.glitch.data.api.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorType {
    MISSING_USER_AGENT("You are missing a valid User-Agent header"),
    INVALID_REQUEST("Invalid request"),
    NOT_FOUND("Resource not found"),
    NOT_AUTHORIZED("Missing authentication"),
    FORBIDDEN("You are not authorised to perform that action"),
    SERVER_ERROR("An unknown error has occurred"),
    RATE_LIMITED("You have exceeded the rate limit"),
    ALREADY_EXISTS("The entity you are creating already exists");

    private final String defaultMessage;
}
