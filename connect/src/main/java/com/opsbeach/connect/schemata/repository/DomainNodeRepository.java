package com.opsbeach.connect.schemata.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import com.opsbeach.connect.schemata.entity.DomainNode;

public interface DomainNodeRepository extends Neo4jRepository<DomainNode, Long> {
    
    @Query("""
                MATCH (n:Organization) MATCH (m:DomainNode) WHERE ID(n) = $organizationId AND ID(m) = $domainId
                CREATE (n)-[r:HAS]->(m) RETURN ID(r)
            """)
    Long createOrganizationDomainRelationship(@Param("organizationId") Long organizationId, @Param("domainId") Long domainId);

    void deleteByClientRepoId(Long clientRepoId);
}
