package com.opsbeach.connect.github.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.github.dto.ActivityDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Activity extends BaseModel {
    @Column(name = "workflow_id")
    private Long workflowId;

    @Enumerated(EnumType.STRING)
    private Type type;
    @Column(name = "source_node_id")
    private Long sourceNodeId;
    @Column(name = "target_node_id")
    private Long targetNodeId;
    
    public enum Type {
        FIELD(0), TABLE(1);

        private final int key;

        Type(int key) {
            this.key = key;
        }

        public int getKey() {
            return this.key;
        }
    }

    public ActivityDto toDto(Activity activity) {

        return ActivityDto.builder().id(activity.getId())
                                 .clientId(activity.getClientId())
                                 .workflowId(activity.getWorkflowId())
                                 .type(activity.getType())
                                 .sourceNodeId(activity.getSourceNodeId())
                                 .targetNodeId(activity.getTargetNodeId())
                                 .build();
    }
}
