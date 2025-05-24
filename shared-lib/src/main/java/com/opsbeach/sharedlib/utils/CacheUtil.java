package com.opsbeach.sharedlib.utils;

import com.opsbeach.sharedlib.security.SecurityUtil;
//import com.opsbeach.sharedlib.service.CacheService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <p>
 * Util class to retrieve values from Cache.
 * </p>
 */

@Service
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheUtil {

    /*private static final String USER_ID = "userId";
    private static final String MOBILE = "mobile";
    private static final String SECRET = "secret";
    private static final String TIME_ZONE = "timeZone";
    private static final String LOGIN_MODE = "loginMode";
    private static final String IS_LOGGED_IN = "isLoggedIn";
    private static final String CUSTOMER_VUA = "customerVua";
    private static final String PARTNER_EMAIL = "partnerEmail";
    private static final String ACC_REF_NUMBER = "accRefNumber";
    private static final String CONSENT_HANDLE = "consentHandle";
    private static final String CONSENT_TO = "consentToDateRange";
    private static final String GOAL_EVENT_FLAG = "goalEventFlag";
    private static final String USER_SESSION_ID = "userSessionId";
    private static final String CONSENT_FROM = "consentFromDateRange";
    private static final String BUDGET_EVENT_FLAG = "budgetEventFlag";
    private static final String PRIMARY_ACCOUNT_ID = "primaryAccountId";
    private static final String ON_BOARDING_EVENT_FLAG = "onboardingEventFlag";
    private static final String IS_DATA_CATEGORIZATION_SUBSCRIBED = "isDataCategorizationSubscribed";
    private static CacheService cacheService;

    @Autowired
    public CacheUtil(CacheService cacheService) {
        CacheUtil.cacheService = cacheService;
    }

    *//**
     * Fetch User ID from Cache.
     *
     * @return long - ID of current logged-in user.
     *//*
    public static long getUserId() {
        return (long) cacheService.get(SecurityUtil.getHashKey(), USER_ID);
    }

    *//**
     * Fetch Consent Handle from Cache.
     *
     * @return String - Consent Handle.
     *//*
    public static String getConsentHandle() {
        return (String) cacheService.get(SecurityUtil.getHashKey(), CONSENT_HANDLE);
    }

    *//**
     * Find whether a user has logged in (or) not.
     *
     * @return Boolean - Boolean value.
     *//*
    public static Boolean checkUserIsLoggedIn() {
        return (boolean) cacheService.get(SecurityUtil.getHashKey(), IS_LOGGED_IN);
    }

    *//**
     * Fetches from date of a Consent.
     *
     * @return String - String value.
     *//*
    public static String getConsentFromDate() {
        return (String) cacheService.get(SecurityUtil.getHashKey(), CONSENT_FROM);
    }

    *//**
     * Fetches the To date of a Consent.
     *
     * @return String - String value.
     *//*
    public static String getConsentToDate() {
        return (String) cacheService.get(SecurityUtil.getHashKey(), CONSENT_TO);
    }

    *//**
     * Fetch Primary account ID from Cache.
     *
     * @return long - Primary Account ID of logged-in user.
     *//*
    public static long getPrimaryAccountId() {
        return (long) cacheService.get(SecurityUtil.getHashKey(), PRIMARY_ACCOUNT_ID);
    }

    *//**
     * Fetch Budget Event Flag from Cache.
     *
     * @return Boolean - Budget Event Flag of logged in user.
     *//*
    public static Boolean getBudgetEventFlag() {
        var object = cacheService.get(SecurityUtil.getHashKey(), BUDGET_EVENT_FLAG);
        if (Objects.nonNull(object)) {
            return (boolean) object;
        }
        return Boolean.FALSE;
    }

    *//**
     * Fetch On-boarding Event Flag from Cache.
     *
     * @return Boolean - On-boarding Event Flag of logged-in user.
     *//*
    public static Boolean getOnboardingEventFlag() {
        var object = cacheService.get(SecurityUtil.getHashKey(), ON_BOARDING_EVENT_FLAG);
        if (Objects.nonNull(object)) {
            return (boolean) object;
        }
        return Boolean.FALSE;
    }

    *//**
     * Fetch Goal Event Flag from Cache.
     *
     * @return Boolean - Goal Event Flag of logged-in user.
     *//*
    public static Boolean getGoalEventFlag() {
        var object = cacheService.get(SecurityUtil.getHashKey(), GOAL_EVENT_FLAG);
        if (Objects.nonNull(object)) {
            return (boolean) object;
        }
        return Boolean.FALSE;
    }

    *//**
     * Fetch Session Id from Cache.
     *
     * @return String - Session ID of One Money.
     *//*
    public static String getSessionId() {
        return (String) cacheService.get(SecurityUtil.getHashKey(), Constants.SESSION_ID);
    }

    *//**
     * Fetches the CustomerVua of current user.
     *
     * @return String - CustomerVua value.
     *//*
    public static String getCustomerVua() {
        return (String) cacheService.get(SecurityUtil.getHashKey(), CUSTOMER_VUA);
    }

    *//**
     * Fetches the Account Reference number.
     *
     * @return String - Account Reference value.
     *//*
    public static String getAccountReferenceNumber() {
        return (String) cacheService.get(SecurityUtil.getHashKey(), ACC_REF_NUMBER);
    }

    *//**
     * Fetches the Mobile number.
     *
     * @return String - Mobile value.
     *//*
    public static String getMobile() {
        return (String) cacheService.get(SecurityUtil.getHashKey(), MOBILE);
    }

    *//**
     * Fetches the TimeZone of the user.
     *
     * @return String - TimeZone.
     *//*
    public static String getCustomerTimeZone() {
        return (String) cacheService.get(SecurityUtil.getHashKey(), TIME_ZONE);
    }

    *//**
     * Find whether the has subscribed to Data Categorization (or) not.
     *
     * @return Boolean - Boolean value.
     *//*
    public static Boolean checkDataCategorizationSubscriptionInCustomer() {
        return (boolean) cacheService.get(SecurityUtil.getHashKey(), IS_DATA_CATEGORIZATION_SUBSCRIBED);
    }

    *//**
     * Find whether the has subscribed to Data Categorization (or) not.
     *
     * @return Boolean - Boolean value.
     *//*
    public static Boolean checkDataCategorizationSubscriptionInPartner() {
        return (boolean) cacheService.get(SecurityUtil.getHashKey(), IS_DATA_CATEGORIZATION_SUBSCRIBED);
    }

    *//**
     * Fetch the email id of the logged in partner
     *
     * @return String - Email.
     *//*
    public static String getPartnerEmail() {
        return (String) cacheService.get(SecurityUtil.getHashKey(), PARTNER_EMAIL);
    }

    *//**
     * Find whether the has subscribed to Data Categorization (or) not.
     *
     * @return Boolean - Boolean value.
     *//*
    public static Boolean checkAlreadyLoggedIn(String hashKey) {
        return (boolean) cacheService.get(hashKey, Constants.IS_ALREADY_LOGGED_IN);
    }

    *//**
     * Fetches the TimeZone of the user.
     *
     * @return String - TimeZone.
     *//*
    public static String getPartnerTimeZone() {
        return (String) cacheService.get(SecurityUtil.getHashKey(), TIME_ZONE);
    }

    *//**
     * Fetch user session Id
     *
     * @return long - Session ID of logged-in user.
     *//*
    public static Long getUserSessionId(String hashKey) {
        return (Long) cacheService.get(hashKey, USER_SESSION_ID);
    }

    *//**
     * Fetch secret
     *
     * @return String - Secret for auth token.
     *//*
    public static String getSecret() {
        return (String) cacheService.get(SecurityUtil.getHashKey(), SECRET);
    }*/
}
