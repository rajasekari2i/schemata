package com.opsbeach.connect.askob.routing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opsbeach.connect.askob.routing.entity.AskobRouting;
import com.opsbeach.connect.core.BaseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AskobRoutingDto extends BaseDto {

    @JsonProperty("channel_origin")
    private String channelOrigin;

    @JsonProperty("channel_to")
    private String channelTo;

    @JsonProperty("origin_workspace_id")
    private Long originWorkspaceId;

    @JsonProperty("to_workspace_id")
    private Long toWorkspaceId;

    public AskobRouting toDomin(AskobRoutingDto askobRoutingDto) {

        return AskobRouting.builder().id(askobRoutingDto.getId())
                                     .clientId(askobRoutingDto.getClientId())
                                     .createdAt(askobRoutingDto.getCreatedAt())
                                     .updatedAt(askobRoutingDto.getUpdatedAt())
                                     .createdBy(askobRoutingDto.getCreatedBy())
                                     .updatedBy(askobRoutingDto.getUpdatedBy())
                                     .channelOrigin(askobRoutingDto.getChannelOrigin())
                                     .channelTo(askobRoutingDto.getChannelTo())
                                     .originWorkspaceId(askobRoutingDto.getOriginWorkspaceId())
                                     .toWorkspaceId(askobRoutingDto.getToWorkspaceId())
                                     .build();                                     
    }
}
