package com.opsbeach.sharedlib.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <p>
 *     Exception Details Dto used to send as a general details.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionDetailDto {
    private long dateTime;
    private String status;
    private String message;
    private String exception;
    private String messageCode;
    private Integer responseCode;
}
