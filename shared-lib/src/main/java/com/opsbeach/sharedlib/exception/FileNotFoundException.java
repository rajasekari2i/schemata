package com.opsbeach.sharedlib.exception;

public class FileNotFoundException extends ServicesException {
    
    public FileNotFoundException(ErrorCode code, String message) {
        super(code, message);
    }
}
