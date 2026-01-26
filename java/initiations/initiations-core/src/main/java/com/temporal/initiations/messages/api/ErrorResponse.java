package com.temporal.initiations.messages.api;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Standard error response for API endpoints.
 *
 * This record is returned by the global exception handler for all error conditions,
 * providing a consistent error structure with timestamp, status code, message, and
 * optional additional details.
 *
 * @param timestamp When the error occurred
 * @param status HTTP status code
 * @param message Error message describing the problem
 * @param details Optional additional error details for debugging
 */
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String message,
    Optional<String> details
) {
}
