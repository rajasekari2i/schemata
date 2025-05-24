package com.opsbeach.connect.ticket.enums;

import com.opsbeach.connect.core.utils.Constants;

public enum TicketType {
    
    ZOHO(Constants.ZOHO), JIRA(Constants.JIRA), ZENDESK(Constants.ZENDESK);

    private final String key;

    TicketType(String key) {
        this.key = key;
    }

    public String getkey() {
        return this.key;
    }
}
