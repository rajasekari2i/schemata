package com.opsbeach.connect.askob.messagerouting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opsbeach.connect.askob.messagerouting.entity.MessageRouting;
import com.opsbeach.connect.core.BaseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class MessageRoutingDto extends BaseDto {
    
    @JsonProperty("to_message_id")
    private Long toMessageId;

    @JsonProperty("from_message_id")
    private Long fromMessageId;

    @JsonProperty("to_workspace_id")
    private Long toWorkspaceId;

    @JsonProperty("from_workspace_id")
    private Long fromWorkspaceId;

    @JsonProperty("to_channel")
    private String toChannel;

    @JsonProperty("from_channel")
    private String fromChannel;

    public MessageRouting toDomin(MessageRoutingDto messageRoutingDto) {
        return MessageRouting.builder().clientId(messageRoutingDto.getClientId())
                                       .id(messageRoutingDto.getId())
                                       .createdAt(messageRoutingDto.getCreatedAt())
                                       .updatedAt(messageRoutingDto.getUpdatedAt())
                                       .createdBy(messageRoutingDto.getCreatedBy())
                                       .updatedBy(messageRoutingDto.getUpdatedBy())
                                       .fromChannel(messageRoutingDto.getFromChannel())
                                       .toChannel(messageRoutingDto.getToChannel())
                                       .fromWorkspaceId(messageRoutingDto.getFromWorkspaceId())
                                       .toWorkspaceId(messageRoutingDto.getToWorkspaceId())
                                       .fromMessageId(messageRoutingDto.getFromMessageId())
                                       .toMessageId(messageRoutingDto.getToMessageId())
                                       .build();
    }
}
