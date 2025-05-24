package com.opsbeach.sharedlib.exception;

public class ClientEmptyException extends ServicesException {

    public ClientEmptyException(final ErrorCode code, final String message) {
        super(code, message);
    }
}