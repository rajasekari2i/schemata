package com.opsbeach.sharedlib.exception;

import com.opsbeach.sharedlib.utils.Constants;
import com.opsbeach.sharedlib.utils.DateUtil;
import com.opsbeach.sharedlib.utils.StringUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

/**
 * <p>
 * Interface - Generic exception response message.
 * </p>
 */
@Component
public class ExceptionResolver {

    public ExceptionDetail resolveError(final HttpStatus statusCode, final ErrorCode messageCode, final String message, final String error, final String gid) {
        final var builder = new ExceptionDetail.Builder();
        builder.setMessage(message);
        builder.setException(error);
        builder.setResponseCode(statusCode.value());
        builder.setMessageCode(String.valueOf(messageCode.getKey()));
        builder.setStatus(StringUtil.constructStringEmptySeparator(gid, Constants.COLON, Constants.FAILED));
        builder.setDateTime(DateUtil.currentDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return builder.build();
    }
}