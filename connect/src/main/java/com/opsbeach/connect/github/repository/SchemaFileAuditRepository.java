package com.opsbeach.connect.github.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opsbeach.connect.core.BaseRepository;
import com.opsbeach.connect.github.entity.SchemaFileAudit;

public interface SchemaFileAuditRepository extends BaseRepository<SchemaFileAudit> {
    
    @Query(value = "SELECT s.* FROM analytics.schema_file_audit s inner join analytics.model m on m.path = s.path where m.node_id = :nodeId", nativeQuery = true)
    List<SchemaFileAudit> findByModelNodeId(@Param("nodeId") Long nodeId);

    @Query(value = "SELECT s.* FROM analytics.schema_file_audit s inner join analytics.model m on m.path = s.path where m.node_id in :nodeIds", nativeQuery = true)
    List<SchemaFileAudit> findByModelNodeIds(@Param("nodeIds") List<Long> nodeIds);

    void deleteAllByClientRepoId(Long clientRepoId);
}
