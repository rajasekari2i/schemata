package com.opsbeach.sharedlib.utils;

import org.springframework.lang.Nullable;

public enum Status {
    ACTIVE(1),
    IN_ACTIVE(2);

    private final int code;

    Status(int code) {
        this.code = code;
    }

    public static Status valueOf(int code) {
        var userActivity = resolve(code);
        if (userActivity == null) {
            throw new IllegalArgumentException("No matching constant found for [" + code + "]");
        }
        return userActivity;
    }

    @Nullable
    public static Status resolve(int code) {
        for (Status status : values()) {
            if (status.code() == code) {
                return status;
            }
        }
        return null;
    }

    public int code() {
        return this.code;
    }
}
