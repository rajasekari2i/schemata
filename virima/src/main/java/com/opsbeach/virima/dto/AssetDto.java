package com.opsbeach.virima.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opsbeach.virima.entity.Asset;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class AssetDto {

    Long id;
    @JsonProperty("created_at") LocalDateTime createdAt;
    @JsonProperty("customer_id") String customerId;
    String blueprint;
    @JsonProperty("ip_address") String ipAddress; 
    @JsonProperty("asset_name") String assetName;
    String status; 
    @JsonProperty("asset_id") String assetId; 
    @JsonProperty("host_name") String hostName;
    @JsonProperty("operating_system") String operatingSystem; 
    @JsonProperty("terminal_id") String terminalId;
    @JsonProperty("missing_components") String missingComponents; 
    @JsonProperty("hardware_asset") String hardwareAsset;

    public Asset toDomain(AssetDto assetDto) {
        return Asset.builder().id(assetDto.id)
                        .customerId(assetDto.customerId)
                        .blueprint(assetDto.blueprint)
                        .ipAddress(assetDto.ipAddress)
                        .assetName(assetDto.assetName)
                        .status(assetDto.status)
                        .assetId(assetDto.assetId)
                        .hostName(assetDto.hostName)
                        .operatingSystem(assetDto.operatingSystem)
                        .terminalId(assetDto.terminalId)
                        .missingComponents(assetDto.missingComponents)
                        .hardwareAsset(assetDto.hardwareAsset)
                        .build();

    }
}
