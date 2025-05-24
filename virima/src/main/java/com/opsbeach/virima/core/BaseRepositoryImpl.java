package com.opsbeach.virima.core;

import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import com.opsbeach.virima.core.utils.Constants;

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

    @Override
    @NonNull
    @SuppressWarnings("null")
    public Optional<T> findOne(Specification<T> specification) {
        Specification<T> baseSpec = Objects.requireNonNull(specification).and(notDeleted);
        return super.findOne(baseSpec);
    }

    @Override
    @NonNull
    public Page<T> findAll(@NonNull Pageable pageable) {
        return super.findAll(notDeleted, pageable);
    }

    @Override
    @NonNull
    @SuppressWarnings("null")
    public Page<T> findAll(Specification<T> specification, @NonNull Pageable pageable) {
        Specification<T> baseSpec = notDeleted.and(specification);
        return super.findAll(baseSpec, Objects.requireNonNull(pageable));
    }

    @Override
    public long count() {
        return super.count(notDeleted);
    }

    @SuppressWarnings("null")
    @Override
    public long count(Specification<T> spec) {
        return super.count(notDeleted.and(spec));
    }

    @Override
    @Transactional
    public void refresh(T t) {
        entityManager.refresh(t);
    }
}