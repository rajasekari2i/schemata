package com.opsbeach.virima.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AssetRelationshipDto(@JsonProperty("customer_id") String customerId,
                                   @JsonProperty("source_cis_asset_id") String sourceAssetId,
                                   @JsonProperty("source_cis_software_name") String sourceSoftwareName,
                                   @JsonProperty("source_cis_host_name") String sourceHostName,
                                   @JsonProperty("relationship_type") String relationshipType,
                                   @JsonProperty("target_cis_asset_id") String targetAssetId,
                                   @JsonProperty("target_cis_host_name") String targetHostName) {
    
}
