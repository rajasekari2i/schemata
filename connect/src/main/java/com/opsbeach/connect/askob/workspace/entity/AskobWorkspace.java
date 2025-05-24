package com.opsbeach.connect.askob.workspace.entity;

import com.opsbeach.connect.askob.workspace.dto.AskobWorkspaceDto;
import com.opsbeach.connect.askob.workspace.enums.AskobType;
import com.opsbeach.connect.core.BaseModel;
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
@Table(name = "askob_workspace")
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AskobWorkspace extends BaseModel {
    private String key;    

    private String name;

    private String token;

    @Enumerated(EnumType.STRING)
    private AskobType type;
    @Column(name = "user_source_id")
    private String userSourceId;

    public AskobWorkspaceDto toDto(AskobWorkspace askobWorkspace) {
        return AskobWorkspaceDto.builder().id(askobWorkspace.getId())
                                          .clientId(askobWorkspace.getClientId())
                                          .createdAt(askobWorkspace.getCreatedAt())
                                          .updatedAt(askobWorkspace.getUpdatedAt())
                                          .createdBy(askobWorkspace.getCreatedBy())
                                          .updatedBy(askobWorkspace.getUpdatedBy())
                                          .key(askobWorkspace.getKey())
                                          .token(askobWorkspace.getToken())
                                          .type(askobWorkspace.getType())
                                          .name(askobWorkspace.getName())
                                          .userSourceId(askobWorkspace.getUserSourceId())
                                          .build();
    }
}
