package com.opsbeach.sharedlib.exception;

public class EncodeException extends ServicesException {

    public EncodeException(ErrorCode errorCode, final String message) {
        super(errorCode, message);
    }
}
