package com.opsbeach.user.repository;

import com.opsbeach.user.base.BaseRepository;
import com.opsbeach.user.entity.Client;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * Repository for Client Entity.
 * </p>
 */
@Repository
public interface ClientRepository extends BaseRepository<Client> {
    /*@Query(value = "SELECT s.schema_name FROM information_schema.schemata s WHERE s.schema_name = :tenant", nativeQuery = true)
    String checkTenant(@Param(value = "tenant") String tenant);*/
}