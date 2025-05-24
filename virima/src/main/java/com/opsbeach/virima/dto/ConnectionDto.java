package com.opsbeach.virima.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConnectionDto(@JsonProperty("asset_id") String assetId,
                            @JsonProperty("target_asset_id") String targetAssetId,
                            @JsonProperty("relationship_type") String relationshipType) {
    
}
