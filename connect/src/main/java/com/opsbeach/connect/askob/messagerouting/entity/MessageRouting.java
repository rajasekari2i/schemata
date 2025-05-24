package com.opsbeach.connect.askob.messagerouting.entity;

import com.opsbeach.connect.askob.messagerouting.dto.MessageRoutingDto;
import com.opsbeach.connect.core.BaseModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "message_routing")
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class MessageRouting extends BaseModel {
    @Column(name = "to_message_id")
    private Long toMessageId;
    @Column(name = "from_message_id")
    private Long fromMessageId;
    @Column(name = "to_workspace_id")
    private Long toWorkspaceId;
    @Column(name = "from_workspace_id")
    private Long fromWorkspaceId;
    @Column(name = "to_channel")
    private String toChannel;
    @Column(name = "from_channel")
    private String fromChannel;

    public MessageRoutingDto toDto(MessageRouting messageRouting) {
        return MessageRoutingDto.builder().clientId(messageRouting.getClientId())
                                          .id(messageRouting.getId())
                                          .createdAt(messageRouting.getCreatedAt())
                                          .updatedAt(messageRouting.getUpdatedAt())
                                          .createdBy(messageRouting.getCreatedBy())
                                          .updatedBy(messageRouting.getUpdatedBy())
                                          .fromChannel(messageRouting.getFromChannel())
                                          .toChannel(messageRouting.getToChannel())
                                          .fromWorkspaceId(messageRouting.getFromWorkspaceId())
                                          .toWorkspaceId(messageRouting.getToWorkspaceId())
                                          .fromMessageId(messageRouting.getFromMessageId())
                                          .toMessageId(messageRouting.getToMessageId())
                                          .build();
    }
}