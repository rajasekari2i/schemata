package com.opsbeach.sharedlib.exception;

/**
 * <p>
 * Common Service exception handler toa added the exception code
 * and read the message from property using message validator.
 * </p>
 */
public class ServicesException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String message;

    public ServicesException(final ErrorCode code, final String message) {
        this.errorCode = code;
        this.message = message;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}