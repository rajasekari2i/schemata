package com.opsbeach.connect.github.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.github.dto.WorkflowDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Workflow extends BaseModel {
    @Setter
    @Column(name = "domain_id")
    private Long domainId;
    @Column(name = "node_id")
    private Long nodeId;
    @Column(name = "schema_name")
    private String schemaName;
    @Column(name = "stack_holders")
    private String stackHolders;
    private String purpose;
    private String creator;
    @Column(name = "additional_reference")
    private String additionalReference;
    @Enumerated(EnumType.STRING)
    private Status status;
    private float rank;
    private String title;

    public enum Status {
        NEW(0), PR_RAISED(1), PR_MERGED(3), PR_CLOSED(4);
        private final int key;
        Status(int key) {
            this.key = key;
        }
        public int getKey() {
            return this.key;
        }
    }

    public WorkflowDto toDto(Workflow workflow) {
        return WorkflowDto.builder().id(workflow.getId())
                                    .domainId(workflow.getDomainId())
                                    .nodeId(workflow.getNodeId())
                                    .schemaName(workflow.getSchemaName())
                                    .stackHolders(workflow.getStackHolders())
                                    .purpose(workflow.getPurpose())
                                    .creator(workflow.getCreator())
                                    .additionalReference(workflow.getAdditionalReference())
                                    .status(workflow.getStatus())
                                    .rank(workflow.getRank())
                                    .title(workflow.getTitle())
                                    .build();
    }
}
