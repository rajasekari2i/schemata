package com.opsbeach.virima.core.specification;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Specification for querying database.
 * </p>
 */
@Component
public class IdSpecifications<T> {

    public static final String ID = "id";
    private static final String DELETED = "isDeleted";
    private static final String CUSTOMER_ID = "customerId";
    private static final String ASSET_NAME = "assetName";
    private static final String CREATED_AT = "createdAt";
    private static final String UPDATED_AT = "updatedAt";

    public Specification<T> findById(long id) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(ID), id);
    }

    public Specification<T> findByDeleted(Boolean isDeleted) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(DELETED), isDeleted);
    }

    public Specification<T> greaterThanCreatedAt(LocalDateTime dateTime) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get(CREATED_AT), dateTime);
    }

    public Specification<T> lessThanCreatedAt(LocalDateTime dateTime) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get(CREATED_AT), dateTime);
    }

    public Specification<T> findByUpdatedAt(LocalDateTime updatedAt) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(UPDATED_AT), updatedAt);
    }

    public Specification<T> findByCustomerId(String customerId) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(CUSTOMER_ID), customerId);
    }

    public Specification<T> findByAssetName(String assetName) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(ASSET_NAME), assetName);
    }
}
