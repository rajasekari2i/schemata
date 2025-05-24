package com.opsbeach.sharedlib.exception;

public class BadRequestException extends ServicesException {

    public BadRequestException(final ErrorCode code, String message) {
        super(code, message);
    }
}