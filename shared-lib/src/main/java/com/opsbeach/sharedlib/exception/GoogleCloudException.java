package com.opsbeach.sharedlib.exception;

public class GoogleCloudException extends ServicesException {

    public GoogleCloudException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
