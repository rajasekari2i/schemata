package com.opsbeach.analytics.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Application constant values.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {
    // Common
    public static final String BASE_URL = "baseURL";
    public static final String IS_DELETED = "isDeleted";
    public static final String UTC = "UTC";
    public static final String CREATED = "CREATED";
    public static final String SLACK = "slack";
    public static final String PAGER_DUTY= "pagerDuty";
    public static final String JIRA = "jira";
    public static final String TEAMS = "teams";
    public static final String WORK_DAY = "workDay";
    public static final String SERVICE_NOW = "Service Now";
    public static final String SALES_FORCE = "Sales Force";
    public static final String FRESH_DESK = "Fresh Desk";
    public static final String ZOHO = "Zoho";
    public static final String ZEN_DESK = "Zen Desk";
    public static final String SLA = "Sla";
    public static final String METRICS = "Metrics";

    //Enums
    public static final String BASIC = "Basic";
    public static final String TOKEN = "Token token=";
    public static final String BEARER = "Basic";
    public static final String INCIDENTS = "Incidents";
    public static final String SERVICES = "Services";
    public static final String INCIDENT_METRICS = "Incident Metrics";
    public static final String INCIDENT_LOG_ENTRY = "Incident Log Entry";
    public static final String TICKET = "Ticket";

    // pagerduty headers
    public static final String X_EARLY_ACCESS = "X-EARLY-ACCESS";
    public static final String ANALYTICS_V2 = "analytics-v2";
    public static final String PAGER_DUTY_ACCEPT = "application/vnd.pagerduty+json;version=2";
    public static final String PAGER_DUTY_CONTENT_TYPE = "application/json";

    // Jira
    public static final String STORY = "Story";
    public static final String TASK = "Task";
    public static final String SUB_TASK = "Sub-task";
    public static final String BUG = "Bug";
    public static final String EPIC = "Epic";
    public static final String KEY = "key";
    public static final String IN_PROGRESS = "IN PROGRESS";

    //AskobArchitecture
    public static final String ASKOB_MESSAGE = "Askob Message";
    public static final String ASKOB_ROUTING = "Askob Routing";
    public static final String ASKOB_WORKSPACE = "Askob Workspace";
    public static final String MESSAGE_ROUTING = "Message Routing";
    public static final String ASKOB_TICKET_LOG = "Askob Ticket Log";
    public static final String NEW = "New";
    public static final String OPEN = "Open";
    public static final String PENDING = "Pending";
    public static final String SOLVED = "Solved";
    public static final String CLOSED = "Closed";

    //Employee
    public static final String EMPLOYEE = "Employee";
    public static final String PILLAR = "Pillar";
    public static final String TEAM = "Team";
}
