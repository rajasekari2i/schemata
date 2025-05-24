package com.opsbeach.connect.core.enums;

import com.opsbeach.connect.core.utils.Constants;

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
    ZENDESK(Constants.ZENDESK),
    METRICS(Constants.METRICS),
    GITHUB(Constants.GITHUB);

    private final String key;

    ServiceType(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
