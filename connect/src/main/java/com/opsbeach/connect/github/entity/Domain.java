package com.opsbeach.connect.github.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.github.dto.DomainDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Domain extends BaseModel {
    private String name;
    @Column(name = "node_id")
    private Long nodeId;
    @Column(name = "client_repo_id")
    private Long clientRepoId;
    public DomainDto toDto(Domain domain) {
        return new DomainDto(domain.getId(), domain.getClientId(), domain.getNodeId(), domain.getClientRepoId(), domain.getName());
    }
}
