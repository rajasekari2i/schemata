package com.opsbeach.connect.core.utils;

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
    public static final String BASE_URL = "base-url";
    public static final String IS_DELETED = "isDeleted";
    public static final String CLIENT_ID = "clientId";
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
    public static final String ZENDESK = "Zendesk";
    public static final String SLA = "Sla";
    public static final String METRICS = "Metrics";
    public static final String GITHUB = "GitHub";

    //Enums
    public static final String BASIC = "Basic";
    public static final String TOKEN = "Token token=";
    public static final String BEARER = "Bearer";
    public static final String INCIDENTS = "Incidents";
    public static final String SERVICES = "Services";
    public static final String INCIDENT_METRICS = "Incident Metrics";
    public static final String INCIDENT_LOG_ENTRY = "Incident Log Entry";
    public static final String TICKET = "Ticket";
    public static final String CREATE_TICKET = "Create Ticket";
    public static final String GET_TICKETS = "Get Tickets";
    public static final String ALERTS = "Alerts";
    public static final String ANALYTICS = "Analytics";
    public static final String EMAIL = "Email";
    public static final String SMS = "SMS";
    public static final String POST_MESSAGE = "Post Message";
    public static final String RENEWAL_ACCESS_TOKEN = "Renewal Access Token";

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
    public static final String IN_PROGRESS = "IN PROGRESS";

    //AskobArchitecture
    public static final String ASKOB_MESSAGE = "Askob Message";
    public static final String ASKOB_ROUTING = "Askob Routing";
    public static final String ASKOB_WORKSPACE = "Askob Workspace";
    public static final String MESSAGE_ROUTING = "Message Routing";
    public static final String TICKET_ACTION = "Ticket Action";
    public static final String TICKET_LOG_ENTRY = "Ticket Log Entry";
    public static final String NEW = "New";
    public static final String OPEN = "Open";
    public static final String PENDING = "Pending";
    public static final String SOLVED = "Solved";
    public static final String CLOSED = "Closed";
    public static final String TICKET_METRICS = "Ticket Metrics";

    //Employee
    public static final String EMPLOYEE = "Employee";
    public static final String PILLAR = "Pillar";
    public static final String TEAM = "Team";

    //Schemata
    public static final String ORGANIZATION = "Organization";
    public static final String DOMIN = "Domin";
    public static final String TABLE = "Table";
    public static final String FIELD = "Field";

    //token
    public static final String AES_KEY = "1De@s21t1@3$5^";
    public static final String CONNECT = "Connect";
    public static final String HIGH = "High";
    public static final String MEDIUM = "Medium";
    public static final String LOW = "Low";

    //headers
    public static final String ACCEPT = "application/json";
    public static final String CONTENT_TYPE = "application/json";

    //github
    public static final String ACTIVITY = "Activity";
    public static final String EVENT_AUDIT = "Event Audit";
    public static final String DOMAIN = "Domain";
    public static final String MODEL = "Model";
    public static final String PULL_REQUEST = "Pull Request";
    public static final String SCHEMA_FILE_AUDIT = "Schema File Audit";
    public static final String WORKFLOW = "Workflow";
    public static final String COMMENT = "Comment";
    public static final String CLIENT_REPO = "Client Repo";
    public static final String CSV_UPLOAD_ROOT_FILE_PATH = "src/schemas";
    public static final String PROTOBUF_SCHEMA_ROOT_FILE_PATH = "src/main/schema";

}
