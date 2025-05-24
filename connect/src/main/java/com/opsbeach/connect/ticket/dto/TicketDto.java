package com.opsbeach.connect.ticket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opsbeach.connect.core.BaseDto;
import com.opsbeach.connect.jira.dto.TicketBodyDto;
import com.opsbeach.connect.ticket.entity.Ticket;
import com.opsbeach.connect.ticket.enums.TicketSeverity;
import com.opsbeach.connect.ticket.enums.TicketStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TicketDto extends BaseDto {

    @JsonProperty("created_by")
    private String ticketCreatedBy;

    @JsonProperty("updated_by")
    private String ticketUpdatedBy;

    @Setter
    @JsonProperty("askob_message_id")
    private Long askobMessageId;

    @JsonProperty("canonical_id")
    @Setter
    private String canonicalId;

    private TicketSeverity severity;

    @Setter
    private TicketStatus status;

    @JsonProperty("assigned_to")
    private String assignedTo;

    private String title;

    private String description;

    @JsonProperty("ticket_body")
    private TicketBodyDto ticketBodyDto;

    public Ticket toDomin(TicketDto ticketDto) {
        return Ticket.builder().id(ticketDto.getId())
                               .clientId(ticketDto.getClientId())
                               .createdAt(ticketDto.getCreatedAt())
                               .updatedAt(ticketDto.getUpdatedAt())
                               .ticketCreatedBy(ticketDto.getTicketCreatedBy())
                               .ticketUpdatedBy(ticketDto.getTicketUpdatedBy())
                               .assignedTo(ticketDto.getAssignedTo())
                               .canonicalId(ticketDto.getCanonicalId())
                               .severity(ticketDto.getSeverity())
                               .askobMessageId(ticketDto.getAskobMessageId())
                               .status(ticketDto.getStatus())
                               .title(ticketDto.getTitle())
                               .description(ticketDto.getDescription())
                               .build();
    }
}
