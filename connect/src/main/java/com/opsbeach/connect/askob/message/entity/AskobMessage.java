package com.opsbeach.connect.askob.message.entity;

import com.opsbeach.connect.askob.message.dto.AskobMessageDto;
import com.opsbeach.connect.askob.workspace.enums.AskobType;
import com.opsbeach.connect.core.BaseModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "askob_message")
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AskobMessage extends BaseModel {
    @Column(name = "thread_ts")
    private String threadTs;
    @Column(name = "message_ts")
    private String messageTs;
    private AskobType type;
    @Column(name = "message_user_id")
    private String messageUserId;

    public AskobMessageDto toDto(AskobMessage askobMessage) {
        
        return AskobMessageDto.builder().id(askobMessage.getId())
                                        .clientId(askobMessage.getClientId())
                                        .createdAt(askobMessage.getCreatedAt())
                                        .updatedAt(askobMessage.getUpdatedAt())
                                        .createdBy(askobMessage.getCreatedBy())
                                        .updatedBy(askobMessage.getUpdatedBy())
                                        .threadTs(askobMessage.getThreadTs())
                                        .messageTs(askobMessage.getMessageTs())
                                        .type(askobMessage.getType())
                                        .messageUserId(askobMessage.getMessageUserId())
                                        .build();
    }
}
