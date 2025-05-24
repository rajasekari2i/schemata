package com.opsbeach.sharedlib.utils;

import org.springframework.lang.Nullable;

import java.util.Objects;

/**
 * <p>
 * Enum of on-Boarding Status of an user.
 * </p>
 */
public enum OnboardStatus {
    ONBOARDED(0),
    ACCOUNT_LINKED(1),
    APPROVED(2),
    REGISTRATION_COMPLETED(3),
    COMPLETED(4),
    DEMO_USER(5);

    private final int code;

    OnboardStatus(int code) {
        this.code = code;
    }

    public static OnboardStatus valueOf(int code) {
        var onboardStatus = resolve(code);
        if (Objects.isNull(onboardStatus)) {
            throw new IllegalArgumentException("No matching constant found for [" + code + "]");
        }
        return onboardStatus;
    }

    @Nullable
    public static OnboardStatus resolve(int code) {
        for (OnboardStatus onboardStatus : values()) {
            if (onboardStatus.code() == code) {
                return onboardStatus;
            }
        }
        return null;
    }

    public int code() {
        return this.code;
    }
}