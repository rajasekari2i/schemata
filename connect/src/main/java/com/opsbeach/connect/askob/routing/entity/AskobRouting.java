package com.opsbeach.connect.askob.routing.entity;

import com.opsbeach.connect.askob.routing.dto.AskobRoutingDto;
import com.opsbeach.connect.core.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "askob_routing")
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AskobRouting extends BaseModel {
    @Column(name = "channel_origin")
    private String channelOrigin;
    @Column(name = "channel_to")
    private String channelTo;
    @Column(name = "origin_workspace_id")
    private Long originWorkspaceId;
    @Column(name = "to_workspace_id")
    private Long toWorkspaceId;

    public AskobRoutingDto toDto(AskobRouting askobRouting) {

        return AskobRoutingDto.builder().id(askobRouting.getId())
                                        .clientId(askobRouting.getClientId())
                                        .createdAt(askobRouting.getCreatedAt())
                                        .updatedAt(askobRouting.getUpdatedAt())
                                        .createdBy(askobRouting.getCreatedBy())
                                        .updatedBy(askobRouting.getUpdatedBy())
                                        .channelOrigin(askobRouting.getChannelOrigin())
                                        .channelTo(askobRouting.getChannelTo())
                                        .originWorkspaceId(askobRouting.getOriginWorkspaceId())
                                        .toWorkspaceId(askobRouting.getToWorkspaceId())
                                        .build();        
    }
}
