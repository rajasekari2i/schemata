package com.opsbeach.virima.entity;

import com.opsbeach.virima.core.BaseModel;
import com.opsbeach.virima.dto.AssetDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Asset extends BaseModel {
    
    @Column(name = "customer_id")
    private String customerId;
    private String blueprint;
    @Column(name = "ip_address")
    private String ipAddress;
    @Column(name = "asset_name")
    private String assetName;
    private String status;
    @Column(name = "asset_id")
    private String assetId;
    @Column(name = "host_name")
    private String hostName;
    @Column(name = "operating_system")
    private String operatingSystem;
    @Column(name = "terminal_id")
    private String terminalId;
    @Column(name = "missing_components")
    private String missingComponents;
    @Column(name = "hardware_asset")
    private String hardwareAsset;

    public AssetDto toDto(Asset asset) {
        return AssetDto.builder().id(asset.getId())
                                 .createdAt(asset.getCreatedAt())
                                 .customerId(asset.getCustomerId())
                                 .blueprint(asset.getBlueprint())
                                 .ipAddress(asset.getIpAddress())
                                 .assetName(asset.getAssetName())
                                 .status(asset.getStatus())
                                 .assetId(asset.getAssetId())
                                 .hostName(asset.getHostName())
                                 .operatingSystem(asset.getOperatingSystem())
                                 .terminalId(asset.getTerminalId())
                                 .missingComponents(asset.getMissingComponents())
                                 .hardwareAsset(asset.getHardwareAsset())
                                 .build();

    }
}
