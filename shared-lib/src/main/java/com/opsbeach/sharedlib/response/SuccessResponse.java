package com.opsbeach.sharedlib.response;

import com.opsbeach.sharedlib.utils.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * <p>
 * Generic success response
 * </p>
 * @param <T>
 */
public class SuccessResponse<T> extends ResponseEntity<Object> {

    private SuccessResponse(Object entity, HttpStatus responseCode) {
        this(Constants.OK, entity, responseCode);
    }

    private SuccessResponse(String message, Object entity, HttpStatus httpStatus) {
        super(new SuccessMessage<T>(message, entity, httpStatus.value()), httpStatus);
    }

    public static <T> SuccessResponse<T> statusOk(Object entity) {
        return new SuccessResponse<>(entity, HttpStatus.OK);
    }

    public static <T> SuccessResponse<T> statusCreated(Object entity) {
        return new SuccessResponse<>(entity, HttpStatus.CREATED);
    }

    public static <T> SuccessResponse<T> statusNoContent(Object entity) {
        return new SuccessResponse<>(entity, HttpStatus.NO_CONTENT);
    }

    public static <T> SuccessResponse<T> statusAccepted(Object entity) {
        return new SuccessResponse<>(entity, HttpStatus.ACCEPTED);
    }
    public static <T> SuccessResponse<T> of(String message, Object entity, HttpStatus httpStatus) {
        return new SuccessResponse<>(message, entity, httpStatus);
    }
}