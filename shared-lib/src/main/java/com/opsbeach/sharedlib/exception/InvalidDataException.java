package com.opsbeach.sharedlib.exception;

/**
 * <p>
 * User defined exception invalid data is exist throw an error.
 * </p>
 */
public class InvalidDataException extends ServicesException {

    public InvalidDataException(final ErrorCode code, final String message) {
        super(code, message);
    }
}