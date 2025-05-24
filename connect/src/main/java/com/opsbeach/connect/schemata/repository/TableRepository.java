package com.opsbeach.connect.schemata.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.enums.SchemaType;

public interface TableRepository extends Neo4jRepository<Table, Long> {
    
    List<Table> findBySchemaTypeAndClientId(SchemaType schemaType, Long clientId);

    Table findByNameAndNameSpaceAndClientId(String name, String nameSpace, Long clientId);

    List<Table> findByPrId(Long prId);

    @Query("""
            MATCH path = (startNode)<-[*]-(endNode:Table)
            WHERE ID(startNode) = $tableId
            WITH NODES(path) AS nodes
            UNWIND nodes AS n
            WITH n
            WHERE 'Table' IN LABELS(n)
            RETURN distinct(ID(n)) as ids
            """)
    Set<Long> getTableIdsConnectedToTable(@Param("tableId") Long tableId);

    @Query("""
            MATCH (a:DomainNode) MATCH (b) WHERE a.clientRepoId = $clientRepoId AND ID(b) in $tableIds
            MERGE (a)-[r:CONTAIN]->(b) RETURN type(r)
            """)
    List<String> createTableDomainRelationShip(@Param("clientRepoId") Long clientRepoId, @Param("tableIds") List<Long> tableIds);

    @Query("""
            MATCH (a:Table) OPTIONAL MATCH (a)-[r]->(b:Table) WITH a,b, CASE WHEN b IS NULL THEN a ELSE b END AS n
            WHERE a.clientId = $clientId and n.domain IS NOT NULL RETURN distinct(n.domain)
            """)
    List<String> getAllDomain(@Param("clientId") Long clientId);

    @Query("""
        MATCH (a:Table) OPTIONAL MATCH (a)-[r]->(b:Table) WITH a,b, CASE WHEN b IS NULL THEN a ELSE b END AS n
        WHERE a.clientId = $clientId and n.subscribers IS NOT NULL RETURN distinct(n.subscribers)
    """)
    List<Object> getAllSubscribers(@Param("clientId") Long clientId);

    @Query("""
        MATCH (a:Table) OPTIONAL MATCH (a)-[r]->(b:Table) WITH a,b, CASE WHEN b IS NULL THEN a ELSE b END AS n
        WHERE a.clientId = $clientId and n.owner IS NOT NULL RETURN distinct(n.owner)
        """)
    List<String> getAllOwner(@Param("clientId") Long clientId);
}
