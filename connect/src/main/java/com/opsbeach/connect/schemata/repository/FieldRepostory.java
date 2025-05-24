package com.opsbeach.connect.schemata.repository;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import com.opsbeach.connect.schemata.entity.Field;

public interface FieldRepostory extends Neo4jRepository<Field, Long> {
    
    List<Field> findByPrId(Long prId);

    @Query(value = "MATCH (n:Field)<-[r]-(m:Table) where ID(n)=$id RETURN ID(m)")
    Long getTableIdOfField(@Param("id") Long id);
}
