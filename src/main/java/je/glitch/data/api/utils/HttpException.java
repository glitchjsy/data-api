package je.glitch.data.api.utils;

import lombok.Getter;

/**
 * Custom exception for HTTP errors.
 */
@Getter
public class HttpException extends RuntimeException {
    private final ErrorType errorType;
    private final int status;

    /**
     * Constructs a new HttpException.
     *
     * @param errorType the type of error
     * @param status    the HTTP status code
     * @param message   the error message
     */
    public HttpException(ErrorType errorType, int status, String message) {
        super(message);
        this.errorType = errorType;
        this.status = status;
    }
}
