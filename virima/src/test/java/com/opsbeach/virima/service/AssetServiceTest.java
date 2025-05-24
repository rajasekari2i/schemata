package com.opsbeach.virima.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jClient.RecordFetchSpec;
import org.springframework.data.neo4j.core.Neo4jClient.UnboundRunnableSpec;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.opsbeach.virima.ResourceLoader;
import com.opsbeach.virima.core.specification.IdSpecifications;
import com.opsbeach.virima.dto.AssetRelationshipDto;
import com.opsbeach.virima.entity.Asset;
import com.opsbeach.virima.repository.AssetNodeRepository;
import com.opsbeach.virima.repository.AssetRepository;

public class AssetServiceTest {

    @InjectMocks
    private AssetService assetService;
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private AssetNodeRepository assetNodeRepository;
    @Spy
    private IdSpecifications<Asset> assetSpecifications;
    @Mock
    private Neo4jClient neo4jClient;
    @Mock
    private UnboundRunnableSpec unboundRunnableSpec;
    @Mock
    private RecordFetchSpec<Map<String,Object>> recordFetchSpec;
    
    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void addTest() {
        var asset = ResourceLoader.getAsset();
        var assetDto = asset.toDto(asset);
        when(assetRepository.save(any(Asset.class))).thenReturn(asset);
        var response = assetService.add(assetDto);
        assertEquals(response.getAssetId(), assetDto.getAssetId());
    }

    @Test
    public void getAllTest() {
        assertEquals(assetService.getAll(null, null, null, null).size(), 0);
        var asset = ResourceLoader.getAsset();
        when(assetRepository.findAll(ArgumentMatchers.<Specification<Asset>>any())).thenReturn(List.of(asset));
        var response = assetService.getAll("1", "AST01", LocalDateTime.now(), LocalDateTime.now());
        assertEquals(response.size(), 1);
        assertEquals(response.get(0).getAssetId(), asset.getAssetId());
    }

    @Test
    public void createRelationshipTest() {
        var response = assetService.createRelationship(new AssetRelationshipDto("1", "AST01", "sf", "HS1", "Installed On", "AST02", "HS1"));
        assertEquals(0, response.size());
    }

    @Test
    public void getConnectionsTest() {
        Collection<Map<String,Object>> value = List.of(Map.of("asset_id", "AST01", "relationship_type", "Installed On", "target_asset_id", "AST02"));
            Mockito.when(neo4jClient.query(Mockito.anyString())).thenReturn(unboundRunnableSpec);
            Mockito.when(unboundRunnableSpec.fetch()).thenReturn(recordFetchSpec);
            Mockito.when(recordFetchSpec.all()).thenReturn(value);
        var response = assetService.getConnections("1", "AST01", 1);
        assertEquals(response.size(), 1);
        assertEquals(response.iterator().next().assetId(), "AST01");
    }
}
