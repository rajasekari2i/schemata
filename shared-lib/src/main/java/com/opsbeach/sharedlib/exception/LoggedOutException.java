package com.opsbeach.sharedlib.exception;

public class LoggedOutException extends ServicesException {

    public LoggedOutException(final ErrorCode code, final String message) {
        super(code, message);
    }
}