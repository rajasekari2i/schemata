package com.opsbeach.sharedlib.exception;

import org.springframework.security.core.AuthenticationException;

public class UnAuthorizedException extends AuthenticationException {
    ErrorCode errorCode;

    public UnAuthorizedException(ErrorCode errorCode, final String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public UnAuthorizedException(final ErrorCode code) {
        super(null);
        this.errorCode = errorCode;
    }

    public UnAuthorizedException(String msg, Throwable t) {
        super(msg, t);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}