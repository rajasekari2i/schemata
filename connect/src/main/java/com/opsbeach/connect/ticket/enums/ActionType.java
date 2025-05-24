package com.opsbeach.connect.ticket.enums;

import com.opsbeach.connect.core.utils.Constants;

public enum ActionType {

    CREATE_TICKET(Constants.CREATE_TICKET), EMAIL(Constants.EMAIL), SMS(Constants.SMS);

    private final String key;

    ActionType(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
