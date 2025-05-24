package com.opsbeach.sharedlib.exception;

public class AlreadyExistException extends ServicesException {
    
    public AlreadyExistException(final ErrorCode code, String message) {
        super(code, message);
    }
}
