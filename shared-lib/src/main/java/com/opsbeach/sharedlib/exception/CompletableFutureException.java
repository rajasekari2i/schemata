package com.opsbeach.sharedlib.exception;

public class CompletableFutureException extends ServicesException {

    public CompletableFutureException(ErrorCode errorCode, final String message) {
        super(errorCode, message);
    }
}
