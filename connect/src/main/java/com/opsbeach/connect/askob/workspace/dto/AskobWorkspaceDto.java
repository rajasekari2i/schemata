package com.opsbeach.connect.askob.workspace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opsbeach.connect.askob.workspace.entity.AskobWorkspace;
import com.opsbeach.connect.askob.workspace.enums.AskobType;
import com.opsbeach.connect.core.BaseDto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AskobWorkspaceDto extends BaseDto {

    private String key;    

    private String name;

    private String token;

    @Enumerated(EnumType.STRING)
    private AskobType type;

    @JsonProperty("user_source_id")
    private String userSourceId;

    public AskobWorkspace toDomin(AskobWorkspaceDto askobWorkspaceDto) {
        return AskobWorkspace.builder().id(askobWorkspaceDto.getId())
                                       .clientId(askobWorkspaceDto.getClientId())
                                       .createdAt(askobWorkspaceDto.getCreatedAt())
                                       .updatedAt(askobWorkspaceDto.getUpdatedAt())
                                       .createdBy(askobWorkspaceDto.getCreatedBy())
                                       .updatedBy(askobWorkspaceDto.getUpdatedBy())
                                       .key(askobWorkspaceDto.getKey())
                                       .token(askobWorkspaceDto.getToken())
                                       .type(askobWorkspaceDto.getType())
                                       .name(askobWorkspaceDto.getName())
                                       .userSourceId(askobWorkspaceDto.getUserSourceId())
                                       .build();
    }
}
