package com.opsbeach.sharedlib.response;

import com.opsbeach.sharedlib.utils.Constants;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Generic success message.
 * </p>
 * @param <T>
 */
@Getter
@Setter
public class SuccessMessage<T> {
    private Object entity;
    private String status;
    private String message;
    private Integer responseCode;

    /**
     * <h1>Success message.</h1>
     *
     * @param message      - Message to be displayed to the user is passed in this attribute.
     * @param entity       - object is passed in this attribute.
     * @param responseCode - response code is passed in this attribute.
     */
    public SuccessMessage(String message, Object entity, Integer responseCode) {
        this.setMessage(message);
        this.setEntity(entity);
        this.setResponseCode(responseCode);
        this.setStatus(Constants.SUCCESS);
    }
}