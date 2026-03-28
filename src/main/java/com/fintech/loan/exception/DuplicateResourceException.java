package com.fintech.loan.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to create a resource that already exists.
 *
 * <p>Results in HTTP 409 CONFLICT response when thrown from a controller.</p>
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
