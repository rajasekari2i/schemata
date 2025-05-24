package com.opsbeach.virima.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import com.opsbeach.virima.entity.VirimaAsset;

import java.util.List;

public interface AssetNodeRepository extends Neo4jRepository<VirimaAsset, Long> {
    
    @Query("""
            MATCH (a:VirimaAsset) MATCH (b:VirimaAsset) WHERE a.customerId = $customerId AND b.customerId = $customerId
            AND a.assetId = $sourceAssetId AND a.hostName = $sourceHostName
            AND b.assetId = $targetAssetId AND b.hostName = $targetHostName
            MERGE (a)-[r:RELATIONSHIP {type: $relationshipType}]->(b) RETURN type(r)
            """)
    List<String> createAssetRelationship(String customerId, String sourceAssetId, String sourceHostName, String targetAssetId, String targetHostName, String relationshipType);

    @Query("""
            MATCH (a:VirimaAsset) MATCH (b:VirimaAsset) WHERE a.customerId = $customerId AND b.customerId = $customerId
            AND a.assetId = $sourceAssetId
            AND b.assetId = $targetAssetId
            MERGE (a)-[r:RELATIONSHIP {type: $relationshipType}]->(b) RETURN type(r)
            """)
    List<String> createAssetRelationship(String customerId, String sourceAssetId, String targetAssetId, String relationshipType);
}
