package com.fintech.loan.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a business rule is violated.
 *
 * <p>Results in HTTP 400 BAD REQUEST response when thrown from a controller.
 * Used for domain-specific validation errors that are not covered by bean validation.</p>
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
