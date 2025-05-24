package com.opsbeach.user.base.specification;

import com.opsbeach.sharedlib.utils.Status;
import com.opsbeach.user.base.Constants;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Specification for querying database.
 * </p>
 */
@Component
public class IdSpecifications<T> {

    private static final String CLIENT_ID = "client_id";
    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String ROLE_ID = "roleId";
    private static final String EMAIL_ID = "emailId";
    private static final String IS_RECENT = "isRecent";
    private static final String LOCATION = "locationName";
    private static final String IS_BATCH_USER = "isBatchUser";
    private static final String IS_SUBSCRIBED = "isSubscribed";
    private static final String USER_ACTIVITY = "userActivity";
    private static final String USER_ID = "userId";
    private static final String USERNAME = "username";

    public Specification<T> findById(long id) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(Constants.ID), id);
    }

    public Specification<T> findByMobileNumber(String mobileNumber) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(Constants.MOBILE), mobileNumber.toLowerCase());
    }

    public Specification<T> notDeleted() {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(Constants.IS_DELETED), Boolean.FALSE);
    }

    public Specification<T> getPermissionsByRoleId(long roleId) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(ROLE_ID), roleId);
    }

    public Specification<T> findByUserId(long userId) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(USER_ID), userId);
    }

    public Specification<T> findByUsername(String username) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(USERNAME), username);
    }

    public Specification<T> findByRecentAddress(long userId) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.and(criteriaBuilder.equal(root.get(USER_ID), userId), criteriaBuilder.equal(root.get(IS_RECENT), Boolean.TRUE));
    }

    public Specification<T> findByName(String name) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(NAME), name);
    }

    public Specification<T> findAddressByAccountId(long accountId) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.ACCOUNT_ID), accountId));
    }

    public Specification<T> getLocationDetails(String city) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(LOCATION), city);
    }

    public Specification<T> findByEmail(String emailId) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(EMAIL_ID), emailId.toLowerCase());
    }

    public Specification<T> findByBatchUser() {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(IS_BATCH_USER), Boolean.TRUE);
    }

    public Specification<T> findByAccessToken(String accessToken) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(Constants.ACCESS_TOKEN), accessToken);
    }

    public Specification<T> findByRefreshToken(String refreshToken) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(Constants.REFRESH_TOKEN), refreshToken);
    }

    public Specification<T> getWidgetDetailsWithType(String type) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.and(criteriaBuilder.equal(root.get(IS_SUBSCRIBED), Boolean.TRUE), criteriaBuilder.equal(root.get(TYPE), type));
    }

    public Specification<T> getWidgetDetails() {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(IS_SUBSCRIBED), Boolean.TRUE);
    }

    public Specification<T> findByOpenSession(Long userId) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.and(criteriaBuilder.equal(root.get(USER_ID), userId), criteriaBuilder.equal(root.get(USER_ACTIVITY), Status.ACTIVE));
    }

}
