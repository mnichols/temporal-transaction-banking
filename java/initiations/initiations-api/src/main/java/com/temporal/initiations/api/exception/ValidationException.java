package com.temporal.initiations.api.exception;

import java.util.Optional;

/**
 * Exception thrown when validation of request data fails.
 *
 * This exception is used for validation failures and includes an error code,
 * message, and optional details for debugging.
 */
public class ValidationException extends RuntimeException {
    private final String code;
    private final Optional<String> details;

    public ValidationException(String code, String message) {
        this(code, message, null);
    }

    public ValidationException(String code, String message, String details) {
        super(message);
        this.code = code;
        this.details = Optional.ofNullable(details);
    }

    public String getCode() {
        return code;
    }

    public Optional<String> getDetails() {
        return details;
    }
}
