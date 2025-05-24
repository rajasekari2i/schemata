package com.opsbeach.sharedlib.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Common constants variable user all over the application.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    //Symbols
    public static final String AT = "@";
    public static final String DOT = ".";
    public static final String EMPTY = "";
    public static final String COLON = ":";
    public static final String COMMA = ",";
    public static final String HYPHEN = "-";
    public static final String UNDERSCORE = "_";
    public static final String SEMI_COLON = ";";
    public static final String PERCENTAGE = "%";
    public static final String EMPTY_SPACE = " ";
    public static final String COMMA_SPACE = ", ";
    public static final String FORWARD_SLASH = "/";
    public static final String ASTERISK_SYMBOL = "*";
    public static final String SPACE_HYPHEN = EMPTY_SPACE + HYPHEN;
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZED_PATH_PREFIX = FORWARD_SLASH + "v1";

    //common
    public static final String OK = "OK";
    public static final String PORT = "port";
    public static final String HOST = "host";
    public static final String TOKEN = "token";
    public static final String FAILED = "FAILED";
    public static final String SUCCESS = "SUCCESS";
    public static final String BEARER = "Bearer";
    public static final String ENABLED = "enabled";
    public static final String BASE_URL = "base-url";
    public static final String GOAL_FOLDER = "goal";
    public static final String DISABLED = "disabled";
    public static final String CUSTOMER = "customer";
    public static final String PREFIX = BEARER + " ";
    public static final String COMMON_FOLDER = "common";
    public static final String PROFILE_FOLDER = "profile";
    public static final String ANONYMOUS_USER = "anonymousUser";
    public static final String TRANSACTION_FOLDER = "transaction";
    public static final String PROFILE_ACTIVE_PRODUCTION = "production";
    public static final String EXECUTED_TENANT_SCHEMA_FOLDER = "executedTenantSchema";

    //URL
    public static final String ENTITY = "entity";
    public static final String STATUS = "status";
    public static final String MESSAGE = "message";
    public static final String SESSION_ID = "sessionId";
    public static final String CLIENT_ID_HEADER = "X-ClientId";
    public static final String IS_ALREADY_LOGGED_IN = "isAlreadyLoggedIn";

    //aws
    public static final String REGION = "region";
    public static final String ACCESS_KEY_ID = "access_key_id";
    public static final String SECRET_ACCESS_KEY = "secret_access_key";

    //github
    public static final String CLIENT_ID = "client-id";
    public static final String CLIENT_SECRET = "client-secret";

    //google
    public static final String PROJECT_ID = "project-id";
    public static final String LOCATION_ID = "location-id";
    public static final String QUEUE_ID = "queue-id";
}
