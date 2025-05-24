package com.opsbeach.virima;

import java.time.LocalDateTime;

import com.opsbeach.virima.dto.AssetDto;
import com.opsbeach.virima.entity.Asset;

public class ResourceLoader {
    
    public static Asset getAsset() {
        return Asset.builder().id(1L).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).isDeleted(Boolean.FALSE).createdBy(1L).updatedBy(1L)
                              .customerId("1").blueprint("Windows Server").ipAddress("10.14.8.194")
                              .assetName("VIRIMA @ 10.14.8.194").status("Shutdown").assetId("AST000001")
                              .hostName("VIRIMA").hostName("Microsoft Windows Server 2008 R2 Enterprise ")
                              .terminalId("1").missingComponents("mouse").hardwareAsset("2")
                              .build();                              
    }

    public static AssetDto getAssetDto() {
        var asset = getAsset();
        return asset.toDto(asset);
    }
}
