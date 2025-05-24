package com.opsbeach.connect.pagerduty.enums;

public enum IncidentStatus {
    TRIGGERED(1), ACKNOWLEDGED(2), RESOLVED(3);

    private final int key;

    IncidentStatus(int key) {
        this.key = key;
    }

    public int getKey() {
        return this.key;
    }
}
