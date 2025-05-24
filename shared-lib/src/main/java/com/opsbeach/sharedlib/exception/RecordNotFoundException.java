package com.opsbeach.sharedlib.exception;

public class RecordNotFoundException extends ServicesException {

    public RecordNotFoundException(final ErrorCode code, final String message) {
        super(code, message);
    }
}