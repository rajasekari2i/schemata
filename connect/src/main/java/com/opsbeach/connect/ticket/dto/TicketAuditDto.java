package com.opsbeach.connect.ticket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opsbeach.connect.core.BaseDto;
import com.opsbeach.connect.ticket.entity.TicketAudit;
import com.opsbeach.connect.ticket.enums.TicketSeverity;
import com.opsbeach.connect.ticket.enums.TicketStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TicketAuditDto extends BaseDto {

    private String ticketCreatedBy;

    private String ticketUpdatedBy;
    
    @JsonProperty("ticket_id")
    private Long ticketId;
    
    @JsonProperty("askob_message_id")
    private Long askobMessageId;

    @JsonProperty("canonicalId")
    private String canonicalId;

    @JsonProperty("ticket_serverity")
    private TicketSeverity severity;

    private TicketStatus status;

    @JsonProperty("assigned_to")
    private String assignedTo;

    private String title;

    private String description;

    public TicketAudit toDomin(TicketAuditDto ticketAuditDto) {
        return TicketAudit.builder().id(ticketAuditDto.getId())
                                       .clientId(ticketAuditDto.getClientId())
                                       .createdAt(ticketAuditDto.getCreatedAt())
                                       .updatedAt(ticketAuditDto.getUpdatedAt())
                                       .ticketCreatedBy(ticketAuditDto.getTicketCreatedBy())
                                       .ticketUpdatedBy(ticketAuditDto.getTicketUpdatedBy())
                                       .ticketId(ticketAuditDto.getTicketId())
                                       .assignedTo(ticketAuditDto.getAssignedTo())
                                       .canonicalId(ticketAuditDto.getCanonicalId())
                                       .severity(ticketAuditDto.getSeverity())
                                       .askobMessageId(ticketAuditDto.getAskobMessageId())
                                       .status(ticketAuditDto.getStatus())
                                       .title(ticketAuditDto.getTitle())
                                       .description(ticketAuditDto.getDescription())
                                       .build();
    }
}
