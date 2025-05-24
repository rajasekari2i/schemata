package com.opsbeach.connect.ticket.enums;

import com.opsbeach.connect.core.utils.Constants;

public enum TicketSeverity {
    HIGH(Constants.HIGH), MEDIUM(Constants.MEDIUM), LOW(Constants.LOW);

    private final String key;

    TicketSeverity(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
