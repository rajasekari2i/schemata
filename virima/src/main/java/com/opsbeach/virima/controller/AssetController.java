package com.opsbeach.virima.controller;

import com.opsbeach.virima.dto.ConnectionDto;
import org.springframework.web.bind.annotation.RestController;

import com.opsbeach.sharedlib.response.SuccessResponse;
import com.opsbeach.virima.dto.AssetDto;
import com.opsbeach.virima.dto.AssetRelationshipDto;
import com.opsbeach.virima.service.AssetService;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/v1/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    
    @PostMapping
    public SuccessResponse<AssetDto> add(@RequestBody AssetDto assetDto) {
        return SuccessResponse.statusCreated(assetService.add(assetDto));
    }

    @PostMapping("/all")
    public SuccessResponse<List<AssetDto>> add(@RequestBody List<AssetDto> assetDtos) {
        return SuccessResponse.statusCreated(assetService.addAll(assetDtos));
    }
    
    @GetMapping
    public SuccessResponse<String> get(@RequestParam(name = "customer_id") String customerId,
                                       @RequestParam(name = "asset_name") String assetName,
                                       @RequestParam(name = "from_date") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime fromDate,
                                       @RequestParam(name = "to_date") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime toDate) {
        return SuccessResponse.statusOk(assetService.getAll(customerId, assetName, fromDate, toDate));
    }
    
    @PostMapping("/relationships")
    public SuccessResponse<String> createRelationship(@RequestBody AssetRelationshipDto assetRelationshipDto) {
        return SuccessResponse.statusCreated(assetService.createRelationship(assetRelationshipDto));
    }

    @PostMapping("/relationships/all")
    public SuccessResponse<List<String>> createRelationship(@RequestBody List<AssetRelationshipDto> assetRelationshipDtos) {
        return SuccessResponse.statusCreated(assetService.createRelationships(assetRelationshipDtos));
    }

    @GetMapping("/connections")
    public SuccessResponse<Set<ConnectionDto>> getConnections(@RequestParam("customer_id") String customerId,
                                                              @RequestParam("asset_id") String assetId, @RequestParam("depth") Integer depth) {
        return SuccessResponse.statusOk(assetService.getConnections(customerId, assetId, depth));
    }
}
