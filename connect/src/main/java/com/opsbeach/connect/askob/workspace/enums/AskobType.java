package com.opsbeach.connect.askob.workspace.enums;

import com.opsbeach.connect.core.utils.Constants;

public enum AskobType {
    
    SLACK(Constants.SLACK), TEAMS(Constants.TEAMS);

    private final String key;

    AskobType(String key) {
        this.key = key;
    }

    public String getkey() {
        return this.key;
    }
}
