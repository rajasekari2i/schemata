package com.opsbeach.connect.core.enums;

import com.opsbeach.connect.core.utils.Constants;

public enum TaskType {
    INCIDENTS(Constants.INCIDENTS), 
    INCIDENT_METRICS(Constants.INCIDENT_METRICS), 
    SERVICES(Constants.SERVICES), 
    INCIDENT_LOG_ENTRY(Constants.INCIDENT_LOG_ENTRY), 
    PILLAR(Constants.PILLAR), 
    CREATE_TICKET(Constants.CREATE_TICKET),
    GET_TICKETS(Constants.GET_TICKETS),
    TICKET_METRICS(Constants.TICKET_METRICS),
    POST_MESSAGE(Constants.POST_MESSAGE),
    RENEWAL_ACCESS_TOKEN(Constants.RENEWAL_ACCESS_TOKEN);

    private final String key;

    TaskType(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
