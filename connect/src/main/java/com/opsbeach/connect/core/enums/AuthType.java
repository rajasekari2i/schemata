package com.opsbeach.connect.core.enums;

import com.opsbeach.connect.core.utils.Constants;

public enum AuthType {
    BASIC(Constants.BASIC), 
    TOKEN(Constants.TOKEN),
    BEARER(Constants.BEARER); 

    private final String key;

    AuthType(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
