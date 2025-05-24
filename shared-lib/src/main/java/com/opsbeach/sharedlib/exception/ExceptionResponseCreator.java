package com.opsbeach.sharedlib.exception;

import com.opsbeach.sharedlib.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

@Slf4j
@Component
public class ExceptionResponseCreator {

    private static final String MESSAGE_FOLLOWING_REASON = "Failed to process the request for the following reason :";
    private static final String MESSAGE_NEW_LINE = "\r\n\t";
    Environment environment;
    ExceptionResolver exceptionResolver;

    public ExceptionResponseCreator(Environment environment, ExceptionResolver exceptionResolver) {
        this.environment = environment;
        this.exceptionResolver = exceptionResolver;
    }

    /**
     * Util Function to create response entity to be used by TribetterExceptionHandler
     * and Authentication Filters unsuccessfulAuthentication overrides.
     */
    public ResponseEntity<Object> getExceptionResponseEntity(HttpStatus status, ErrorCode errorCode,
                                                             Exception exception, String message) {
        String uuid = getUUId();
        String errorKey = "OPSBEACH-" + errorCode.getKey();
        var errorAsString = getErrorAsString(exception);
        var exceptionLog = String.join(" - ", errorKey, message);
        var errorLog = String.join(": ", uuid, exceptionLog);
        log.error(errorLog);
        log.error("{} {} {} {} {} ", uuid, Constants.COLON, MESSAGE_FOLLOWING_REASON, errorAsString, errorKey);
        var resolver = new ExceptionResolver();
        String errorStack = isProdProfile().equals(Boolean.TRUE) ? Constants.EMPTY : errorAsString;
        return new ResponseEntity<>(resolver.resolveError(status, errorCode, message, errorStack, uuid), status);
    }

    public ResponseEntity<Object> getExceptionResponseEntity(HttpStatus status, ErrorCode errorCode,
                                                             Exception exception) {
        return getExceptionResponseEntity(status, errorCode, exception, exception.getMessage());
    }

    public String getErrorAsString(Exception exception) {
        return getErrorStack(exception)
                .replace(MESSAGE_NEW_LINE, " ")
                .replace("\r", " ")
                .replace("\t", " ");
    }

    /**
     * <p>
     * Generate Java Util UUID
     * </p>
     *
     * @return UUID as string
     */
    public String getUUId() {
        return UUID.randomUUID().toString().replace(Constants.HYPHEN, Constants.EMPTY);
    }

    /**
     * <p>
     * Convert exception stack trace in to string and return
     * </p>
     *
     * @param ex - Used to convert as string
     * @return Stack value as string
     */
    public String getErrorStack(Exception ex) {
        var errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    private Boolean isProdProfile() {
        for (String profile : environment.getActiveProfiles()) {
            if (profile.equals(Constants.PROFILE_ACTIVE_PRODUCTION)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
}