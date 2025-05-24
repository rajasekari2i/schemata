package com.opsbeach.connect.schemata.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import com.opsbeach.connect.schemata.entity.Organization;

public interface OrganizationRepository extends Neo4jRepository<Organization, Long>  {
    
    Organization findByClinetId(Long clinetId);
}
