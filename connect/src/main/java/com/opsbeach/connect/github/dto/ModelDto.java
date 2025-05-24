package com.opsbeach.connect.github.dto;

import com.opsbeach.connect.github.entity.Model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ModelDto {

    Long id;
    Long clientId;
    String type;
    String name;
    Long schemaFileAuditId;
    String path;
    Long nodeId;
    String nameSpace;
    Long clientRepoId;
    String checksum;
    Long domainId;
    Long pullRequestId;
    
    public Model toDomain(ModelDto modelDto) {
        return Model.builder().id(modelDto.id)
                              .clientId(modelDto.clientId)
                              .type(modelDto.type)
                              .name(modelDto.name)
                              .nameSpace(modelDto.nameSpace)
                              .schemaFileAuditId(modelDto.schemaFileAuditId)
                              .path(modelDto.path)
                              .nodeId(modelDto.nodeId)
                              .domainId(modelDto.domainId)
                              .checksum(modelDto.checksum)
                              .clientRepoId(modelDto.clientRepoId)
                              .pullRequestId(modelDto.pullRequestId)
                              .build();
    }
}
