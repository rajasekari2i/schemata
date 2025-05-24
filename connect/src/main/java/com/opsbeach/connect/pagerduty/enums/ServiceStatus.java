package com.opsbeach.connect.pagerduty.enums;

public enum ServiceStatus {
    ACTIVE(1), WARNING(2), CRITICAL(3), MAINTENANCE(4), DISABLED(5);

    private final int key;

    ServiceStatus(int key) {
        this.key = key;
    }

    public int getKey() {
        return this.key;
    }
}
