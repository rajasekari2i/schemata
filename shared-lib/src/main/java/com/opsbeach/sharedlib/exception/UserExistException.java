package com.opsbeach.sharedlib.exception;

public class UserExistException extends ServicesException {

    public UserExistException(final ErrorCode code, final String message) {
        super(code, message);
    }
}