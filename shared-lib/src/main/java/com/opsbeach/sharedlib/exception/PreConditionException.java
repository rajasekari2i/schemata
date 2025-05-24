package com.opsbeach.sharedlib.exception;

public class PreConditionException extends ServicesException {

    public PreConditionException(ErrorCode errorCode, final String message) {
        super(errorCode, message);
    }
}
