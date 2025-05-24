package com.opsbeach.connect.askob.message.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opsbeach.connect.askob.message.entity.AskobMessage;
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
public class AskobMessageDto extends BaseDto {
    
    @JsonProperty("thread_ts")
    private String threadTs;

    @JsonProperty("message_ts")
    private String messageTs;

    @Enumerated(EnumType.STRING)
    private AskobType type;

    @JsonProperty("message_user_id")
    private String messageUserId;

    public AskobMessage toDomin(AskobMessageDto askobMessageDto) {
          
        return AskobMessage.builder().id(askobMessageDto.getId())
                                     .clientId(askobMessageDto.getClientId())
                                     .createdAt(askobMessageDto.getCreatedAt())
                                     .updatedAt(askobMessageDto.getUpdatedAt())
                                     .createdBy(askobMessageDto.getCreatedBy())
                                     .updatedBy(askobMessageDto.getUpdatedBy())
                                     .threadTs(askobMessageDto.getThreadTs())
                                     .messageTs(askobMessageDto.getMessageTs())
                                     .type(askobMessageDto.getType())
                                     .messageUserId(askobMessageDto.getMessageUserId())
                                     .build();
    }
}
