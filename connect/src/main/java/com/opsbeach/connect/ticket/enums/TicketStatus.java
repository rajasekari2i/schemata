package com.opsbeach.connect.ticket.enums;

import com.opsbeach.connect.core.utils.Constants;

public enum TicketStatus {
    
    NEW(Constants.NEW), OPEN(Constants.OPEN), PENDING(Constants.PENDING), SOLVED(Constants.SOLVED), CLOSED(Constants.CLOSED);

    private final String key;

    TicketStatus(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
