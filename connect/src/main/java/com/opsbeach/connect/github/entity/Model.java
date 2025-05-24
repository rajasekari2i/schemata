package com.opsbeach.connect.github.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.github.dto.ModelDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@Table
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Model extends BaseModel {
    private String type;
    private String name;
    @Column(name = "name_space")
    private String nameSpace;
    @Column(name = "schema_file_audit_id")
    private Long schemaFileAuditId;
    private String path;
    @Column(name = "node_id")
    private Long nodeId;
    @Column(name = "domain_id")
    private Long domainId;
    private String checksum;
    @Column(name = "client_repo_id")
    private Long clientRepoId;
    @Setter
    @Column(name = "pull_request_id")
    private Long pullRequestId;
    public ModelDto toDto(Model model) {
        return ModelDto.builder().id(model.getId())
                                 .clientId(model.getClientId())
                                 .type(model.getType())
                                 .name(model.getName())
                                 .nameSpace(model.getNameSpace())
                                 .schemaFileAuditId(model.getSchemaFileAuditId())
                                 .path(model.getPath())
                                 .nodeId(model.getNodeId())
                                 .domainId(model.getDomainId())
                                 .checksum(model.getChecksum())
                                 .clientRepoId(model.getClientRepoId())
                                 .build();
    }
}
