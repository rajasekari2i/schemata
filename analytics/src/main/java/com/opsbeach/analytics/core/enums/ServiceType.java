package com.opsbeach.analytics.core.enums;

import com.opsbeach.analytics.core.utils.Constants;

public enum ServiceType {
    SLACK(Constants.SLACK),
    PAGER_DUTY(Constants.PAGER_DUTY),
    TEAMS(Constants.TEAMS),
    JIRA(Constants.JIRA),
    WORK_DAY(Constants.WORK_DAY),
    SERVICE_NOW(Constants.SERVICE_NOW), 
    SALES_FORCE(Constants.SALES_FORCE), 
    FRESH_DESK(Constants.FRESH_DESK), 
    ZOHO(Constants.ZOHO), 
    ZEN_DESK(Constants.ZEN_DESK);

    private final String key;

    ServiceType(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
