package com.opsbeach.sharedlib.response;

public enum SuccessCode {
    
    CREATED(1001),
    UPDATED(1002),
    FETCHED(1003),
    DELETED(1004),
    FETCHED_ALL_DATA(1005),
    OTP_SENT_SUCCESSFULLY(1006);

    private final int key;

    SuccessCode(int key) {
        this.key = key;
    }

    public int getKey() {
        return this.key;
    }
}
