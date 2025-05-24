package com.opsbeach.connect.github.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.github.dto.SchemaFileAuditDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@Table(name = "schema_file_audit")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class SchemaFileAudit extends BaseModel {
    private String name;
    @Column(name = "client_repo_id")
    private Long clientRepoId;
    @Column(name = "file_type")
    private String fileType;
    private String path;
    private String checksum;
    @Column(name = "root_node_id")
    private Long rootNodeId;
    @Column(name = "pull_request_id")
    private Long pullRequestId;

    public SchemaFileAuditDto toDto(SchemaFileAudit schemaFileAudit) {
        return SchemaFileAuditDto.builder().id(schemaFileAudit.getId())
                                           .name(schemaFileAudit.getName())
                                           .clientRepoId(schemaFileAudit.getClientRepoId())
                                           .fileType(schemaFileAudit.getFileType())
                                           .path(schemaFileAudit.getPath())
                                           .checksum(schemaFileAudit.getChecksum())
                                           .build();
    }
}
