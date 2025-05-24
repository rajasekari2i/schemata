package com.opsbeach.user.base;

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
    public static final String ID = "id";
    public static final String WEEK = "WEEK";
    public static final String MONTH = "MONTH";
    public static final String MOBILE = "mobile";
    public static final String TRUE_VALUE = "true";
    public static final String BASE_URL = "baseURL";
    public static final String TIME_ZONE = "timeZone";
    public static final String TENANT_ID = "tenantId";
    public static final String IS_DELETED = "isDeleted";
    public static final String SESSION_ID = "sessionId";
    public static final String ACCOUNT_ID = "accountId";
    public static final String LOGIN_MODE = "loginMode";
    public static final String IS_LOGGED_IN = "isLoggedIn";
    public static final String CUSTOMER_VUA = "customerVua";
    public static final String END_OF_DAY = "T23:59:59.999Z";
    public static final String START_OF_DAY = "T00:00:00.000Z";
    public static final String AUTHORIZATION = "Authorization";
    public static final String DEFAULT_CLIENT_SCHEMA = "user";
    public static final String CONSENT_HANDLE = "consentHandle";
    public static final String CURRENT_DEVICE = "currentDevice";
    public static final String USER_SESSION_ID = "userSessionId";
    public static final String IS_DATA_CATEGORIZATION_SUBSCRIBED = "isDataCategorizationSubscribed";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";

    //Table
    public static final String TABLE_USER = "\"user\"";
    public static final String ADMIN_USER_NAME = "schematalabs@gmail.com";
    public static final String ADMIN_CLIENT_NAME = "demo client";
}
