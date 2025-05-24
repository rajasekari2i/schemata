package com.opsbeach.connect.core;

import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.sharedlib.security.SecurityUtil;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 * Base repository implementation of a table which every table extends.
 * </p>
 */
public class BaseRepositoryImpl<T extends BaseModel> extends SimpleJpaRepository<T, Long> implements BaseRepository<T> {

    private final EntityManager entityManager;
    private final Specification<T> notDeleted = (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(Constants.IS_DELETED), Boolean.FALSE);

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    private boolean isClientIdEmpty() {
        return ObjectUtils.isEmpty(SecurityUtil.getClientId());
    }

    private Specification<T> clientSpec() {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(Constants.CLIENT_ID), SecurityUtil.getClientId());
    }

    @Override
    public Optional<T> findOne(@Nullable Specification<T> specification) {
        Specification<T> baseSpec = Objects.requireNonNull(specification).and(notDeleted);
        baseSpec = isClientIdEmpty() ? baseSpec : baseSpec.and(clientSpec());
        return super.findOne(baseSpec);
    }

    @Override
    public List<T> findAll() {
        Specification<T> baseSpec = isClientIdEmpty() ? notDeleted : notDeleted.and(clientSpec());
        return super.findAll(baseSpec);
    }

    @Override
    public List<T> findAll(@Nullable Specification<T> specification) {
        Specification<T> baseSpec = Objects.requireNonNull(specification).and(notDeleted);
        baseSpec = isClientIdEmpty() ? baseSpec : baseSpec.and(clientSpec());
        return super.findAll(baseSpec);
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        Specification<T> baseSpec = isClientIdEmpty() ? notDeleted : notDeleted.and(clientSpec());
        return super.findAll(baseSpec, pageable);
    }

    @Override
    public Page<T> findAll(@Nullable Specification<T> specification, Pageable pageable) {
        Specification<T> baseSpec = notDeleted.and(specification);
        baseSpec = isClientIdEmpty() ? baseSpec : baseSpec.and(clientSpec());
        return super.findAll(baseSpec, Objects.requireNonNull(pageable));
    }

    @Override
    public <S extends T> S save(S entity) {
        if (Boolean.FALSE.equals(isClientIdEmpty())) {
            entity.setClientId(SecurityUtil.getClientId());
            if (ObjectUtils.isEmpty(entity.getId())) entity.setCreatedBy(SecurityUtil.getUserDetails().getId());
            entity.setUpdatedBy(SecurityUtil.getUserDetails().getId());
        }
        return super.save(entity);
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> entityList) {
        // no need to set client Id in this method becauese saveAll() calls save() method to each and every object.
        // In save() method we set client Id.
        return super.saveAll(entityList);
    }

    @Override
    public long count() {
        Specification<T> baseSpec = isClientIdEmpty() ? notDeleted : notDeleted.and(clientSpec());
        return super.count(baseSpec);
    }

    @Override
    public long count(@Nullable Specification<T> spec) {
        Specification<T> baseSpec = isClientIdEmpty() ? notDeleted : notDeleted.and(clientSpec());
        return super.count(baseSpec.and(spec));
    }

    @Override
    @Transactional
    public void refresh(T t) {
        entityManager.refresh(t);
    }
}