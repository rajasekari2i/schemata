package com.opsbeach.sharedlib.utils;

import com.opsbeach.sharedlib.exception.CompletableFutureException;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.security.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class FutureUtil {

    private final ResponseMessage responseMessage;
    private final ApplicationConfig applicationConfig;

    public FutureUtil(ResponseMessage responseMessage, ApplicationConfig applicationConfig) {
        this.responseMessage = responseMessage;
        this.applicationConfig = applicationConfig;
    }

    /**
     * Cancel the {@link Future} if it is still executing. The {@link Future} will be interrupted.
     */
    public void tryCancelFuture(Future<?> future) {
        if (!(future.isDone() || future.isCancelled())) {
            future.cancel(Boolean.TRUE);
        }
    }

    /**
     * Get value from the {@link Future} {@code get} method with a timeout (configured in application properties), defaulted to 60 seconds.
     * Encapsulates any thrown exception in a {@link RuntimeException} so that this method can be used as an action to {@code Iterable<T>.forEach}.
     *
     * @return Return value from {@code get} call on the {@link Future}
     */
    public <T> T safeGet(Future<T> future) {
        try {
            return future.get(applicationConfig.getThreadingFutureTimeout(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Error in retrieving future - {}", e.getMessage());
            Thread.currentThread().interrupt();
            throw new CompletableFutureException(ErrorCode.COMPLETABLE_FUTURE, responseMessage.getErrorMessage(ErrorCode.COMPLETABLE_FUTURE, e.getMessage()));
        }
    }
}
