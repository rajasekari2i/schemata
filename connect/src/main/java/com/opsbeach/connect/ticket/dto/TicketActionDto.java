package com.opsbeach.connect.ticket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opsbeach.connect.core.BaseDto;
import com.opsbeach.connect.ticket.entity.TicketAction;
import com.opsbeach.connect.ticket.enums.TicketType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TicketActionDto extends BaseDto {
    
    @JsonProperty("ticket_id")
    private Long ticketId;

    private TicketType type;
    
    @JsonProperty("reference_id")
    private String referenceId;


    public TicketAction toDomin(TicketActionDto ticketActionDto) {
        return TicketAction.builder().clientId(ticketActionDto.getClientId())
                                     .id(ticketActionDto.getId())
                                     .createdAt(ticketActionDto.getCreatedAt())
                                     .updatedAt(ticketActionDto.getUpdatedAt())
                                     .createdBy(ticketActionDto.getCreatedBy())
                                     .updatedBy(ticketActionDto.getUpdatedBy())
                                     .ticketId(ticketActionDto.getTicketId())
                                     .type(ticketActionDto.getType())
                                     .referenceId(ticketActionDto.getReferenceId())
                                     .build();
    }
}
