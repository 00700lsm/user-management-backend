package dev.usermanagement.shared;

import org.springframework.http.HttpStatus;

/** Business error rendered as ApiError by ApiExceptionHandler. Runtime, so it rolls back transactions. */
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
    public HttpStatus status() { return status; }
    public String code() { return code; }
}
