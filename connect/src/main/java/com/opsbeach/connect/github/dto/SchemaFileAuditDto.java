package com.opsbeach.connect.github.dto;

import com.opsbeach.connect.github.entity.SchemaFileAudit;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SchemaFileAuditDto {

    Long id;
    String name;
    Long clientRepoId;
    String fileType;
    String path;
    Long rootNodeId;
    String checksum;
    Long pullRequestId;

    public SchemaFileAudit toDomain(SchemaFileAuditDto schemaFileAuditDto) {
        return SchemaFileAudit.builder().id(schemaFileAuditDto.id)
                                        .name(schemaFileAuditDto.name)
                                        .clientRepoId(schemaFileAuditDto.clientRepoId)
                                        .fileType(schemaFileAuditDto.fileType)
                                        .path(schemaFileAuditDto.path)
                                        .checksum(schemaFileAuditDto.checksum)
                                        .pullRequestId(schemaFileAuditDto.pullRequestId)
                                        .build();
    }
}
