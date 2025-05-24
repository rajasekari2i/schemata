package com.opsbeach.analytics.core.enums;

import com.opsbeach.analytics.core.utils.Constants;

public enum TaskType {
    INCIDENTS(Constants.INCIDENTS), 
    INCIDENT_METRICS(Constants.INCIDENT_METRICS), 
    SERVICES(Constants.SERVICES), 
    INCIDENT_LOG_ENTRY(Constants.INCIDENT_LOG_ENTRY), 
    PILLAR(Constants.PILLAR), 
    TICKET(Constants.TICKET);

    private final String key;

    TaskType(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
