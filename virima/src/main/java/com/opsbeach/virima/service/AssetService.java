package com.opsbeach.virima.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import com.opsbeach.virima.core.specification.IdSpecifications;
import com.opsbeach.virima.dto.AssetDto;
import com.opsbeach.virima.dto.AssetRelationshipDto;
import com.opsbeach.virima.dto.ConnectionDto;
import com.opsbeach.virima.entity.Asset;
import com.opsbeach.virima.entity.VirimaAsset;
import com.opsbeach.virima.repository.AssetNodeRepository;
import com.opsbeach.virima.repository.AssetRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetService {
    
    private final AssetRepository assetRepository;

    private final IdSpecifications<Asset> assetSpecifications;
    
    private final AssetNodeRepository assetNodeRepository;

    private final Neo4jClient neo4jClient;

    public AssetDto add(AssetDto assetDto) {
        var asset = assetDto.toDomain(assetDto);
        assetRepository.save(asset);
        createAssetNode(asset);
        return asset.toDto(asset);
    }

    public  List<AssetDto> addAll(List<AssetDto> assetDtos) {
        return assetDtos.stream().map(this::add).toList();
    }

    private void createAssetNode(Asset asset) {
        var assetNode = VirimaAsset.builder().customerId(asset.getCustomerId())
                                 .assetId(asset.getAssetId()).hostName(asset.getHostName()).build();
        assetNodeRepository.save(assetNode);
    }

    public List<AssetDto> getAll(String customerId, String assetName, LocalDateTime fromDate, LocalDateTime toDate) {
        Specification<Asset> specification = Specification.where(null);
        if (Objects.nonNull(customerId)){
            specification = specification.and(assetSpecifications.findByCustomerId(customerId));
        }
        if (Objects.nonNull(assetName)){
            specification = specification.and(assetSpecifications.findByAssetName(assetName));
        }
        if (Objects.nonNull(fromDate)){
            specification = specification.and(assetSpecifications.greaterThanCreatedAt(fromDate));
        }
        if (Objects.nonNull(toDate)){
            specification = specification.and(assetSpecifications.lessThanCreatedAt(toDate));
        }
        var assets = assetRepository.findAll(specification);
        return assets.isEmpty() ? List.of() : assets.stream().map(asset -> assets.get(0).toDto(asset)).toList();
    }

    public List<String> createRelationship(AssetRelationshipDto assetRelationshipDto) {
        return assetNodeRepository.createAssetRelationship(assetRelationshipDto.customerId(), assetRelationshipDto.sourceAssetId(), assetRelationshipDto.targetAssetId(), assetRelationshipDto.relationshipType());
    }

    public List<List<String>> createRelationships(List<AssetRelationshipDto> assetRelationshipDtos) {
        return assetRelationshipDtos.stream().map(this::createRelationship).toList();
    }

    public Set<ConnectionDto> getConnections(String customerId, String assetId, Integer depth) {
        /*
         * The bellow cypher query is to return the paths of selected node to depth 'n'.
         * Which fetch the paths of node till the depth. and collect the nodes of the path as 'nodes' and relationships as 'rels'
         * UNWIND range(0, size(rels)-1) AS i which gives the index of each rel in rels with the help of it we can get fromNode, relationshipType, and EndNode.
         */
        var query = """
            Match path = (a:VirimaAsset)-[r:RELATIONSHIP*1..$depth]->(b:VirimaAsset)
            where a.assetId = '$assetId' and a.customerId = '$customerId'
            WITH nodes(path) AS nodes, relationships(path) AS rels
            UNWIND range(0, size(rels)-1) AS i
            RETURN nodes[i].assetId AS asset_id, rels[i].type AS relationship_type, nodes[i+1].assetId AS target_asset_id
            """;
        query = query.replace("$depth", depth.toString()).replace("$assetId", assetId).replace("$customerId", customerId);
        System.out.println(query);
        Set<ConnectionDto> connectionDtos = new HashSet<>();
        neo4jClient.query(query).fetch().all().forEach(map -> {
            connectionDtos.add(new ConnectionDto(map.get("asset_id").toString(), map.get("target_asset_id").toString(), map.get("relationship_type").toString()));
        });
        return connectionDtos;
    }
}
