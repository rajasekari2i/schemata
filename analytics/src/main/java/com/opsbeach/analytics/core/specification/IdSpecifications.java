package com.opsbeach.analytics.core.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.opsbeach.sharedlib.security.SecurityUtil;

/**
 * <p>
 * Specification for querying database.
 * </p>
 */
@Component
public class IdSpecifications<T> {

    private static final String CLIENT_ID = "clientId";
    public static final String ID = "id";
    private static final String DELETED = "isDeleted";

    public Specification<T> findById(long id) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(ID), id);
    }

    public Specification<T> findByClientId() {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(CLIENT_ID), SecurityUtil.getClientId());
    }

    public Specification<T> findByDeleted(Boolean isDeleted) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(DELETED), isDeleted);
    }
}
