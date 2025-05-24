package com.opsbeach.virima.entity;

import java.util.List;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Node
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VirimaAsset {

    @Id
    @GeneratedValue
    private Long id;
    
    private String customerId;
    
    private String assetId;

    private String hostName;

    @Relationship(value = "RELATIONSHIP")
    private List<VirimaAsset> virimaAssets;
}
